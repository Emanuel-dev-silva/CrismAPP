package com.example.crismapp.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.example.crismapp.R
import kotlinx.coroutines.delay

// Classes de dados do escopo da tela adulta
data class EncontroCatequeseAdulto(val id: String, val numero: Int, val dataManual: String, val turmaId: String)

data class ParcelaFinanceiraAdulta(
    val id: String = "",
    val numeroParcela: Int = 0,
    val alunoId: String = "",
    val valor: Double = 0.0,
    val statusPago: Boolean = false,
    val recebidoPor: String = "",
    val dataPagamento: Long = 0L
)

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)
private val Aviso_Blue = Color(0xFF1976D2)

private enum class DestinoAvisoAdulto {
    GERAL,
    CATEGORIA,
    TURMA
}

class MascaraDataTransformationAdulta : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val out = StringBuilder()

        for (i in originalText.indices) {
            out.append(originalText[i])
            if (i == 1 || i == 3) {
                out.append("/")
            }
        }

        val dataMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 4) return offset + 1
                return offset + 2
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return offset - 2
            }
        }

        return TransformedText(AnnotatedString(out.toString()), dataMapping)
    }
}

fun validarDigitosDataAdulta(puros: String): Boolean {
    if (puros.length >= 2) {
        val dia = puros.substring(0, 2).toIntOrNull() ?: 0
        if (dia < 1 || dia > 31) return false
    }
    if (puros.length >= 4) {
        val mes = puros.substring(2, 4).toIntOrNull() ?: 0
        if (mes < 1 || mes > 12) return false
    }
    return true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurmaAdultaScreen(navController: NavController) {
    val view = LocalView.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val catequistaLogado = FirebaseAuthRepository.catequistaAtual
    val possuiPermissaoTotal =
        catequistaLogado?.possuiPermissaoTotal() == true

    val nomeCatequistaLogado = catequistaLogado
        ?.nome
        .orEmpty()
        .ifBlank { "Catequista" }

    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }
    var showDadosPopup by remember { mutableStateOf(false) }
    var showAvisosPopup by remember { mutableStateOf(false) }
    var showFinanceiroPopup by remember { mutableStateOf(false) }
    var showFrequenciaPopup by remember { mutableStateOf(false) }
    var showTurmasPopup by remember { mutableStateOf(false) }
    var showDocumentosDialog by remember { mutableStateOf(false) }

    var crismandoDocumentosSelecionado by remember { mutableStateOf<Crismando?>(null) }
    var abaDocumentosSelecionada by remember { mutableStateOf(PerfilDocumentacao.CRISMANDO) }
    var cadastroDocumentosCrismando by remember { mutableStateOf(CadastroDocumentacao()) }
    var cadastroDocumentosPadrinho by remember {
        mutableStateOf(CadastroDocumentacao(perfil = PerfilDocumentacao.PADRINHO.name))
    }
    var carregandoDocumentosCadastro by remember { mutableStateOf(false) }
    var responsavelDocumentosInput by remember { mutableStateOf("") }

    var crismandoParaArquivar by remember { mutableStateOf<Crismando?>(null) }
    var motivoArquivamentoInput by remember { mutableStateOf("") }
    var responsavelArquivamentoInput by remember { mutableStateOf("") }

    var idTurmaSelecionada by remember { mutableStateOf<String?>(null) }
    var nomeTurmaSelecionada by remember { mutableStateOf<String?>(null) }
    var encontroSelecionado by remember { mutableStateOf<Int?>(null) }
    var crismandoSelecionado by remember { mutableStateOf<String?>(null) }
    var nomeCrismandoSelecionadoFixo by remember { mutableStateOf("") }

    var modoCriarTurma by remember { mutableStateOf(false) }
    var novoNomeTurma by remember { mutableStateOf("") }
    var novoNomeCrismando by remember { mutableStateOf("") }

    var novaDataEncontroInput by remember { mutableStateOf("") }

    var idEncontroEmEdicao by remember { mutableStateOf<String?>(null) }
    var dataEncontroEdicaoInput by remember { mutableStateOf("") }

    // Estados para controle do Alerta de Exclusão de Encontro com Delay
    var idEncontroParaExcluir by remember { mutableStateOf<String?>(null) }
    var numeroEncontroParaExcluir by remember { mutableStateOf(0) }
    var liberarBotoesConfirmacaoExcluir by remember { mutableStateOf(false) }

    // Estados para controle do Alerta de Exclusão de Turma com Delay
    var idTurmaParaExcluir by remember { mutableStateOf<String?>(null) }
    var nomeTurmaParaExcluir by remember { mutableStateOf("") }
    var liberarBotoesConfirmacaoExcluirTurma by remember { mutableStateOf(false) }

    // Estados para controle do Alerta de Exclusão de Aviso com Delay
    var idAvisoParaExcluir by remember { mutableStateOf<String?>(null) }
    var textoAvisoParaExcluir by remember { mutableStateOf("") }
    var liberarBotoesConfirmacaoExcluirAviso by remember { mutableStateOf(false) }

    // Estados para o Gerenciamento de Fluxo Financeiro Seguro
    var parcelaSelecionadaFinanceira by remember { mutableStateOf<Int?>(null) }
    var catequistaResponsavelInput by remember { mutableStateOf("") }
    var showAlertaFinanceiroEtapa1 by remember { mutableStateOf(false) }
    var showAlertaFinanceiroEtapa2 by remember { mutableStateOf(false) }
    var liberarBotaoFinanceiroEtapa1 by remember { mutableStateOf(false) }
    var liberarBotaoFinanceiroEtapa2 by remember { mutableStateOf(false) }
    var showPagamentoInfoDialog by remember { mutableStateOf(false) }
    var nomeCatequistaPagamento by remember { mutableStateOf("") }

    var modoEdicaoFrequencia by remember { mutableStateOf(false) }
    var exibirPorcentagemFalta by remember { mutableStateOf(false) }

    val frequenciaPorEncontro = remember { mutableStateMapOf<String, StatusFrequencia>() }

    var novoAvisoTexto by remember { mutableStateOf("") }
    var destinoAvisoSelecionado by remember { mutableStateOf<DestinoAvisoAdulto?>(null) }
    var listaAvisosAtivos by remember { mutableStateOf(listOf<Aviso>()) }
    var listaTurmasFirestore by remember { mutableStateOf(listOf<Turma>()) }
    var listaCrismandosFirestore by remember { mutableStateOf(listOf<Crismando>()) }

    var listaParcelasFinanceiras by remember { mutableStateOf<List<ParcelaFinanceiraAdulta>>(emptyList()) }

    var listaEncontrosFirestore by remember { mutableStateOf(listOf<EncontroCatequeseAdulto>()) }
    var listaFrequenciasFirestore by remember { mutableStateOf<List<RegistroFrequencia>>(emptyList()) }
    var todasFrequenciasGeraisByTurma by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    var animarImagem by remember { mutableStateOf(false) }
    var animarTextos by remember { mutableStateOf(false) }
    var animarBotoesAcao by remember { mutableStateOf(false) }

    val visualTransformationData = remember { MascaraDataTransformationAdulta() }

    LaunchedEffect(possuiPermissaoTotal) {
        if (!possuiPermissaoTotal) {
            showTurmasPopup = false
            showDocumentosDialog = false
            crismandoDocumentosSelecionado = null
            crismandoParaArquivar = null
            idTurmaParaExcluir = null
            nomeTurmaParaExcluir = ""

            /*
             * Catequista comum nunca pode permanecer em um destino
             * geral ou de categoria, mesmo que a tela tenha ficado
             * salva no back stack após outro login.
             */
            destinoAvisoSelecionado = DestinoAvisoAdulto.TURMA
        }
    }

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window
        window.statusBarColor = Crisma_Primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        delay(100); animarImagem = true
        delay(200); animarTextos = true
        delay(300); animarBotoesAcao = true
    }

    LaunchedEffect(idEncontroParaExcluir) {
        if (idEncontroParaExcluir != null) {
            liberarBotoesConfirmacaoExcluir = false
            delay(2000)
            liberarBotoesConfirmacaoExcluir = true
        }
    }

    LaunchedEffect(idTurmaParaExcluir) {
        if (idTurmaParaExcluir != null && possuiPermissaoTotal) {
            liberarBotoesConfirmacaoExcluirTurma = false
            delay(2000)
            liberarBotoesConfirmacaoExcluirTurma = true
        }
    }

    LaunchedEffect(idAvisoParaExcluir) {
        if (idAvisoParaExcluir != null) {
            liberarBotoesConfirmacaoExcluirAviso = false
            delay(2000)
            liberarBotoesConfirmacaoExcluirAviso = true
        }
    }

    LaunchedEffect(showAlertaFinanceiroEtapa1) {
        if (showAlertaFinanceiroEtapa1) {
            liberarBotaoFinanceiroEtapa1 = false
            delay(2000)
            liberarBotaoFinanceiroEtapa1 = true
        }
    }

    LaunchedEffect(showAlertaFinanceiroEtapa2) {
        if (showAlertaFinanceiroEtapa2) {
            liberarBotaoFinanceiroEtapa2 = false
            delay(2000)
            liberarBotaoFinanceiroEtapa2 = true
        }
    }

    // 1. Ouvinte reativo das turmas jovens
    DisposableEffect(Unit) {
        val listener = FirebaseRepository.ouvirTurmas(
            categoria = "adulta",
            onUpdate = { turmas ->
                listaTurmasFirestore = turmas
            },
            onError = {
                Toast.makeText(
                    context,
                    "Não foi possível carregar as turmas.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        onDispose {
            listener.remove()
        }
    }

    // 2. Ouvinte reativo para os crismandos da turma selecionada
    DisposableEffect(idTurmaSelecionada) {
        val turmaId = idTurmaSelecionada

        if (turmaId == null) {
            listaCrismandosFirestore = emptyList()
            onDispose { }
        } else {
            val listener = FirebaseRepository.ouvirCrismandosDaTurma(
                turmaId = turmaId,
                onUpdate = { crismandos ->
                    listaCrismandosFirestore = crismandos
                },
                onError = {
                    Toast.makeText(
                        context,
                        "Não foi possível carregar os crismandos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            onDispose {
                listener.remove()
            }
        }
    }

    // 2.B Ouvinte reativo dos encontros da turma selecionada
    DisposableEffect(idTurmaSelecionada) {
        val turmaId = idTurmaSelecionada

        if (turmaId == null) {
            listaEncontrosFirestore = emptyList()
            onDispose { }
        } else {
            val listener = FirebaseRepository.ouvirEncontrosDaTurma(
                turmaId = turmaId,
                onUpdate = { encontros ->
                    listaEncontrosFirestore = encontros.map { encontro ->
                        EncontroCatequeseAdulto(
                            id = encontro.id,
                            numero = encontro.numero,
                            dataManual = encontro.dataManual,
                            turmaId = encontro.turmaId
                        )
                    }
                },
                onError = {
                    Toast.makeText(
                        context,
                        "Não foi possível carregar os encontros.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            onDispose {
                listener.remove()
            }
        }
    }

    // 2.C Ouvinte reativo de todas as frequências da turma
    DisposableEffect(idTurmaSelecionada) {
        val turmaId = idTurmaSelecionada

        if (turmaId == null) {
            listaFrequenciasFirestore = emptyList()
            todasFrequenciasGeraisByTurma = emptyList()
            onDispose { }
        } else {
            val listener = FirebaseRepository.ouvirFrequenciasDaTurma(
                turmaId = turmaId,
                onUpdate = { frequencias ->
                    listaFrequenciasFirestore = frequencias

                    // Mantém o formato que a área de estatísticas já utiliza,
                    // sem alterar o visual nem os cálculos existentes.
                    todasFrequenciasGeraisByTurma = frequencias.map { frequencia ->
                        mapOf(
                            "alunoId" to frequencia.alunoId,
                            "turmaId" to frequencia.turmaId,
                            "encontro" to frequencia.encontro,
                            "status" to frequencia.status
                        )
                    }
                },
                onError = {
                    Toast.makeText(
                        context,
                        "Não foi possível carregar as frequências.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            onDispose {
                listener.remove()
            }
        }
    }

    // Ouvinte reativo financeiro do crismando selecionado
    DisposableEffect(crismandoSelecionado) {
        val alunoId = crismandoSelecionado

        if (alunoId == null) {
            listaParcelasFinanceiras = emptyList()
            onDispose { }
        } else {
            val listener = FirebaseRepository.ouvirFinanceiroDoAluno(
                alunoId = alunoId,
                onUpdate = { pagamentos ->
                    listaParcelasFinanceiras = pagamentos.map { pagamento ->
                        ParcelaFinanceiraAdulta(
                            id = pagamento.id,
                            numeroParcela = pagamento.obterNumeroParcela(),
                            alunoId = pagamento.alunoId,
                            statusPago = pagamento.estaPago(),
                            recebidoPor = pagamento.obterResponsavelPagamento(),
                            dataPagamento = pagamento.obterDataPagamento()
                        )
                    }
                },
                onError = {
                    Toast.makeText(
                        context,
                        "Não foi possível carregar os pagamentos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            onDispose {
                listener.remove()
            }
        }
    }

    LaunchedEffect(crismandoDocumentosSelecionado?.id) {
        val crismando = crismandoDocumentosSelecionado
        if (crismando == null) {
            cadastroDocumentosCrismando = CadastroDocumentacao()
            cadastroDocumentosPadrinho = CadastroDocumentacao(perfil = PerfilDocumentacao.PADRINHO.name)
            carregandoDocumentosCadastro = false
            return@LaunchedEffect
        }

        carregandoDocumentosCadastro = true
        var concluidos = 0
        fun concluir() {
            concluidos += 1
            if (concluidos >= 2) carregandoDocumentosCadastro = false
        }

        FirebaseRepository.carregarCadastroDocumentacao(
            alunoId = crismando.id,
            perfil = PerfilDocumentacao.CRISMANDO,
            onSuccess = { cadastro ->
                cadastroDocumentosCrismando = cadastro.copy(
                    alunoId = crismando.id,
                    turmaId = cadastro.turmaId.ifBlank { crismando.turmaId },
                    perfil = PerfilDocumentacao.CRISMANDO.name
                )
                concluir()
            },
            onError = { erro ->
                Toast.makeText(context, erro.message ?: "Erro ao carregar documentos do crismando.", Toast.LENGTH_SHORT).show()
                concluir()
            }
        )

        FirebaseRepository.carregarCadastroDocumentacao(
            alunoId = crismando.id,
            perfil = PerfilDocumentacao.PADRINHO,
            onSuccess = { cadastro ->
                cadastroDocumentosPadrinho = cadastro.copy(
                    alunoId = crismando.id,
                    turmaId = cadastro.turmaId.ifBlank { crismando.turmaId },
                    perfil = PerfilDocumentacao.PADRINHO.name
                )
                concluir()
            },
            onError = { erro ->
                Toast.makeText(context, erro.message ?: "Erro ao carregar documentos do padrinho.", Toast.LENGTH_SHORT).show()
                concluir()
            }
        )
    }

    // 3. Ouvinte reativo dos avisos do destino selecionado
    DisposableEffect(
        destinoAvisoSelecionado,
        idTurmaSelecionada,
        possuiPermissaoTotal
    ) {
        val destinoId = when {
            !possuiPermissaoTotal &&
                destinoAvisoSelecionado != DestinoAvisoAdulto.TURMA -> null

            else -> when (destinoAvisoSelecionado) {
                DestinoAvisoAdulto.GERAL -> "GERAL"
                DestinoAvisoAdulto.CATEGORIA -> "CATEGORIA_ADULTA"
                DestinoAvisoAdulto.TURMA -> idTurmaSelecionada
                null -> null
            }
        }

        if (destinoId.isNullOrBlank()) {
            listaAvisosAtivos = emptyList()
            onDispose { }
        } else {
            val listener = FirebaseRepository.ouvirAvisosPorDestino(
                destinoId = destinoId,
                onUpdate = { avisos ->
                    listaAvisosAtivos = avisos
                },
                onError = {
                    Toast.makeText(
                        context,
                        "Não foi possível carregar os avisos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            onDispose {
                listener.remove()
            }
        }
    }

    // 4. Reflete no mapa local as frequências do encontro selecionado
    LaunchedEffect(
        listaFrequenciasFirestore,
        listaCrismandosFirestore,
        idTurmaSelecionada,
        encontroSelecionado
    ) {
        val turmaId = idTurmaSelecionada
        val encontro = encontroSelecionado

        if (turmaId == null || encontro == null) {
            return@LaunchedEffect
        }

        listaCrismandosFirestore.forEach { crismando ->
            val chave = "Jov_${turmaId}_${encontro}_${crismando.id}"

            val registro = listaFrequenciasFirestore.firstOrNull { frequencia ->
                frequencia.alunoId == crismando.id &&
                        frequencia.encontro == encontro
            }

            frequenciaPorEncontro[chave] =
                registro?.obterStatus() ?: StatusFrequencia.NENHUM
        }
    }

    // Lógica do Switch de preenchimento automático
    LaunchedEffect(modoEdicaoFrequencia, idTurmaSelecionada, encontroSelecionado) {
        if (idTurmaSelecionada == null || encontroSelecionado == null) return@LaunchedEffect

        listaCrismandosFirestore.forEach { crismando ->
            val chave = "Jov_${idTurmaSelecionada}_${encontroSelecionado}_${crismando.id}"
            if (modoEdicaoFrequencia) {
                if (frequenciaPorEncontro[chave] == null || frequenciaPorEncontro[chave] == StatusFrequencia.NENHUM) {
                    frequenciaPorEncontro[chave] = StatusFrequencia.PRESENTE
                }
            } else {
                if (frequenciaPorEncontro[chave] == StatusFrequencia.PRESENTE) {
                    frequenciaPorEncontro[chave] = StatusFrequencia.NENHUM
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(0.65f).background(Crisma_Primary).padding(horizontal = 16.dp, vertical = 24.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        UserIconWithLabelAdulta(Icons.Outlined.Info, "Sobre o App") { showSobreNosDialog = true }
                        UserIconWithLabelAdulta(Icons.Outlined.Phone, "Contatos") { showContatosDialog = true }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (animarImagem) {
                        Image(painter = painterResource(id = R.drawable.imagem_crisma), contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp))
                    }

                    if (animarTextos) {
                        Column {
                            Text("\nGestão: Turma Adulta", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = Crisma_Gold, thickness = 2.dp, modifier = Modifier.fillMaxWidth(0.76f).padding(vertical = 12.dp))
                            Text("Administração e Pastoral", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(modifier = Modifier.fillMaxWidth().height(screenHeight * 0.08f).offset(y = -(screenHeight * 0.04f)).background(Color.White)) {
                    Button(
                        onClick = {
                            navController.navigate("turmaJovemScreen") {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Turma Jovem", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Crisma_Primary.copy(alpha = 0.3f)))
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Light_Gray_Darker),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Turma Adulta", color = Crisma_Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .background(Color.White),
                contentAlignment = Alignment.TopCenter
            ) {
                if (animarBotoesAcao) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (possuiPermissaoTotal) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                            SmallMenuCardAdulta(
                                title = "Frequência",
                                icon = Icons.Outlined.CheckCircle,
                                modifier = Modifier.weight(1f)
                            ) {
                                idTurmaSelecionada = null
                                encontroSelecionado = null
                                modoEdicaoFrequencia = false
                                showFrequenciaPopup = true
                            }
                            SmallMenuCardAdulta(
                                title = "Turmas",
                                icon = Icons.Outlined.Groups,
                                modifier = Modifier.weight(1f)
                            ) {
                                idTurmaSelecionada = null
                                nomeTurmaSelecionada = null
                                modoCriarTurma = false
                                showTurmasPopup = true
                            }
                            SmallMenuCardAdulta(
                                title = "Avisos",
                                icon = Icons.Outlined.Notifications,
                                modifier = Modifier.weight(1f)
                            ) {
                                destinoAvisoSelecionado = null
                                idTurmaSelecionada = null
                                nomeTurmaSelecionada = null
                                novoAvisoTexto = ""
                                listaAvisosAtivos = emptyList()
                                showAvisosPopup = true
                            }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                            SmallMenuCardAdulta(
                                title = "Financeiro",
                                icon = Icons.Outlined.Payments,
                                modifier = Modifier.weight(1f)
                            ) {
                                idTurmaSelecionada = null
                                crismandoSelecionado = null
                                parcelaSelecionadaFinanceira = null
                                catequistaResponsavelInput =
                                    nomeCatequistaLogado
                                showFinanceiroPopup = true
                            }
                            SmallMenuCardAdulta(
                                title = "Dados",
                                icon = Icons.Outlined.BarChart,
                                modifier = Modifier.weight(1f)
                            ) {
                                idTurmaSelecionada = null
                                exibirPorcentagemFalta = false
                                showDadosPopup = true
                            }
                            SmallMenuCardAdulta(
                                title = "Voltar",
                                icon = Icons.Outlined.ArrowBack,
                                modifier = Modifier.weight(1f)
                            ) {
                                navController.navigate("catequistaOptions") {
                                    popUpTo("turmaAdultaScreen") {
                                        inclusive = true
                                    }
                                }
                            }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                            SmallMenuCardAdulta(
                                title = "Frequência",
                                icon = Icons.Outlined.CheckCircle,
                                modifier = Modifier.weight(1f)
                            ) {
                                idTurmaSelecionada = null
                                encontroSelecionado = null
                                modoEdicaoFrequencia = false
                                showFrequenciaPopup = true
                            }
                            SmallMenuCardAdulta(
                                title = "Avisos",
                                icon = Icons.Outlined.Notifications,
                                modifier = Modifier.weight(1f)
                            ) {
                                idTurmaSelecionada = null
                                nomeTurmaSelecionada = null
                                novoAvisoTexto = ""
                                listaAvisosAtivos = emptyList()

                                // Catequista comum entra direto em "Sua Turma".
                                destinoAvisoSelecionado =
                                    DestinoAvisoAdulto.TURMA

                                showAvisosPopup = true
                            }
                            SmallMenuCardAdulta(
                                title = "Financeiro",
                                icon = Icons.Outlined.Payments,
                                modifier = Modifier.weight(1f)
                            ) {
                                idTurmaSelecionada = null
                                crismandoSelecionado = null
                                parcelaSelecionadaFinanceira = null
                                catequistaResponsavelInput =
                                    nomeCatequistaLogado
                                showFinanceiroPopup = true
                            }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                            SmallMenuCardAdulta(
                                title = "Dados",
                                icon = Icons.Outlined.BarChart,
                                modifier = Modifier.weight(1f)
                            ) {
                                idTurmaSelecionada = null
                                exibirPorcentagemFalta = false
                                showDadosPopup = true
                            }
                            SmallMenuCardAdulta(
                                title = "Voltar",
                                icon = Icons.Outlined.ArrowBack,
                                modifier = Modifier.weight(1f)
                            ) {
                                navController.navigate("catequistaOptions") {
                                    popUpTo("turmaAdultaScreen") {
                                        inclusive = true
                                    }
                                }
                            }
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTurmasPopup && possuiPermissaoTotal) {
        val titTurma = when {
            modoCriarTurma -> "Nova Turma"
            idTurmaSelecionada != null -> "Editar: $nomeTurmaSelecionada"
            else -> "Gerenciar Turmas"
        }
        CustomPopupAdulta(title = titTurma, onDismiss = { showTurmasPopup = false }) {
            if (modoCriarTurma) {
                item {
                    OutlinedTextField(value = novoNomeTurma, onValueChange = { novoNomeTurma = it }, label = { Text("Nome da Turma") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (novoNomeTurma.isNotBlank()) {
                                FirebaseRepository.criarTurma(
                                    nome = novoNomeTurma,
                                    categoria = "adulta",
                                    onSuccess = { turmaCriada ->
                                        Toast.makeText(
                                            context,
                                            "Turma criada: ${turmaCriada.id}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        novoNomeTurma = ""
                                        modoCriarTurma = false
                                    },
                                    onError = { erro ->
                                        Toast.makeText(
                                            context,
                                            erro.message ?: "Erro ao criar a turma.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                        shape = RoundedCornerShape(4.dp)
                    ) { Text("Criar Turma", fontWeight = FontWeight.Bold) }
                    TextButton(onClick = { modoCriarTurma = false }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar", color = Color.Gray) }
                }
            } else if (idTurmaSelecionada == null) {
                items(listaTurmasFirestore) { turma ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(turma.nome, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                idTurmaSelecionada = turma.id
                                nomeTurmaSelecionada = turma.nome
                            }) { Icon(Icons.Outlined.Edit, "Editar", tint = Color.Gray) }

                            IconButton(onClick = {
                                idTurmaParaExcluir = turma.id
                                nomeTurmaParaExcluir = turma.nome
                            }) {
                                Icon(Icons.Outlined.Delete, "Excluir", tint = Color.Red.copy(0.7f))
                            }
                        }
                    }
                }
                item {
                    Button(onClick = { modoCriarTurma = true }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary), shape = RoundedCornerShape(4.dp)) {
                        Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text("Nova Turma", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                item {
                    TextButton(onClick = { idTurmaSelecionada = null; nomeTurmaSelecionada = null }) {
                        Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary)
                        Text(" Voltar", color = Crisma_Primary)
                    }
                    OutlinedTextField(
                        value = novoNomeCrismando,
                        onValueChange = { novoNomeCrismando = it },
                        placeholder = { Text("Nome do Crismando...") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (novoNomeCrismando.isNotBlank()) {
                                    FirebaseRepository.criarCrismando(
                                        nome = novoNomeCrismando,
                                        turmaId = idTurmaSelecionada!!,
                                        categoria = "adulta",
                                        onSuccess = { crismandoCriado ->
                                            Toast.makeText(
                                                context,
                                                "Adicionado! Código: ${crismandoCriado.obterMatricula()}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            novoNomeCrismando = ""
                                        },
                                        onError = { erro ->
                                            Toast.makeText(
                                                context,
                                                erro.message ?: "Erro ao cadastrar o crismando.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }) { Icon(Icons.Outlined.AddCircle, null, tint = Crisma_Primary) }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(listaCrismandosFirestore) { crismando ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(crismando.nome, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Matrícula: ${crismando.id}", fontSize = 11.sp, color = Color.Gray)
                            }
                            IconButton(onClick = {
                                crismandoDocumentosSelecionado = crismando
                                abaDocumentosSelecionada = PerfilDocumentacao.CRISMANDO
                                responsavelDocumentosInput = ""
                                showDocumentosDialog = true
                            }) {
                                Icon(Icons.Outlined.Description, "Documentos", tint = Crisma_Primary)
                            }

                            IconButton(onClick = {
                                crismandoParaArquivar = crismando
                                motivoArquivamentoInput = ""
                                responsavelArquivamentoInput = ""
                            }) {
                                Icon(Icons.Outlined.Archive, "Arquivar", tint = Color.Red.copy(alpha = 0.65f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFrequenciaPopup) {
        val titFreq = when {
            encontroSelecionado != null -> "Encontro $encontroSelecionado - ${listaEncontrosFirestore.firstOrNull { it.numero == encontroSelecionado }?.dataManual ?: ""}"
            idTurmaSelecionada != null -> "Encontros: $nomeTurmaSelecionada"
            else -> "Frequência - Selecione a Turma"
        }
        CustomPopupAdulta(title = titFreq, onDismiss = { showFrequenciaPopup = false; idTurmaSelecionada = null; encontroSelecionado = null; idEncontroEmEdicao = null; modoEdicaoFrequencia = false }) {
            if (idTurmaSelecionada == null) {
                items(listaTurmasFirestore) { turma ->
                    Card(
                        onClick = {
                            idTurmaSelecionada = turma.id
                            nomeTurmaSelecionada = turma.nome
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(turma.nome, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Outlined.ArrowForwardIos, null, modifier = Modifier.size(14.dp), tint = Crisma_Primary)}
                    }
                }
            } else if (encontroSelecionado == null) {
                item {
                    TextButton(onClick = { idTurmaSelecionada = null; nomeTurmaSelecionada = null }) {
                        Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary)
                        Text(" Voltar para Turmas", color = Crisma_Primary)
                    }
                }

                items(listaEncontrosFirestore) { encontro ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f).clickable { encontroSelecionado = encontro.numero }) {
                                Text("Encontro ${encontro.numero} - ${encontro.dataManual}", color = Color.Black, fontWeight = FontWeight.Medium)
                            }

                            Row {
                                IconButton(onClick = {
                                    idEncontroEmEdicao = encontro.id
                                    dataEncontroEdicaoInput = encontro.dataManual.filter { it.isDigit() }
                                }) {
                                    Icon(Icons.Outlined.Edit, "Editar Data", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = {
                                    idEncontroParaExcluir = encontro.id
                                    numeroEncontroParaExcluir = encontro.numero
                                }) {
                                    Icon(Icons.Outlined.Delete, "Excluir Encontro", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    if (idEncontroEmEdicao == encontro.id) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
                            border = BorderStroke(1.dp, Crisma_Gold),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = dataEncontroEdicaoInput,
                                    onValueChange = { newValue ->
                                        val puros = newValue.filter { it.isDigit() }
                                        if (puros.length <= 8 && validarDigitosDataAdulta(puros)) {
                                            dataEncontroEdicaoInput = puros
                                        }
                                    },
                                    placeholder = { Text("Nova data (DDMMAAAA)...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    visualTransformation = visualTransformationData
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (dataEncontroEdicaoInput.length == 8) {
                                            val anoInserido = dataEncontroEdicaoInput.substring(4, 8).toIntOrNull() ?: 0
                                            if (anoInserido < 2026) {
                                                Toast.makeText(context, "O ano não pode ser menor que 2026!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val dataComBarras = StringBuilder(dataEncontroEdicaoInput)
                                                    .insert(2, "/").insert(5, "/").toString()

                                                FirebaseRepository.atualizarDataEncontro(
                                                    encontroId = encontro.id,
                                                    dataManual = dataComBarras,
                                                    onSuccess = {
                                                        Toast.makeText(
                                                            context,
                                                            "Data atualizada!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        idEncontroEmEdicao = null
                                                    },
                                                    onError = { erro ->
                                                        Toast.makeText(
                                                            context,
                                                            erro.message ?: "Erro ao atualizar a data.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                )
                                            }
                                        } else {
                                            Toast.makeText(context, "Digite os 8 números da data!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Salvar", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        OutlinedTextField(
                            value = novaDataEncontroInput,
                            onValueChange = { newValue ->
                                val puros = newValue.filter { it.isDigit() }
                                if (puros.length <= 8 && validarDigitosDataAdulta(puros)) {
                                    novaDataEncontroInput = puros
                                }
                            },
                            placeholder = { Text("Digite a data (Ex: 22062026)...") },
                            label = { Text("Data do Encontro") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = visualTransformationData
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (novaDataEncontroInput.length == 8) {
                                    val anoInserido = novaDataEncontroInput.substring(4, 8).toIntOrNull() ?: 0
                                    if (anoInserido < 2026) {
                                        Toast.makeText(context, "O ano não pode ser menor que 2026!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val proximoNumero =
                                            (listaEncontrosFirestore.maxOfOrNull { it.numero } ?: 0) + 1

                                        val dataComBarras = StringBuilder(novaDataEncontroInput)
                                            .insert(2, "/")
                                            .insert(5, "/")
                                            .toString()

                                        FirebaseRepository.salvarEncontro(
                                            turmaId = idTurmaSelecionada!!,
                                            numero = proximoNumero,
                                            dataManual = dataComBarras,
                                            onSuccess = {
                                                Toast.makeText(
                                                    context,
                                                    "Encontro $proximoNumero adicionado!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                novaDataEncontroInput = ""
                                            },
                                            onError = { erro ->
                                                Toast.makeText(
                                                    context,
                                                    erro.message ?: "Erro ao adicionar o encontro.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    }
                                } else {
                                    Toast.makeText(context, "Digite a data completa com 8 números!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(Icons.Outlined.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Adicionar Encontro", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { encontroSelecionado = null; modoEdicaoFrequencia = false }) {
                            Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary)
                            Text(" Voltar para Encontros", color = Crisma_Primary)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Edit, null, tint = Crisma_Primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Preencher", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Switch(
                                checked = modoEdicaoFrequencia,
                                onCheckedChange = { modoEdicaoFrequencia = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2E7D32))
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                items(listaCrismandosFirestore) { crismando ->
                    val chaveMap = "Jov_${idTurmaSelecionada}_${encontroSelecionado}_${crismando.id}"
                    val status = frequenciaPorEncontro[chaveMap] ?: StatusFrequencia.NENHUM

                    val factorOpacity = if (modoEdicaoFrequencia) 1.0f else 0.4f

                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF0F0F0)), shape = RoundedCornerShape(4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(crismando.nome, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = if (modoEdicaoFrequencia) 1.0f else 0.6f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { frequenciaPorEncontro[chaveMap] = StatusFrequencia.PRESENTE },
                                    enabled = modoEdicaoFrequencia,
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(status == StatusFrequencia.PRESENTE) Color(0xFF2E7D32) else Color(0xFFE8F5E9),
                                        contentColor = if(status == StatusFrequencia.PRESENTE) Color.White else Color(0xFF2E7D32),
                                        disabledContainerColor = if(status == StatusFrequencia.PRESENTE) Color(0xFF2E7D32).copy(alpha = factorOpacity) else Color(0xFFE8F5E9).copy(alpha = factorOpacity),
                                        disabledContentColor = if(status == StatusFrequencia.PRESENTE) Color.White.copy(alpha = factorOpacity) else Color(0xFF2E7D32).copy(alpha = factorOpacity)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("PRESENTE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { frequenciaPorEncontro[chaveMap] = StatusFrequencia.FALTA },
                                    enabled = modoEdicaoFrequencia,
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(status == StatusFrequencia.FALTA) Color(0xFFFF0000) else Color(0xFFFFEBEE),
                                        contentColor = if(status == StatusFrequencia.FALTA) Color.White else Color(0xFFFF0000),
                                        disabledContainerColor = if(status == StatusFrequencia.FALTA) Color(0xFFFF0000).copy(alpha = factorOpacity) else Color(0xFFFFEBEE).copy(alpha = factorOpacity),
                                        disabledContentColor = if(status == StatusFrequencia.FALTA) Color.White.copy(alpha = factorOpacity) else Color(0xFFFF0000).copy(alpha = factorOpacity)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("FALTA", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { frequenciaPorEncontro[chaveMap] = StatusFrequencia.JUSTIFICADA },
                                    enabled = modoEdicaoFrequencia,
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(status == StatusFrequencia.JUSTIFICADA) Color(0xFFF57C00) else Color(0xFFFFF3E0),
                                        contentColor = if(status == StatusFrequencia.JUSTIFICADA) Color.White else Color(0xFFF57C00),
                                        disabledContainerColor = if(status == StatusFrequencia.JUSTIFICADA) Color(0xFFF57C00).copy(alpha = factorOpacity) else Color(0xFFFFF3E0).copy(alpha = factorOpacity),
                                        disabledContentColor = if(status == StatusFrequencia.JUSTIFICADA) Color.White.copy(alpha = factorOpacity) else Color(0xFFF57C00).copy(alpha = factorOpacity)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("JUST.", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (listaCrismandosFirestore.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Não há crismandos cadastrados nesta turma.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                var operacoesConcluidas = 0
                                var ocorreuErro = false

                                listaCrismandosFirestore.forEach { crismando ->
                                    val chaveMap =
                                        "Jov_${idTurmaSelecionada}_${encontroSelecionado}_${crismando.id}"

                                    val statusParaSalvar =
                                        frequenciaPorEncontro[chaveMap]
                                            ?: StatusFrequencia.NENHUM

                                    FirebaseRepository.salvarFrequencia(
                                        turmaId = idTurmaSelecionada!!,
                                        encontro = encontroSelecionado!!,
                                        alunoId = crismando.id,
                                        status = statusParaSalvar,
                                        onSuccess = {
                                            operacoesConcluidas++

                                            if (
                                                operacoesConcluidas == listaCrismandosFirestore.size &&
                                                !ocorreuErro
                                            ) {
                                                Toast.makeText(
                                                    context,
                                                    "Chamada de Jovens salva com sucesso!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                modoEdicaoFrequencia = false
                                                showFrequenciaPopup = false
                                            }
                                        },
                                        onError = { erro ->
                                            operacoesConcluidas++
                                            ocorreuErro = true

                                            Toast.makeText(
                                                context,
                                                erro.message ?: "Erro ao salvar a frequência.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Sincronizar no Firebase", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showFinanceiroPopup) {
        CustomPopupAdulta(title = "Financeiro Adultos", onDismiss = { showFinanceiroPopup = false; idTurmaSelecionada = null; crismandoSelecionado = null; parcelaSelecionadaFinanceira = null }) {
            if (idTurmaSelecionada == null) {
                items(listaTurmasFirestore) { turma ->
                    Card(onClick = { idTurmaSelecionada = turma.id }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            // O weight(1f) faz o texto ocupar todo o espaço disponível, empurrando o ícone para o fim
                            Text(turma.nome, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))

                            // Um pequeno espaço de segurança antes do ícone
                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(Icons.Outlined.Payments, null, tint = Crisma_Primary)
                        }
                    }
                }
            } else if (crismandoSelecionado == null) {
                item { TextButton(onClick = { idTurmaSelecionada = null }) { Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary); Text(" Voltar", color = Crisma_Primary) } }
                items(listaCrismandosFirestore) { aluno ->
                    Card(onClick = { crismandoSelecionado = aluno.id; nomeCrismandoSelecionadoFixo = aluno.nome }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(aluno.nome, color = Color.Black, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Outlined.ChevronRight, null, tint = Crisma_Primary)
                        }
                    }
                }
            } else if (parcelaSelecionadaFinanceira == null) {
                item {
                    TextButton(onClick = { crismandoSelecionado = null }) {
                        Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary)
                        Text(" Voltar", color = Crisma_Primary)
                    }
                }
                items((1..12).toList()) { numeroParcela ->
                    val parcelaExistente = listaParcelasFinanceiras.firstOrNull { it.numeroParcela == numeroParcela }
                    val parcelaPaga = parcelaExistente?.statusPago == true

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Parcela $numeroParcela", modifier = Modifier.weight(1f))

                            if (parcelaPaga) {
                                Button(
                                    onClick = {
                                        nomeCatequistaPagamento = parcelaExistente?.recebidoPor ?: "Não identificado"
                                        showPagamentoInfoDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("PAGO", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { parcelaSelecionadaFinanceira = numeroParcela },
                                    colors = ButtonDefaults.buttonColors(containerColor = Light_Gray_Darker),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Pagar", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    val alunoIdFixo = crismandoSelecionado!!
                    val alunoNomeFixo = nomeCrismandoSelecionadoFixo
                    val parcelaFixa = parcelaSelecionadaFinanceira!!

                    Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        TextButton({ parcelaSelecionadaFinanceira = null }) { Icon(Icons.Outlined.ArrowBack, null); Text(" Voltar") }
                        Text("Lançando Parcela $parcelaFixa", fontWeight = FontWeight.Bold)
                        Text("Crismando: $alunoNomeFixo", color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = nomeCatequistaLogado,
                            onValueChange = {},
                            label = { Text("Recebido por") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAlertaFinanceiroEtapa1 = true },
                            enabled = nomeCatequistaLogado.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Confirmar Recebimento", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDadosPopup) {
        val tituloDados = if (idTurmaSelecionada == null) "Estatísticas Jovens" else "Métricas: $nomeTurmaSelecionada"

        CustomPopupAdulta(title = tituloDados, onDismiss = { showDadosPopup = false; idTurmaSelecionada = null }) {
            if (idTurmaSelecionada == null) {
                items(listaTurmasFirestore) { turma ->
                    Card(onClick = { idTurmaSelecionada = turma.id; nomeTurmaSelecionada = turma.nome }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(turma.nome, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Outlined.BarChart, null, tint = Crisma_Primary)
                        }
                    }
                }
            } else {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { idTurmaSelecionada = null }) {
                            Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary)
                            Text(" Voltar para as turmas", color = Crisma_Primary)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (exibirPorcentagemFalta) Icons.Outlined.TrendingUp else Icons.Outlined.Assessment,
                                        contentDescription = null,
                                        tint = Crisma_Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (exibirPorcentagemFalta) "Faltas (%)" else "Presenças (%)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                }

                                // SUBSTiTUA APENAS ESTE COMPONENTE SWITCH ABAIXO:
                                Switch(
                                    checked = !exibirPorcentagemFalta, // Mantém a lógica usada na tela adulta
                                    onCheckedChange = { exibirPorcentagemFalta = !it },
                                    colors = SwitchDefaults.colors(
                                        // Ligado (On) -> Fundo Vermelho, Bolinha Branca
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32),
                                        checkedBorderColor = Color(0xFF2E7D32),

                                        // Desligado (Off) -> Fundo Verde, Bolinha Branca
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFFF0000),
                                        uncheckedBorderColor = Color(0xFFFF0000)
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                items(listaCrismandosFirestore) { crismando ->
                    val totalEncontrosTurma = listaEncontrosFirestore.size
                    val totalPresencasAluno = todasFrequenciasGeraisByTurma.count {
                        it["alunoId"] == crismando.id && it["status"] == "PRESENTE"
                    }
                    val totalFaltasAluno = todasFrequenciasGeraisByTurma.count {
                        it["alunoId"] == crismando.id && it["status"] == "FALTA"
                    }
                    val totalJustificadasAluno = todasFrequenciasGeraisByTurma.count {
                        it["alunoId"] == crismando.id && it["status"] == "JUSTIFICADA"
                    }

                    val porcentagemCalculada = if (totalEncontrosTurma > 0) {
                        if (exibirPorcentagemFalta) {
                            (totalFaltasAluno.toFloat() / totalEncontrosTurma.toFloat()) * 100f
                        } else {
                            (totalPresencasAluno.toFloat() / totalEncontrosTurma.toFloat()) * 100f
                        }
                    } else 0f

                    val textoPorcentagem = String.format("%.1f%%", porcentagemCalculada)

                    val corAlertaMétrica = if (exibirPorcentagemFalta) {
                        if (porcentagemCalculada > 25f) Color.Red else Color(0xFF2E7D32)
                    } else {
                        if (porcentagemCalculada < 75f) Color.Red else Color(0xFF2E7D32)
                    }

                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(crismando.nome, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "P: $totalPresencasAluno | F: $totalFaltasAluno | J: $totalJustificadasAluno | Total de Aulas: $totalEncontrosTurma",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = textoPorcentagem,
                                color = corAlertaMétrica,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAvisosPopup) {
        /*
         * Esta segunda proteção garante que o seletor de avisos
         * gerais nunca apareça para CATEQUISTA_COMUM, mesmo que
         * algum estado antigo da tela ainda exista.
         */
        val destinoAtual = if (possuiPermissaoTotal) {
            destinoAvisoSelecionado
        } else {
            DestinoAvisoAdulto.TURMA
        }

        val tituloAvisos = when {
            destinoAtual == null -> "Escolha o tipo de aviso"
            destinoAtual == DestinoAvisoAdulto.GERAL -> "Aviso Geral"
            destinoAtual == DestinoAvisoAdulto.CATEGORIA ->
                "Todas as turmas adultas"
            idTurmaSelecionada == null ->
                "Sua turma - selecione a turma"
            else -> "Avisos: $nomeTurmaSelecionada"
        }

        CustomPopupAdulta(
            title = tituloAvisos,
            onDismiss = {
                showAvisosPopup = false
                destinoAvisoSelecionado = null
                idTurmaSelecionada = null
                nomeTurmaSelecionada = null
                novoAvisoTexto = ""
                listaAvisosAtivos = emptyList()
            }
        ) {
            when {
                destinoAtual == null -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (possuiPermissaoTotal) {
                                BotaoDestinoAvisoAdulto(
                                    titulo = "Aviso Geral",
                                    descricao = "Será mostrado para todas as turmas jovens e adultas.",
                                    cor = Crisma_Gold,
                                    corTexto = Color.Black
                                ) {
                                    destinoAvisoSelecionado =
                                        DestinoAvisoAdulto.GERAL
                                }

                                BotaoDestinoAvisoAdulto(
                                    titulo = "Turmas Adultas",
                                    descricao = "Será mostrado para todas as turmas adultas.",
                                    cor = Aviso_Blue,
                                    corTexto = Color.White
                                ) {
                                    destinoAvisoSelecionado =
                                        DestinoAvisoAdulto.CATEGORIA
                                }
                            }

                            BotaoDestinoAvisoAdulto(
                                titulo = "Sua Turma",
                                descricao = "Será mostrado somente para uma turma específica.",
                                cor = Crisma_Primary,
                                corTexto = Color.White
                            ) {
                                destinoAvisoSelecionado =
                                    DestinoAvisoAdulto.TURMA
                            }
                        }
                    }
                }

                destinoAtual == DestinoAvisoAdulto.TURMA &&
                        idTurmaSelecionada == null -> {

                    item {
                        TextButton(
                            onClick = {
                                if (possuiPermissaoTotal) {
                                    destinoAvisoSelecionado = null
                                    listaAvisosAtivos = emptyList()
                                } else {
                                    showAvisosPopup = false
                                    destinoAvisoSelecionado = null
                                    idTurmaSelecionada = null
                                    nomeTurmaSelecionada = null
                                }
                            }
                        ) {
                            Icon(
                                Icons.Outlined.ArrowBack,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = Crisma_Primary
                            )
                            Text(
                                text = if (possuiPermissaoTotal) {
                                    " Voltar aos tipos de aviso"
                                } else {
                                    " Fechar"
                                },
                                color = Crisma_Primary
                            )
                        }
                    }

                    items(listaTurmasFirestore) { turma ->
                        Card(
                            onClick = {
                                idTurmaSelecionada = turma.id
                                nomeTurmaSelecionada = turma.nome
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF7F7)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Crisma_Primary
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = turma.nome,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.Outlined.ArrowForwardIos,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Crisma_Primary
                                )
                            }
                        }
                    }
                }

                else -> {
                    item {
                        val corDestino = when (destinoAtual) {
                            DestinoAvisoAdulto.GERAL -> Crisma_Gold
                            DestinoAvisoAdulto.CATEGORIA -> Aviso_Blue
                            DestinoAvisoAdulto.TURMA -> Crisma_Primary
                            null -> Crisma_Primary
                        }

                        val corTextoBotao = if (
                            destinoAtual == DestinoAvisoAdulto.GERAL
                        ) {
                            Color.Black
                        } else {
                            Color.White
                        }

                        val destinoId = when (destinoAtual) {
                            DestinoAvisoAdulto.GERAL -> "GERAL"
                            DestinoAvisoAdulto.CATEGORIA -> "CATEGORIA_ADULTA"
                            DestinoAvisoAdulto.TURMA ->
                                idTurmaSelecionada.orEmpty()
                            null -> ""
                        }

                        val tipoAviso = when (destinoAtual) {
                            DestinoAvisoAdulto.GERAL -> "GERAL"
                            DestinoAvisoAdulto.CATEGORIA -> "CATEGORIA"
                            DestinoAvisoAdulto.TURMA -> "TURMA"
                            null -> "TURMA"
                        }

                        val descricaoDestino = when (destinoAtual) {
                            DestinoAvisoAdulto.GERAL ->
                                "Todos os crismandos verão este aviso."

                            DestinoAvisoAdulto.CATEGORIA ->
                                "Todas as turmas adultas verão este aviso."

                            DestinoAvisoAdulto.TURMA ->
                                "Somente a turma $nomeTurmaSelecionada verá este aviso."

                            null -> ""
                        }

                        TextButton(
                            onClick = {
                                if (possuiPermissaoTotal) {
                                    destinoAvisoSelecionado = null
                                } else {
                                    destinoAvisoSelecionado =
                                        DestinoAvisoAdulto.TURMA
                                }

                                idTurmaSelecionada = null
                                nomeTurmaSelecionada = null
                                novoAvisoTexto = ""
                                listaAvisosAtivos = emptyList()
                            }
                        ) {
                            Icon(
                                Icons.Outlined.ArrowBack,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = Crisma_Primary
                            )
                            Text(
                                text = if (possuiPermissaoTotal) {
                                    " Voltar aos tipos de aviso"
                                } else {
                                    " Voltar às turmas"
                                },
                                color = Crisma_Primary
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = corDestino.copy(
                                    alpha = 0.12f
                                )
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = corDestino
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = descricaoDestino,
                                modifier = Modifier.padding(12.dp),
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = novoAvisoTexto,
                            onValueChange = {
                                novoAvisoTexto = it
                            },
                            placeholder = {
                                Text("Digite o aviso...")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val destinoPermitido =
                                    possuiPermissaoTotal ||
                                        destinoAtual ==
                                            DestinoAvisoAdulto.TURMA

                                if (!destinoPermitido) {
                                    Toast.makeText(
                                        context,
                                        "Seu acesso permite publicar apenas para uma turma específica.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (
                                    novoAvisoTexto.isNotBlank() &&
                                    destinoId.isNotBlank()
                                ) {
                                    FirebaseRepository.criarAviso(
                                        turmaId = destinoId,
                                        texto = novoAvisoTexto,
                                        tipo = tipoAviso,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Aviso publicado com sucesso!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            novoAvisoTexto = ""
                                        },
                                        onError = { erro ->
                                            Toast.makeText(
                                                context,
                                                erro.message
                                                    ?: "Erro ao publicar o aviso.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = corDestino,
                                contentColor = corTextoBotao
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = corDestino
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Publicar",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(
                                vertical = 12.dp
                            ),
                            color = corDestino.copy(alpha = 0.55f)
                        )
                    }

                    items(
                        items = listaAvisosAtivos,
                        key = { it.id }
                    ) { aviso ->
                        CardAvisoAdministrativoAdulto(
                            aviso = aviso,
                            onExcluir = {
                                idAvisoParaExcluir = aviso.id
                                textoAvisoParaExcluir =
                                    aviso.texto
                            }
                        )
                    }
                }
            }
        }
    }

    if (
        showDocumentosDialog &&
        crismandoDocumentosSelecionado != null &&
        possuiPermissaoTotal
    ) {
        DocumentosAdultoDialog(
            nomeCrismando = crismandoDocumentosSelecionado!!.nome,
            matricula = crismandoDocumentosSelecionado!!.id,
            abaSelecionada = abaDocumentosSelecionada,
            onAbaSelecionada = { abaDocumentosSelecionada = it },
            cadastroCrismando = cadastroDocumentosCrismando,
            onCadastroCrismandoChange = { cadastroDocumentosCrismando = it },
            cadastroPadrinho = cadastroDocumentosPadrinho,
            onCadastroPadrinhoChange = { cadastroDocumentosPadrinho = it },
            responsavel = responsavelDocumentosInput,
            onResponsavelChange = { responsavelDocumentosInput = it },
            carregando = carregandoDocumentosCadastro,
            onDismiss = {
                showDocumentosDialog = false
                crismandoDocumentosSelecionado = null
                responsavelDocumentosInput = ""
            },
            onSalvar = {
                val cadastro = if (abaDocumentosSelecionada == PerfilDocumentacao.CRISMANDO) {
                    cadastroDocumentosCrismando
                } else {
                    cadastroDocumentosPadrinho
                }

                if (responsavelDocumentosInput.isBlank()) {
                    Toast.makeText(context, "Informe quem atualizou os documentos.", Toast.LENGTH_SHORT).show()
                } else {
                    carregandoDocumentosCadastro = true
                    FirebaseRepository.salvarCadastroDocumentacao(
                        cadastro = cadastro.copy(
                            alunoId = crismandoDocumentosSelecionado!!.id,
                            turmaId = crismandoDocumentosSelecionado!!.turmaId,
                            perfil = abaDocumentosSelecionada.name
                        ),
                        responsavel = responsavelDocumentosInput,
                        onSuccess = {
                            carregandoDocumentosCadastro = false
                            Toast.makeText(context, "Documentos salvos!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { erro ->
                            carregandoDocumentosCadastro = false
                            Toast.makeText(context, erro.message ?: "Erro ao salvar os documentos.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        )
    }

    if (crismandoParaArquivar != null && possuiPermissaoTotal) {
        AlertDialog(
            onDismissRequest = {
                crismandoParaArquivar = null
                motivoArquivamentoInput = ""
                responsavelArquivamentoInput = ""
            },
            title = { Text("Arquivar crismando", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${crismandoParaArquivar!!.nome} deixará de aparecer na turma, mas pagamentos, frequências e documentos serão preservados no servidor.")
                    OutlinedTextField(
                        value = motivoArquivamentoInput,
                        onValueChange = { motivoArquivamentoInput = it },
                        label = { Text("Motivo") },
                        placeholder = { Text("Ex.: desistência") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = responsavelArquivamentoInput,
                        onValueChange = { responsavelArquivamentoInput = it },
                        label = { Text("Responsável") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { crismandoParaArquivar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val crismando = crismandoParaArquivar
                        if (crismando == null || motivoArquivamentoInput.isBlank() || responsavelArquivamentoInput.isBlank()) {
                            Toast.makeText(context, "Informe o motivo e o responsável.", Toast.LENGTH_SHORT).show()
                        } else {
                            FirebaseRepository.arquivarCrismando(
                                matricula = crismando.id,
                                situacao = SituacaoCrismando.DESISTENTE,
                                motivo = motivoArquivamentoInput,
                                responsavel = responsavelArquivamentoInput,
                                onSuccess = {
                                    Toast.makeText(context, "Crismando arquivado. O histórico foi preservado.", Toast.LENGTH_SHORT).show()
                                    crismandoParaArquivar = null
                                    motivoArquivamentoInput = ""
                                    responsavelArquivamentoInput = ""
                                },
                                onError = { erro ->
                                    Toast.makeText(context, erro.message ?: "Erro ao arquivar o crismando.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary)
                ) { Text("Arquivar") }
            }
        )
    }

    if (showSobreNosDialog) AlertDialog(onDismissRequest = { showSobreNosDialog = false }, confirmButton = { TextButton(onClick = { showSobreNosDialog = false }) { Text("OK", color = Crisma_Primary, fontWeight = FontWeight.Bold) } }, title = { Text("Sobre") }, text = { Text("CrismAPP - Gestão Catequética.") })
    if (showContatosDialog) AlertDialog(onDismissRequest = { showContatosDialog = false }, confirmButton = { TextButton(onClick = { showContatosDialog = false }) { Text("OK", color = Crisma_Primary, fontWeight = FontWeight.Bold) } }, title = { Text("Contatos") }, text = { Text("Paróquia: (81) 98593-9076") })

    if (idEncontroParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { idEncontroParaExcluir = null },
            title = { Text("Confirmar Exclusão", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja mesmo excluir the ${numeroEncontroParaExcluir}º Encontro? Essa ação é irreversível.") },
            dismissButton = {
                TextButton(onClick = { idEncontroParaExcluir = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            confirmButton = {
                AnimatedVisibility(
                    visible = liberarBotoesConfirmacaoExcluir,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Button(
                        onClick = {
                            FirebaseRepository.excluirEncontro(
                                encontroId = idEncontroParaExcluir!!,
                                turmaId = idTurmaSelecionada!!,
                                numeroEncontro = numeroEncontroParaExcluir,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Encontro e frequências removidos!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    idEncontroParaExcluir = null
                                },
                                onError = { erro ->
                                    Toast.makeText(
                                        context,
                                        erro.message ?: "Erro ao excluir o encontro.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Sim, Excluir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    if (idTurmaParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { idTurmaParaExcluir = null },
            title = { Text("Arquivar Turma Adulta", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja arquivar a turma \"$nomeTurmaParaExcluir\"? Nenhum aluno, pagamento ou frequência será apagado.") },
            dismissButton = {
                TextButton(onClick = { idTurmaParaExcluir = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            confirmButton = {
                AnimatedVisibility(
                    visible = liberarBotoesConfirmacaoExcluirTurma,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Button(
                        onClick = {
                            FirebaseRepository.excluirTurmaDefinitivamente(
                                turmaId = idTurmaParaExcluir!!,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Turma arquivada. Os dados foram preservados.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    idTurmaParaExcluir = null
                                    idTurmaSelecionada = null
                                    nomeTurmaSelecionada = null
                                },
                                onError = { erro ->
                                    Toast.makeText(
                                        context,
                                        erro.message ?: "Erro ao arquivar a turma.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Confirmar Exclusão", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    if (idAvisoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { idAvisoParaExcluir = null },
            title = { Text("Excluir Aviso Adulto", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja mesmo excluir permanentemente o aviso: \"$textoAvisoParaExcluir\"?") },
            dismissButton = {
                TextButton(onClick = { idAvisoParaExcluir = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            confirmButton = {
                AnimatedVisibility(
                    visible = liberarBotoesConfirmacaoExcluirAviso,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Button(
                        onClick = {
                            FirebaseRepository.excluirAviso(
                                avisoId = idAvisoParaExcluir!!,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Aviso removido com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    idAvisoParaExcluir = null
                                },
                                onError = { erro ->
                                    Toast.makeText(
                                        context,
                                        erro.message ?: "Erro ao excluir o aviso.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Confirmar Exclusão", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    if (showPagamentoInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPagamentoInfoDialog = false },
            confirmButton = {
                Button(onClick = { showPagamentoInfoDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                    Text("OK")
                }
            },
            title = { Text("Pagamento Registrado", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)) },
            text = { Text("Essa parcela foi recebida por:\n\n$nomeCatequistaPagamento") }
        )
    }

    if (showAlertaFinanceiroEtapa1) {
        AlertDialog(
            onDismissRequest = { showAlertaFinanceiroEtapa1 = false },
            title = { Text("Registro Imutável", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("NÃO será possível reverter, editar ou excluir esta informação posterior ao envio.") },
            dismissButton = { TextButton({ showAlertaFinanceiroEtapa1 = false }) { Text("Cancelar", color = Color.Gray) } },
            confirmButton = {
                AnimatedVisibility(visible = liberarBotaoFinanceiroEtapa1) {
                    Button(
                        onClick = { showAlertaFinanceiroEtapa1 = false; showAlertaFinanceiroEtapa2 = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(4.dp)
                    ) { Text("Estou Ciente, Avançar") }
                }
            }
        )
    }

    if (showAlertaFinanceiroEtapa2) {
        val alunoIdSalvar = crismandoSelecionado ?: ""
        val parcelaSalvar = parcelaSelecionadaFinanceira ?: 0

        AlertDialog(
            onDismissRequest = { showAlertaFinanceiroEtapa2 = false },
            title = { Text("Segurança") },
            text = { Text("Confirma o recebimento sob responsabilidade de: \"$nomeCatequistaLogado\"?") },
            dismissButton = { TextButton({ showAlertaFinanceiroEtapa2 = false }) { Text("Voltar", color = Color.Gray) } },
            confirmButton = {
                AnimatedVisibility(visible = liberarBotaoFinanceiroEtapa2) {
                    Button(
                        onClick = {
                            FirebaseRepository.salvarPagamento(
                                turmaId = idTurmaSelecionada!!,
                                alunoId = alunoIdSalvar,
                                parcela = parcelaSalvar,
                                recebidoPor = nomeCatequistaLogado,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Parcela registrada no Firebase!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showAlertaFinanceiroEtapa2 = false
                                    parcelaSelecionadaFinanceira = null
                                    catequistaResponsavelInput = nomeCatequistaLogado
                                    crismandoSelecionado = null
                                },
                                onError = { erro ->
                                    Toast.makeText(
                                        context,
                                        erro.message ?: "Erro ao registrar a parcela.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(4.dp)
                    ) { Text("Confirmar e Gravar") }
                }
            }
        )
    }
}


@Composable
private fun BotaoDestinoAvisoAdulto(
    titulo: String,
    descricao: String,
    cor: Color,
    corTexto: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = cor,
            contentColor = corTexto
        ),
        border = BorderStroke(
            width = 1.dp,
            color = cor
        ),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = descricao,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun CardAvisoAdministrativoAdulto(
    aviso: Aviso,
    onExcluir: () -> Unit
) {
    val destino = aviso.turmaId
        .trim()
        .uppercase()

    val corAviso = when {
        destino == "GERAL" -> Crisma_Gold

        destino == "CATEGORIA_JOVEM" ||
                destino == "CATEGORIA_ADULTA" ||
                destino == "TURMA_JOVEM" ||
                destino == "TURMA_ADULTA" -> Aviso_Blue

        else -> Crisma_Primary
    }

    val rotulo = when {
        destino == "GERAL" -> "AVISO GERAL"

        destino == "CATEGORIA_JOVEM" ||
                destino == "TURMA_JOVEM" ->
            "TURMAS JOVENS"

        destino == "CATEGORIA_ADULTA" ||
                destino == "TURMA_ADULTA" ->
            "TURMAS ADULTAS"

        else -> "SUA TURMA"
    }

    val corTextoRotulo = if (corAviso == Crisma_Gold) {
        Color.Black
    } else {
        Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = corAviso.copy(alpha = 0.10f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = corAviso
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = corAviso,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(
                            horizontal = 7.dp,
                            vertical = 3.dp
                        )
                ) {
                    Text(
                        text = rotulo,
                        color = corTextoRotulo,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = aviso.texto,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onExcluir
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Excluir aviso",
                    tint = Crisma_Primary.copy(
                        alpha = 0.75f
                    )
                )
            }
        }
    }
}

@Composable
private fun DocumentosAdultoDialog(
    nomeCrismando: String,
    matricula: String,
    abaSelecionada: PerfilDocumentacao,
    onAbaSelecionada: (PerfilDocumentacao) -> Unit,
    cadastroCrismando: CadastroDocumentacao,
    onCadastroCrismandoChange: (CadastroDocumentacao) -> Unit,
    cadastroPadrinho: CadastroDocumentacao,
    onCadastroPadrinhoChange: (CadastroDocumentacao) -> Unit,
    responsavel: String,
    onResponsavelChange: (String) -> Unit,
    carregando: Boolean,
    onDismiss: () -> Unit,
    onSalvar: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val cadastroAtual = if (abaSelecionada == PerfilDocumentacao.CRISMANDO) cadastroCrismando else cadastroPadrinho
    val atualizar: (CadastroDocumentacao) -> Unit = if (abaSelecionada == PerfilDocumentacao.CRISMANDO) onCadastroCrismandoChange else onCadastroPadrinhoChange

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().height(screenHeight * 0.82f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().background(Crisma_Primary).padding(14.dp)) {
                    Column(modifier = Modifier.padding(end = 32.dp)) {
                        Text("Documentos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("$nomeCrismando • $matricula", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    }
                    Icon(Icons.Outlined.Close, "Fechar", tint = Color.White, modifier = Modifier.align(Alignment.CenterEnd).clickable { onDismiss() })
                }

                TabRow(
                    selectedTabIndex = if (abaSelecionada == PerfilDocumentacao.CRISMANDO) 0 else 1,
                    containerColor = Color.White,
                    contentColor = Crisma_Primary
                ) {
                    Tab(
                        selected = abaSelecionada == PerfilDocumentacao.CRISMANDO,
                        onClick = { onAbaSelecionada(PerfilDocumentacao.CRISMANDO) },
                        text = { Text("Crismando") }
                    )
                    Tab(
                        selected = abaSelecionada == PerfilDocumentacao.PADRINHO,
                        onClick = { onAbaSelecionada(PerfilDocumentacao.PADRINHO) },
                        text = { Text("Padrinho") }
                    )
                }

                if (carregando) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Crisma_Primary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        item { DocumentoSecaoTitulo("Sacramentos") }

                        if (abaSelecionada == PerfilDocumentacao.PADRINHO) {
                            item {
                                EscolhaSimNaoDocumento("Possui Crisma?", cadastroAtual.crismaPossui) { possui ->
                                    atualizar(cadastroAtual.copy(crismaPossui = possui, crismaEntregue = if (possui) cadastroAtual.crismaEntregue else false))
                                }
                            }
                            if (cadastroAtual.crismaPossui) {
                                item {
                                    DocumentoSwitchLinha("Comprovante de Crisma entregue?", cadastroAtual.crismaEntregue) {
                                        atualizar(cadastroAtual.copy(crismaEntregue = it))
                                    }
                                }
                            }
                        }

                        if (abaSelecionada == PerfilDocumentacao.CRISMANDO || !cadastroAtual.crismaPossui) {
                            item {
                                EscolhaSimNaoDocumento("Possui Primeira Comunhão?", cadastroAtual.primeiraComunhaoPossui) { possui ->
                                    atualizar(cadastroAtual.copy(
                                        primeiraComunhaoPossui = possui,
                                        primeiraComunhaoEntregue = if (possui) cadastroAtual.primeiraComunhaoEntregue else false
                                    ))
                                }
                            }
                            if (cadastroAtual.primeiraComunhaoPossui) {
                                item {
                                    DocumentoSwitchLinha("Comprovante de Primeira Comunhão entregue?", cadastroAtual.primeiraComunhaoEntregue) {
                                        atualizar(cadastroAtual.copy(primeiraComunhaoEntregue = it))
                                    }
                                }
                            } else {
                                item {
                                    DocumentoSwitchLinha("Comprovante de Batismo entregue?", cadastroAtual.batismoEntregue) {
                                        atualizar(cadastroAtual.copy(batismoEntregue = it))
                                    }
                                }
                            }
                        }

                        item { DocumentoSecaoTitulo("Identificação") }
                        item {
                            DocumentoSwitchLinha("Documento de identificação entregue?", cadastroAtual.identificacaoEntregue) { entregue ->
                                atualizar(cadastroAtual.copy(
                                    identificacaoEntregue = entregue,
                                    tipoIdentificacao = if (entregue) cadastroAtual.tipoIdentificacao else TipoIdentificacaoDocumento.NAO_INFORMADO.name,
                                    identificacaoOutro = if (entregue) cadastroAtual.identificacaoOutro else ""
                                ))
                            }
                        }
                        if (cadastroAtual.identificacaoEntregue) {
                            item {
                                TipoIdentificacaoSelector(cadastroAtual.obterTipoIdentificacao()) { tipo ->
                                    atualizar(cadastroAtual.copy(
                                        tipoIdentificacao = tipo.name,
                                        identificacaoOutro = if (tipo == TipoIdentificacaoDocumento.OUTRO) cadastroAtual.identificacaoOutro else ""
                                    ))
                                }
                            }
                            if (cadastroAtual.obterTipoIdentificacao() == TipoIdentificacaoDocumento.OUTRO) {
                                item {
                                    OutlinedTextField(
                                        value = cadastroAtual.identificacaoOutro,
                                        onValueChange = { atualizar(cadastroAtual.copy(identificacaoOutro = it)) },
                                        label = { Text("Qual documento?") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        item { DocumentoSecaoTitulo("Casamento") }
                        item {
                            StatusCasamentoSelector(cadastroAtual.obterStatusCasamento()) { status ->
                                atualizar(cadastroAtual.copy(casamentoStatus = status.name))
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = responsavel,
                                onValueChange = onResponsavelChange,
                                label = { Text("Responsável pela atualização") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Crisma_Primary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Crisma_Primary
                        )
                    ) {
                        Text("Fechar")
                    }
                    Button(
                        onClick = onSalvar,
                        enabled = !carregando,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary)
                    ) {
                        Icon(Icons.Outlined.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Salvar aba")
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentoSecaoTitulo(titulo: String) {
    Text(titulo, color = Crisma_Primary, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
}

@Composable
private fun EscolhaSimNaoDocumento(
    titulo: String,
    valor: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9F9F9)
        ),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Crisma_Primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = titulo,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onValueChange(true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (valor) {
                            Crisma_Primary
                        } else {
                            Color.White
                        },
                        contentColor = if (valor) {
                            Color.White
                        } else {
                            Crisma_Primary
                        }
                    ),
                    border = BorderStroke(1.dp, Crisma_Primary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
                ) {
                    Text("Sim")
                }

                Button(
                    onClick = { onValueChange(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!valor) {
                            Crisma_Primary
                        } else {
                            Color.White
                        },
                        contentColor = if (!valor) {
                            Color.White
                        } else {
                            Crisma_Primary
                        }
                    ),
                    border = BorderStroke(1.dp, Crisma_Primary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
                ) {
                    Text("Não")
                }
            }
        }
    }
}

@Composable
private fun DocumentoSwitchLinha(
    titulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9F9F9)
        ),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Crisma_Primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titulo,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF2E7D32)
                )
            )
        }
    }
}

@Composable
private fun TipoIdentificacaoSelector(selecionado: TipoIdentificacaoDocumento, onSelecionado: (TipoIdentificacaoDocumento) -> Unit) {
    Column {
        Text("Qual documento?", fontWeight = FontWeight.SemiBold)
        TipoRadioDocumento("Identidade", selecionado == TipoIdentificacaoDocumento.IDENTIDADE) { onSelecionado(TipoIdentificacaoDocumento.IDENTIDADE) }
        TipoRadioDocumento("CNH", selecionado == TipoIdentificacaoDocumento.CNH) { onSelecionado(TipoIdentificacaoDocumento.CNH) }
        TipoRadioDocumento("Outro", selecionado == TipoIdentificacaoDocumento.OUTRO) { onSelecionado(TipoIdentificacaoDocumento.OUTRO) }
    }
}

@Composable
private fun StatusCasamentoSelector(selecionado: StatusCasamentoDocumento, onSelecionado: (StatusCasamentoDocumento) -> Unit) {
    Column {
        TipoRadioDocumento("Comprovante entregue", selecionado == StatusCasamentoDocumento.ENTREGUE) { onSelecionado(StatusCasamentoDocumento.ENTREGUE) }
        TipoRadioDocumento("Não entregue", selecionado == StatusCasamentoDocumento.NAO_ENTREGUE) { onSelecionado(StatusCasamentoDocumento.NAO_ENTREGUE) }
        TipoRadioDocumento("Não é casado", selecionado == StatusCasamentoDocumento.NAO_CASADO) { onSelecionado(StatusCasamentoDocumento.NAO_CASADO) }
    }
}

@Composable
private fun TipoRadioDocumento(
    texto: String,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selecionado,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Crisma_Primary,
                unselectedColor = Crisma_Primary.copy(alpha = 0.65f)
            )
        )

        Text(
            text = texto,
            fontSize = 14.sp
        )
    }
}

@Composable
fun UserIconWithLabelAdulta(icon: ImageVector, label: String, onClick: () -> Unit) {
    Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp).clickable { onClick() })
}

@Composable
fun SmallMenuCardAdulta(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = Crisma_Primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun CustomPopupAdulta(title: String, onDismiss: () -> Unit, content: LazyListScope.() -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().height(screenHeight * 0.52f).background(Color.White, shape = RoundedCornerShape(2.dp)).border(width = 1.dp, color = Crisma_Primary, shape = RoundedCornerShape(2.dp))) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().background(Crisma_Primary).padding(12.dp)) {
                    Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.align(Alignment.CenterEnd).clickable { onDismiss() })
                }
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
            }
        }
    }
}