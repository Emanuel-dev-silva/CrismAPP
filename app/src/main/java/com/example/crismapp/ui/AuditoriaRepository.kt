package com.example.crismapp.ui

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Centraliza a gravação e a leitura dos eventos de auditoria.
 *
 * A função adicionarAoLote não executa commit.
 * Ela adiciona o documento ao mesmo WriteBatch usado pela operação
 * principal, garantindo que os dados e o histórico sejam salvos juntos.
 */
object AuditoriaRepository {

    private const val COLECAO_AUDITORIA = "auditoria"
    private const val ORIGEM_ANDROID = "ANDROID_APP"
    private const val VERSAO_ESTRUTURA = 1
    private const val FUSO_RECIFE = "America/Recife"

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun adicionarAoLote(
        lote: WriteBatch,
        tipo: TipoEventoAuditoria,
        entidade: EntidadeAuditoria,
        documentoOrigemId: String,
        alunoId: String,
        turmaId: String,
        dadosAnteriores: Map<String, Any?> = emptyMap(),
        dadosNovos: Map<String, Any?> = emptyMap(),
        responsavelInformado: String = ""
    ): String {
        val perfil = FirebaseAuthRepository.catequistaAtual

        val uid = perfil?.uid
            ?.trim()
            .orEmpty()
            .ifBlank {
                auth.currentUser?.uid.orEmpty()
            }

        val nome = perfil?.nome
            ?.trim()
            .orEmpty()
            .ifBlank {
                responsavelInformado.trim()
            }
            .ifBlank {
                "Sistema"
            }

        val login = perfil?.login
            ?.trim()
            .orEmpty()

        val resumo = criarResumoLegivel(
            tipo = tipo,
            alunoId = alunoId,
            dadosAnteriores = dadosAnteriores,
            dadosNovos = dadosNovos,
            responsavel = nome
        )

        val referencia = db.collection(COLECAO_AUDITORIA)
            .document(
                criarIdLegivel(
                    tipo = tipo,
                    alunoId = alunoId
                )
            )

        val dadosAuditoria = hashMapOf<String, Any>(
            "tipo" to tipo.name,
            "entidade" to entidade.name,
            "resumo" to resumo,
            "documentoOrigemId" to documentoOrigemId.trim(),
            "alunoId" to alunoId.trim().uppercase(Locale.ROOT),
            "turmaId" to turmaId.trim(),
            "dadosAnteriores" to dadosAnteriores,
            "dadosNovos" to dadosNovos,
            "responsavelUid" to uid,
            "responsavelNome" to nome,
            "responsavelLogin" to login,
            "mesReferencia" to obterMesReferenciaAtual(),
            "origem" to ORIGEM_ANDROID,
            "versaoEstrutura" to VERSAO_ESTRUTURA,
            "dataEvento" to FieldValue.serverTimestamp()
        )

        lote.set(
            referencia,
            dadosAuditoria
        )

        return referencia.id
    }

    /**
     * Busca eventos usando dataEvento, que é gravada pelo servidor.
     *
     * fimExclusivoMillis não entra no período.
     */
    fun buscarEventosEntre(
        inicioMillis: Long,
        fimExclusivoMillis: Long,
        onSuccess: (List<RegistroAuditoria>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (
            inicioMillis <= 0L ||
            fimExclusivoMillis <= inicioMillis
        ) {
            onError(
                IllegalArgumentException(
                    "O período informado para a auditoria é inválido."
                )
            )
            return
        }

        db.collection(COLECAO_AUDITORIA)
            .whereGreaterThanOrEqualTo(
                "dataEvento",
                Timestamp(Date(inicioMillis))
            )
            .whereLessThan(
                "dataEvento",
                Timestamp(Date(fimExclusivoMillis))
            )
            .orderBy(
                "dataEvento",
                Query.Direction.ASCENDING
            )
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(
                    snapshot.documents.map {
                        converterDocumento(it)
                    }
                )
            }
            .addOnFailureListener(onError)
    }

    private fun converterDocumento(
        documento: DocumentSnapshot
    ): RegistroAuditoria {
        @Suppress("UNCHECKED_CAST")
        val dadosAnteriores =
            documento.get("dadosAnteriores")
                as? Map<String, Any?>
                ?: emptyMap()

        @Suppress("UNCHECKED_CAST")
        val dadosNovos =
            documento.get("dadosNovos")
                as? Map<String, Any?>
                ?: emptyMap()

        return RegistroAuditoria(
            id = documento.id,
            tipo = documento.getString("tipo").orEmpty(),
            entidade = documento.getString("entidade").orEmpty(),
            resumo = documento.getString("resumo").orEmpty(),
            documentoOrigemId = documento
                .getString("documentoOrigemId")
                .orEmpty(),
            alunoId = documento.getString("alunoId").orEmpty(),
            turmaId = documento.getString("turmaId").orEmpty(),
            dadosAnteriores = dadosAnteriores,
            dadosNovos = dadosNovos,
            responsavelUid = documento
                .getString("responsavelUid")
                .orEmpty(),
            responsavelNome = documento
                .getString("responsavelNome")
                .orEmpty(),
            responsavelLogin = documento
                .getString("responsavelLogin")
                .orEmpty(),
            mesReferencia = documento
                .getString("mesReferencia")
                .orEmpty(),
            origem = documento
                .getString("origem")
                .orEmpty(),
            versaoEstrutura = documento
                .getLong("versaoEstrutura")
                ?.toInt()
                ?: 1,
            dataEvento = obterDataEmMillis(
                documento,
                "dataEvento"
            )
        )
    }

    private fun criarIdLegivel(
        tipo: TipoEventoAuditoria,
        alunoId: String
    ): String {
        val dataHora = SimpleDateFormat(
            "yyyyMMdd-HHmmss-SSS",
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone(FUSO_RECIFE)
        }.format(Date())

        val alunoTratado = alunoId
            .trim()
            .uppercase(Locale.ROOT)
            .replace(
                Regex("[^A-Z0-9-]"),
                ""
            )
            .take(20)
            .ifBlank {
                "SEM-ALUNO"
            }

        val sufixoUnico = UUID.randomUUID()
            .toString()
            .take(6)
            .uppercase(Locale.ROOT)

        return buildString {
            append("AUD-")
            append(dataHora)
            append("-")
            append(tipo.name)
            append("-")
            append(alunoTratado)
            append("-")
            append(sufixoUnico)
        }
    }

    private fun criarResumoLegivel(
        tipo: TipoEventoAuditoria,
        alunoId: String,
        dadosAnteriores: Map<String, Any?>,
        dadosNovos: Map<String, Any?>,
        responsavel: String
    ): String {
        val aluno = alunoId
            .trim()
            .uppercase(Locale.ROOT)
            .ifBlank {
                "crismando não identificado"
            }

        fun valor(
            mapa: Map<String, Any?>,
            chave: String,
            padrao: String = "não informado"
        ): String {
            return mapa[chave]
                ?.toString()
                ?.trim()
                .orEmpty()
                .ifBlank {
                    padrao
                }
        }

        return when (tipo) {
            TipoEventoAuditoria.PAGAMENTO_REGISTRADO -> {
                val parcela = valor(
                    dadosNovos,
                    "parcela"
                )

                "Parcela $parcela de $aluno registrada como paga por $responsavel."
            }

            TipoEventoAuditoria.PAGAMENTO_REEMBOLSADO -> {
                val parcela = valor(
                    dadosNovos,
                    "parcela"
                )

                "Pagamento da parcela $parcela de $aluno reembolsado por $responsavel."
            }

            TipoEventoAuditoria.PAGAMENTO_ESTORNADO -> {
                val parcela = valor(
                    dadosNovos,
                    "parcela"
                )

                "Lançamento da parcela $parcela de $aluno estornado por $responsavel."
            }

            TipoEventoAuditoria.FREQUENCIA_REGISTRADA -> {
                val encontro = valor(
                    dadosNovos,
                    "encontro"
                )

                val statusAnterior = valor(
                    dadosAnteriores,
                    "status",
                    StatusFrequencia.NENHUM.name
                )

                val statusNovo = valor(
                    dadosNovos,
                    "status"
                )

                if (
                    statusAnterior.equals(
                        StatusFrequencia.NENHUM.name,
                        ignoreCase = true
                    )
                ) {
                    "Frequência de $aluno registrada como $statusNovo no encontro $encontro."
                } else {
                    "Frequência de $aluno no encontro $encontro alterada de $statusAnterior para $statusNovo."
                }
            }

            TipoEventoAuditoria.FREQUENCIA_REMOVIDA -> {
                val encontro = valor(
                    dadosNovos,
                    "encontro"
                )

                val statusAnterior = valor(
                    dadosAnteriores,
                    "status"
                )

                "Frequência de $aluno removida do encontro $encontro. Status anterior: $statusAnterior."
            }

            TipoEventoAuditoria.DOCUMENTOS_ATUALIZADOS -> {
                val perfil = valor(
                    dadosNovos,
                    "perfil"
                )

                "Documentação do perfil $perfil de $aluno atualizada por $responsavel."
            }
        }
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

    private fun obterMesReferenciaAtual(): String {
        return SimpleDateFormat(
            "yyyy-MM",
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone(FUSO_RECIFE)
        }.format(Date())
    }
}
