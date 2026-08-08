package com.example.crismapp.ui

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ReferenciaRelatorioMensal(
    val ano: Int,
    val mes: Int,
    val chave: String,
    val titulo: String
)

data class ResultadoRelatorioMensal(
    val referencia: ReferenciaRelatorioMensal,
    val nomeArquivo: String,
    val conteudo: String,
    val quantidadeAlteracoes: Int,
    val quantidadeCrismandos: Int
)

object RelatorioMensalRepository {

    private const val COLECAO_AUDITORIA = "auditoria"
    private const val COLECAO_TURMAS = "turmas"
    private const val COLECAO_USUARIOS = "usuarios"
    private const val COLECAO_FINANCEIRO = "financeiro"
    private const val COLECAO_FREQUENCIAS = "frequencias"
    private const val COLECAO_DOCUMENTOS = "documentos"
    private const val COLECAO_RELATORIOS = "relatorios_mensais"

    private const val TOTAL_PARCELAS = 12
    private const val FUSO_RECIFE = "America/Recife"

    private val localeBrasil = Locale("pt", "BR")
    private val fusoRecife = TimeZone.getTimeZone(FUSO_RECIFE)

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private data class TurmaRelatorio(
        val id: String,
        val nome: String,
        val categoria: String
    )

    private data class AlunoRelatorio(
        val id: String,
        val nome: String,
        val turmaId: String,
        val categoria: String,
        val ativo: Boolean,
        val situacao: String
    )

    private data class PagamentoRelatorio(
        val alunoId: String,
        val parcela: Int,
        val status: String
    )

    private data class FrequenciaRelatorio(
        val alunoId: String,
        val status: String
    )

    private data class DocumentoRelatorio(
        val alunoId: String,
        val perfil: String,
        val tipo: String,
        val nome: String,
        val status: String
    )

    private data class EventoRelatorio(
        val alunoId: String,
        val turmaId: String,
        val tipo: String,
        val resumo: String,
        val dataEvento: Long
    )

    fun obterReferenciaMesAnterior(): ReferenciaRelatorioMensal {
        val calendario = Calendar.getInstance(fusoRecife, localeBrasil)
        calendario.add(Calendar.MONTH, -1)
        return criarReferencia(calendario)
    }

    fun obterReferenciaMesAtual(): ReferenciaRelatorioMensal {
        return criarReferencia(
            Calendar.getInstance(fusoRecife, localeBrasil)
        )
    }

    fun moverReferencia(
        referencia: ReferenciaRelatorioMensal,
        quantidadeMeses: Int
    ): ReferenciaRelatorioMensal {
        val calendario = Calendar.getInstance(
            fusoRecife,
            localeBrasil
        ).apply {
            clear()
            set(
                referencia.ano,
                referencia.mes - 1,
                1,
                12,
                0,
                0
            )
            add(Calendar.MONTH, quantidadeMeses)
        }

        return criarReferencia(calendario)
    }

    fun verificarRelatorioGerado(
        referencia: ReferenciaRelatorioMensal,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        db.collection(COLECAO_RELATORIOS)
            .document(referencia.chave)
            .get()
            .addOnSuccessListener { documento ->
                onSuccess(
                    documento.exists() &&
                            documento.getString("status")
                                .orEmpty()
                                .equals(
                                    "SALVO",
                                    ignoreCase = true
                                )
                )
            }
            .addOnFailureListener(onError)
    }

    fun gerarRelatorio(
        referencia: ReferenciaRelatorioMensal,
        onSuccess: (ResultadoRelatorioMensal) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val tarefaAuditoria = db.collection(COLECAO_AUDITORIA)
            .whereEqualTo(
                "mesReferencia",
                referencia.chave
            )
            .get()

        val tarefaTurmas = db.collection(COLECAO_TURMAS).get()
        val tarefaUsuarios = db.collection(COLECAO_USUARIOS).get()
        val tarefaFinanceiro = db.collection(COLECAO_FINANCEIRO).get()
        val tarefaFrequencias = db.collection(COLECAO_FREQUENCIAS).get()
        val tarefaDocumentos = db.collection(COLECAO_DOCUMENTOS).get()

        Tasks.whenAll(
            listOf(
                tarefaAuditoria,
                tarefaTurmas,
                tarefaUsuarios,
                tarefaFinanceiro,
                tarefaFrequencias,
                tarefaDocumentos
            )
        ).addOnSuccessListener {
            try {
                val turmas = converterTurmas(
                    tarefaTurmas.result
                )

                val alunos = converterAlunos(
                    tarefaUsuarios.result
                )

                val pagamentos = converterPagamentos(
                    tarefaFinanceiro.result
                )

                val frequencias = converterFrequencias(
                    tarefaFrequencias.result
                )

                val documentos = converterDocumentos(
                    tarefaDocumentos.result
                )

                val eventos = converterEventos(
                    tarefaAuditoria.result
                )

                val conteudo = montarConteudo(
                    referencia = referencia,
                    turmas = turmas,
                    alunos = alunos,
                    pagamentos = pagamentos,
                    frequencias = frequencias,
                    documentos = documentos,
                    eventos = eventos
                )

                onSuccess(
                    ResultadoRelatorioMensal(
                        referencia = referencia,
                        nomeArquivo =
                        "CrismAPP-Relatorio-${referencia.chave}.txt",
                        conteudo = conteudo,
                        quantidadeAlteracoes = eventos.size,
                        quantidadeCrismandos = alunos.size
                    )
                )
            } catch (erro: Exception) {
                onError(erro)
            }
        }.addOnFailureListener(onError)
    }

    fun marcarRelatorioComoSalvo(
        resultado: ResultadoRelatorioMensal,
        tamanhoBytes: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val catequista = FirebaseAuthRepository.catequistaAtual

        if (
            catequista == null ||
            !catequista.possuiPermissaoTotal()
        ) {
            onError(
                IllegalStateException(
                    "Somente administradores podem registrar o relatório."
                )
            )
            return
        }

        val dados = mapOf(
            "referencia" to resultado.referencia.chave,
            "tituloPeriodo" to resultado.referencia.titulo,
            "nomeArquivo" to resultado.nomeArquivo,
            "quantidadeAlteracoes" to
                    resultado.quantidadeAlteracoes,
            "quantidadeCrismandos" to
                    resultado.quantidadeCrismandos,
            "tamanhoBytes" to tamanhoBytes,
            "geradoPorUid" to catequista.uid,
            "geradoPorNome" to catequista.nome,
            "status" to "SALVO",
            "dataGeracao" to FieldValue.serverTimestamp()
        )

        db.collection(COLECAO_RELATORIOS)
            .document(resultado.referencia.chave)
            .set(dados)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onError)
    }

    private fun criarReferencia(
        calendario: Calendar
    ): ReferenciaRelatorioMensal {
        val ano = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH) + 1

        val titulo = SimpleDateFormat(
            "MMMM 'de' yyyy",
            localeBrasil
        ).apply {
            timeZone = fusoRecife
        }.format(calendario.time)
            .replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(localeBrasil)
                } else {
                    it.toString()
                }
            }

        return ReferenciaRelatorioMensal(
            ano = ano,
            mes = mes,
            chave = String.format(
                Locale.US,
                "%04d-%02d",
                ano,
                mes
            ),
            titulo = titulo
        )
    }

    private fun converterTurmas(
        snapshot: QuerySnapshot
    ): List<TurmaRelatorio> {
        return snapshot.documents.map { documento ->
            TurmaRelatorio(
                id = documento.id,
                nome = documento.getString("nome")
                    .orEmpty()
                    .trim()
                    .ifBlank { documento.id },
                categoria = normalizarCategoria(
                    documento.getString("categoria").orEmpty()
                )
            )
        }
    }

    private fun converterAlunos(
        snapshot: QuerySnapshot
    ): List<AlunoRelatorio> {
        return snapshot.documents.mapNotNull { documento ->
            val nome = documento.getString("nome")
                .orEmpty()
                .trim()

            if (nome.isBlank()) {
                null
            } else {
                AlunoRelatorio(
                    id = documento.id.uppercase(Locale.ROOT),
                    nome = nome,
                    turmaId = documento.getString("turmaId")
                        .orEmpty()
                        .trim(),
                    categoria = normalizarCategoria(
                        documento.getString("categoria").orEmpty()
                    ),
                    ativo = documento.getBoolean("ativo") ?: true,
                    situacao = documento.getString("situacao")
                        .orEmpty()
                        .trim()
                        .ifBlank {
                            if (
                                documento.getBoolean("ativo") != false
                            ) {
                                "ATIVO"
                            } else {
                                "INATIVO"
                            }
                        }
                )
            }
        }
    }

    private fun converterPagamentos(
        snapshot: QuerySnapshot
    ): List<PagamentoRelatorio> {
        return snapshot.documents.mapNotNull { documento ->
            val alunoId = documento.getString("alunoId")
                .orEmpty()
                .trim()
                .uppercase(Locale.ROOT)

            val parcela = documento.getLong("parcela")
                ?.toInt()
                ?: documento.getLong("numeroParcela")
                    ?.toInt()
                ?: 0

            val statusSalvo = documento.getString("status")
                .orEmpty()
                .trim()
                .uppercase(Locale.ROOT)

            val status = if (statusSalvo.isNotBlank()) {
                statusSalvo
            } else if (
                documento.getBoolean("statusPago") == true
            ) {
                "PAGO"
            } else {
                "PENDENTE"
            }

            if (
                alunoId.isBlank() ||
                parcela <= 0
            ) {
                null
            } else {
                PagamentoRelatorio(
                    alunoId = alunoId,
                    parcela = parcela,
                    status = status
                )
            }
        }
    }

    private fun converterFrequencias(
        snapshot: QuerySnapshot
    ): List<FrequenciaRelatorio> {
        return snapshot.documents.mapNotNull { documento ->
            val alunoId = documento.getString("alunoId")
                .orEmpty()
                .trim()
                .uppercase(Locale.ROOT)

            if (alunoId.isBlank()) {
                null
            } else {
                FrequenciaRelatorio(
                    alunoId = alunoId,
                    status = documento.getString("status")
                        .orEmpty()
                        .trim()
                        .uppercase(Locale.ROOT)
                )
            }
        }
    }

    private fun converterDocumentos(
        snapshot: QuerySnapshot
    ): List<DocumentoRelatorio> {
        return snapshot.documents.mapNotNull { documento ->
            val alunoId = documento.getString("alunoId")
                .orEmpty()
                .trim()
                .uppercase(Locale.ROOT)

            if (alunoId.isBlank()) {
                null
            } else {
                DocumentoRelatorio(
                    alunoId = alunoId,
                    perfil = documento.getString("perfil")
                        .orEmpty()
                        .trim()
                        .uppercase(Locale.ROOT)
                        .ifBlank {
                            quandoPerfilPeloId(documento.id)
                        },
                    tipo = documento.getString("tipo")
                        .orEmpty()
                        .trim()
                        .uppercase(Locale.ROOT),
                    nome = documento.getString("nome")
                        .orEmpty()
                        .trim()
                        .ifBlank {
                            documento.getString("tipo")
                                .orEmpty()
                                .trim()
                        },
                    status = documento.getString("status")
                        .orEmpty()
                        .trim()
                        .uppercase(Locale.ROOT)
                        .ifBlank {
                            "NAO_INFORMADO"
                        }
                )
            }
        }
    }

    private fun converterEventos(
        snapshot: QuerySnapshot
    ): List<EventoRelatorio> {
        return snapshot.documents.map { documento ->
            EventoRelatorio(
                alunoId = documento.getString("alunoId")
                    .orEmpty()
                    .trim()
                    .uppercase(Locale.ROOT),
                turmaId = documento.getString("turmaId")
                    .orEmpty()
                    .trim(),
                tipo = documento.getString("tipo")
                    .orEmpty()
                    .trim()
                    .uppercase(Locale.ROOT),
                resumo = documento.getString("resumo")
                    .orEmpty()
                    .trim()
                    .ifBlank {
                        criarResumoAntigo(documento)
                    },
                dataEvento = obterDataEmMillis(
                    documento,
                    "dataEvento"
                )
            )
        }.sortedBy {
            it.dataEvento
        }
    }

    private fun montarConteudo(
        referencia: ReferenciaRelatorioMensal,
        turmas: List<TurmaRelatorio>,
        alunos: List<AlunoRelatorio>,
        pagamentos: List<PagamentoRelatorio>,
        frequencias: List<FrequenciaRelatorio>,
        documentos: List<DocumentoRelatorio>,
        eventos: List<EventoRelatorio>
    ): String {
        val catequista = FirebaseAuthRepository.catequistaAtual

        val turmasPorId = turmas.associateBy { it.id }
        val alunosPorId = alunos.associateBy { it.id }

        val pagamentosPorAluno = pagamentos.groupBy {
            it.alunoId
        }

        val frequenciasPorAluno = frequencias.groupBy {
            it.alunoId
        }

        val documentosPorAluno = documentos.groupBy {
            it.alunoId
        }

        val eventosPorAluno = eventos.groupBy {
            it.alunoId
        }

        val dataGeracao = SimpleDateFormat(
            "dd/MM/yyyy 'às' HH:mm:ss",
            localeBrasil
        ).apply {
            timeZone = fusoRecife
        }.format(Date())

        val totalPagamentos = pagamentos.count {
            it.status == "PAGO"
        }

        val totalFaltas = frequencias.count {
            it.status == "FALTA"
        }

        val totalJustificadas = frequencias.count {
            it.status == "JUSTIFICADA"
        }

        val totalDocumentosPendentes = documentos.count {
            it.status == "NAO_ENTREGUE"
        }

        /*
         * Separadores curtos para evitar quebras em visualizadores
         * de TXT com fonte grande ou telas mais estreitas.
         */
        val separadorTurma = "=".repeat(28)
        val separadorAluno = "-".repeat(28)

        return buildString {
            appendLine("CRISMAPP - RELATÓRIO MENSAL")
            appendLine()
            appendLine("Mês: ${referencia.titulo}")
            appendLine("Gerado em: $dataGeracao")
            appendLine(
                "Responsável: ${
                    catequista?.nome
                        .orEmpty()
                        .ifBlank { "Administrador" }
                }"
            )
            appendLine()
            appendLine(
                "Situação cadastral: momento da geração"
            )
            appendLine(
                "Histórico: alterações do mês selecionado"
            )
            appendLine()
            appendLine(separadorTurma)
            appendLine("RESUMO GERAL")
            appendLine(separadorTurma)
            appendLine("Turmas: ${turmas.size}")
            appendLine("Crismandos: ${alunos.size}")
            appendLine(
                "Ativos: ${alunos.count { it.ativo }}"
            )
            appendLine("Alterações no mês: ${eventos.size}")
            appendLine("Parcelas pagas: $totalPagamentos")
            appendLine("Faltas: $totalFaltas")
            appendLine("Justificadas: $totalJustificadas")
            appendLine(
                "Documentos pendentes: " +
                        totalDocumentosPendentes
            )
            appendLine()

            val alunosAgrupados = alunos.groupBy { aluno ->
                val turma = turmasPorId[aluno.turmaId]

                val categoria = normalizarCategoria(
                    turma?.categoria
                        .orEmpty()
                        .ifBlank {
                            aluno.categoria
                        }
                )

                categoria to aluno.turmaId
            }

            val gruposOrdenados = alunosAgrupados.entries
                .sortedWith(
                    compareBy<
                            Map.Entry<
                                    Pair<String, String>,
                                    List<AlunoRelatorio>
                                    >
                            > {
                        ordemCategoria(it.key.first)
                    }.thenBy {
                        turmasPorId[it.key.second]
                            ?.nome
                            .orEmpty()
                            .lowercase(localeBrasil)
                    }
                )

            gruposOrdenados.forEach { entrada ->
                val categoria = entrada.key.first
                val turmaId = entrada.key.second
                val turma = turmasPorId[turmaId]

                val nomeTurma = turma
                    ?.nome
                    .orEmpty()
                    .ifBlank {
                        turmaId.ifBlank {
                            "Turma não identificada"
                        }
                    }

                appendLine(separadorTurma)
                appendLine("TURMA: $nomeTurma")
                appendLine(
                    "Categoria: ${tituloCategoria(categoria)}"
                )
                appendLine(
                    "Código: ${
                        turmaId.ifBlank { "Não informado" }
                    }"
                )
                appendLine(
                    "Crismandos: ${entrada.value.size}"
                )
                appendLine(separadorTurma)

                entrada.value
                    .sortedBy {
                        it.nome.lowercase(localeBrasil)
                    }
                    .forEach { aluno ->
                        val pagamentosAluno =
                            pagamentosPorAluno[aluno.id].orEmpty()

                        val frequenciasAluno =
                            frequenciasPorAluno[aluno.id].orEmpty()

                        val documentosAluno =
                            documentosPorAluno[aluno.id].orEmpty()

                        val eventosAluno =
                            eventosPorAluno[aluno.id].orEmpty()

                        val parcelasPagas = pagamentosAluno
                            .filter { it.status == "PAGO" }
                            .map { it.parcela }
                            .filter { it in 1..TOTAL_PARCELAS }
                            .distinct()
                            .sorted()

                        val parcelasPendentes = (
                                1..TOTAL_PARCELAS
                                ).filter {
                                it !in parcelasPagas
                            }

                        val presentes = frequenciasAluno.count {
                            it.status == "PRESENTE"
                        }

                        val faltas = frequenciasAluno.count {
                            it.status == "FALTA"
                        }

                        val justificadas = frequenciasAluno.count {
                            it.status == "JUSTIFICADA"
                        }

                        appendLine()
                        appendLine(separadorAluno)
                        appendLine("CRISMANDO")
                        appendLine("Nome: ${aluno.nome}")
                        appendLine("Matrícula: ${aluno.id}")
                        appendLine(
                            "Situação: ${
                                formatarSituacaoAluno(aluno)
                            }"
                        )

                        appendLine()
                        appendLine("PAGAMENTOS")
                        appendLine(
                            "Pagas: ${parcelasPagas.size} de " +
                                    TOTAL_PARCELAS
                        )
                        appendLine(
                            "Quais: ${
                                formatarFaixasNumericas(
                                    parcelasPagas
                                )
                            }"
                        )
                        appendLine(
                            "Pendentes: ${parcelasPendentes.size} de " +
                                    TOTAL_PARCELAS
                        )
                        appendLine(
                            "Quais: ${
                                formatarFaixasNumericas(
                                    parcelasPendentes
                                )
                            }"
                        )

                        appendLine()
                        appendLine("FREQUÊNCIA")
                        appendLine("Presenças: $presentes")
                        appendLine("Faltas: $faltas")
                        appendLine("Justificadas: $justificadas")
                        appendLine(
                            "Registros: ${frequenciasAluno.size}"
                        )

                        appendLine()
                        appendLine("DOCUMENTOS")

                        if (documentosAluno.isEmpty()) {
                            appendLine(
                                "Nenhum documento cadastrado."
                            )
                        } else {
                            documentosAluno
                                .groupBy {
                                    formatarPerfilDocumento(
                                        it.perfil
                                    )
                                }
                                .toSortedMap()
                                .forEach { (perfil, itens) ->
                                    appendLine("$perfil:")

                                    itens.sortedBy {
                                        it.nome.lowercase(
                                            localeBrasil
                                        )
                                    }.forEach { documento ->
                                        val nomeDocumento =
                                            documento.nome
                                                .ifBlank {
                                                    documento.tipo
                                                }
                                                .ifBlank {
                                                    "Documento"
                                                }

                                        appendLine(
                                            "  $nomeDocumento: ${
                                                formatarStatusDocumento(
                                                    documento.status
                                                )
                                            }"
                                        )
                                    }
                                }
                        }

                        appendLine()
                        appendLine(
                            "HISTÓRICO DE ${referencia.titulo.uppercase(localeBrasil)}"
                        )

                        if (eventosAluno.isEmpty()) {
                            appendLine(
                                "Nenhuma alteração registrada."
                            )
                        } else {
                            eventosAluno.forEach { evento ->
                                appendLine(
                                    "${
                                        formatarDataHora(
                                            evento.dataEvento
                                        )
                                    }"
                                )
                                appendLine("  ${evento.resumo}")
                            }
                        }
                    }

                appendLine()
            }

            val eventosSemAluno = eventos.filter {
                it.alunoId.isBlank() ||
                        it.alunoId !in alunosPorId
            }

            if (eventosSemAluno.isNotEmpty()) {
                appendLine(separadorTurma)
                appendLine("ALTERAÇÕES SEM CADASTRO")
                appendLine(separadorTurma)

                eventosSemAluno.forEach { evento ->
                    appendLine(
                        formatarDataHora(evento.dataEvento)
                    )
                    appendLine("  ${evento.resumo}")
                }

                appendLine()
            }

            appendLine(separadorTurma)
            appendLine("FIM DO RELATÓRIO")
            appendLine(separadorTurma)
        }
    }

    /**
     * Converte sequências longas em faixas mais curtas:
     *
     * 1, 2, 3, 4, 5 -> 1-5
     * 1, 2, 4, 5, 7 -> 1-2, 4-5, 7
     */
    private fun formatarFaixasNumericas(
        valores: Collection<Int>
    ): String {
        val numeros = valores
            .distinct()
            .sorted()

        if (numeros.isEmpty()) {
            return "Nenhuma"
        }

        val partes = mutableListOf<String>()
        var inicio = numeros.first()
        var fim = inicio

        numeros.drop(1).forEach { numero ->
            if (numero == fim + 1) {
                fim = numero
            } else {
                partes += if (inicio == fim) {
                    inicio.toString()
                } else {
                    "$inicio-$fim"
                }

                inicio = numero
                fim = numero
            }
        }

        partes += if (inicio == fim) {
            inicio.toString()
        } else {
            "$inicio-$fim"
        }

        return partes.joinToString(", ")
    }

    private fun formatarSituacaoAluno(
        aluno: AlunoRelatorio
    ): String {
        val situacaoSalva = aluno.situacao
            .trim()
            .replace("_", " ")
            .lowercase(localeBrasil)
            .replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(localeBrasil)
                } else {
                    it.toString()
                }
            }

        return situacaoSalva.ifBlank {
            if (aluno.ativo) {
                "Ativo"
            } else {
                "Inativo"
            }
        }
    }

    private fun formatarPerfilDocumento(
        perfil: String
    ): String {
        return when (
            perfil.trim().uppercase(Locale.ROOT)
        ) {
            "CRISMANDO" -> "Crismando"
            "PADRINHO" -> "Padrinho"
            else -> perfil
                .trim()
                .replace("_", " ")
                .lowercase(localeBrasil)
                .replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(localeBrasil)
                    } else {
                        it.toString()
                    }
                }
                .ifBlank {
                    "Sem perfil"
                }
        }
    }

    private fun formatarStatusDocumento(
        status: String
    ): String {
        return when (
            status.trim().uppercase(Locale.ROOT)
        ) {
            "ENTREGUE" -> "Entregue"
            "NAO_ENTREGUE" -> "Não entregue"
            "PENDENTE" -> "Pendente"
            "DISPENSADO" -> "Dispensado"
            "NAO_SE_APLICA" -> "Não se aplica"
            else -> status
                .trim()
                .replace("_", " ")
                .lowercase(localeBrasil)
                .replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(localeBrasil)
                    } else {
                        it.toString()
                    }
                }
                .ifBlank {
                    "Não informado"
                }
        }
    }

    private fun formatarDataHora(
        millis: Long
    ): String {
        if (millis <= 0L) {
            return "Data não informada"
        }

        return SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            localeBrasil
        ).apply {
            timeZone = fusoRecife
        }.format(Date(millis))
    }

    private fun normalizarCategoria(
        valor: String
    ): String {
        val categoria = valor
            .trim()
            .lowercase(Locale.ROOT)

        return when {
            categoria.contains("adult") -> "adulta"
            categoria.contains("jov") -> "jovem"
            else -> categoria.ifBlank {
                "sem_categoria"
            }
        }
    }

    private fun tituloCategoria(
        categoria: String
    ): String {
        return when (normalizarCategoria(categoria)) {
            "adulta" -> "Adultos"
            "jovem" -> "Jovens"
            else -> "Sem categoria"
        }
    }

    private fun ordemCategoria(
        categoria: String
    ): Int {
        return when (normalizarCategoria(categoria)) {
            "adulta" -> 0
            "jovem" -> 1
            else -> 2
        }
    }

    private fun quandoPerfilPeloId(
        documentoId: String
    ): String {
        return when {
            documentoId.contains(
                "-PADRINHO-",
                ignoreCase = true
            ) -> {
                "PADRINHO"
            }

            documentoId.contains(
                "-CRISMANDO-",
                ignoreCase = true
            ) -> {
                "CRISMANDO"
            }

            else -> {
                ""
            }
        }
    }

    private fun criarResumoAntigo(
        documento: DocumentSnapshot
    ): String {
        val tipo = documento.getString("tipo")
            .orEmpty()
            .trim()

        val alunoId = documento.getString("alunoId")
            .orEmpty()
            .trim()
            .ifBlank {
                "crismando não identificado"
            }

        return "$tipo — $alunoId"
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
}