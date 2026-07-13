package com.example.crismapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================================
// 1. ENUMS GLOBAIS
// ==========================================================

/**
 * Situação registrada em cada frequência.
 */
enum class StatusFrequencia {
    PRESENTE,
    FALTA,
    JUSTIFICADA,
    NENHUM
}

/**
 * Situação dos documentos do crismando.
 */
enum class StatusDocumento {
    ENTREGUE,
    NAO_ENTREGUE,
    NAO_POSSUI
}

/**
 * Situação financeira de uma parcela.
 *
 * PAGO:
 * O pagamento foi recebido normalmente.
 *
 * PENDENTE:
 * A parcela ainda não foi paga.
 *
 * REEMBOLSADO:
 * O pagamento foi recebido, mas o dinheiro foi devolvido.
 *
 * ESTORNADO:
 * O lançamento foi feito por engano e foi cancelado.
 */
enum class StatusPagamento {
    PAGO,
    PENDENTE,
    REEMBOLSADO,
    ESTORNADO
}

/**
 * Situação atual do crismando.
 *
 * ATIVO:
 * Participa normalmente da turma.
 *
 * DESISTENTE:
 * Deixou a preparação, mas o histórico será preservado.
 *
 * TRANSFERIDO:
 * Foi transferido para outra turma ou comunidade.
 *
 * INATIVO:
 * Cadastro desativado por outro motivo.
 */
enum class SituacaoCrismando {
    ATIVO,
    DESISTENTE,
    TRANSFERIDO,
    INATIVO
}

/**
 * Tipos de movimentação que poderão ser registrados
 * na coleção "movimentacoes".
 */
enum class TipoMovimentacaoCrismando {
    ARQUIVAMENTO,
    DESISTENCIA,
    TRANSFERENCIA,
    REATIVACAO,
    ALTERACAO_CADASTRAL,
    REEMBOLSO,
    ESTORNO
}


// ==========================================================
// 2. MODELOS PRINCIPAIS DO FIREBASE
// ==========================================================

/**
 * Representa um documento da coleção "turmas".
 *
 * Exemplo:
 *
 * turmas/JOV-2026-MATRIZ
 *     codigo: "JOV-2026-MATRIZ"
 *     nome: "Matriz"
 *     categoria: "jovem"
 *     ativa: true
 */
data class Turma(
    val id: String = "",
    val nome: String = "",
    val categoria: String = "",
    val dataCriacao: Long = 0L
)

/**
 * Representa um documento da coleção "usuarios".
 *
 * A matrícula é usada como ID do documento:
 *
 * usuarios/CX-1234
 */
data class Crismando(
    val id: String = "",
    val nome: String = "",
    val turmaId: String = "",
    val matricula: String = "",
    val categoria: String = "",
    val ativo: Boolean = true,
    val dataCriacao: Long = 0L,

    // Novos campos de situação.
    val situacao: String = SituacaoCrismando.ATIVO.name,
    val motivoSituacao: String = "",
    val dataSituacao: Long = 0L,
    val atualizadoPor: String = ""
) {

    /**
     * Compatibilidade com cadastros antigos que não possuem
     * o campo matrícula dentro do documento.
     */
    fun obterMatricula(): String {
        return matricula.ifBlank { id }
    }

    /**
     * Converte o texto salvo no Firebase para o enum.
     */
    fun obterSituacao(): SituacaoCrismando {
        return try {
            SituacaoCrismando.valueOf(
                situacao.uppercase()
            )
        } catch (erro: IllegalArgumentException) {
            if (ativo) {
                SituacaoCrismando.ATIVO
            } else {
                SituacaoCrismando.INATIVO
            }
        }
    }

    /**
     * Confirma se o crismando deve aparecer normalmente
     * nas listas de alunos ativos.
     */
    fun estaAtivo(): Boolean {
        return ativo &&
                obterSituacao() == SituacaoCrismando.ATIVO
    }
}

/**
 * Representa um aviso.
 */
data class Aviso(
    val id: String = "",
    val texto: String = "",
    val tipo: String = "gerais",
    val turmaId: String = "",
    val dataCriacao: Long = 0L
)

/**
 * Representa um encontro da catequese.
 */
data class RegistroEncontro(
    val id: String = "",
    val numero: Int = 0,
    val dataManual: String = "",
    val turmaId: String = "",
    val dataCriacao: Long = 0L
)

/**
 * Representa a frequência de um crismando.
 */
data class RegistroFrequencia(
    val id: String = "",
    val alunoId: String = "",
    val turmaId: String = "",
    val encontro: Int = 0,
    val status: String = StatusFrequencia.NENHUM.name,
    val dataRegistro: Long = 0L
) {

    fun obterStatus(): StatusFrequencia {
        return try {
            StatusFrequencia.valueOf(
                status.uppercase()
            )
        } catch (erro: IllegalArgumentException) {
            StatusFrequencia.NENHUM
        }
    }
}

/**
 * Representa um registro financeiro.
 *
 * O modelo continua aceitando campos antigos:
 *
 * numeroParcela / parcela
 * recebidoPor / catequista
 * status / statusPago
 * dataPagamento / dataLancamento
 */
data class RegistroFinanceiro(
    val id: String = "",
    val alunoId: String = "",
    val turmaId: String = "",

    val numeroParcela: Int = 0,
    val parcela: Int = 0,

    val status: String = StatusPagamento.PENDENTE.name,
    val statusPago: Boolean = false,

    val recebidoPor: String = "",
    val catequista: String = "",

    val dataPagamento: Long = 0L,
    val dataLancamento: Long = 0L,

    // Informações de reembolso.
    val dataReembolso: Long = 0L,
    val reembolsadoPor: String = "",
    val motivoReembolso: String = "",

    // Informações de estorno.
    val dataEstorno: Long = 0L,
    val estornadoPor: String = "",
    val motivoEstorno: String = ""
) {

    /**
     * Descobre o número da parcela nos formatos novo e antigo.
     */
    fun obterNumeroParcela(): Int {
        return if (numeroParcela > 0) {
            numeroParcela
        } else {
            parcela
        }
    }

    /**
     * Converte o texto do Firebase para StatusPagamento.
     */
    fun obterStatus(): StatusPagamento {
        return try {
            StatusPagamento.valueOf(
                status.uppercase()
            )
        } catch (erro: IllegalArgumentException) {
            if (statusPago) {
                StatusPagamento.PAGO
            } else {
                StatusPagamento.PENDENTE
            }
        }
    }

    /**
     * Retorna true somente quando a parcela está
     * atualmente considerada paga.
     *
     * Parcelas reembolsadas e estornadas não são contadas
     * como pagas no carnê atual.
     */
    fun estaPago(): Boolean {
        return when (obterStatus()) {
            StatusPagamento.PAGO -> true

            StatusPagamento.PENDENTE -> {
                // Compatibilidade com registros antigos.
                statusPago
            }

            StatusPagamento.REEMBOLSADO,
            StatusPagamento.ESTORNADO -> false
        }
    }

    /**
     * Informa se o dinheiro chegou a ser recebido em algum
     * momento, mesmo que posteriormente tenha sido devolvido.
     */
    fun foiRecebido(): Boolean {
        return when (obterStatus()) {
            StatusPagamento.PAGO,
            StatusPagamento.REEMBOLSADO -> true

            StatusPagamento.PENDENTE,
            StatusPagamento.ESTORNADO -> false
        }
    }

    fun estaReembolsado(): Boolean {
        return obterStatus() == StatusPagamento.REEMBOLSADO
    }

    fun estaEstornado(): Boolean {
        return obterStatus() == StatusPagamento.ESTORNADO
    }

    /**
     * Compatibilidade entre os campos recebidoPor e catequista.
     */
    fun obterResponsavelPagamento(): String {
        return recebidoPor.ifBlank { catequista }
    }

    /**
     * Compatibilidade entre as datas nova e antiga.
     */
    fun obterDataPagamento(): Long {
        return if (dataPagamento > 0L) {
            dataPagamento
        } else {
            dataLancamento
        }
    }
}

/**
 * Representa um documento exigido do crismando.
 */
data class RegistroDocumento(
    val id: String = "",
    val alunoId: String = "",
    val turmaId: String = "",
    val nome: String = "",
    val tipo: String = "",
    val status: String = StatusDocumento.NAO_ENTREGUE.name,
    val dataAtualizacao: Long = 0L
) {

    fun obterStatus(): StatusDocumento {
        return try {
            StatusDocumento.valueOf(
                status.uppercase()
            )
        } catch (erro: IllegalArgumentException) {
            StatusDocumento.NAO_ENTREGUE
        }
    }
}


// ==========================================================
// 3. HISTÓRICO E MOVIMENTAÇÕES
// ==========================================================

/**
 * Representa um resumo arquivado do crismando.
 *
 * Coleção:
 *
 * historico_alunos/{matricula}
 *
 * Esse documento não será apagado pelo aplicativo.
 * Ele servirá para consultas administrativas.
 */
data class HistoricoCrismando(
    val id: String = "",
    val matricula: String = "",
    val nome: String = "",

    val situacao: String = SituacaoCrismando.INATIVO.name,
    val motivo: String = "",

    val turmaAnteriorId: String = "",
    val turmaAnteriorNome: String = "",
    val categoria: String = "",

    val dataArquivamento: Long = 0L,
    val arquivadoPor: String = "",

    val totalPresencas: Int = 0,
    val totalFaltas: Int = 0,
    val totalJustificadas: Int = 0,

    val parcelasPagas: List<Int> = emptyList(),
    val parcelasReembolsadas: List<Int> = emptyList(),
    val parcelasEstornadas: List<Int> = emptyList()
) {

    fun obterSituacao(): SituacaoCrismando {
        return try {
            SituacaoCrismando.valueOf(
                situacao.uppercase()
            )
        } catch (erro: IllegalArgumentException) {
            SituacaoCrismando.INATIVO
        }
    }
}

/**
 * Representa uma movimentação do crismando.
 *
 * Exemplos:
 *
 * transferência;
 * desistência;
 * reativação;
 * reembolso;
 * estorno.
 *
 * Coleção:
 *
 * movimentacoes/{idAutomatico}
 */
data class MovimentacaoCrismando(
    val id: String = "",
    val alunoId: String = "",
    val nomeAluno: String = "",

    val tipo: String = TipoMovimentacaoCrismando.ALTERACAO_CADASTRAL.name,

    val turmaOrigemId: String = "",
    val turmaOrigemNome: String = "",

    val turmaDestinoId: String = "",
    val turmaDestinoNome: String = "",

    val motivo: String = "",
    val responsavel: String = "",
    val dataMovimentacao: Long = 0L
) {

    fun obterTipo(): TipoMovimentacaoCrismando {
        return try {
            TipoMovimentacaoCrismando.valueOf(
                tipo.uppercase()
            )
        } catch (erro: IllegalArgumentException) {
            TipoMovimentacaoCrismando.ALTERACAO_CADASTRAL
        }
    }
}


// ==========================================================
// 4. COMPONENTES VISUAIS EXISTENTES
// Nenhuma alteração visual foi realizada.
// ==========================================================

@Composable
fun UserIconWithLabel(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Componente dos botões de grade das telas.
 *
 * O código visual original foi mantido.
 */
@Composable
fun SmallMenuCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() }
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFF0F0F0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFFFF0000),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}