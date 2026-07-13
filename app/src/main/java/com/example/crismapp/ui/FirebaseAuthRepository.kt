package com.example.crismapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import java.text.Normalizer
import java.util.Locale

enum class NivelAcessoCatequista {
    ADMIN_TOTAL,
    CATEQUISTA_COMUM
}

data class CatequistaAutenticado(
    val uid: String = "",
    val nome: String = "",
    val login: String = "",
    val nivel: NivelAcessoCatequista = NivelAcessoCatequista.CATEQUISTA_COMUM,
    val ativo: Boolean = true,
    val paroquiaId: String = ""
) {
    fun possuiPermissaoTotal(): Boolean {
        return nivel == NivelAcessoCatequista.ADMIN_TOTAL
    }
}

object FirebaseAuthRepository {

    private const val COLECAO_CATEQUISTAS = "catequistas"

    /*
     * O catequista nunca vê esse domínio.
     *
     * Exemplo:
     * login digitado: emanuel
     * e-mail interno: emanuel@acesso.crismapp.app
     */
    private const val DOMINIO_INTERNO = "acesso.crismapp.app"

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    var catequistaAtual: CatequistaAutenticado? by mutableStateOf(null)
        private set

    fun normalizarLogin(login: String): String {
        val semAcentos = Normalizer.normalize(
            login.trim(),
            Normalizer.Form.NFD
        ).replace(
            Regex("\\p{Mn}+"),
            ""
        )

        return semAcentos
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9._-]"), "")
    }

    fun gerarEmailInterno(login: String): String {
        val loginTratado = normalizarLogin(login)
        return "$loginTratado@$DOMINIO_INTERNO"
    }

    fun entrarComLoginEPin(
        login: String,
        pin: String,
        onSuccess: (CatequistaAutenticado) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val loginTratado = normalizarLogin(login)
        val pinTratado = pin.filter { it.isDigit() }

        if (loginTratado.isBlank()) {
            onError(
                IllegalArgumentException(
                    "Informe o nome de usuário."
                )
            )
            return
        }

        if (pinTratado.length != 6) {
            onError(
                IllegalArgumentException(
                    "O PIN deve possuir 6 dígitos."
                )
            )
            return
        }

        val emailInterno = gerarEmailInterno(loginTratado)

        auth.signInWithEmailAndPassword(
            emailInterno,
            pinTratado
        ).addOnSuccessListener { resultado ->

            val usuario = resultado.user

            if (usuario == null) {
                auth.signOut()
                onError(
                    IllegalStateException(
                        "Não foi possível identificar o usuário."
                    )
                )
                return@addOnSuccessListener
            }

            carregarPerfilCatequista(
                uid = usuario.uid,
                loginEsperado = loginTratado,
                onSuccess = onSuccess,
                onError = onError
            )
        }.addOnFailureListener { erro ->
            auth.signOut()
            catequistaAtual = null
            onError(traduzirErroLogin(erro))
        }
    }

    fun restaurarSessao(
        onSuccess: (CatequistaAutenticado) -> Unit,
        onSemSessao: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val usuarioAtual = auth.currentUser

        if (usuarioAtual == null) {
            catequistaAtual = null
            onSemSessao()
            return
        }

        carregarPerfilCatequista(
            uid = usuarioAtual.uid,
            loginEsperado = null,
            onSuccess = onSuccess,
            onError = { erro ->
                auth.signOut()
                catequistaAtual = null
                onError(erro)
            }
        )
    }

    private fun carregarPerfilCatequista(
        uid: String,
        loginEsperado: String?,
        onSuccess: (CatequistaAutenticado) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLECAO_CATEQUISTAS)
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->

                if (!documento.exists()) {
                    auth.signOut()
                    catequistaAtual = null
                    onError(
                        IllegalStateException(
                            "Esta conta ainda não foi autorizada como catequista."
                        )
                    )
                    return@addOnSuccessListener
                }

                val ativo = documento.getBoolean("ativo") ?: false

                if (!ativo) {
                    auth.signOut()
                    catequistaAtual = null
                    onError(
                        IllegalStateException(
                            "Este acesso está desativado."
                        )
                    )
                    return@addOnSuccessListener
                }

                val loginSalvo = normalizarLogin(
                    documento.getString("login").orEmpty()
                )

                if (
                    loginEsperado != null &&
                    loginSalvo.isNotBlank() &&
                    loginSalvo != loginEsperado
                ) {
                    auth.signOut()
                    catequistaAtual = null
                    onError(
                        IllegalStateException(
                            "O login informado não corresponde a esta conta."
                        )
                    )
                    return@addOnSuccessListener
                }

                val nivelTexto = documento
                    .getString("nivel")
                    .orEmpty()
                    .uppercase(Locale.ROOT)

                val nivel = try {
                    NivelAcessoCatequista.valueOf(nivelTexto)
                } catch (_: IllegalArgumentException) {
                    NivelAcessoCatequista.CATEQUISTA_COMUM
                }

                val perfil = CatequistaAutenticado(
                    uid = uid,
                    nome = documento.getString("nome")
                        ?.trim()
                        .orEmpty()
                        .ifBlank { loginSalvo },
                    login = loginSalvo,
                    nivel = nivel,
                    ativo = true,
                    paroquiaId = documento.getString("paroquiaId")
                        .orEmpty()
                )

                catequistaAtual = perfil
                onSuccess(perfil)
            }
            .addOnFailureListener { erro ->
                auth.signOut()
                catequistaAtual = null
                onError(erro)
            }
    }

    fun possuiSessaoFirebase(): Boolean {
        return auth.currentUser != null
    }

    fun possuiPermissaoTotal(): Boolean {
        return catequistaAtual?.possuiPermissaoTotal() == true
    }

    fun sair() {
        auth.signOut()
        catequistaAtual = null
    }

    private fun traduzirErroLogin(erro: Exception): Exception {
        val codigo = (erro as? FirebaseAuthException)
            ?.errorCode
            .orEmpty()

        val mensagem = when (codigo) {
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_WRONG_PASSWORD",
            "ERROR_USER_NOT_FOUND",
            "ERROR_INVALID_EMAIL" -> {
                "Usuário ou PIN incorreto."
            }

            "ERROR_USER_DISABLED" -> {
                "Este acesso foi desativado."
            }

            "ERROR_TOO_MANY_REQUESTS" -> {
                "Muitas tentativas. Aguarde alguns minutos."
            }

            "ERROR_NETWORK_REQUEST_FAILED" -> {
                "Sem conexão com a internet."
            }

            else -> {
                erro.message ?: "Não foi possível entrar."
            }
        }

        return IllegalStateException(mensagem)
    }
}