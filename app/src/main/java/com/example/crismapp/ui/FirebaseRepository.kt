package com.example.crismapp.ui

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.Normalizer
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

object FirebaseRepository {

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private const val COLECAO_TURMAS = "turmas"
    private const val COLECAO_USUARIOS = "usuarios"
    private const val COLECAO_AVISOS = "avisos"
    private const val COLECAO_ENCONTROS = "encontros"
    private const val COLECAO_FREQUENCIAS = "frequencias"
    private const val COLECAO_FINANCEIRO = "financeiro"
    private const val COLECAO_FINANCEIRO_ANTIGO = "financeiro Jovens"
    private const val COLECAO_DOCUMENTOS = "documentos"

    private const val TAMANHO_LOTE_EXCLUSAO = 400

    // ==========================================================
    // FUNÇÕES AUXILIARES
    // ==========================================================

    private fun normalizarCategoria(categoria: String): String {
        return categoria
            .trim()
            .lowercase(Locale.ROOT)
    }

    private fun normalizarMatricula(matricula: String): String {
        return matricula
            .uppercase(Locale.ROOT)
            .replace(" ", "")
            .trim()
    }

    private fun criarSlug(texto: String): String {
        val textoSemAcentos = Normalizer.normalize(
            texto,
            Normalizer.Form.NFD
        ).replace(
            Regex("\\p{Mn}+"),
            ""
        )

        return textoSemAcentos
            .uppercase(Locale.ROOT)
            .replace(Regex("[^A-Z0-9]+"), "-")
            .trim('-')
            .take(24)
            .ifBlank { "TURMA" }
    }

    private fun prefixoDaCategoria(categoria: String): String {
        return when (normalizarCategoria(categoria)) {
            "jovem" -> "JOV"
            "adulta" -> "ADU"
            else -> "TUR"
        }
    }

    private fun numeroComTresDigitos(numero: Int): String {
        return numero.toString().padStart(3, '0')
    }

    private fun numeroComDoisDigitos(numero: Int): String {
        return numero.toString().padStart(2, '0')
    }

    private fun obterDataEmMillis(
        documento: DocumentSnapshot,
        campo: String
    ): Long {
        return when (val valor = documento.get(campo)) {
            is Timestamp -> valor.toDate().time
            is Number -> valor.toLong()
            else -> 0L
        }
    }

    // ==========================================================
    // TURMAS
    // ==========================================================

    fun ouvirTurmas(
        categoria: String,
        onUpdate: (List<Turma>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        val categoriaNormalizada = normalizarCategoria(categoria)

        return db.collection(COLECAO_TURMAS)
            .whereEqualTo("categoria", categoriaNormalizada)
            .addSnapshotListener { snapshot, erro ->

                if (erro != null) {
                    onError(erro)
                    return@addSnapshotListener
                }

                val turmas = snapshot
                    ?.documents
                    ?.mapNotNull { documento ->

                        val nome = documento.getString("nome")
                            ?.trim()
                            .orEmpty()

                        val ativa = documento.getBoolean("ativa") ?: true

                        if (nome.isBlank() || !ativa) {
                            null
                        } else {
                            Turma(
                                id = documento.id,
                                nome = nome,
                                categoria = documento.getString("categoria")
                                    ?: categoriaNormalizada,
                                dataCriacao = documento.getLong("dataCriacao")
                                    ?: 0L
                            )
                        }
                    }
                    ?.sortedBy { it.nome.lowercase(Locale.ROOT) }
                    .orEmpty()

                onUpdate(turmas)
            }
    }

    fun criarTurma(
        nome: String,
        categoria: String,
        onSuccess: (Turma) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val nomeTratado = nome.trim()
        val categoriaTratada = normalizarCategoria(categoria)

        if (nomeTratado.isBlank()) {
            onError(
                IllegalArgumentException(
                    "O nome da turma não pode ficar vazio."
                )
            )
            return
        }

        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)

        val prefixo = prefixoDaCategoria(categoriaTratada)
        val slug = criarSlug(nomeTratado)

        val idBase = "$prefixo-$anoAtual-$slug"

        encontrarIdTurmaDisponivel(
            idBase = idBase,
            tentativa = 1,
            onSuccess = { idDisponivel ->

                val agora = System.currentTimeMillis()

                val dadosTurma = hashMapOf<String, Any>(
                    "codigo" to idDisponivel,
                    "nome" to nomeTratado,
                    "categoria" to categoriaTratada,
                    "ano" to anoAtual,
                    "ativa" to true,
                    "dataCriacao" to agora
                )

                db.collection(COLECAO_TURMAS)
                    .document(idDisponivel)
                    .set(dadosTurma)
                    .addOnSuccessListener {
                        onSuccess(
                            Turma(
                                id = idDisponivel,
                                nome = nomeTratado,
                                categoria = categoriaTratada,
                                dataCriacao = agora
                            )
                        )
                    }
                    .addOnFailureListener(onError)
            },
            onError = onError
        )
    }

    private fun encontrarIdTurmaDisponivel(
        idBase: String,
        tentativa: Int,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (tentativa > 99) {
            onError(
                IllegalStateException(
                    "Não foi possível gerar um código disponível para a turma."
                )
            )
            return
        }

        val idCandidato = if (tentativa == 1) {
            idBase
        } else {
            "$idBase-${numeroComDoisDigitos(tentativa)}"
        }

        db.collection(COLECAO_TURMAS)
            .document(idCandidato)
            .get()
            .addOnSuccessListener { documento ->

                if (!documento.exists()) {
                    onSuccess(idCandidato)
                } else {
                    encontrarIdTurmaDisponivel(
                        idBase = idBase,
                        tentativa = tentativa + 1,
                        onSuccess = onSuccess,
                        onError = onError
                    )
                }
            }
            .addOnFailureListener(onError)
    }

    fun atualizarNomeTurma(
        turmaId: String,
        novoNome: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val nomeTratado = novoNome.trim()

        if (nomeTratado.isBlank()) {
            onError(
                IllegalArgumentException(
                    "O nome da turma não pode ficar vazio."
                )
            )
            return
        }

        db.collection(COLECAO_TURMAS)
            .document(turmaId)
            .update(
                mapOf(
                    "nome" to nomeTratado,
                    "dataAtualizacao" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    fun desativarTurma(
        turmaId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLECAO_TURMAS)
            .document(turmaId)
            .update(
                mapOf(
                    "ativa" to false,
                    "dataAtualizacao" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    // ==========================================================
    // CRISMANDOS
    // ==========================================================

    fun ouvirCrismandosDaTurma(
        turmaId: String,
        onUpdate: (List<Crismando>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        return db.collection(COLECAO_USUARIOS)
            .whereEqualTo("turmaId", turmaId)
            .addSnapshotListener { snapshot, erro ->

                if (erro != null) {
                    onError(erro)
                    return@addSnapshotListener
                }

                val crismandos = snapshot
                    ?.documents
                    ?.mapNotNull { documento ->

                        val nome = documento.getString("nome")
                            ?.trim()
                            .orEmpty()

                        val ativo = documento.getBoolean("ativo") ?: true

                        if (nome.isBlank() || !ativo) {
                            null
                        } else {
                            Crismando(
                                id = documento.id,
                                nome = nome,
                                turmaId = documento.getString("turmaId")
                                    .orEmpty(),
                                matricula = documento.getString("matricula")
                                    ?: documento.id,
                                categoria = documento.getString("categoria")
                                    .orEmpty(),
                                ativo = ativo,
                                dataCriacao = documento.getLong("dataCriacao")
                                    ?: 0L
                            )
                        }
                    }
                    ?.sortedBy { it.nome.lowercase(Locale.ROOT) }
                    .orEmpty()

                onUpdate(crismandos)
            }
    }

    fun criarCrismando(
        nome: String,
        turmaId: String,
        categoria: String,
        onSuccess: (Crismando) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val nomeTratado = nome.trim()

        if (nomeTratado.isBlank()) {
            onError(
                IllegalArgumentException(
                    "O nome do crismando não pode ficar vazio."
                )
            )
            return
        }

        if (turmaId.isBlank()) {
            onError(
                IllegalArgumentException(
                    "Nenhuma turma foi selecionada."
                )
            )
            return
        }

        gerarMatriculaDisponivel(
            tentativa = 1,
            onSuccess = { matricula ->

                val agora = System.currentTimeMillis()
                val categoriaTratada = normalizarCategoria(categoria)

                val dadosCrismando = hashMapOf<String, Any>(
                    "matricula" to matricula,
                    "nome" to nomeTratado,
                    "turmaId" to turmaId,
                    "categoria" to categoriaTratada,
                    "ativo" to true,
                    "dataCriacao" to agora
                )

                db.collection(COLECAO_USUARIOS)
                    .document(matricula)
                    .set(dadosCrismando)
                    .addOnSuccessListener {
                        onSuccess(
                            Crismando(
                                id = matricula,
                                nome = nomeTratado,
                                turmaId = turmaId,
                                matricula = matricula,
                                categoria = categoriaTratada,
                                ativo = true,
                                dataCriacao = agora
                            )
                        )
                    }
                    .addOnFailureListener(onError)
            },
            onError = onError
        )
    }

    private fun gerarMatriculaDisponivel(
        tentativa: Int,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (tentativa > 30) {
            onError(
                IllegalStateException(
                    "Não foi possível gerar uma matrícula disponível."
                )
            )
            return
        }

        val numero = Random.nextInt(
            from = 1000,
            until = 10000
        )

        val matricula = "CX-$numero"

        db.collection(COLECAO_USUARIOS)
            .document(matricula)
            .get()
            .addOnSuccessListener { documento ->

                if (!documento.exists()) {
                    onSuccess(matricula)
                } else {
                    gerarMatriculaDisponivel(
                        tentativa = tentativa + 1,
                        onSuccess = onSuccess,
                        onError = onError
                    )
                }
            }
            .addOnFailureListener(onError)
    }

    fun atualizarCrismando(
        matricula: String,
        nome: String,
        turmaId: String,
        categoria: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(matricula)
        val nomeTratado = nome.trim()

        if (matriculaTratada.isBlank() || nomeTratado.isBlank()) {
            onError(
                IllegalArgumentException(
                    "Matrícula ou nome inválido."
                )
            )
            return
        }

        val dadosAtualizados = mapOf(
            "matricula" to matriculaTratada,
            "nome" to nomeTratado,
            "turmaId" to turmaId,
            "categoria" to normalizarCategoria(categoria),
            "dataAtualizacao" to System.currentTimeMillis()
        )

        db.collection(COLECAO_USUARIOS)
            .document(matriculaTratada)
            .set(
                dadosAtualizados,
                SetOptions.merge()
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    fun desativarCrismando(
        matricula: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(matricula)

        db.collection(COLECAO_USUARIOS)
            .document(matriculaTratada)
            .update(
                mapOf(
                    "ativo" to false,
                    "dataAtualizacao" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    /**
     * Apaga definitivamente:
     *
     * usuarios/{matricula}
     * frequencias do aluno
     * financeiro do aluno
     * financeiro Jovens antigo
     * documentos do aluno
     */
    fun excluirCrismandoDefinitivamente(
        matricula: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(matricula)

        val tarefas = listOf(
            excluirConsultaEmLotes(
                db.collection(COLECAO_FREQUENCIAS)
                    .whereEqualTo("alunoId", matriculaTratada)
            ),
            excluirConsultaEmLotes(
                db.collection(COLECAO_FINANCEIRO)
                    .whereEqualTo("alunoId", matriculaTratada)
            ),
            excluirConsultaEmLotes(
                db.collection(COLECAO_FINANCEIRO_ANTIGO)
                    .whereEqualTo("alunoId", matriculaTratada)
            ),
            excluirConsultaEmLotes(
                db.collection(COLECAO_DOCUMENTOS)
                    .whereEqualTo("alunoId", matriculaTratada)
            )
        )

        Tasks.whenAll(tarefas)
            .continueWithTask {
                db.collection(COLECAO_USUARIOS)
                    .document(matriculaTratada)
                    .delete()
            }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    // ==========================================================
    // ENCONTROS
    // ==========================================================

    fun ouvirEncontrosDaTurma(
        turmaId: String,
        onUpdate: (List<RegistroEncontro>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        return db.collection(COLECAO_ENCONTROS)
            .whereEqualTo("turmaId", turmaId)
            .addSnapshotListener { snapshot, erro ->

                if (erro != null) {
                    onError(erro)
                    return@addSnapshotListener
                }

                val encontros = snapshot
                    ?.documents
                    ?.mapNotNull { documento ->

                        val numero = documento.getLong("numero")
                            ?.toInt()
                            ?: 0

                        if (numero <= 0) {
                            null
                        } else {
                            RegistroEncontro(
                                id = documento.id,
                                numero = numero,
                                dataManual = documento.getString("dataManual")
                                    .orEmpty(),
                                turmaId = documento.getString("turmaId")
                                    .orEmpty(),
                                dataCriacao = documento.getLong("dataCriacao")
                                    ?: 0L
                            )
                        }
                    }
                    ?.sortedBy { it.numero }
                    .orEmpty()

                onUpdate(encontros)
            }
    }

    fun salvarEncontro(
        turmaId: String,
        numero: Int,
        dataManual: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (turmaId.isBlank() || numero <= 0) {
            onError(
                IllegalArgumentException(
                    "Turma ou número do encontro inválido."
                )
            )
            return
        }

        val numeroFormatado = numeroComTresDigitos(numero)
        val encontroId = "ENC-$turmaId-E$numeroFormatado"

        val dadosEncontro = hashMapOf<String, Any>(
            "turmaId" to turmaId,
            "numero" to numero,
            "dataManual" to dataManual.trim(),
            "dataCriacao" to System.currentTimeMillis()
        )

        db.collection(COLECAO_ENCONTROS)
            .document(encontroId)
            .set(
                dadosEncontro,
                SetOptions.merge()
            )
            .addOnSuccessListener {
                onSuccess(encontroId)
            }
            .addOnFailureListener(onError)
    }

    fun atualizarDataEncontro(
        encontroId: String,
        dataManual: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLECAO_ENCONTROS)
            .document(encontroId)
            .update(
                mapOf(
                    "dataManual" to dataManual.trim(),
                    "dataAtualizacao" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    fun excluirEncontro(
        encontroId: String,
        turmaId: String,
        numeroEncontro: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        excluirConsultaEmLotes(
            db.collection(COLECAO_FREQUENCIAS)
                .whereEqualTo("turmaId", turmaId)
                .whereEqualTo("encontro", numeroEncontro)
        ).continueWithTask {
            db.collection(COLECAO_ENCONTROS)
                .document(encontroId)
                .delete()
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener(onError)
    }

    // ==========================================================
    // FREQUÊNCIAS
    // ==========================================================

    fun ouvirFrequenciasDaTurma(
        turmaId: String,
        onUpdate: (List<RegistroFrequencia>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        return db.collection(COLECAO_FREQUENCIAS)
            .whereEqualTo("turmaId", turmaId)
            .addSnapshotListener { snapshot, erro ->

                if (erro != null) {
                    onError(erro)
                    return@addSnapshotListener
                }

                val frequencias = snapshot
                    ?.documents
                    ?.map { documento ->
                        RegistroFrequencia(
                            id = documento.id,
                            alunoId = documento.getString("alunoId")
                                .orEmpty(),
                            turmaId = documento.getString("turmaId")
                                .orEmpty(),
                            encontro = documento.getLong("encontro")
                                ?.toInt()
                                ?: 0,
                            status = documento.getString("status")
                                ?: StatusFrequencia.NENHUM.name,
                            dataRegistro = documento.getLong("dataRegistro")
                                ?: documento.getLong("dataAtualizacao")
                                ?: 0L
                        )
                    }
                    .orEmpty()

                onUpdate(frequencias)
            }
    }

    fun salvarFrequencia(
        turmaId: String,
        encontro: Int,
        alunoId: String,
        status: StatusFrequencia,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(alunoId)
        val numeroEncontro = numeroComTresDigitos(encontro)

        val frequenciaId =
            "FREQ-$turmaId-E$numeroEncontro-$matriculaTratada"

        val referencia = db.collection(COLECAO_FREQUENCIAS)
            .document(frequenciaId)

        if (status == StatusFrequencia.NENHUM) {
            referencia.delete()
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener(onError)

            return
        }

        val dadosFrequencia = hashMapOf<String, Any>(
            "alunoId" to matriculaTratada,
            "turmaId" to turmaId,
            "encontro" to encontro,
            "status" to status.name,
            "dataRegistro" to System.currentTimeMillis()
        )

        referencia
            .set(dadosFrequencia)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    // ==========================================================
    // FINANCEIRO
    // ==========================================================

    fun ouvirFinanceiroDoAluno(
        alunoId: String,
        onUpdate: (List<RegistroFinanceiro>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        val matriculaTratada = normalizarMatricula(alunoId)

        return db.collection(COLECAO_FINANCEIRO)
            .whereEqualTo("alunoId", matriculaTratada)
            .addSnapshotListener { snapshot, erro ->

                if (erro != null) {
                    onError(erro)
                    return@addSnapshotListener
                }

                val pagamentos = snapshot
                    ?.documents
                    ?.map { documento ->
                        RegistroFinanceiro(
                            id = documento.id,
                            alunoId = documento.getString("alunoId")
                                .orEmpty(),
                            turmaId = documento.getString("turmaId")
                                .orEmpty(),

                            numeroParcela = documento.getLong("numeroParcela")
                                ?.toInt()
                                ?: 0,

                            parcela = documento.getLong("parcela")
                                ?.toInt()
                                ?: 0,

                            status = documento.getString("status")
                                ?: StatusPagamento.PENDENTE.name,

                            statusPago = documento.getBoolean("statusPago")
                                ?: false,

                            recebidoPor = documento.getString("recebidoPor")
                                .orEmpty(),

                            catequista = documento.getString("catequista")
                                .orEmpty(),

                            dataPagamento = obterDataEmMillis(
                                documento = documento,
                                campo = "dataPagamento"
                            ),

                            dataLancamento = obterDataEmMillis(
                                documento = documento,
                                campo = "dataLancamento"
                            )
                        )
                    }
                    ?.sortedBy { it.obterNumeroParcela() }
                    .orEmpty()

                onUpdate(pagamentos)
            }
    }

    fun salvarPagamento(
        turmaId: String,
        alunoId: String,
        parcela: Int,
        recebidoPor: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(alunoId)
        val responsavelTratado = recebidoPor.trim()

        if (parcela <= 0) {
            onError(
                IllegalArgumentException(
                    "Número de parcela inválido."
                )
            )
            return
        }

        if (responsavelTratado.isBlank()) {
            onError(
                IllegalArgumentException(
                    "Informe quem recebeu o pagamento."
                )
            )
            return
        }

        val parcelaFormatada = numeroComDoisDigitos(parcela)

        val pagamentoId =
            "PAG-$turmaId-$matriculaTratada-P$parcelaFormatada"

        val dadosPagamento = hashMapOf<String, Any>(
            "alunoId" to matriculaTratada,
            "turmaId" to turmaId,
            "parcela" to parcela,
            "status" to StatusPagamento.PAGO.name,
            "recebidoPor" to responsavelTratado,
            "dataPagamento" to FieldValue.serverTimestamp()
        )

        db.collection(COLECAO_FINANCEIRO)
            .document(pagamentoId)
            .set(dadosPagamento)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    fun removerPagamento(
        turmaId: String,
        alunoId: String,
        parcela: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(alunoId)
        val parcelaFormatada = numeroComDoisDigitos(parcela)

        val pagamentoId =
            "PAG-$turmaId-$matriculaTratada-P$parcelaFormatada"

        db.collection(COLECAO_FINANCEIRO)
            .document(pagamentoId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    // ==========================================================
    // AVISOS
    // ==========================================================

    fun ouvirAvisosDaTurma(
        turmaId: String,
        categoria: String,
        onUpdate: (List<Aviso>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        val categoriaTratada = normalizarCategoria(categoria)

        val destinoAntigo = when (categoriaTratada) {
            "jovem" -> "turma_jovem"
            "adulta" -> "turma_adulta"
            else -> ""
        }

        val destinos = listOf(
            turmaId,
            "GERAL",
            destinoAntigo
        ).filter {
            it.isNotBlank()
        }.distinct()

        val consulta = if (destinos.size == 1) {
            db.collection(COLECAO_AVISOS)
                .whereEqualTo("turmaId", destinos.first())
        } else {
            db.collection(COLECAO_AVISOS)
                .whereIn("turmaId", destinos)
        }

        return consulta.addSnapshotListener { snapshot, erro ->

            if (erro != null) {
                onError(erro)
                return@addSnapshotListener
            }

            val avisos = snapshot
                ?.documents
                ?.mapNotNull { documento ->

                    val texto = documento.getString("texto")
                        ?.trim()
                        .orEmpty()

                    if (texto.isBlank()) {
                        null
                    } else {
                        Aviso(
                            id = documento.id,
                            texto = texto,
                            tipo = documento.getString("tipo")
                                ?: "gerais",
                            turmaId = documento.getString("turmaId")
                                .orEmpty(),
                            dataCriacao = documento.getLong("dataCriacao")
                                ?: 0L
                        )
                    }
                }
                ?.sortedByDescending { it.dataCriacao }
                .orEmpty()

            onUpdate(avisos)
        }
    }

    fun criarAviso(
        turmaId: String,
        texto: String,
        tipo: String = "gerais",
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val textoTratado = texto.trim()

        if (turmaId.isBlank()) {
            onError(
                IllegalArgumentException(
                    "Selecione uma turma antes de enviar o aviso."
                )
            )
            return
        }

        if (textoTratado.isBlank()) {
            onError(
                IllegalArgumentException(
                    "O texto do aviso não pode ficar vazio."
                )
            )
            return
        }

        val dadosAviso = hashMapOf<String, Any>(
            "texto" to textoTratado,
            "tipo" to tipo.trim().ifBlank { "gerais" },
            "turmaId" to turmaId,
            "dataCriacao" to System.currentTimeMillis()
        )

        db.collection(COLECAO_AVISOS)
            .add(dadosAviso)
            .addOnSuccessListener { documento ->
                onSuccess(documento.id)
            }
            .addOnFailureListener(onError)
    }

    fun excluirAviso(
        avisoId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLECAO_AVISOS)
            .document(avisoId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    // ==========================================================
    // EXCLUSÃO DEFINITIVA DE TURMA
    // ==========================================================

    /**
     * Apaga a turma e os registros diretamente ligados ao turmaId.
     *
     * Esta função deve ser chamada somente depois de uma
     * confirmação clara na interface.
     */
    fun excluirTurmaDefinitivamente(
        turmaId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val tarefas = listOf(
            excluirConsultaEmLotes(
                db.collection(COLECAO_USUARIOS)
                    .whereEqualTo("turmaId", turmaId)
            ),
            excluirConsultaEmLotes(
                db.collection(COLECAO_AVISOS)
                    .whereEqualTo("turmaId", turmaId)
            ),
            excluirConsultaEmLotes(
                db.collection(COLECAO_ENCONTROS)
                    .whereEqualTo("turmaId", turmaId)
            ),
            excluirConsultaEmLotes(
                db.collection(COLECAO_FREQUENCIAS)
                    .whereEqualTo("turmaId", turmaId)
            ),
            excluirConsultaEmLotes(
                db.collection(COLECAO_FINANCEIRO)
                    .whereEqualTo("turmaId", turmaId)
            ),
            excluirConsultaEmLotes(
                db.collection(COLECAO_DOCUMENTOS)
                    .whereEqualTo("turmaId", turmaId)
            )
        )

        Tasks.whenAll(tarefas)
            .continueWithTask {
                db.collection(COLECAO_TURMAS)
                    .document(turmaId)
                    .delete()
            }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    // ==========================================================
    // EXCLUSÃO EM LOTES
    // ==========================================================

    /**
     * O Firestore não apaga automaticamente os documentos
     * encontrados por uma consulta.
     *
     * Esta função lê e apaga os documentos em pequenos lotes,
     * repetindo até a consulta ficar vazia.
     */
    private fun excluirConsultaEmLotes(
        consulta: Query
    ): Task<Void> {

        return consulta
            .limit(TAMANHO_LOTE_EXCLUSAO.toLong())
            .get()
            .continueWithTask { tarefaConsulta ->

                if (!tarefaConsulta.isSuccessful) {
                    throw tarefaConsulta.exception
                        ?: IllegalStateException(
                            "Falha ao consultar documentos para exclusão."
                        )
                }

                val snapshot = tarefaConsulta.result

                if (snapshot == null || snapshot.isEmpty) {
                    return@continueWithTask Tasks.forResult<Void>(null)
                }

                val lote = db.batch()

                snapshot.documents.forEach { documento ->
                    lote.delete(documento.reference)
                }

                lote.commit()
                    .continueWithTask { tarefaLote ->

                        if (!tarefaLote.isSuccessful) {
                            throw tarefaLote.exception
                                ?: IllegalStateException(
                                    "Falha ao excluir documentos."
                                )
                        }

                        excluirConsultaEmLotes(consulta)
                    }
            }
    }
}