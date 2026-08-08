package com.example.crismapp.ui

import java.util.Locale

/**
 * Áreas do aplicativo que já possuem rastreamento de alterações.
 *
 * Nesta primeira etapa:
 * - financeiro;
 * - frequência;
 * - documentos.
 */
enum class EntidadeAuditoria {
    FINANCEIRO,
    FREQUENCIA,
    DOCUMENTOS
}

/**
 * Tipos de eventos registrados na coleção "auditoria".
 */
enum class TipoEventoAuditoria {
    PAGAMENTO_REGISTRADO,
    PAGAMENTO_REEMBOLSADO,
    PAGAMENTO_ESTORNADO,
    FREQUENCIA_REGISTRADA,
    FREQUENCIA_REMOVIDA,
    DOCUMENTOS_ATUALIZADOS
}

/**
 * Representa um documento da coleção:
 *
 * auditoria/{idAutomatico}
 *
 * Os documentos de auditoria são imutáveis.
 */
data class RegistroAuditoria(
    val id: String = "",
    val tipo: String = "",
    val entidade: String = "",
    val resumo: String = "",
    val documentoOrigemId: String = "",
    val alunoId: String = "",
    val turmaId: String = "",
    val dadosAnteriores: Map<String, Any?> = emptyMap(),
    val dadosNovos: Map<String, Any?> = emptyMap(),
    val responsavelUid: String = "",
    val responsavelNome: String = "",
    val responsavelLogin: String = "",
    val mesReferencia: String = "",
    val origem: String = "ANDROID_APP",
    val versaoEstrutura: Int = 1,
    val dataEvento: Long = 0L
) {
    fun obterTipo(): TipoEventoAuditoria? {
        return try {
            TipoEventoAuditoria.valueOf(
                tipo.trim().uppercase(Locale.ROOT)
            )
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun obterEntidade(): EntidadeAuditoria? {
        return try {
            EntidadeAuditoria.valueOf(
                entidade.trim().uppercase(Locale.ROOT)
            )
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
