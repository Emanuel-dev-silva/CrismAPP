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
 * Valores usados na coleção "frequencias" do Firebase.
 *
 * O texto salvo no Firebase deverá ser:
 * PRESENTE
 * FALTA
 * JUSTIFICADA
 * NENHUM
 */
enum class StatusFrequencia {
    PRESENTE,
    FALTA,
    JUSTIFICADA,
    NENHUM
}

/**
 * Valores que serão usados posteriormente na coleção
 * "documentos".
 */
enum class StatusDocumento {
    ENTREGUE,
    NAO_ENTREGUE,
    NAO_POSSUI
}

/**
 * Valores usados para identificar o estado das parcelas.
 */
enum class StatusPagamento {
    PAGO,
    PENDENTE
}


// ==========================================================
// 2. MODELOS PRINCIPAIS DO FIREBASE
// ==========================================================

/**
 * Representa um documento da coleção "turmas".
 *
 * Exemplo no Firebase:
 *
 * turmas/{turmaId}
 *     nome: "Turma São José"
 *     categoria: "jovem"
 *     dataCriacao: 123456789
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
 * Atualmente a matrícula também é usada como ID do documento.
 * Por isso, durante a transição, o campo id e o campo matricula
 * poderão possuir o mesmo valor.
 *
 * Exemplo:
 *
 * usuarios/CX-1234
 *     nome: "Emanuel"
 *     matricula: "CX-1234"
 *     turmaId: "abc123"
 *     categoria: "jovem"
 */
data class Crismando(
    val id: String = "",
    val nome: String = "",
    val turmaId: String = "",
    val matricula: String = "",
    val categoria: String = "",
    val ativo: Boolean = true,
    val dataCriacao: Long = 0L
) {

    /**
     * Compatibilidade com alunos antigos que ainda não possuem
     * o campo "matricula" salvo dentro do documento.
     *
     * Nesses casos, o próprio ID do documento será usado.
     */
    fun obterMatricula(): String {
        return matricula.ifBlank { id }
    }
}

/**
 * Representa um documento da coleção "avisos".
 *
 * O turmaId deverá conter o ID real da turma.
 * Não usaremos futuramente valores genéricos como:
 *
 * "turma_jovem"
 * "turma_adulta"
 */
data class Aviso(
    val id: String = "",
    val texto: String = "",
    val tipo: String = "gerais",
    val turmaId: String = "",
    val dataCriacao: Long = 0L
)

/**
 * Representa um documento da coleção "encontros".
 */
data class RegistroEncontro(
    val id: String = "",
    val numero: Int = 0,
    val dataManual: String = "",
    val turmaId: String = "",
    val dataCriacao: Long = 0L
)

/**
 * Representa um documento da coleção "frequencias".
 *
 * Exemplo:
 *
 * frequencias/{registroId}
 *     alunoId: "CX-1234"
 *     turmaId: "abc123"
 *     encontro: 1
 *     status: "PRESENTE"
 */
data class RegistroFrequencia(
    val id: String = "",
    val alunoId: String = "",
    val turmaId: String = "",
    val encontro: Int = 0,
    val status: String = StatusFrequencia.NENHUM.name,
    val dataRegistro: Long = 0L
) {

    /**
     * Converte com segurança o texto salvo no Firebase
     * para o enum StatusFrequencia.
     */
    fun obterStatus(): StatusFrequencia {
        return try {
            StatusFrequencia.valueOf(status.uppercase())
        } catch (erro: IllegalArgumentException) {
            StatusFrequencia.NENHUM
        }
    }
}

/**
 * Representa um documento da coleção "financeiro".
 *
 * O projeto atual possui documentos usando nomes diferentes:
 *
 * numeroParcela ou parcela
 * recebidoPor ou catequista
 * status ou statusPago
 *
 * Este modelo aceita os dois formatos para não perder
 * compatibilidade com os registros que já estão no Firebase.
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
    val dataLancamento: Long = 0L
) {

    /**
     * Descobre o número da parcela independentemente do
     * nome usado no documento antigo.
     */
    fun obterNumeroParcela(): Int {
        return if (numeroParcela > 0) {
            numeroParcela
        } else {
            parcela
        }
    }

    /**
     * Aceita tanto:
     *
     * status = "PAGO"
     *
     * quanto:
     *
     * statusPago = true
     */
    fun estaPago(): Boolean {
        return statusPago || status.equals(
            other = StatusPagamento.PAGO.name,
            ignoreCase = true
        )
    }

    /**
     * Aceita os campos antigos "catequista" e "recebidoPor".
     */
    fun obterResponsavelPagamento(): String {
        return recebidoPor.ifBlank { catequista }
    }

    /**
     * Aceita as datas dos dois formatos já utilizados.
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
 * Representará um documento da coleção "documentos".
 *
 * Essa parte ainda será implementada nas telas do catequista.
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
            StatusDocumento.valueOf(status.uppercase())
        } catch (erro: IllegalArgumentException) {
            StatusDocumento.NAO_ENTREGUE
        }
    }
}


// ==========================================================
// 3. COMPONENTES VISUAIS EXISTENTES
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