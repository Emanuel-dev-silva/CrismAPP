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



enum class PerfilDocumentacao {
    CRISMANDO,
    PADRINHO
}

enum class TipoIdentificacaoDocumento {
    IDENTIDADE,
    CNH,
    OUTRO,
    NAO_INFORMADO
}

enum class StatusCasamentoDocumento {
    ENTREGUE,
    NAO_ENTREGUE,
    NAO_CASADO,
    NAO_INFORMADO
}

data class CadastroDocumentacao(
    val alunoId: String = "",
    val turmaId: String = "",
    val perfil: String = PerfilDocumentacao.CRISMANDO.name,
    val primeiraComunhaoPossui: Boolean = false,
    val primeiraComunhaoEntregue: Boolean = false,
    val batismoEntregue: Boolean = false,
    val crismaPossui: Boolean = false,
    val crismaEntregue: Boolean = false,
    val identificacaoEntregue: Boolean = false,
    val tipoIdentificacao: String = TipoIdentificacaoDocumento.NAO_INFORMADO.name,
    val identificacaoOutro: String = "",
    val casamentoStatus: String = StatusCasamentoDocumento.NAO_INFORMADO.name,
    val atualizadoPor: String = "",
    val dataAtualizacao: Long = 0L
) {
    fun obterPerfil(): PerfilDocumentacao {
        return try {
            PerfilDocumentacao.valueOf(perfil.uppercase(Locale.ROOT))
        } catch (_: IllegalArgumentException) {
            PerfilDocumentacao.CRISMANDO
        }
    }

    fun obterTipoIdentificacao(): TipoIdentificacaoDocumento {
        return try {
            TipoIdentificacaoDocumento.valueOf(
                tipoIdentificacao.uppercase(Locale.ROOT)
            )
        } catch (_: IllegalArgumentException) {
            TipoIdentificacaoDocumento.NAO_INFORMADO
        }
    }

    fun obterStatusCasamento(): StatusCasamentoDocumento {
        return try {
            StatusCasamentoDocumento.valueOf(
                casamentoStatus.uppercase(Locale.ROOT)
            )
        } catch (_: IllegalArgumentException) {
            StatusCasamentoDocumento.NAO_INFORMADO
        }
    }
}

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
    private const val COLECAO_DOCUMENTOS_CONFIGURACAO = "documentos_configuracao"
    private const val COLECAO_HISTORICO_ALUNOS = "historico_alunos"
    private const val COLECAO_MOVIMENTACOES = "movimentacoes"
    private const val COLECAO_ATALHOS_INICIO = "atalhos_inicio"

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

    private fun obterNumeroParcela(documento: DocumentSnapshot): Int {
        return documento.getLong("parcela")?.toInt()
            ?: documento.getLong("numeroParcela")?.toInt()
            ?: 0
    }

    private fun obterStatusPagamento(documento: DocumentSnapshot): StatusPagamento {
        val statusSalvo = documento.getString("status")
            ?.trim()
            ?.uppercase(Locale.ROOT)

        return try {
            if (statusSalvo.isNullOrBlank()) {
                if (documento.getBoolean("statusPago") == true) {
                    StatusPagamento.PAGO
                } else {
                    StatusPagamento.PENDENTE
                }
            } else {
                StatusPagamento.valueOf(statusSalvo)
            }
        } catch (_: IllegalArgumentException) {
            if (documento.getBoolean("statusPago") == true) {
                StatusPagamento.PAGO
            } else {
                StatusPagamento.PENDENTE
            }
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
                    "situacao" to SituacaoCrismando.ATIVO.name,
                    "motivoSituacao" to "",
                    "dataSituacao" to agora,
                    "atualizadoPor" to "",
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

    /**
     * Arquiva o crismando sem apagar nenhum pagamento,
     * frequência ou documento.
     *
     * O resumo é salvo em:
     *
     * historico_alunos/{matricula}
     *
     * E a movimentação completa é registrada em:
     *
     * movimentacoes/{idAutomatico}
     */
    fun arquivarCrismando(
        matricula: String,
        situacao: SituacaoCrismando = SituacaoCrismando.DESISTENTE,
        motivo: String,
        responsavel: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(matricula)
        val motivoTratado = motivo.trim()
        val responsavelTratado = responsavel.trim().ifBlank { "Sistema" }

        if (matriculaTratada.isBlank()) {
            onError(IllegalArgumentException("Matrícula inválida."))
            return
        }

        val referenciaUsuario = db.collection(COLECAO_USUARIOS)
            .document(matriculaTratada)

        referenciaUsuario.get()
            .addOnSuccessListener { usuarioDocumento ->
                if (!usuarioDocumento.exists()) {
                    onError(
                        IllegalStateException(
                            "O crismando não foi encontrado no servidor."
                        )
                    )
                    return@addOnSuccessListener
                }

                val nomeAluno = usuarioDocumento.getString("nome")
                    .orEmpty()
                    .trim()

                val turmaAnteriorId = usuarioDocumento.getString("turmaId")
                    .orEmpty()
                    .trim()

                val categoria = usuarioDocumento.getString("categoria")
                    .orEmpty()
                    .trim()

                fun continuarArquivamento(turmaAnteriorNome: String) {
                    db.collection(COLECAO_FREQUENCIAS)
                        .whereEqualTo("alunoId", matriculaTratada)
                        .get()
                        .addOnSuccessListener { frequenciasSnapshot ->

                            db.collection(COLECAO_FINANCEIRO)
                                .whereEqualTo("alunoId", matriculaTratada)
                                .get()
                                .addOnSuccessListener { financeiroSnapshot ->

                                    val totalPresencas = frequenciasSnapshot.documents.count {
                                        it.getString("status")
                                            ?.equals(
                                                StatusFrequencia.PRESENTE.name,
                                                ignoreCase = true
                                            ) == true
                                    }

                                    val totalFaltas = frequenciasSnapshot.documents.count {
                                        it.getString("status")
                                            ?.equals(
                                                StatusFrequencia.FALTA.name,
                                                ignoreCase = true
                                            ) == true
                                    }

                                    val totalJustificadas = frequenciasSnapshot.documents.count {
                                        it.getString("status")
                                            ?.equals(
                                                StatusFrequencia.JUSTIFICADA.name,
                                                ignoreCase = true
                                            ) == true
                                    }

                                    val parcelasPagas = financeiroSnapshot.documents
                                        .filter {
                                            obterStatusPagamento(it) ==
                                                    StatusPagamento.PAGO
                                        }
                                        .map { obterNumeroParcela(it) }
                                        .filter { it > 0 }
                                        .distinct()
                                        .sorted()

                                    val parcelasReembolsadas =
                                        financeiroSnapshot.documents
                                            .filter {
                                                obterStatusPagamento(it) ==
                                                        StatusPagamento.REEMBOLSADO
                                            }
                                            .map { obterNumeroParcela(it) }
                                            .filter { it > 0 }
                                            .distinct()
                                            .sorted()

                                    val parcelasEstornadas =
                                        financeiroSnapshot.documents
                                            .filter {
                                                obterStatusPagamento(it) ==
                                                        StatusPagamento.ESTORNADO
                                            }
                                            .map { obterNumeroParcela(it) }
                                            .filter { it > 0 }
                                            .distinct()
                                            .sorted()

                                    val historico = hashMapOf<String, Any>(
                                        "matricula" to matriculaTratada,
                                        "nome" to nomeAluno,
                                        "situacao" to situacao.name,
                                        "motivo" to motivoTratado,
                                        "turmaAnteriorId" to turmaAnteriorId,
                                        "turmaAnteriorNome" to turmaAnteriorNome,
                                        "categoria" to categoria,
                                        "dataArquivamento" to
                                                FieldValue.serverTimestamp(),
                                        "arquivadoPor" to responsavelTratado,
                                        "totalPresencas" to totalPresencas,
                                        "totalFaltas" to totalFaltas,
                                        "totalJustificadas" to totalJustificadas,
                                        "parcelasPagas" to parcelasPagas,
                                        "parcelasReembolsadas" to
                                                parcelasReembolsadas,
                                        "parcelasEstornadas" to
                                                parcelasEstornadas
                                    )

                                    val movimentacaoReferencia =
                                        db.collection(COLECAO_MOVIMENTACOES)
                                            .document()

                                    val movimentacao =
                                        hashMapOf<String, Any>(
                                            "alunoId" to matriculaTratada,
                                            "nomeAluno" to nomeAluno,
                                            "tipo" to
                                                    TipoMovimentacaoCrismando
                                                        .ARQUIVAMENTO.name,
                                            "situacao" to situacao.name,
                                            "turmaOrigemId" to turmaAnteriorId,
                                            "turmaOrigemNome" to
                                                    turmaAnteriorNome,
                                            "turmaDestinoId" to "",
                                            "turmaDestinoNome" to "",
                                            "motivo" to motivoTratado,
                                            "responsavel" to
                                                    responsavelTratado,
                                            "dataMovimentacao" to
                                                    FieldValue.serverTimestamp()
                                        )

                                    val lote = db.batch()

                                    lote.set(
                                        db.collection(
                                            COLECAO_HISTORICO_ALUNOS
                                        ).document(matriculaTratada),
                                        historico,
                                        SetOptions.merge()
                                    )

                                    lote.update(
                                        referenciaUsuario,
                                        mapOf(
                                            "ativo" to false,
                                            "situacao" to situacao.name,
                                            "motivoSituacao" to motivoTratado,
                                            "dataSituacao" to
                                                    FieldValue.serverTimestamp(),
                                            "atualizadoPor" to
                                                    responsavelTratado,
                                            "dataAtualizacao" to
                                                    FieldValue.serverTimestamp()
                                        )
                                    )

                                    lote.set(
                                        movimentacaoReferencia,
                                        movimentacao
                                    )

                                    lote.commit()
                                        .addOnSuccessListener {
                                            onSuccess()
                                        }
                                        .addOnFailureListener(onError)
                                }
                                .addOnFailureListener(onError)
                        }
                        .addOnFailureListener(onError)
                }

                if (turmaAnteriorId.isBlank()) {
                    continuarArquivamento("")
                } else {
                    db.collection(COLECAO_TURMAS)
                        .document(turmaAnteriorId)
                        .get()
                        .addOnSuccessListener { turmaDocumento ->
                            continuarArquivamento(
                                turmaDocumento.getString("nome")
                                    .orEmpty()
                                    .trim()
                            )
                        }
                        .addOnFailureListener {
                            // A turma pode ter sido arquivada ou removida
                            // manualmente. Ainda assim, preservamos o aluno.
                            continuarArquivamento("")
                        }
                }
            }
            .addOnFailureListener(onError)
    }

    /**
     * Mantido para compatibilidade com as telas atuais.
     *
     * A partir de agora esta função NÃO apaga nada.
     * Ela apenas arquiva o aluno com segurança.
     */
    fun desativarCrismando(
        matricula: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        arquivarCrismando(
            matricula = matricula,
            situacao = SituacaoCrismando.INATIVO,
            motivo = "Cadastro arquivado pelo aplicativo.",
            responsavel = "Sistema",
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * Nome antigo preservado para o projeto continuar compilando.
     *
     * Apesar do nome, NÃO existe mais exclusão definitiva
     * de aluno pelo aplicativo.
     */
    fun excluirCrismandoDefinitivamente(
        matricula: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        arquivarCrismando(
            matricula = matricula,
            situacao = SituacaoCrismando.INATIVO,
            motivo = "Cadastro arquivado pelo aplicativo.",
            responsavel = "Sistema",
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun reativarCrismando(
        matricula: String,
        responsavel: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(matricula)
        val responsavelTratado = responsavel.trim().ifBlank { "Sistema" }

        val usuarioReferencia = db.collection(COLECAO_USUARIOS)
            .document(matriculaTratada)

        usuarioReferencia.get()
            .addOnSuccessListener { usuarioDocumento ->
                if (!usuarioDocumento.exists()) {
                    onError(
                        IllegalStateException(
                            "O crismando não foi encontrado."
                        )
                    )
                    return@addOnSuccessListener
                }

                val movimentacaoReferencia =
                    db.collection(COLECAO_MOVIMENTACOES).document()

                val lote = db.batch()

                lote.update(
                    usuarioReferencia,
                    mapOf(
                        "ativo" to true,
                        "situacao" to SituacaoCrismando.ATIVO.name,
                        "motivoSituacao" to "",
                        "dataSituacao" to FieldValue.serverTimestamp(),
                        "atualizadoPor" to responsavelTratado,
                        "dataAtualizacao" to FieldValue.serverTimestamp()
                    )
                )

                lote.set(
                    movimentacaoReferencia,
                    mapOf(
                        "alunoId" to matriculaTratada,
                        "nomeAluno" to
                                usuarioDocumento.getString("nome").orEmpty(),
                        "tipo" to
                                TipoMovimentacaoCrismando.REATIVACAO.name,
                        "turmaOrigemId" to
                                usuarioDocumento.getString("turmaId").orEmpty(),
                        "turmaOrigemNome" to "",
                        "turmaDestinoId" to
                                usuarioDocumento.getString("turmaId").orEmpty(),
                        "turmaDestinoNome" to "",
                        "motivo" to "Cadastro reativado.",
                        "responsavel" to responsavelTratado,
                        "dataMovimentacao" to
                                FieldValue.serverTimestamp()
                    )
                )

                lote.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener(onError)
            }
            .addOnFailureListener(onError)
    }

    /**
     * Transfere o aluno sem alterar ou apagar os registros
     * financeiros e de frequência já existentes.
     */
    fun transferirCrismando(
        matricula: String,
        novaTurmaId: String,
        motivo: String,
        responsavel: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(matricula)
        val novaTurmaTratada = novaTurmaId.trim()
        val motivoTratado = motivo.trim()
        val responsavelTratado = responsavel.trim().ifBlank { "Sistema" }

        if (matriculaTratada.isBlank() || novaTurmaTratada.isBlank()) {
            onError(
                IllegalArgumentException(
                    "Matrícula ou turma de destino inválida."
                )
            )
            return
        }

        val usuarioReferencia = db.collection(COLECAO_USUARIOS)
            .document(matriculaTratada)

        usuarioReferencia.get()
            .addOnSuccessListener { usuarioDocumento ->
                if (!usuarioDocumento.exists()) {
                    onError(
                        IllegalStateException(
                            "O crismando não foi encontrado."
                        )
                    )
                    return@addOnSuccessListener
                }

                val turmaOrigemId = usuarioDocumento
                    .getString("turmaId")
                    .orEmpty()

                if (turmaOrigemId == novaTurmaTratada) {
                    onError(
                        IllegalArgumentException(
                            "O crismando já pertence a essa turma."
                        )
                    )
                    return@addOnSuccessListener
                }

                db.collection(COLECAO_TURMAS)
                    .document(novaTurmaTratada)
                    .get()
                    .addOnSuccessListener { turmaDestinoDocumento ->
                        if (!turmaDestinoDocumento.exists()) {
                            onError(
                                IllegalStateException(
                                    "A turma de destino não foi encontrada."
                                )
                            )
                            return@addOnSuccessListener
                        }

                        fun concluirTransferencia(
                            turmaOrigemNome: String
                        ) {
                            val turmaDestinoNome =
                                turmaDestinoDocumento.getString("nome")
                                    .orEmpty()

                            val categoriaDestino =
                                turmaDestinoDocumento.getString("categoria")
                                    .orEmpty()

                            val movimentacaoReferencia =
                                db.collection(COLECAO_MOVIMENTACOES)
                                    .document()

                            val lote = db.batch()

                            lote.update(
                                usuarioReferencia,
                                mapOf(
                                    "turmaId" to novaTurmaTratada,
                                    "categoria" to categoriaDestino,
                                    "ativo" to true,
                                    "situacao" to
                                            SituacaoCrismando.ATIVO.name,
                                    "motivoSituacao" to "",
                                    "dataSituacao" to
                                            FieldValue.serverTimestamp(),
                                    "atualizadoPor" to responsavelTratado,
                                    "dataAtualizacao" to
                                            FieldValue.serverTimestamp()
                                )
                            )

                            lote.set(
                                movimentacaoReferencia,
                                mapOf(
                                    "alunoId" to matriculaTratada,
                                    "nomeAluno" to
                                            usuarioDocumento.getString("nome")
                                                .orEmpty(),
                                    "tipo" to
                                            TipoMovimentacaoCrismando
                                                .TRANSFERENCIA.name,
                                    "turmaOrigemId" to turmaOrigemId,
                                    "turmaOrigemNome" to
                                            turmaOrigemNome,
                                    "turmaDestinoId" to
                                            novaTurmaTratada,
                                    "turmaDestinoNome" to
                                            turmaDestinoNome,
                                    "motivo" to motivoTratado,
                                    "responsavel" to
                                            responsavelTratado,
                                    "dataMovimentacao" to
                                            FieldValue.serverTimestamp()
                                )
                            )

                            lote.commit()
                                .addOnSuccessListener { onSuccess() }
                                .addOnFailureListener(onError)
                        }

                        if (turmaOrigemId.isBlank()) {
                            concluirTransferencia("")
                        } else {
                            db.collection(COLECAO_TURMAS)
                                .document(turmaOrigemId)
                                .get()
                                .addOnSuccessListener {
                                    concluirTransferencia(
                                        it.getString("nome").orEmpty()
                                    )
                                }
                                .addOnFailureListener {
                                    concluirTransferencia("")
                                }
                        }
                    }
                    .addOnFailureListener(onError)
            }
            .addOnFailureListener(onError)
    }


    // ==========================================================
    // DOCUMENTAÇÃO DO CRISMANDO E DO PADRINHO
    // ==========================================================

    fun carregarCadastroDocumentacao(
        alunoId: String,
        perfil: PerfilDocumentacao,
        onSuccess: (CadastroDocumentacao) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(alunoId)
        if (matriculaTratada.isBlank()) {
            onError(IllegalArgumentException("Matrícula inválida."))
            return
        }

        val documentoId = "$matriculaTratada-${perfil.name}"
        db.collection(COLECAO_DOCUMENTOS_CONFIGURACAO)
            .document(documentoId)
            .get()
            .addOnSuccessListener { documento ->
                if (!documento.exists()) {
                    onSuccess(
                        CadastroDocumentacao(
                            alunoId = matriculaTratada,
                            perfil = perfil.name
                        )
                    )
                    return@addOnSuccessListener
                }

                onSuccess(
                    CadastroDocumentacao(
                        alunoId = documento.getString("alunoId") ?: matriculaTratada,
                        turmaId = documento.getString("turmaId").orEmpty(),
                        perfil = documento.getString("perfil") ?: perfil.name,
                        primeiraComunhaoPossui = documento.getBoolean("primeiraComunhaoPossui") ?: false,
                        primeiraComunhaoEntregue = documento.getBoolean("primeiraComunhaoEntregue") ?: false,
                        batismoEntregue = documento.getBoolean("batismoEntregue") ?: false,
                        crismaPossui = documento.getBoolean("crismaPossui") ?: false,
                        crismaEntregue = documento.getBoolean("crismaEntregue") ?: false,
                        identificacaoEntregue = documento.getBoolean("identificacaoEntregue") ?: false,
                        tipoIdentificacao = documento.getString("tipoIdentificacao")
                            ?: TipoIdentificacaoDocumento.NAO_INFORMADO.name,
                        identificacaoOutro = documento.getString("identificacaoOutro").orEmpty(),
                        casamentoStatus = documento.getString("casamentoStatus")
                            ?: StatusCasamentoDocumento.NAO_INFORMADO.name,
                        atualizadoPor = documento.getString("atualizadoPor").orEmpty(),
                        dataAtualizacao = obterDataEmMillis(documento, "dataAtualizacao")
                    )
                )
            }
            .addOnFailureListener(onError)
    }

    fun salvarCadastroDocumentacao(
        cadastro: CadastroDocumentacao,
        responsavel: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(cadastro.alunoId)
        val turmaTratada = cadastro.turmaId.trim()
        val responsavelTratado = responsavel.trim().ifBlank { "Sistema" }
        val perfil = cadastro.obterPerfil()

        if (matriculaTratada.isBlank() || turmaTratada.isBlank()) {
            onError(IllegalArgumentException("Crismando ou turma inválida para salvar os documentos."))
            return
        }

        if (cadastro.obterStatusCasamento() == StatusCasamentoDocumento.NAO_INFORMADO) {
            onError(IllegalArgumentException("Informe a situação do comprovante de casamento."))
            return
        }

        if (cadastro.identificacaoEntregue && cadastro.obterTipoIdentificacao() == TipoIdentificacaoDocumento.NAO_INFORMADO) {
            onError(IllegalArgumentException("Informe qual documento de identificação foi entregue."))
            return
        }

        if (cadastro.identificacaoEntregue && cadastro.obterTipoIdentificacao() == TipoIdentificacaoDocumento.OUTRO && cadastro.identificacaoOutro.isBlank()) {
            onError(IllegalArgumentException("Descreva o outro documento de identificação."))
            return
        }

        val configuracaoId = "$matriculaTratada-${perfil.name}"
        val lote = db.batch()

        lote.set(
            db.collection(COLECAO_DOCUMENTOS_CONFIGURACAO).document(configuracaoId),
            mapOf(
                "alunoId" to matriculaTratada,
                "turmaId" to turmaTratada,
                "perfil" to perfil.name,
                "primeiraComunhaoPossui" to cadastro.primeiraComunhaoPossui,
                "primeiraComunhaoEntregue" to cadastro.primeiraComunhaoEntregue,
                "batismoEntregue" to cadastro.batismoEntregue,
                "crismaPossui" to cadastro.crismaPossui,
                "crismaEntregue" to cadastro.crismaEntregue,
                "identificacaoEntregue" to cadastro.identificacaoEntregue,
                "tipoIdentificacao" to cadastro.obterTipoIdentificacao().name,
                "identificacaoOutro" to cadastro.identificacaoOutro.trim(),
                "casamentoStatus" to cadastro.obterStatusCasamento().name,
                "atualizadoPor" to responsavelTratado,
                "dataAtualizacao" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )

        fun referenciaDocumento(tipo: String) = db.collection(COLECAO_DOCUMENTOS)
            .document("DOC-$matriculaTratada-${perfil.name}-$tipo")

        fun salvarDocumento(tipo: String, nome: String, status: StatusDocumento, detalhe: String = "") {
            lote.set(
                referenciaDocumento(tipo),
                mapOf(
                    "alunoId" to matriculaTratada,
                    "turmaId" to turmaTratada,
                    "perfil" to perfil.name,
                    "tipo" to tipo,
                    "nome" to nome,
                    "status" to status.name,
                    "detalhe" to detalhe,
                    "atualizadoPor" to responsavelTratado,
                    "dataAtualizacao" to FieldValue.serverTimestamp()
                )
            )
        }

        fun apagarDocumento(tipo: String) {
            lote.delete(referenciaDocumento(tipo))
        }

        when (perfil) {
            PerfilDocumentacao.CRISMANDO -> {
                apagarDocumento("CRISMA")
                if (cadastro.primeiraComunhaoPossui) {
                    salvarDocumento(
                        "PRIMEIRA_COMUNHAO",
                        "Comprovante de Primeira Comunhão",
                        if (cadastro.primeiraComunhaoEntregue) StatusDocumento.ENTREGUE else StatusDocumento.NAO_ENTREGUE,
                        "Possui Primeira Comunhão"
                    )
                    apagarDocumento("BATISMO")
                } else {
                    salvarDocumento(
                        "BATISMO",
                        "Comprovante de Batismo",
                        if (cadastro.batismoEntregue) StatusDocumento.ENTREGUE else StatusDocumento.NAO_ENTREGUE,
                        "Não possui Primeira Comunhão"
                    )
                    apagarDocumento("PRIMEIRA_COMUNHAO")
                }
            }

            PerfilDocumentacao.PADRINHO -> {
                if (cadastro.crismaPossui) {
                    salvarDocumento(
                        "CRISMA",
                        "Comprovante de Crisma do Padrinho",
                        if (cadastro.crismaEntregue) StatusDocumento.ENTREGUE else StatusDocumento.NAO_ENTREGUE,
                        "Possui Crisma"
                    )
                    apagarDocumento("PRIMEIRA_COMUNHAO")
                    apagarDocumento("BATISMO")
                } else if (cadastro.primeiraComunhaoPossui) {
                    salvarDocumento(
                        "PRIMEIRA_COMUNHAO",
                        "Comprovante de Primeira Comunhão do Padrinho",
                        if (cadastro.primeiraComunhaoEntregue) StatusDocumento.ENTREGUE else StatusDocumento.NAO_ENTREGUE,
                        "Não possui Crisma"
                    )
                    apagarDocumento("CRISMA")
                    apagarDocumento("BATISMO")
                } else {
                    salvarDocumento(
                        "BATISMO",
                        "Comprovante de Batismo do Padrinho",
                        if (cadastro.batismoEntregue) StatusDocumento.ENTREGUE else StatusDocumento.NAO_ENTREGUE,
                        "Não possui Crisma nem Primeira Comunhão"
                    )
                    apagarDocumento("CRISMA")
                    apagarDocumento("PRIMEIRA_COMUNHAO")
                }
            }
        }

        val identificacaoDetalhe = when (cadastro.obterTipoIdentificacao()) {
            TipoIdentificacaoDocumento.IDENTIDADE -> "Identidade"
            TipoIdentificacaoDocumento.CNH -> "CNH"
            TipoIdentificacaoDocumento.OUTRO -> cadastro.identificacaoOutro.trim()
            TipoIdentificacaoDocumento.NAO_INFORMADO -> "Tipo não informado"
        }

        salvarDocumento(
            "IDENTIFICACAO",
            if (perfil == PerfilDocumentacao.CRISMANDO) "Documento de Identificação" else "Documento de Identificação do Padrinho",
            if (cadastro.identificacaoEntregue) StatusDocumento.ENTREGUE else StatusDocumento.NAO_ENTREGUE,
            identificacaoDetalhe
        )

        when (cadastro.obterStatusCasamento()) {
            StatusCasamentoDocumento.ENTREGUE -> salvarDocumento(
                "CASAMENTO",
                if (perfil == PerfilDocumentacao.CRISMANDO) "Comprovante de Casamento" else "Comprovante de Casamento do Padrinho",
                StatusDocumento.ENTREGUE,
                "Casado e comprovante entregue"
            )
            StatusCasamentoDocumento.NAO_ENTREGUE -> salvarDocumento(
                "CASAMENTO",
                if (perfil == PerfilDocumentacao.CRISMANDO) "Comprovante de Casamento" else "Comprovante de Casamento do Padrinho",
                StatusDocumento.NAO_ENTREGUE,
                "Casado, comprovante pendente"
            )
            StatusCasamentoDocumento.NAO_CASADO -> salvarDocumento(
                "CASAMENTO",
                if (perfil == PerfilDocumentacao.CRISMANDO) "Comprovante de Casamento" else "Comprovante de Casamento do Padrinho",
                StatusDocumento.NAO_POSSUI,
                "Não é casado"
            )
            StatusCasamentoDocumento.NAO_INFORMADO -> Unit
        }

        AuditoriaRepository.adicionarAoLote(
            lote = lote,
            tipo = TipoEventoAuditoria.DOCUMENTOS_ATUALIZADOS,
            entidade = EntidadeAuditoria.DOCUMENTOS,
            documentoOrigemId = configuracaoId,
            alunoId = matriculaTratada,
            turmaId = turmaTratada,
            dadosNovos = mapOf(
                "perfil" to perfil.name,
                "primeiraComunhaoPossui" to
                        cadastro.primeiraComunhaoPossui,
                "primeiraComunhaoEntregue" to
                        cadastro.primeiraComunhaoEntregue,
                "batismoEntregue" to cadastro.batismoEntregue,
                "crismaPossui" to cadastro.crismaPossui,
                "crismaEntregue" to cadastro.crismaEntregue,
                "identificacaoEntregue" to
                        cadastro.identificacaoEntregue,
                "tipoIdentificacao" to
                        cadastro.obterTipoIdentificacao().name,
                "identificacaoOutro" to
                        cadastro.identificacaoOutro.trim(),
                "casamentoStatus" to
                        cadastro.obterStatusCasamento().name
            ),
            responsavelInformado = responsavelTratado
        )

        lote.commit()
            .addOnSuccessListener { onSuccess() }
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

        /*
         * A tela envia todos os crismandos ao salvar a chamada.
         * Esta leitura evita criar eventos de auditoria para alunos
         * cujo status não mudou.
         */
        referencia.get()
            .addOnSuccessListener { documentoAtual ->
                val statusAtualTexto = documentoAtual
                    .getString("status")
                    ?.trim()
                    ?.uppercase(Locale.ROOT)
                    ?: StatusFrequencia.NENHUM.name

                val statusAtual = try {
                    StatusFrequencia.valueOf(statusAtualTexto)
                } catch (_: IllegalArgumentException) {
                    StatusFrequencia.NENHUM
                }

                if (statusAtual == status) {
                    onSuccess()
                    return@addOnSuccessListener
                }

                val lote = db.batch()

                val tipoEvento = if (
                    status == StatusFrequencia.NENHUM
                ) {
                    lote.delete(referencia)
                    TipoEventoAuditoria.FREQUENCIA_REMOVIDA
                } else {
                    val dadosFrequencia =
                        hashMapOf<String, Any>(
                            "alunoId" to matriculaTratada,
                            "turmaId" to turmaId,
                            "encontro" to encontro,
                            "status" to status.name,
                            "dataRegistro" to
                                    System.currentTimeMillis()
                        )

                    lote.set(
                        referencia,
                        dadosFrequencia
                    )

                    TipoEventoAuditoria.FREQUENCIA_REGISTRADA
                }

                AuditoriaRepository.adicionarAoLote(
                    lote = lote,
                    tipo = tipoEvento,
                    entidade = EntidadeAuditoria.FREQUENCIA,
                    documentoOrigemId = frequenciaId,
                    alunoId = matriculaTratada,
                    turmaId = turmaId,
                    dadosAnteriores =
                            documentoAtual.data
                                ?: emptyMap(),
                    dadosNovos = mapOf(
                        "encontro" to encontro,
                        "status" to status.name
                    )
                )

                lote.commit()
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener(onError)
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
                            ),

                            dataReembolso = obterDataEmMillis(
                                documento = documento,
                                campo = "dataReembolso"
                            ),

                            reembolsadoPor = documento
                                .getString("reembolsadoPor")
                                .orEmpty(),

                            motivoReembolso = documento
                                .getString("motivoReembolso")
                                .orEmpty(),

                            dataEstorno = obterDataEmMillis(
                                documento = documento,
                                campo = "dataEstorno"
                            ),

                            estornadoPor = documento
                                .getString("estornadoPor")
                                .orEmpty(),

                            motivoEstorno = documento
                                .getString("motivoEstorno")
                                .orEmpty()
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

        val referenciaPagamento =
            db.collection(COLECAO_FINANCEIRO)
                .document(pagamentoId)

        val lote = db.batch()

        lote.set(
            referenciaPagamento,
            dadosPagamento
        )

        AuditoriaRepository.adicionarAoLote(
            lote = lote,
            tipo = TipoEventoAuditoria.PAGAMENTO_REGISTRADO,
            entidade = EntidadeAuditoria.FINANCEIRO,
            documentoOrigemId = pagamentoId,
            alunoId = matriculaTratada,
            turmaId = turmaId,
            dadosNovos = mapOf(
                "parcela" to parcela,
                "status" to StatusPagamento.PAGO.name,
                "recebidoPor" to responsavelTratado
            ),
            responsavelInformado = responsavelTratado
        )

        lote.commit()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    fun reembolsarPagamento(
        turmaId: String,
        alunoId: String,
        parcela: Int,
        responsavel: String,
        motivo: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        alterarStatusPagamentoComHistorico(
            turmaId = turmaId,
            alunoId = alunoId,
            parcela = parcela,
            novoStatus = StatusPagamento.REEMBOLSADO,
            responsavel = responsavel,
            motivo = motivo,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun estornarPagamento(
        turmaId: String,
        alunoId: String,
        parcela: Int,
        responsavel: String,
        motivo: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        alterarStatusPagamentoComHistorico(
            turmaId = turmaId,
            alunoId = alunoId,
            parcela = parcela,
            novoStatus = StatusPagamento.ESTORNADO,
            responsavel = responsavel,
            motivo = motivo,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun alterarStatusPagamentoComHistorico(
        turmaId: String,
        alunoId: String,
        parcela: Int,
        novoStatus: StatusPagamento,
        responsavel: String,
        motivo: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val matriculaTratada = normalizarMatricula(alunoId)
        val parcelaFormatada = numeroComDoisDigitos(parcela)
        val responsavelTratado = responsavel.trim().ifBlank { "Sistema" }
        val motivoTratado = motivo.trim()

        if (parcela <= 0) {
            onError(
                IllegalArgumentException(
                    "Número de parcela inválido."
                )
            )
            return
        }

        val pagamentoId =
            "PAG-$turmaId-$matriculaTratada-P$parcelaFormatada"

        val pagamentoReferencia =
            db.collection(COLECAO_FINANCEIRO)
                .document(pagamentoId)

        pagamentoReferencia.get()
            .addOnSuccessListener { pagamentoDocumento ->
                if (!pagamentoDocumento.exists()) {
                    onError(
                        IllegalStateException(
                            "O pagamento informado não foi encontrado."
                        )
                    )
                    return@addOnSuccessListener
                }

                val usuarioReferencia =
                    db.collection(COLECAO_USUARIOS)
                        .document(matriculaTratada)

                usuarioReferencia.get()
                    .addOnSuccessListener { usuarioDocumento ->
                        val movimentacaoReferencia =
                            db.collection(COLECAO_MOVIMENTACOES)
                                .document()

                        val camposPagamento = when (novoStatus) {
                            StatusPagamento.REEMBOLSADO -> mapOf(
                                "status" to
                                        StatusPagamento.REEMBOLSADO.name,
                                "dataReembolso" to
                                        FieldValue.serverTimestamp(),
                                "reembolsadoPor" to
                                        responsavelTratado,
                                "motivoReembolso" to
                                        motivoTratado
                            )

                            StatusPagamento.ESTORNADO -> mapOf(
                                "status" to
                                        StatusPagamento.ESTORNADO.name,
                                "dataEstorno" to
                                        FieldValue.serverTimestamp(),
                                "estornadoPor" to
                                        responsavelTratado,
                                "motivoEstorno" to
                                        motivoTratado
                            )

                            else -> {
                                onError(
                                    IllegalArgumentException(
                                        "Status financeiro inválido."
                                    )
                                )
                                return@addOnSuccessListener
                            }
                        }

                        val tipoMovimentacao =
                            if (
                                novoStatus ==
                                StatusPagamento.REEMBOLSADO
                            ) {
                                TipoMovimentacaoCrismando.REEMBOLSO
                            } else {
                                TipoMovimentacaoCrismando.ESTORNO
                            }

                        val lote = db.batch()

                        lote.update(
                            pagamentoReferencia,
                            camposPagamento
                        )

                        lote.set(
                            movimentacaoReferencia,
                            mapOf(
                                "alunoId" to matriculaTratada,
                                "nomeAluno" to
                                        usuarioDocumento.getString("nome")
                                            .orEmpty(),
                                "tipo" to tipoMovimentacao.name,
                                "turmaOrigemId" to turmaId,
                                "turmaOrigemNome" to "",
                                "turmaDestinoId" to "",
                                "turmaDestinoNome" to "",
                                "parcela" to parcela,
                                "motivo" to motivoTratado,
                                "responsavel" to
                                        responsavelTratado,
                                "dataMovimentacao" to
                                        FieldValue.serverTimestamp()
                            )
                        )

                        val tipoAuditoria =
                            if (
                                novoStatus ==
                                StatusPagamento.REEMBOLSADO
                            ) {
                                TipoEventoAuditoria
                                    .PAGAMENTO_REEMBOLSADO
                            } else {
                                TipoEventoAuditoria
                                    .PAGAMENTO_ESTORNADO
                            }

                        AuditoriaRepository.adicionarAoLote(
                            lote = lote,
                            tipo = tipoAuditoria,
                            entidade = EntidadeAuditoria.FINANCEIRO,
                            documentoOrigemId = pagamentoId,
                            alunoId = matriculaTratada,
                            turmaId = turmaId,
                            dadosAnteriores =
                                    pagamentoDocumento.data
                                        ?: emptyMap(),
                            dadosNovos = mapOf(
                                "parcela" to parcela,
                                "status" to novoStatus.name,
                                "motivo" to motivoTratado,
                                "responsavel" to
                                        responsavelTratado,
                                "nomeAluno" to
                                        usuarioDocumento
                                            .getString("nome")
                                            .orEmpty()
                            ),
                            responsavelInformado =
                                    responsavelTratado
                        )

                        lote.commit()
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener(onError)
                    }
                    .addOnFailureListener(onError)
            }
            .addOnFailureListener(onError)
    }

    /**
     * Compatibilidade com as telas atuais.
     *
     * Remover uma baixa agora significa ESTORNAR.
     * O documento financeiro permanece salvo.
     */
    fun removerPagamento(
        turmaId: String,
        alunoId: String,
        parcela: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        estornarPagamento(
            turmaId = turmaId,
            alunoId = alunoId,
            parcela = parcela,
            responsavel = "Sistema",
            motivo = "Baixa removida pelo aplicativo.",
            onSuccess = onSuccess,
            onError = onError
        )
    }

    // ==========================================================
    // ATALHOS DA PRIMEIRA TELA
    // ==========================================================

    fun ouvirAtalhosIniciais(
        onUpdate: (List<AtalhoInicialConfiguracao>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {
        return db.collection(COLECAO_ATALHOS_INICIO)
            .addSnapshotListener { snapshot, erro ->
                if (erro != null) {
                    onError(erro)
                    return@addSnapshotListener
                }

                val documentosPorId = snapshot
                    ?.documents
                    ?.associateBy {
                        it.id.trim().uppercase(Locale.ROOT)
                    }
                    .orEmpty()

                val links = listOf(
                    AtalhosIniciaisPadrao.biblia(),
                    AtalhosIniciaisPadrao.catecismo()
                ).map { padrao ->
                    val documento =
                        documentosPorId[padrao.id]

                    if (documento == null) {
                        padrao
                    } else {
                        padrao.copy(
                            titulo = documento
                                .getString("titulo")
                                .orEmpty()
                                .trim()
                                .ifBlank {
                                    padrao.titulo
                                },
                            descricao = documento
                                .getString("descricao")
                                .orEmpty()
                                .trim()
                                .ifBlank {
                                    padrao.descricao
                                },
                            url = documento
                                .getString("url")
                                .orEmpty()
                                .trim()
                                .ifBlank {
                                    padrao.url
                                },
                            iconeCodigo = documento
                                .getString("iconeCodigo")
                                .orEmpty()
                                .trim()
                                .uppercase(Locale.ROOT)
                                .takeIf {
                                    it in
                                            IconesAtalhoInicial
                                                .codigosPermitidos
                                }
                                ?: padrao.iconeCodigo
                        )
                    }
                }

                val ajudaPadrao =
                    AtalhosIniciaisPadrao.ajuda()

                val documentoBaseAjuda =
                    documentosPorId[
                        AtalhosIniciaisPadrao.ID_BIBLIA
                    ]

                val ajuda = if (documentoBaseAjuda == null) {
                    ajudaPadrao
                } else {
                    ajudaPadrao.copy(
                        titulo = documentoBaseAjuda
                            .getString("ajudaTitulo")
                            .orEmpty()
                            .trim()
                            .ifBlank {
                                ajudaPadrao.titulo
                            },
                        descricao = documentoBaseAjuda
                            .getString("ajudaDescricao")
                            .orEmpty()
                            .trim()
                            .ifBlank {
                                ajudaPadrao.descricao
                            },
                        url = documentoBaseAjuda
                            .getString("ajudaMensagem")
                            .orEmpty()
                            .trim()
                            .ifBlank {
                                ajudaPadrao.url
                            },
                        iconeCodigo = documentoBaseAjuda
                            .getString("ajudaIconeCodigo")
                            .orEmpty()
                            .trim()
                            .uppercase(Locale.ROOT)
                            .takeIf {
                                it in
                                        IconesAtalhoInicial
                                            .codigosPermitidos
                            }
                            ?: ajudaPadrao.iconeCodigo
                    )
                }

                fun carregarConteudoAuxiliar(
                    padrao: AtalhoInicialConfiguracao,
                    prefixo: String
                ): AtalhoInicialConfiguracao {
                    if (documentoBaseAjuda == null) {
                        return padrao
                    }

                    return padrao.copy(
                        titulo = documentoBaseAjuda
                            .getString("${prefixo}Titulo")
                            .orEmpty()
                            .trim()
                            .ifBlank { padrao.titulo },
                        descricao = documentoBaseAjuda
                            .getString("${prefixo}Descricao")
                            .orEmpty()
                            .trim()
                            .ifBlank { padrao.descricao },
                        url = documentoBaseAjuda
                            .getString("${prefixo}Mensagem")
                            .orEmpty()
                            .trim()
                            .ifBlank { padrao.url },
                        iconeCodigo = documentoBaseAjuda
                            .getString("${prefixo}IconeCodigo")
                            .orEmpty()
                            .trim()
                            .uppercase(Locale.ROOT)
                            .takeIf {
                                it in IconesAtalhoInicial.codigosPermitidos
                            }
                            ?: padrao.iconeCodigo
                    )
                }

                val sobre = carregarConteudoAuxiliar(
                    AtalhosIniciaisPadrao.sobre(),
                    "sobre"
                )

                val contatos = carregarConteudoAuxiliar(
                    AtalhosIniciaisPadrao.contatos(),
                    "contatos"
                )

                onUpdate(links + listOf(ajuda, sobre, contatos))
            }
    }

    fun salvarAtalhoInicial(
        configuracao: AtalhoInicialConfiguracao,
        responsavel: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val idTratado = configuracao.id
            .trim()
            .uppercase(Locale.ROOT)

        val prefixoConteudo = when (idTratado) {
            AtalhosIniciaisPadrao.ID_AJUDA -> "ajuda"
            AtalhosIniciaisPadrao.ID_SOBRE -> "sobre"
            AtalhosIniciaisPadrao.ID_CONTATOS -> "contatos"
            else -> null
        }

        if (prefixoConteudo != null) {
            salvarConteudoInstitucional(
                configuracao = configuracao,
                prefixo = prefixoConteudo,
                responsavel = responsavel,
                onSuccess = onSuccess,
                onError = onError
            )
            return
        }

        val tituloTratado = configuracao.titulo
            .trim()

        val descricaoTratada = configuracao.descricao
            .trim()

        val urlTratada = configuracao.url
            .trim()

        val iconeTratado = configuracao.iconeCodigo
            .trim()
            .uppercase(Locale.ROOT)

        if (
            idTratado != AtalhosIniciaisPadrao.ID_BIBLIA &&
            idTratado != AtalhosIniciaisPadrao.ID_CATECISMO
        ) {
            onError(
                IllegalArgumentException(
                    "O botão selecionado é inválido."
                )
            )
            return
        }

        if (
            tituloTratado.isBlank() ||
            tituloTratado.length > 18
        ) {
            onError(
                IllegalArgumentException(
                    "O nome deve ter entre 1 e 18 caracteres."
                )
            )
            return
        }

        if (
            descricaoTratada.isBlank() ||
            descricaoTratada.length > 24
        ) {
            onError(
                IllegalArgumentException(
                    "O texto pequeno deve ter entre 1 e 24 caracteres."
                )
            )
            return
        }

        if (
            !urlTratada.startsWith(
                "https://",
                ignoreCase = true
            ) &&
            !urlTratada.startsWith(
                "http://",
                ignoreCase = true
            )
        ) {
            onError(
                IllegalArgumentException(
                    "Digite um link completo começando com https://"
                )
            )
            return
        }

        if (
            iconeTratado !in
            IconesAtalhoInicial.codigosPermitidos
        ) {
            onError(
                IllegalArgumentException(
                    "O ícone selecionado é inválido."
                )
            )
            return
        }

        val dados = mapOf(
            "id" to idTratado,
            "titulo" to tituloTratado,
            "descricao" to descricaoTratada,
            "url" to urlTratada,
            "iconeCodigo" to iconeTratado,
            "atualizadoPor" to responsavel
                .trim()
                .ifBlank {
                    "Administrador"
                },
            "dataAtualizacao" to
                    FieldValue.serverTimestamp()
        )

        db.collection(COLECAO_ATALHOS_INICIO)
            .document(idTratado)
            .set(
                dados,
                SetOptions.merge()
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    private fun salvarConteudoInstitucional(
        configuracao: AtalhoInicialConfiguracao,
        prefixo: String,
        responsavel: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val titulo = configuracao.titulo.trim()
        val descricao = configuracao.descricao.trim()
        val mensagem = configuracao.url.trim()
        val icone = configuracao.iconeCodigo
            .trim()
            .uppercase(Locale.ROOT)

        if (titulo.isBlank() || titulo.length > 18) {
            onError(
                IllegalArgumentException(
                    "O título deve ter entre 1 e 18 caracteres."
                )
            )
            return
        }

        if (
            descricao.isBlank() ||
            descricao.length > 36
        ) {
            onError(
                IllegalArgumentException(
                    "O texto pequeno deve ter entre 1 e 36 caracteres."
                )
            )
            return
        }

        if (
            mensagem.isBlank() ||
            mensagem.length > 400
        ) {
            onError(
                IllegalArgumentException(
                    "A mensagem deve ter entre 1 e 400 caracteres."
                )
            )
            return
        }

        if (
            icone !in
            IconesAtalhoInicial.codigosPermitidos
        ) {
            onError(
                IllegalArgumentException(
                    "O ícone selecionado é inválido."
                )
            )
            return
        }

        val referenciaBiblia =
            db.collection(COLECAO_ATALHOS_INICIO)
                .document(
                    AtalhosIniciaisPadrao.ID_BIBLIA
                )

        referenciaBiblia.get()
            .addOnSuccessListener { documento ->
                val bibliaPadrao =
                    AtalhosIniciaisPadrao.biblia()

                fun stringAtual(
                    campo: String,
                    padrao: String
                ): String {
                    return documento
                        .getString(campo)
                        .orEmpty()
                        .trim()
                        .ifBlank { padrao }
                }

                val iconeBibliaAtual = documento
                    .getString("iconeCodigo")
                    .orEmpty()
                    .trim()
                    .uppercase(Locale.ROOT)
                    .takeIf {
                        it in
                                IconesAtalhoInicial
                                    .codigosPermitidos
                    }
                    ?: bibliaPadrao.iconeCodigo

                val dados = mapOf(
                    "id" to
                            AtalhosIniciaisPadrao.ID_BIBLIA,
                    "titulo" to stringAtual(
                        "titulo",
                        bibliaPadrao.titulo
                    ),
                    "descricao" to stringAtual(
                        "descricao",
                        bibliaPadrao.descricao
                    ),
                    "url" to stringAtual(
                        "url",
                        bibliaPadrao.url
                    ),
                    "iconeCodigo" to iconeBibliaAtual,
                    "${prefixo}Titulo" to titulo,
                    "${prefixo}Descricao" to descricao,
                    "${prefixo}Mensagem" to mensagem,
                    "${prefixo}IconeCodigo" to icone,
                    "${prefixo}AtualizadoPor" to responsavel
                        .trim()
                        .ifBlank {
                            "Administrador"
                        },
                    "${prefixo}DataAtualizacao" to
                            FieldValue.serverTimestamp(),
                    "atualizadoPor" to responsavel
                        .trim()
                        .ifBlank {
                            "Administrador"
                        },
                    "dataAtualizacao" to
                            FieldValue.serverTimestamp()
                )

                referenciaBiblia
                    .set(
                        dados,
                        SetOptions.merge()
                    )
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener(onError)
            }
            .addOnFailureListener(onError)
    }

    // ==========================================================
    // AVISOS
    // ==========================================================

    /**
     * Destinos padronizados:
     *
     * GERAL
     *     Aparece para todos os crismandos.
     *
     * CATEGORIA_JOVEM
     *     Aparece para todas as turmas jovens.
     *
     * CATEGORIA_ADULTA
     *     Aparece para todas as turmas adultas.
     *
     * ID REAL DA TURMA
     *     Aparece somente para a turma selecionada.
     */
    fun ouvirAvisosDaTurma(
        turmaId: String,
        categoria: String,
        onUpdate: (List<Aviso>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        val categoriaTratada = normalizarCategoria(categoria)

        val destinoCategoria = when (categoriaTratada) {
            "jovem" -> "CATEGORIA_JOVEM"
            "adulta", "adulto" -> "CATEGORIA_ADULTA"
            else -> ""
        }

        // Compatibilidade com os avisos antigos.
        val destinoAntigo = when (categoriaTratada) {
            "jovem" -> "turma_jovem"
            "adulta", "adulto" -> "turma_adulta"
            else -> ""
        }

        val destinos = listOf(
            turmaId,
            "GERAL",
            destinoCategoria,
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
                                ?: "TURMA",
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

    /**
     * Usado nas telas administrativas para mostrar somente
     * os avisos do destino que está sendo editado.
     */
    fun ouvirAvisosPorDestino(
        destinoId: String,
        onUpdate: (List<Aviso>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        val destinoTratado = destinoId.trim()

        return db.collection(COLECAO_AVISOS)
            .whereEqualTo("turmaId", destinoTratado)
            .addSnapshotListener { snapshot, erro ->

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
                                    ?: "TURMA",
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
        tipo: String = "TURMA",
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val destinoTratado = turmaId.trim()
        val textoTratado = texto.trim()
        val tipoTratado = tipo
            .trim()
            .uppercase(Locale.ROOT)
            .ifBlank { "TURMA" }

        if (destinoTratado.isBlank()) {
            onError(
                IllegalArgumentException(
                    "Selecione o destino do aviso."
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
            "tipo" to tipoTratado,
            "turmaId" to destinoTratado,
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
    fun arquivarTurma(
        turmaId: String,
        motivo: String,
        responsavel: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val motivoTratado = motivo.trim()
        val responsavelTratado = responsavel.trim().ifBlank { "Sistema" }

        db.collection(COLECAO_TURMAS)
            .document(turmaId)
            .update(
                mapOf(
                    "ativa" to false,
                    "motivoArquivamento" to motivoTratado,
                    "arquivadaPor" to responsavelTratado,
                    "dataArquivamento" to
                            FieldValue.serverTimestamp(),
                    "dataAtualizacao" to
                            FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    /**
     * Nome antigo mantido para as telas atuais compilarem.
     *
     * A turma e todos os seus dados são apenas arquivados.
     * Nenhum aluno, pagamento ou frequência é apagado.
     */
    fun excluirTurmaDefinitivamente(
        turmaId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        arquivarTurma(
            turmaId = turmaId,
            motivo = "Turma arquivada pelo aplicativo.",
            responsavel = "Sistema",
            onSuccess = onSuccess,
            onError = onError
        )
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