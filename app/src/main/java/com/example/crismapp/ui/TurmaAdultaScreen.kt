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
import androidx.compose.ui.text.style.TextAlign
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
    var showAtalhosDialog by remember {
        mutableStateOf(false)
    }

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

    var atalhosIniciais by remember {
        mutableStateOf(
            AtalhosIniciaisPadrao.lista()
        )
    }

    val sobreInicial = atalhosIniciais
        .firstOrNull {
            it.id == AtalhosIniciaisPadrao.ID_SOBRE
        }
        ?: AtalhosIniciaisPadrao.sobre()

    val contatosInicial = atalhosIniciais
        .firstOrNull {
            it.id == AtalhosIniciaisPadrao.ID_CONTATOS
        }
        ?: AtalhosIniciaisPadrao.contatos()
    var carregandoAtalhos by remember {
        mutableStateOf(true)
    }
    var salvandoAtalho by remember {
        mutableStateOf(false)
    }

    val visualTransformationData = remember { MascaraDataTransformationAdulta() }

    LaunchedEffect(possuiPermissaoTotal) {
        if (!possuiPermissaoTotal) {
            showTurmasPopup = false
            showDocumentosDialog = false
            showAtalhosDialog = false
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

    /*
     * Configuração compartilhada dos botões da primeira tela.
     * Alterar pela turma jovem ou adulta modifica os mesmos
     * documentos no Firestore.
     */
    DisposableEffect(Unit) {
        val listener =
            FirebaseRepository.ouvirAtalhosIniciais(
                onUpdate = { configuracoes ->
                    atalhosIniciais = configuracoes
                    carregandoAtalhos = false
                },
                onError = {
                    atalhosIniciais =
                        AtalhosIniciaisPadrao.lista()
                    carregandoAtalhos = false
                }
            )

        onDispose {
            listener.remove()
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
                    perfil = PerfilDocumentacao.PADRINHO.name,
                    crismaPossui = true,
                    primeiraComunhaoPossui = false,
                    primeiraComunhaoEntregue = false,
                    batismoEntregue = false
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
                        UserIconWithLabel(
                            iconeAtalhoInicial(sobreInicial.iconeCodigo),
                            sobreInicial.titulo
                        ) { showSobreNosDialog = true }
                        UserIconWithLabel(
                            iconeAtalhoInicial(contatosInicial.iconeCodigo),
                            contatosInicial.titulo
                        ) { showContatosDialog = true }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (animarImagem) {
                        Image(painter = painterResource(id = R.drawable.imagem_crisma), contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp))
                    }

                    if (animarTextos) {
                        Column {
                            Text("\nGestão: Turma Adulta", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = Crisma_Gold, thickness = 2.dp, modifier = Modifier.fillMaxWidth(0.76f).padding(vertical = 12.dp))
                            Text(
                                text = "$nomeCatequistaLogado - ${
                                    if (possuiPermissaoTotal) {
                                        "Permissão total"
                                    } else {
                                        "Permissão comum"
                                    }
                                }",
                                fontSize = 16.sp,
                                color = Color.White
                            )
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
                                    icon =
                                    Icons.Outlined.CheckCircle,
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
                                    icon =
                                    Icons.Outlined.Notifications,
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

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

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
                                    parcelaSelecionadaFinanceira =
                                        null
                                    catequistaResponsavelInput =
                                        nomeCatequistaLogado
                                    showFinanceiroPopup = true
                                }

                                SmallMenuCardAdulta(
                                    title = "Links",
                                    icon = Icons.Outlined.Link,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    showAtalhosDialog = true
                                }

                                SmallMenuCardAdulta(
                                    title = "Voltar",
                                    icon =
                                    Icons.Outlined.ArrowBack,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    navController.navigate(
                                        "catequistaOptions"
                                    ) {
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
                                    icon =
                                    Icons.Outlined.CheckCircle,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    idTurmaSelecionada = null
                                    encontroSelecionado = null
                                    modoEdicaoFrequencia = false
                                    showFrequenciaPopup = true
                                }

                                SmallMenuCardAdulta(
                                    title = "Avisos",
                                    icon =
                                    Icons.Outlined.Notifications,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    destinoAvisoSelecionado =
                                        DestinoAvisoAdulto.TURMA
                                    idTurmaSelecionada = null
                                    nomeTurmaSelecionada = null
                                    novoAvisoTexto = ""
                                    listaAvisosAtivos = emptyList()
                                    showAvisosPopup = true
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

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
                                    parcelaSelecionadaFinanceira =
                                        null
                                    catequistaResponsavelInput =
                                        nomeCatequistaLogado
                                    showFinanceiroPopup = true
                                }

                                SmallMenuCardAdulta(
                                    title = "Voltar",
                                    icon =
                                    Icons.Outlined.ArrowBack,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    navController.navigate(
                                        "catequistaOptions"
                                    ) {
                                        popUpTo("turmaAdultaScreen") {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (
        showAtalhosDialog &&
        possuiPermissaoTotal
    ) {
        EditorAtalhosIniciaisDialog(
            configuracoes = atalhosIniciais,
            carregando = carregandoAtalhos,
            salvando = salvandoAtalho,
            onDismiss = {
                if (!salvandoAtalho) {
                    showAtalhosDialog = false
                }
            },
            onSalvar = { configuracao ->
                salvandoAtalho = true

                FirebaseRepository.salvarAtalhoInicial(
                    configuracao = configuracao,
                    responsavel =
                    nomeCatequistaLogado,
                    onSuccess = {
                        salvandoAtalho = false

                        Toast.makeText(
                            context,
                            "Tela inicial atualizada.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = { erro ->
                        salvandoAtalho = false

                        Toast.makeText(
                            context,
                            erro.message
                                ?: "Não foi possível atualizar a tela inicial.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        )
    }

    if (showTurmasPopup && possuiPermissaoTotal) {
        val titTurma = when {
            modoCriarTurma ->
                "Nova turma Adulta"

            idTurmaSelecionada != null ->
                nomeTurmaSelecionada
                    ?.let { "Turma: $it" }
                    ?: "Gerenciar turma"

            else ->
                "Gerenciar turmas adulta"
        }

        CustomPopupAdulta(
            title = titTurma,
            onDismiss = {
                showTurmasPopup = false
                modoCriarTurma = false
                novoNomeTurma = ""
                novoNomeCrismando = ""
                idTurmaSelecionada = null
                nomeTurmaSelecionada = null
            }
        ) {
            if (modoCriarTurma) {
                item {
                    BotaoVoltarGestaoAdulto(
                        texto = "Voltar para as turmas",
                        onClick = {
                            modoCriarTurma = false
                            novoNomeTurma = ""
                        }
                    )

                    MensagemOrientacaoGestaoAdulto(
                        titulo = "Criar nova turma",
                        descricao =
                        "Informe um nome claro para identificar a turma.",
                        icone = Icons.Outlined.Add
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F8F8)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFFEEEEEE)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            OutlinedTextField(
                                value = novoNomeTurma,
                                onValueChange = {
                                    novoNomeTurma = it.take(40)
                                },
                                label = {
                                    Text("Nome da turma")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                        Icons.Outlined.Groups,
                                        contentDescription = null,
                                        tint = Crisma_Primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor =
                                    Color(0xFFFBFBFB),
                                    unfocusedContainerColor =
                                    Color(0xFFFBFBFB),
                                    focusedBorderColor =
                                    Crisma_Primary,
                                    unfocusedBorderColor =
                                    Color(0xFFE2E2E2),
                                    cursorColor =
                                    Crisma_Primary
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val nomeTratado =
                                        novoNomeTurma.trim()

                                    if (nomeTratado.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Digite o nome da turma.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        FirebaseRepository
                                            .criarTurma(
                                                nome = nomeTratado,
                                                categoria = "adulta",
                                                onSuccess = {
                                                        turmaCriada ->
                                                    Toast.makeText(
                                                        context,
                                                        "Turma criada: ${turmaCriada.id}",
                                                        Toast.LENGTH_LONG
                                                    ).show()

                                                    novoNomeTurma = ""
                                                    modoCriarTurma =
                                                        false
                                                },
                                                onError = { erro ->
                                                    Toast.makeText(
                                                        context,
                                                        erro.message
                                                            ?: "Erro ao criar a turma.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                    Crisma_Primary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(modifier = Modifier.width(7.dp))

                                Text(
                                    text = "Criar turma",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else if (idTurmaSelecionada == null) {
                item {
                    MensagemOrientacaoGestaoAdulto(
                        titulo = "Gerencie as turmas",
                        descricao =
                        "Abra uma turma para administrar crismandos e documentos.",
                        icone = Icons.Outlined.Groups
                    )
                }

                items(listaTurmasFirestore) { turma ->
                    val quantidadeDaTurma =
                        listaCrismandosFirestore.count {
                            it.turmaId == turma.id
                        }

                    CardTurmaAdministrativaAdulto(
                        nome = turma.nome,
                        quantidadeCrismandos =
                        quantidadeDaTurma,
                        onAbrir = {
                            idTurmaSelecionada =
                                turma.id
                            nomeTurmaSelecionada =
                                turma.nome
                        },
                        onExcluir = {
                            idTurmaParaExcluir =
                                turma.id
                            nomeTurmaParaExcluir =
                                turma.nome
                        }
                    )
                }

                if (listaTurmasFirestore.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma turma cadastrada.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            modoCriarTurma = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                            Crisma_Primary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Nova turma",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                item {
                    BotaoVoltarGestaoAdulto(
                        texto = "Voltar para as turmas",
                        onClick = {
                            idTurmaSelecionada = null
                            nomeTurmaSelecionada = null
                            novoNomeCrismando = ""
                        }
                    )

                    MensagemOrientacaoGestaoAdulto(
                        titulo =
                        nomeTurmaSelecionada
                            ?: "Crismandos da turma",
                        descricao =
                        "Adicione crismandos e gerencie documentos ou arquivamentos.",
                        icone = Icons.Outlined.Groups
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F8F8)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFFEEEEEE)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment =
                                Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector =
                                    Icons.Outlined.PersonAdd,
                                    contentDescription = null,
                                    tint = Crisma_Primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Column {
                                    Text(
                                        text = "Adicionar crismando",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight =
                                        FontWeight.Bold
                                    )

                                    Text(
                                        text =
                                        "O código de matrícula será criado automaticamente.",
                                        color = Color(0xFF666666),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(9.dp))

                            OutlinedTextField(
                                value = novoNomeCrismando,
                                onValueChange = {
                                    novoNomeCrismando =
                                        it.take(60)
                                },
                                label = {
                                    Text("Nome do crismando")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                        Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = Crisma_Primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor =
                                    Color(0xFFFBFBFB),
                                    unfocusedContainerColor =
                                    Color(0xFFFBFBFB),
                                    focusedBorderColor =
                                    Crisma_Primary,
                                    unfocusedBorderColor =
                                    Color(0xFFE2E2E2),
                                    cursorColor =
                                    Crisma_Primary
                                )
                            )

                            Spacer(modifier = Modifier.height(9.dp))

                            Button(
                                onClick = {
                                    val nomeTratado =
                                        novoNomeCrismando.trim()

                                    if (nomeTratado.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Digite o nome do crismando.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        FirebaseRepository
                                            .criarCrismando(
                                                nome = nomeTratado,
                                                turmaId =
                                                idTurmaSelecionada!!,
                                                categoria =
                                                "adulta",
                                                onSuccess = {
                                                        crismandoCriado ->
                                                    Toast.makeText(
                                                        context,
                                                        "Adicionado. Código: ${crismandoCriado.obterMatricula()}",
                                                        Toast.LENGTH_LONG
                                                    ).show()

                                                    novoNomeCrismando =
                                                        ""
                                                },
                                                onError = { erro ->
                                                    Toast.makeText(
                                                        context,
                                                        erro.message
                                                            ?: "Erro ao cadastrar o crismando.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                    Crisma_Primary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector =
                                    Icons.Outlined.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(modifier = Modifier.width(7.dp))

                                Text(
                                    text = "Adicionar crismando",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                items(listaCrismandosFirestore) { crismando ->
                    CardCrismandoAdministrativoAdulto(
                        nome = crismando.nome,
                        matricula = crismando.id,
                        onDocumentos = {
                            crismandoDocumentosSelecionado =
                                crismando
                            abaDocumentosSelecionada =
                                PerfilDocumentacao.CRISMANDO
                            responsavelDocumentosInput =
                                ""
                            showDocumentosDialog =
                                true
                        },
                        onArquivar = {
                            crismandoParaArquivar =
                                crismando
                            motivoArquivamentoInput =
                                ""
                            responsavelArquivamentoInput =
                                ""
                        }
                    )
                }

                if (listaCrismandosFirestore.isEmpty()) {
                    item {
                        Text(
                            text =
                            "Nenhum crismando ativo nesta turma.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showFrequenciaPopup) {
        val titFreq = when {
            encontroSelecionado != null ->
                "Encontro $encontroSelecionado - ${
                    listaEncontrosFirestore
                        .firstOrNull {
                            it.numero == encontroSelecionado
                        }
                        ?.dataManual
                        .orEmpty()
                }"

            idTurmaSelecionada != null ->
                "Encontros: $nomeTurmaSelecionada"

            else ->
                "Frequência - selecione a turma"
        }

        CustomPopupAdulta(
            title = titFreq,
            onDismiss = {
                showFrequenciaPopup = false
                idTurmaSelecionada = null
                nomeTurmaSelecionada = null
                encontroSelecionado = null
                idEncontroEmEdicao = null
                modoEdicaoFrequencia = false
            }
        ) {
            if (idTurmaSelecionada == null) {
                item {
                    MensagemOrientacaoGestaoAdulto(
                        titulo = "Selecione uma turma",
                        descricao =
                        "Escolha a turma para consultar encontros e preencher a frequência.",
                        icone = Icons.Outlined.Groups
                    )
                }

                items(listaTurmasFirestore) { turma ->
                    CardSelecaoGestaoAdulto(
                        titulo = turma.nome,
                        descricao =
                        "Abrir encontros e chamada desta turma",
                        icone = Icons.Outlined.Groups,
                        onClick = {
                            idTurmaSelecionada = turma.id
                            nomeTurmaSelecionada = turma.nome
                        }
                    )
                }

                if (listaTurmasFirestore.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma turma cadastrada.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (encontroSelecionado == null) {
                item {
                    BotaoVoltarGestaoAdulto(
                        texto = "Voltar para as turmas",
                        onClick = {
                            idTurmaSelecionada = null
                            nomeTurmaSelecionada = null
                        }
                    )

                    MensagemOrientacaoGestaoAdulto(
                        titulo = "Encontros da turma",
                        descricao =
                        "Selecione um encontro para abrir a chamada ou adicione uma nova data.",
                        icone = Icons.Outlined.DateRange
                    )
                }

                items(listaEncontrosFirestore) { encontro ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F8F8)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFFEEEEEE)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 9.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DateRange,
                                contentDescription = null,
                                tint = Crisma_Primary,
                                modifier = Modifier.size(21.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        encontroSelecionado =
                                            encontro.numero
                                    }
                            ) {
                                Text(
                                    text = "Encontro ${encontro.numero}",
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = encontro.dataManual,
                                    color = Color(0xFF666666),
                                    fontSize = 10.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    idEncontroEmEdicao = encontro.id
                                    dataEncontroEdicaoInput =
                                        encontro.dataManual
                                            .filter { it.isDigit() }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Editar data",
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    idEncontroParaExcluir = encontro.id
                                    numeroEncontroParaExcluir =
                                        encontro.numero
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription =
                                    "Excluir encontro",
                                    tint = Crisma_Primary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    if (idEncontroEmEdicao == encontro.id) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFFBE8)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Crisma_Gold
                            ),
                            elevation = CardDefaults.cardElevation(0.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Editar data do encontro",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(7.dp))

                                OutlinedTextField(
                                    value = dataEncontroEdicaoInput,
                                    onValueChange = { novoValor ->
                                        val puros =
                                            novoValor.filter {
                                                it.isDigit()
                                            }

                                        if (
                                            puros.length <= 8 &&
                                            validarDigitosDataAdulta(puros)
                                        ) {
                                            dataEncontroEdicaoInput =
                                                puros
                                        }
                                    },
                                    label = {
                                        Text("Nova data")
                                    },
                                    placeholder = {
                                        Text("DDMMAAAA")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation =
                                    visualTransformationData,
                                    shape = RoundedCornerShape(10.dp),
                                    colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor =
                                        Color(0xFFFBFBFB),
                                        unfocusedContainerColor =
                                        Color(0xFFFBFBFB),
                                        focusedBorderColor =
                                        Crisma_Primary,
                                        unfocusedBorderColor =
                                        Color(0xFFE2E2E2),
                                        cursorColor =
                                        Crisma_Primary
                                    )
                                )

                                Spacer(modifier = Modifier.height(7.dp))

                                Button(
                                    onClick = {
                                        if (
                                            dataEncontroEdicaoInput
                                                .length == 8
                                        ) {
                                            val anoInserido =
                                                dataEncontroEdicaoInput
                                                    .substring(4, 8)
                                                    .toIntOrNull()
                                                    ?: 0

                                            if (anoInserido < 2026) {
                                                Toast.makeText(
                                                    context,
                                                    "O ano não pode ser menor que 2026",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                val dataComBarras =
                                                    StringBuilder(
                                                        dataEncontroEdicaoInput
                                                    )
                                                        .insert(2, "/")
                                                        .insert(5, "/")
                                                        .toString()

                                                FirebaseRepository
                                                    .atualizarDataEncontro(
                                                        encontroId =
                                                        encontro.id,
                                                        dataManual =
                                                        dataComBarras,
                                                        onSuccess = {
                                                            Toast
                                                                .makeText(
                                                                    context,
                                                                    "Data atualizada",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()

                                                            idEncontroEmEdicao =
                                                                null
                                                        },
                                                        onError = {
                                                                erro ->
                                                            Toast
                                                                .makeText(
                                                                    context,
                                                                    erro.message
                                                                        ?: "Erro ao atualizar a data.",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                        }
                                                    )
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Digite os 8 números da data",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                        Crisma_Primary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector =
                                        Icons.Outlined.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )

                                    Spacer(
                                        modifier = Modifier.width(7.dp)
                                    )

                                    Text(
                                        text = "Salvar nova data",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F8F8)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFFEEEEEE)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment =
                                Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    tint = Crisma_Primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Column {
                                    Text(
                                        text = "Adicionar encontro",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text =
                                        "Informe a data do próximo encontro.",
                                        color = Color(0xFF666666),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(9.dp))

                            OutlinedTextField(
                                value = novaDataEncontroInput,
                                onValueChange = { novoValor ->
                                    val puros =
                                        novoValor.filter {
                                            it.isDigit()
                                        }

                                    if (
                                        puros.length <= 8 &&
                                        validarDigitosDataAdulta(puros)
                                    ) {
                                        novaDataEncontroInput = puros
                                    }
                                },
                                label = {
                                    Text("Data do encontro")
                                },
                                placeholder = {
                                    Text("DDMMAAAA")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation =
                                visualTransformationData,
                                shape = RoundedCornerShape(10.dp),
                                colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor =
                                    Color(0xFFFBFBFB),
                                    unfocusedContainerColor =
                                    Color(0xFFFBFBFB),
                                    focusedBorderColor =
                                    Crisma_Primary,
                                    unfocusedBorderColor =
                                    Color(0xFFE2E2E2),
                                    cursorColor =
                                    Crisma_Primary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (
                                        novaDataEncontroInput.length == 8
                                    ) {
                                        val anoInserido =
                                            novaDataEncontroInput
                                                .substring(4, 8)
                                                .toIntOrNull()
                                                ?: 0

                                        if (anoInserido < 2026) {
                                            Toast.makeText(
                                                context,
                                                "O ano não pode ser menor que 2026",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            val proximoNumero =
                                                (
                                                        listaEncontrosFirestore
                                                            .maxOfOrNull {
                                                                it.numero
                                                            }
                                                            ?: 0
                                                        ) + 1

                                            val dataComBarras =
                                                StringBuilder(
                                                    novaDataEncontroInput
                                                )
                                                    .insert(2, "/")
                                                    .insert(5, "/")
                                                    .toString()

                                            FirebaseRepository
                                                .salvarEncontro(
                                                    turmaId =
                                                    idTurmaSelecionada!!,
                                                    numero =
                                                    proximoNumero,
                                                    dataManual =
                                                    dataComBarras,
                                                    onSuccess = {
                                                        Toast.makeText(
                                                            context,
                                                            "Encontro $proximoNumero adicionado",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        novaDataEncontroInput =
                                                            ""
                                                    },
                                                    onError = {
                                                            erro ->
                                                        Toast.makeText(
                                                            context,
                                                            erro.message
                                                                ?: "Erro ao adicionar o encontro.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                )
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Digite a data completa com 8 números",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                    Crisma_Primary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )

                                Spacer(modifier = Modifier.width(7.dp))

                                Text(
                                    text = "Adicionar encontro",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    BotaoVoltarGestaoAdulto(
                        texto = "Voltar para os encontros",
                        onClick = {
                            encontroSelecionado = null
                            modoEdicaoFrequencia = false
                        }
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor =
                            if (modoEdicaoFrequencia) {
                                Color(0xFFFFF8F8)
                            } else {
                                Color(0xFFF8F8F8)
                            }
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color =
                            if (modoEdicaoFrequencia) {
                                Crisma_Primary.copy(alpha = 0.55f)
                            } else {
                                Color(0xFFEEEEEE)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 9.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = Crisma_Primary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(9.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text =
                                    if (modoEdicaoFrequencia) {
                                        "Preenchimento ativado"
                                    } else {
                                        "Ative para preencher"
                                    },
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text =
                                    if (modoEdicaoFrequencia) {
                                        "Marque a situação de cada crismando."
                                    } else {
                                        "Os registros estão somente para consulta."
                                    },
                                    color = Color(0xFF666666),
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp
                                )
                            }

                            Switch(
                                checked = modoEdicaoFrequencia,
                                onCheckedChange = {
                                    modoEdicaoFrequencia = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Crisma_Primary,
                                    uncheckedThumbColor =
                                    Color(0xFF888888),
                                    uncheckedTrackColor =
                                    Color(0xFFE5E5E5),
                                    uncheckedBorderColor =
                                    Color(0xFFD5D5D5)
                                )
                            )
                        }
                    }
                }

                items(listaCrismandosFirestore) { crismando ->
                    val chaveMap =
                        "Jov_${idTurmaSelecionada}_${encontroSelecionado}_${crismando.id}"

                    val status =
                        frequenciaPorEncontro[chaveMap]
                            ?: StatusFrequencia.NENHUM

                    val statusTexto = when (status) {
                        StatusFrequencia.PRESENTE -> "Presente"
                        StatusFrequencia.FALTA -> "Falta"
                        StatusFrequencia.JUSTIFICADA ->
                            "Justificada"

                        StatusFrequencia.NENHUM ->
                            "Não preenchido"
                    }

                    val statusCor = when (status) {
                        StatusFrequencia.PRESENTE ->
                            Color(0xFF333333)

                        StatusFrequencia.FALTA ->
                            Crisma_Primary

                        StatusFrequencia.JUSTIFICADA ->
                            Color(0xFF8A6D00)

                        StatusFrequencia.NENHUM ->
                            Color(0xFF777777)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F8F8)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFFEEEEEE)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(11.dp)
                        ) {
                            Row(
                                verticalAlignment =
                                Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector =
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Crisma_Primary,
                                    modifier = Modifier.size(19.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Text(
                                    text = crismando.nome,
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                Surface(
                                    color =
                                    statusCor.copy(alpha = 0.10f),
                                    shape =
                                    RoundedCornerShape(20.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        statusCor.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Text(
                                        text = statusTexto,
                                        color = statusCor,
                                        fontSize = 9.sp,
                                        fontWeight =
                                        FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(9.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                Arrangement.spacedBy(5.dp)
                            ) {
                                BotaoStatusFrequenciaAdulto(
                                    texto = "PRESENTE",
                                    selecionado =
                                    status ==
                                            StatusFrequencia.PRESENTE,
                                    habilitado =
                                    modoEdicaoFrequencia,
                                    corSelecionada =
                                    Color(0xFF333333),
                                    corTextoSelecionado =
                                    Color.White,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        frequenciaPorEncontro[
                                            chaveMap
                                        ] =
                                            StatusFrequencia.PRESENTE
                                    }
                                )

                                BotaoStatusFrequenciaAdulto(
                                    texto = "FALTA",
                                    selecionado =
                                    status ==
                                            StatusFrequencia.FALTA,
                                    habilitado =
                                    modoEdicaoFrequencia,
                                    corSelecionada =
                                    Crisma_Primary,
                                    corTextoSelecionado =
                                    Color.White,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        frequenciaPorEncontro[
                                            chaveMap
                                        ] =
                                            StatusFrequencia.FALTA
                                    }
                                )

                                BotaoStatusFrequenciaAdulto(
                                    texto = "JUST.",
                                    selecionado =
                                    status ==
                                            StatusFrequencia.JUSTIFICADA,
                                    habilitado =
                                    modoEdicaoFrequencia,
                                    corSelecionada =
                                    Crisma_Gold,
                                    corTextoSelecionado =
                                    Color.Black,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        frequenciaPorEncontro[
                                            chaveMap
                                        ] =
                                            StatusFrequencia
                                                .JUSTIFICADA
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (
                                listaCrismandosFirestore.isEmpty()
                            ) {
                                Toast.makeText(
                                    context,
                                    "Não há crismandos cadastrados nesta turma.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                var operacoesConcluidas = 0
                                var ocorreuErro = false

                                listaCrismandosFirestore
                                    .forEach { crismando ->
                                        val chaveMap =
                                            "Jov_${idTurmaSelecionada}_${encontroSelecionado}_${crismando.id}"

                                        val statusParaSalvar =
                                            frequenciaPorEncontro[
                                                chaveMap
                                            ]
                                                ?: StatusFrequencia
                                                    .NENHUM

                                        FirebaseRepository
                                            .salvarFrequencia(
                                                turmaId =
                                                idTurmaSelecionada!!,
                                                encontro =
                                                encontroSelecionado!!,
                                                alunoId =
                                                crismando.id,
                                                status =
                                                statusParaSalvar,
                                                onSuccess = {
                                                    operacoesConcluidas++

                                                    if (
                                                        operacoesConcluidas ==
                                                        listaCrismandosFirestore
                                                            .size &&
                                                        !ocorreuErro
                                                    ) {
                                                        Toast.makeText(
                                                            context,
                                                            "Chamada de Adultos salva com sucesso",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        modoEdicaoFrequencia =
                                                            false

                                                        showFrequenciaPopup =
                                                            false
                                                    }
                                                },
                                                onError = {
                                                        erro ->
                                                    operacoesConcluidas++
                                                    ocorreuErro = true

                                                    Toast.makeText(
                                                        context,
                                                        erro.message
                                                            ?: "Erro ao salvar a frequência.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                    }
                            }
                        },
                        enabled = modoEdicaoFrequencia,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Crisma_Primary,
                            disabledContainerColor =
                            Color(0xFFE9E9E9),
                            disabledContentColor =
                            Color(0xFF999999)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Salvar frequência",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showFinanceiroPopup) {
        val tituloFinanceiro = when {
            parcelaSelecionadaFinanceira != null ->
                "Registrar parcela $parcelaSelecionadaFinanceira"

            crismandoSelecionado != null ->
                "Parcelas: $nomeCrismandoSelecionadoFixo"

            idTurmaSelecionada != null ->
                "Financeiro: $nomeTurmaSelecionada"

            else ->
                "Financeiro Adultos"
        }

        CustomPopupAdulta(
            title = tituloFinanceiro,
            onDismiss = {
                showFinanceiroPopup = false
                idTurmaSelecionada = null
                nomeTurmaSelecionada = null
                crismandoSelecionado = null
                parcelaSelecionadaFinanceira = null
            }
        ) {
            if (idTurmaSelecionada == null) {
                item {
                    MensagemOrientacaoGestaoAdulto(
                        titulo = "Selecione uma turma",
                        descricao =
                        "Escolha a turma para consultar contribuições e registrar pagamentos.",
                        icone = Icons.Outlined.Payments
                    )
                }

                items(listaTurmasFirestore) { turma ->
                    CardSelecaoGestaoAdulto(
                        titulo = turma.nome,
                        descricao =
                        "Abrir crismandos e parcelas desta turma",
                        icone = Icons.Outlined.Payments,
                        onClick = {
                            idTurmaSelecionada = turma.id
                            nomeTurmaSelecionada = turma.nome
                        }
                    )
                }

                if (listaTurmasFirestore.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma turma cadastrada.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (crismandoSelecionado == null) {
                item {
                    BotaoVoltarGestaoAdulto(
                        texto = "Voltar para as turmas",
                        onClick = {
                            idTurmaSelecionada = null
                            nomeTurmaSelecionada = null
                        }
                    )

                    MensagemOrientacaoGestaoAdulto(
                        titulo = "Selecione o crismando",
                        descricao =
                        "Escolha quem terá as parcelas consultadas ou atualizadas.",
                        icone = Icons.Outlined.Person
                    )
                }

                items(listaCrismandosFirestore) { aluno ->
                    CardSelecaoGestaoAdulto(
                        titulo = aluno.nome,
                        descricao = "Matrícula: ${aluno.matricula}",
                        icone = Icons.Outlined.Person,
                        onClick = {
                            crismandoSelecionado = aluno.id
                            nomeCrismandoSelecionadoFixo =
                                aluno.nome
                        }
                    )
                }

                if (listaCrismandosFirestore.isEmpty()) {
                    item {
                        Text(
                            text =
                            "Nenhum crismando ativo nesta turma.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (parcelaSelecionadaFinanceira == null) {
                val totalPagas =
                    listaParcelasFinanceiras.count {
                        it.statusPago
                    }

                item {
                    BotaoVoltarGestaoAdulto(
                        texto = "Voltar para os crismandos",
                        onClick = {
                            crismandoSelecionado = null
                        }
                    )

                    ResumoFinanceiroAlunoAdulto(
                        nome = nomeCrismandoSelecionadoFixo,
                        totalPagas = totalPagas,
                        totalParcelas = 12
                    )
                }

                items((1..12).toList()) { numeroParcela ->
                    val parcelaExistente =
                        listaParcelasFinanceiras
                            .firstOrNull {
                                it.numeroParcela ==
                                        numeroParcela
                            }

                    val parcelaPaga =
                        parcelaExistente?.statusPago == true

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor =
                            if (parcelaPaga) {
                                Color(0xFFFFFBE8)
                            } else {
                                Color(0xFFF8F8F8)
                            }
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color =
                            if (parcelaPaga) {
                                Crisma_Gold.copy(alpha = 0.75f)
                            } else {
                                Color(0xFFEEEEEE)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 10.dp
                                ),
                            verticalAlignment =
                            Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                if (parcelaPaga) {
                                    Icons.Outlined.CheckCircle
                                } else {
                                    Icons.Outlined.Payments
                                },
                                contentDescription = null,
                                tint =
                                if (parcelaPaga) {
                                    Color(0xFF8A6D00)
                                } else {
                                    Crisma_Primary
                                },
                                modifier = Modifier.size(21.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Parcela $numeroParcela",
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text =
                                    if (parcelaPaga) {
                                        "Pagamento confirmado"
                                    } else {
                                        "Aguardando pagamento"
                                    },
                                    color = Color(0xFF666666),
                                    fontSize = 10.sp
                                )
                            }

                            if (parcelaPaga) {
                                Button(
                                    onClick = {
                                        nomeCatequistaPagamento =
                                            parcelaExistente
                                                ?.recebidoPor
                                                ?: "Não identificado"

                                        showPagamentoInfoDialog =
                                            true
                                    },
                                    colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                        Crisma_Gold,
                                        contentColor =
                                        Color.Black
                                    ),
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp
                                    ),
                                    shape = RoundedCornerShape(9.dp)
                                ) {
                                    Text(
                                        text = "PAGO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        parcelaSelecionadaFinanceira =
                                            numeroParcela
                                    },
                                    colors =
                                    ButtonDefaults
                                        .outlinedButtonColors(
                                            containerColor =
                                            Color(0xFFFFF8F8),
                                            contentColor =
                                            Crisma_Primary
                                        ),
                                    border = BorderStroke(
                                        1.dp,
                                        Crisma_Primary.copy(
                                            alpha = 0.55f
                                        )
                                    ),
                                    contentPadding = PaddingValues(
                                        horizontal = 11.dp
                                    ),
                                    shape = RoundedCornerShape(9.dp)
                                ) {
                                    Text(
                                        text = "Registrar",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    val alunoNomeFixo =
                        nomeCrismandoSelecionadoFixo

                    val parcelaFixa =
                        parcelaSelecionadaFinanceira!!

                    BotaoVoltarGestaoAdulto(
                        texto = "Voltar para as parcelas",
                        onClick = {
                            parcelaSelecionadaFinanceira =
                                null
                        }
                    )

                    MensagemOrientacaoGestaoAdulto(
                        titulo =
                        "Registrar pagamento da parcela $parcelaFixa",
                        descricao =
                        "Confira os dados antes de confirmar o recebimento.",
                        icone = Icons.Outlined.Payments
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F8F8)
                        ),
                        border = BorderStroke(
                            1.dp,
                            Color(0xFFEEEEEE)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment =
                                Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector =
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Crisma_Primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Column {
                                    Text(
                                        text = alunoNomeFixo,
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Parcela $parcelaFixa",
                                        color = Color(0xFF666666),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = nomeCatequistaLogado,
                                onValueChange = {},
                                label = {
                                    Text("Recebido por")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                        Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = Crisma_Primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                readOnly = true,
                                shape = RoundedCornerShape(10.dp),
                                colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor =
                                    Color(0xFFFBFBFB),
                                    unfocusedContainerColor =
                                    Color(0xFFFBFBFB),
                                    focusedBorderColor =
                                    Crisma_Primary,
                                    unfocusedBorderColor =
                                    Color(0xFFE2E2E2),
                                    focusedTextColor =
                                    Color(0xFF333333),
                                    unfocusedTextColor =
                                    Color(0xFF333333)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    showAlertaFinanceiroEtapa1 =
                                        true
                                },
                                enabled =
                                nomeCatequistaLogado
                                    .isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                    Crisma_Primary,
                                    disabledContainerColor =
                                    Color(0xFFE9E9E9)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector =
                                    Icons.Outlined.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Text(
                                    text = "Confirmar recebimento",
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
                    Card(onClick = { idTurmaSelecionada = turma.id; nomeTurmaSelecionada = turma.nome }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF0F0F0)), shape = RoundedCornerShape(10.dp)) {
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
                                .background(Color(0xFFF7F7F7), shape = RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF7F7F7), shape = RoundedCornerShape(10.dp))
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

                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(10.dp)) {
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
                            MensagemOrientacaoAvisoAdulto(
                                titulo = "Escolha onde deseja publicar",
                                descricao = "Selecione o grupo que deverá receber este aviso."
                            )

                            Spacer(modifier = Modifier.height(2.dp))
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

                    item {
                        MensagemOrientacaoAvisoAdulto(
                            titulo = "Selecione a turma",
                            descricao = "Escolha a turma que deseja enviar o aviso."
                        )
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
                            shape = RoundedCornerShape(10.dp)
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
                            shape = RoundedCornerShape(10.dp)
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
                                                "Aviso publicado com sucesso",
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
                            shape = RoundedCornerShape(10.dp)
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
                val cadastro = if (
                    abaDocumentosSelecionada == PerfilDocumentacao.CRISMANDO
                ) {
                    cadastroDocumentosCrismando
                } else {
                    cadastroDocumentosPadrinho.copy(
                        crismaPossui = true,
                        primeiraComunhaoPossui = false,
                        primeiraComunhaoEntregue = false,
                        batismoEntregue = false
                    )
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
                            Toast.makeText(context, "Documentos salvos", Toast.LENGTH_SHORT).show()
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

    if (showSobreNosDialog) {
        ConteudoInstitucionalDialog(
            configuracao = sobreInicial,
            botaoTexto = "Entendido",
            onDismiss = { showSobreNosDialog = false }
        )
    }

    if (showContatosDialog) {
        ConteudoInstitucionalDialog(
            configuracao = contatosInicial,
            botaoTexto = "Fechar",
            onDismiss = { showContatosDialog = false }
        )
    }

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
                                        "Encontro e frequências removidos",
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
                        shape = RoundedCornerShape(10.dp)
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
                        shape = RoundedCornerShape(10.dp)
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
                                        "Aviso removido com sucesso",
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
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Confirmar Exclusão", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    if (showPagamentoInfoDialog) {
        AlertDialog(
            onDismissRequest = {
                showPagamentoInfoDialog = false
            },
            containerColor = Color(0xFFF8F8F8),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF8A6D00)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPagamentoInfoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Crisma_Primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Fechar")
                }
            },
            title = {
                Text(
                    text = "Pagamento registrado",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text =
                    "Esta parcela foi recebida por:\n\n$nomeCatequistaPagamento",
                    color = Color(0xFF444444)
                )
            }
        )
    }

    if (showAlertaFinanceiroEtapa1) {
        AlertDialog(
            onDismissRequest = {
                showAlertaFinanceiroEtapa1 = false
            },
            containerColor = Color(0xFFF8F8F8),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Crisma_Primary
                )
            },
            title = {
                Text(
                    text = "Registro permanente",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text =
                    "Após o envio, esta informação não poderá ser editada ou excluída pelo aplicativo.",
                    color = Color(0xFF444444)
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAlertaFinanceiroEtapa1 = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = Color(0xFF666666)
                    )
                }
            },
            confirmButton = {
                AnimatedVisibility(
                    visible =
                    liberarBotaoFinanceiroEtapa1
                ) {
                    Button(
                        onClick = {
                            showAlertaFinanceiroEtapa1 =
                                false

                            showAlertaFinanceiroEtapa2 =
                                true
                        },
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                            Crisma_Primary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Estou ciente",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }

    if (showAlertaFinanceiroEtapa2) {
        val alunoIdSalvar =
            crismandoSelecionado ?: ""

        val parcelaSalvar =
            parcelaSelecionadaFinanceira ?: 0

        AlertDialog(
            onDismissRequest = {
                showAlertaFinanceiroEtapa2 = false
            },
            containerColor = Color(0xFFF8F8F8),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = Crisma_Primary
                )
            },
            title = {
                Text(
                    text = "Confirmar recebimento",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text =
                    "Confirma o recebimento sob responsabilidade de \"$nomeCatequistaLogado\"?",
                    color = Color(0xFF444444)
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAlertaFinanceiroEtapa2 = false
                    }
                ) {
                    Text(
                        text = "Voltar",
                        color = Color(0xFF666666)
                    )
                }
            },
            confirmButton = {
                AnimatedVisibility(
                    visible =
                    liberarBotaoFinanceiroEtapa2
                ) {
                    Button(
                        onClick = {
                            FirebaseRepository.salvarPagamento(
                                turmaId =
                                idTurmaSelecionada!!,
                                alunoId =
                                alunoIdSalvar,
                                parcela =
                                parcelaSalvar,
                                recebidoPor =
                                nomeCatequistaLogado,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Parcela registrada no Firebase",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    showAlertaFinanceiroEtapa2 =
                                        false

                                    parcelaSelecionadaFinanceira =
                                        null

                                    catequistaResponsavelInput =
                                        nomeCatequistaLogado

                                    crismandoSelecionado =
                                        null
                                },
                                onError = { erro ->
                                    Toast.makeText(
                                        context,
                                        erro.message
                                            ?: "Erro ao registrar a parcela.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        },
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                            Crisma_Primary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Confirmar e gravar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }

}



@Composable
private fun MensagemOrientacaoGestaoAdulto(
    titulo: String,
    descricao: String,
    icone: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8F8)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Crisma_Primary.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = Crisma_Primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    color = Color.Black,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = descricao,
                    color = Color(0xFF666666),
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CardSelecaoGestaoAdulto(
    titulo: String,
    descricao: String,
    icone: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 70.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFEEEEEE)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 13.dp,
                    vertical = 11.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = Crisma_Primary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    color = Color.Black,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = descricao,
                    color = Color(0xFF666666),
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = Crisma_Primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}


@Composable
private fun CardTurmaAdministrativaAdulto(
    nome: String,
    quantidadeCrismandos: Int,
    onAbrir: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFEEEEEE)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = null,
                    tint = Crisma_Primary,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = nome,
                        color = Color.Black,
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text =
                        if (quantidadeCrismandos == 1) {
                            "1 crismando ativo"
                        } else {
                            "$quantidadeCrismandos crismandos ativos"
                        },
                        color = Color(0xFF666666),
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAbrir,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFFF8F8),
                        contentColor = Crisma_Primary
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Crisma_Primary.copy(alpha = 0.45f)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Abrir turma",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onExcluir,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFAFAFA),
                        contentColor = Crisma_Primary
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFFE2E2E2)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Excluir",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CardCrismandoAdministrativoAdulto(
    nome: String,
    matricula: String,
    onDocumentos: () -> Unit,
    onArquivar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFEEEEEE)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Crisma_Primary,
                    modifier = Modifier.size(21.dp)
                )

                Spacer(modifier = Modifier.width(9.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = nome,
                        color = Color.Black,
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Matrícula: $matricula",
                        color = Color(0xFF666666),
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDocumentos,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFFF8F8),
                        contentColor = Crisma_Primary
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Crisma_Primary.copy(alpha = 0.45f)
                    ),
                    contentPadding = PaddingValues(horizontal = 7.dp),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = "Documentos",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onArquivar,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFAFAFA),
                        contentColor = Crisma_Primary
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFFE2E2E2)
                    ),
                    contentPadding = PaddingValues(horizontal = 7.dp),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Archive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = "Arquivar",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BotaoVoltarGestaoAdulto(
    texto: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = 4.dp,
            vertical = 2.dp
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.ArrowBack,
            contentDescription = null,
            tint = Crisma_Primary,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = texto,
            color = Crisma_Primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BotaoStatusFrequenciaAdulto(
    texto: String,
    selecionado: Boolean,
    habilitado: Boolean,
    corSelecionada: Color,
    corTextoSelecionado: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val fundo = if (selecionado) {
        corSelecionada
    } else {
        Color(0xFFFAFAFA)
    }

    val textoCor = if (selecionado) {
        corTextoSelecionado
    } else {
        corSelecionada
    }

    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = fundo,
            contentColor = textoCor,
            disabledContainerColor = fundo.copy(
                alpha = if (selecionado) 0.65f else 0.75f
            ),
            disabledContentColor = textoCor.copy(alpha = 0.65f)
        ),
        border = BorderStroke(
            width = if (selecionado) 1.5.dp else 1.dp,
            color = corSelecionada.copy(
                alpha = if (habilitado) 0.65f else 0.30f
            )
        ),
        shape = RoundedCornerShape(9.dp)
    ) {
        Text(
            text = texto,
            fontSize = 9.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ResumoFinanceiroAlunoAdulto(
    nome: String,
    totalPagas: Int,
    totalParcelas: Int
) {
    val pendentes = (totalParcelas - totalPagas)
        .coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFEEEEEE)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Crisma_Primary,
                    modifier = Modifier.size(21.dp)
                )

                Spacer(modifier = Modifier.width(9.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = nome,
                        color = Color.Black,
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Resumo das contribuições",
                        color = Color(0xFF666666),
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFFBE8)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Crisma_Gold.copy(alpha = 0.75f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment =
                        Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = totalPagas.toString(),
                            color = Color(0xFF7A6200),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "Pagas",
                            color = Color(0xFF666666),
                            fontSize = 9.sp
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF7F7)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Crisma_Primary.copy(alpha = 0.35f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment =
                        Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = pendentes.toString(),
                            color = Crisma_Primary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "Pendentes",
                            color = Color(0xFF666666),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MensagemOrientacaoAvisoAdulto(
    titulo: String,
    descricao: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8F8)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Crisma_Primary.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Crisma_Primary,
                modifier = Modifier.size(19.dp)
            )

            Spacer(modifier = Modifier.width(9.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    color = Color.Black,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = descricao,
                    color = Color(0xFF666666),
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
private fun BotaoDestinoAvisoAdulto(
    titulo: String,
    descricao: String,
    cor: Color,
    @Suppress("UNUSED_PARAMETER")
    corTexto: Color,
    onClick: () -> Unit
) {
    val tituloNormalizado = titulo.lowercase()

    val icone = when {
        "geral" in tituloNormalizado ->
            Icons.Outlined.Notifications

        "jovens" in tituloNormalizado ||
                "adultas" in tituloNormalizado ->
            Icons.Outlined.Groups

        else ->
            Icons.Outlined.School
    }

    val corVisivel = if (cor == Crisma_Gold) {
        Color(0xFF8A6D00)
    } else {
        cor
    }

    val fundo = when {
        cor == Crisma_Gold ->
            Color(0xFFFFFBE8)

        cor == Aviso_Blue ->
            Color(0xFFF5F9FE)

        else ->
            Color(0xFFFFF7F7)
    }

    val rotuloDestino = when {
        "geral" in tituloNormalizado ->
            "Toda a paróquia"

        "jovens" in tituloNormalizado ||
                "adultas" in tituloNormalizado ->
            "Categoria completa"

        else ->
            "Turma específica"
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 78.dp),
        colors = CardDefaults.cardColors(
            containerColor = fundo
        ),
        border = BorderStroke(
            width = 1.dp,
            color = corVisivel.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 13.dp,
                    vertical = 11.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = corVisivel,
                modifier = Modifier.size(23.dp)
            )

            Spacer(modifier = Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    color = Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = descricao,
                    color = Color(0xFF5F5F5F),
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = rotuloDestino,
                    color = corVisivel,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = corVisivel,
                modifier = Modifier.size(14.dp)
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

    val fundoAviso = when {
        destino == "GERAL" ->
            Color(0xFFFFF8D1)

        destino == "CATEGORIA_JOVEM" ||
                destino == "CATEGORIA_ADULTA" ||
                destino == "TURMA_JOVEM" ||
                destino == "TURMA_ADULTA" ->
            Color(0xFFE3F2FD)

        else ->
            Color(0xFFFFEBEE)
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
            containerColor = fundoAviso
        ),
        border = BorderStroke(
            width = 1.dp,
            color = corAviso
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = corAviso,
                            shape = RoundedCornerShape(10.dp)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = corAviso,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = aviso.texto,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onExcluir
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Excluir aviso",
                    tint = Crisma_Primary.copy(alpha = 0.75f)
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
                            /*
                             * Todo padrinho precisa ser crismado.
                             *
                             * Por isso, nesta aba não existe mais escolha de
                             * Primeira Comunhão ou Batismo. O único comprovante
                             * sacramental exigido é o de Crisma.
                             */
                            item {
                                DocumentoSwitchLinha(
                                    "Comprovante de Crisma entregue?",
                                    cadastroAtual.crismaEntregue
                                ) { entregue ->
                                    atualizar(
                                        cadastroAtual.copy(
                                            crismaPossui = true,
                                            crismaEntregue = entregue,
                                            primeiraComunhaoPossui = false,
                                            primeiraComunhaoEntregue = false,
                                            batismoEntregue = false
                                        )
                                    )
                                }
                            }
                        } else {
                            /*
                             * As opções de Primeira Comunhão e Batismo
                             * continuam existindo somente para o crismando.
                             */
                            item {
                                EscolhaSimNaoDocumento(
                                    "Possui Primeira Comunhão?",
                                    cadastroAtual.primeiraComunhaoPossui
                                ) { possui ->
                                    atualizar(
                                        cadastroAtual.copy(
                                            primeiraComunhaoPossui = possui,
                                            primeiraComunhaoEntregue =
                                            if (possui) {
                                                cadastroAtual.primeiraComunhaoEntregue
                                            } else {
                                                false
                                            }
                                        )
                                    )
                                }
                            }

                            if (cadastroAtual.primeiraComunhaoPossui) {
                                item {
                                    DocumentoSwitchLinha(
                                        "Comprovante de Primeira Comunhão entregue?",
                                        cadastroAtual.primeiraComunhaoEntregue
                                    ) { entregue ->
                                        atualizar(
                                            cadastroAtual.copy(
                                                primeiraComunhaoEntregue = entregue
                                            )
                                        )
                                    }
                                }
                            } else {
                                item {
                                    DocumentoSwitchLinha(
                                        "Comprovante de Batismo entregue?",
                                        cadastroAtual.batismoEntregue
                                    ) { entregue ->
                                        atualizar(
                                            cadastroAtual.copy(
                                                batismoEntregue = entregue
                                            )
                                        )
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

                HorizontalDivider(color = Color(0xFFF0F0F0))
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
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
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
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
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
fun SmallMenuCardAdulta(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val descricao = when (title) {
        "Frequência" -> "Encontros e presenças"
        "Turmas" -> "Alunos e documentos"
        "Avisos" -> "Comunicados"
        "Financeiro" -> "Parcelas e pagamentos"
        "Links" -> "Acessos da tela inicial"
        "Voltar" -> "Painel do catequista"
        else -> "Gerenciar informações"
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(74.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8),
            disabledContainerColor = Color(0xFFF5F5F5)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFEEEEEE)
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 5.dp,
                    vertical = 7.dp
                ),
            horizontalAlignment =
            Alignment.CenterHorizontally,
            verticalArrangement =
            Arrangement.Center
        ) {
            /*
             * Ícone sem caixa ou margem vermelha ao fundo.
             * O vermelho aparece apenas no próprio desenho.
             */
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Crisma_Primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                color = Color.Black,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = descricao,
                color = Color.Gray,
                fontSize = 8.sp,
                lineHeight = 9.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CustomPopupAdulta(
    title: String,
    onDismiss: () -> Unit,
    content: LazyListScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val tituloNormalizado = title.lowercase()

    val iconeCabecalho = when {
        "frequ" in tituloNormalizado ->
            Icons.Outlined.CheckCircle
        "finance" in tituloNormalizado ->
            Icons.Outlined.Payments
        "aviso" in tituloNormalizado ->
            Icons.Outlined.Notifications
        "turma" in tituloNormalizado ||
                "editar" in tituloNormalizado ->
            Icons.Outlined.Groups
        "document" in tituloNormalizado ->
            Icons.Outlined.Description
        else ->
            Icons.Outlined.ListAlt
    }

    val subtitulo = when {
        "frequ" in tituloNormalizado ->
            "Organize encontros e acompanhe os registros"
        "finance" in tituloNormalizado ->
            "Consulte e atualize parcelas e pagamentos"
        "aviso" in tituloNormalizado ->
            "Crie e acompanhe os comunicados"
        "turma" in tituloNormalizado ||
                "editar" in tituloNormalizado ->
            "Selecione uma turma ou gerencie seus integrantes"
        else ->
            "Selecione ou atualize as informações"
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = screenHeight * 0.42f,
                    max = screenHeight * 0.72f
                ),
            color = Color(0xFFFAFAFA),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = Color(0xFFE8E8E8)
            ),
            tonalElevation = 0.dp,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(
                            horizontal = 16.dp,
                            vertical = 14.dp
                        ),
                    verticalAlignment =
                    Alignment.CenterVertically
                ) {
                    /*
                     * Cabeçalho sem fundo colorido atrás do ícone.
                     */
                    Icon(
                        imageVector = iconeCabecalho,
                        contentDescription = null,
                        tint = Crisma_Primary,
                        modifier = Modifier.size(23.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            color = Color.Black,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = subtitulo,
                            color = Color.Gray,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            maxLines = 2
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color(0xFF666666)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Fechar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0xFFECECEC),
                    thickness = 1.dp
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFFAFAFA))
                        .padding(
                            horizontal = 14.dp,
                            vertical = 12.dp
                        ),
                    verticalArrangement =
                    Arrangement.spacedBy(8.dp),
                    content = content
                )
            }
        }
    }
}
