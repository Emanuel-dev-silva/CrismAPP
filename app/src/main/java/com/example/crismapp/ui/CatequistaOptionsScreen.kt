package com.example.crismapp.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.example.crismapp.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.util.Locale

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Gold = Color(0xFFFFD700)
private val customFont = FontFamily.Default

private const val TOTAL_PARCELAS_PAINEL = 12

private data class AlunoPainel(
    val id: String,
    val turmaId: String,
    val ativo: Boolean
)

private data class EncontroPainel(
    val turmaId: String
)

private data class FrequenciaPainel(
    val alunoId: String,
    val status: String
)

private data class PagamentoPainel(
    val alunoId: String,
    val numeroParcela: Int,
    val pago: Boolean
)

private data class DocumentoPainel(
    val alunoId: String,
    val perfil: String,
    val status: String
)

@Composable
fun CatequistaOptionsScreen(navController: NavController) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val db = remember { FirebaseFirestore.getInstance() }

    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }
    var animarImagem by remember { mutableStateOf(false) }
    var animarTextos by remember { mutableStateOf(false) }
    var animarBotoes by remember { mutableStateOf(false) }
    var animarIconesTopo by remember { mutableStateOf(true) }

    var alunosPainel by remember { mutableStateOf(emptyList<AlunoPainel>()) }
    var encontrosPainel by remember { mutableStateOf(emptyList<EncontroPainel>()) }
    var frequenciasPainel by remember { mutableStateOf(emptyList<FrequenciaPainel>()) }
    var pagamentosPainel by remember { mutableStateOf(emptyList<PagamentoPainel>()) }
    var documentosPainel by remember { mutableStateOf(emptyList<DocumentoPainel>()) }

    var carregandoAlunos by remember { mutableStateOf(true) }
    var carregandoEncontros by remember { mutableStateOf(true) }
    var carregandoFrequencias by remember { mutableStateOf(true) }
    var carregandoPagamentos by remember { mutableStateOf(true) }
    var carregandoDocumentos by remember { mutableStateOf(true) }

    val carregandoPainel =
        carregandoAlunos ||
                carregandoEncontros ||
                carregandoFrequencias ||
                carregandoPagamentos ||
                carregandoDocumentos

    val alunosAtivos by remember(alunosPainel) {
        derivedStateOf {
            alunosPainel.filter { aluno ->
                aluno.ativo &&
                        aluno.id.isNotBlank() &&
                        aluno.turmaId.isNotBlank()
            }
        }
    }

    val totalCrismandosAtivos by remember(alunosAtivos) {
        derivedStateOf {
            alunosAtivos.map { it.id }.distinct().size
        }
    }

    val totalDocumentacaoPendente by remember(
        alunosAtivos,
        documentosPainel
    ) {
        derivedStateOf {
            alunosAtivos.count { aluno ->
                val documentosDoAluno = documentosPainel.filter {
                    it.alunoId.equals(aluno.id, ignoreCase = true)
                }

                val perfisConfigurados = documentosDoAluno
                    .map { it.perfil.uppercase(Locale.ROOT) }
                    .toSet()

                documentosDoAluno.isEmpty() ||
                        "CRISMANDO" !in perfisConfigurados ||
                        "PADRINHO" !in perfisConfigurados ||
                        documentosDoAluno.any {
                            it.status.equals(
                                "NAO_ENTREGUE",
                                ignoreCase = true
                            )
                        }
            }
        }
    }

    val totalFrequenciaAbaixoDe75 by remember(
        alunosAtivos,
        encontrosPainel,
        frequenciasPainel
    ) {
        derivedStateOf {
            alunosAtivos.count { aluno ->
                val totalEncontrosDaTurma = encontrosPainel.count {
                    it.turmaId == aluno.turmaId
                }

                if (totalEncontrosDaTurma <= 0) {
                    false
                } else {
                    val totalPresencas = frequenciasPainel.count {
                        it.alunoId.equals(aluno.id, ignoreCase = true) &&
                                it.status.equals(
                                    "PRESENTE",
                                    ignoreCase = true
                                )
                    }

                    val porcentagem =
                        totalPresencas.toFloat() /
                                totalEncontrosDaTurma.toFloat() *
                                100f

                    porcentagem < 75f
                }
            }
        }
    }

    val totalParcelasPendentes by remember(
        alunosAtivos,
        pagamentosPainel
    ) {
        derivedStateOf {
            alunosAtivos.sumOf { aluno ->
                val parcelasPagas = pagamentosPainel
                    .filter {
                        it.alunoId.equals(aluno.id, ignoreCase = true) &&
                                it.pago &&
                                it.numeroParcela in 1..TOTAL_PARCELAS_PAINEL
                    }
                    .map { it.numeroParcela }
                    .distinct()
                    .size

                (TOTAL_PARCELAS_PAINEL - parcelasPagas)
                    .coerceAtLeast(0)
            }
        }
    }

    val catequista = FirebaseAuthRepository.catequistaAtual

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window

        window.statusBarColor = Crisma_Primary.toArgb()

        WindowCompat.getInsetsController(
            window,
            view
        ).isAppearanceLightStatusBars = false

        if (FirebaseAuthRepository.catequistaAtual == null) {
            FirebaseAuthRepository.restaurarSessao(
                onSuccess = {},
                onSemSessao = {
                    navController.navigate("loginCatequista") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onError = { erro ->
                    Toast.makeText(
                        context,
                        erro.message ?: "Acesso não autorizado.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.navigate("loginCatequista") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        delay(100)
        animarImagem = true
        delay(200)
        animarIconesTopo = true
        delay(400)
        animarTextos = true
        delay(600)
        animarBotoes = true
    }

    DisposableEffect(Unit) {
        val listenerAlunos = db.collection("usuarios")
            .addSnapshotListener { snapshot, erro ->
                carregandoAlunos = false

                if (erro != null || snapshot == null) {
                    alunosPainel = emptyList()
                    return@addSnapshotListener
                }

                alunosPainel = snapshot.documents.mapNotNull { documento ->
                    val nome = documento.getString("nome")
                        .orEmpty()
                        .trim()

                    if (nome.isBlank()) {
                        null
                    } else {
                        AlunoPainel(
                            id = documento.id,
                            turmaId = documento.getString("turmaId").orEmpty(),
                            ativo = documento.getBoolean("ativo") ?: true
                        )
                    }
                }
            }

        val listenerEncontros = db.collection("encontros")
            .addSnapshotListener { snapshot, erro ->
                carregandoEncontros = false

                if (erro != null || snapshot == null) {
                    encontrosPainel = emptyList()
                    return@addSnapshotListener
                }

                encontrosPainel = snapshot.documents.mapNotNull { documento ->
                    val turmaId = documento.getString("turmaId").orEmpty()

                    if (turmaId.isBlank()) {
                        null
                    } else {
                        EncontroPainel(turmaId = turmaId)
                    }
                }
            }

        val listenerFrequencias = db.collection("frequencias")
            .addSnapshotListener { snapshot, erro ->
                carregandoFrequencias = false

                if (erro != null || snapshot == null) {
                    frequenciasPainel = emptyList()
                    return@addSnapshotListener
                }

                frequenciasPainel = snapshot.documents.mapNotNull { documento ->
                    val alunoId = documento.getString("alunoId").orEmpty()

                    if (alunoId.isBlank()) {
                        null
                    } else {
                        FrequenciaPainel(
                            alunoId = alunoId,
                            status = documento.getString("status").orEmpty()
                        )
                    }
                }
            }

        val listenerPagamentos = db.collection("financeiro")
            .addSnapshotListener { snapshot, erro ->
                carregandoPagamentos = false

                if (erro != null || snapshot == null) {
                    pagamentosPainel = emptyList()
                    return@addSnapshotListener
                }

                pagamentosPainel = snapshot.documents.mapNotNull { documento ->
                    val alunoId = documento.getString("alunoId").orEmpty()

                    val numeroParcela =
                        documento.getLong("parcela")?.toInt()
                            ?: documento.getLong("numeroParcela")?.toInt()
                            ?: 0

                    val status = documento
                        .getString("status")
                        .orEmpty()
                        .trim()
                        .uppercase(Locale.ROOT)

                    val pago =
                        status == "PAGO" ||
                                (
                                        status.isBlank() &&
                                                documento.getBoolean("statusPago") == true
                                        )

                    if (
                        alunoId.isBlank() ||
                        numeroParcela !in 1..TOTAL_PARCELAS_PAINEL
                    ) {
                        null
                    } else {
                        PagamentoPainel(
                            alunoId = alunoId,
                            numeroParcela = numeroParcela,
                            pago = pago
                        )
                    }
                }
            }

        val listenerDocumentos = db.collection("documentos")
            .addSnapshotListener { snapshot, erro ->
                carregandoDocumentos = false

                if (erro != null || snapshot == null) {
                    documentosPainel = emptyList()
                    return@addSnapshotListener
                }

                documentosPainel = snapshot.documents.mapNotNull { documento ->
                    val alunoId = documento.getString("alunoId").orEmpty()

                    val perfilSalvo = documento
                        .getString("perfil")
                        .orEmpty()
                        .trim()
                        .uppercase(Locale.ROOT)

                    val perfil = when {
                        perfilSalvo.isNotBlank() -> perfilSalvo

                        documento.id.contains(
                            "-PADRINHO-",
                            ignoreCase = true
                        ) -> "PADRINHO"

                        documento.id.contains(
                            "-CRISMANDO-",
                            ignoreCase = true
                        ) -> "CRISMANDO"

                        else -> ""
                    }

                    if (alunoId.isBlank()) {
                        null
                    } else {
                        DocumentoPainel(
                            alunoId = alunoId,
                            perfil = perfil,
                            status = documento.getString("status").orEmpty()
                        )
                    }
                }
            }

        onDispose {
            listenerAlunos.remove()
            listenerEncontros.remove()
            listenerFrequencias.remove()
            listenerPagamentos.remove()
            listenerDocumentos.remove()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
                    .background(Crisma_Primary)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 24.dp
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (animarIconesTopo) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            horizontalArrangement =
                            Arrangement.SpaceBetween
                        ) {
                            UserIconWithLabel(
                                Icons.Outlined.Info,
                                "Sobre o App"
                            ) {
                                showSobreNosDialog = true
                            }

                            UserIconWithLabel(
                                Icons.Outlined.Phone,
                                "Contatos"
                            ) {
                                showContatosDialog = true
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (animarImagem) {
                        Image(
                            painter = painterResource(
                                id = R.drawable.imagem_crisma
                            ),
                            contentDescription = "Logo CrismAPP",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }

                    if (animarTextos) {
                        Column {
                            Text(
                                text = "\nPainel do Catequista",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = customFont
                            )

                            HorizontalDivider(
                                color = Crisma_Gold,
                                thickness = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth(0.76f)
                                    .padding(vertical = 12.dp)
                            )

                            val nome = catequista
                                ?.nome
                                .orEmpty()
                                .ifBlank { "Catequista" }

                            val nivel = if (
                                catequista?.possuiPermissaoTotal() == true
                            ) {
                                "Permissão total"
                            } else {
                                "Permissão comum"
                            }

                            Text(
                                text = "Olá, $nome — $nivel",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontFamily = customFont
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.08f)
                        .offset(y = -(screenHeight * 0.04f))
                        .background(Color.White)
                ) {
                    Button(
                        onClick = {
                            navController.navigate(
                                "turmaJovemScreen"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Crisma_Primary
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = "Turma Jovem",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(
                                Crisma_Primary.copy(alpha = 0.3f)
                            )
                    )

                    Button(
                        onClick = {
                            navController.navigate(
                                "turmaAdultaScreen"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Crisma_Primary
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = "Turma Adulta",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .offset(y = -(screenHeight * 0.04f))
                    .background(Color.White),
                contentAlignment = Alignment.TopCenter
            ) {
                if (animarBotoes) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = 14.dp,
                                end = 14.dp,
                                bottom = 10.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 12.dp)
                            ) {
                                Text(
                                    text = "Visão geral da paróquia",
                                    fontSize = 16.sp,
                                    lineHeight = 16.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Indicadores atualizados em tempo real\n",
                                    modifier = Modifier.offset(y = (0).dp),
                                    fontSize = 11.sp,
                                    lineHeight = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            if (carregandoPainel) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(17.dp),
                                    color = Crisma_Primary,
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PainelIndicadorCard(
                                valor = totalCrismandosAtivos,
                                titulo = "Crismandos ativos",
                                detalhe = "Jovens e adultos",
                                icone = Icons.Outlined.Groups,
                                corDestaque = Crisma_Primary,
                                corFundo = Color(0xFFFFFCFC),
                                carregando = carregandoAlunos,
                                modifier = Modifier.weight(1f)
                            )

                            PainelIndicadorCard(
                                valor = totalDocumentacaoPendente,
                                titulo = "Documentação",
                                detalhe = "Crismandos pendentes",
                                icone = Icons.Outlined.Description,
                                corDestaque = Crisma_Primary,
                                corFundo = Color(0xFFFFFCFC),
                                carregando =
                                carregandoAlunos ||
                                        carregandoDocumentos,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PainelIndicadorCard(
                                valor = totalFrequenciaAbaixoDe75,
                                titulo = "Frequência baixa",
                                detalhe = "Abaixo de 75%",
                                icone = Icons.Outlined.Assessment,
                                corDestaque = Crisma_Primary,
                                corFundo = Color(0xFFFFFCFC),
                                carregando =
                                carregandoAlunos ||
                                        carregandoEncontros ||
                                        carregandoFrequencias,
                                modifier = Modifier.weight(1f)
                            )

                            PainelIndicadorCard(
                                valor = totalParcelasPendentes,
                                titulo = "Parcelas pendentes",
                                detalhe = "Total ainda não pago",
                                icone = Icons.Outlined.Payments,
                                corDestaque = Crisma_Primary,
                                corFundo = Color(0xFFFFFCFC),
                                carregando =
                                carregandoAlunos ||
                                        carregandoPagamentos,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        PainelSairCard(
                            onClick = {
                                FirebaseAuthRepository.sair()

                                navController.navigate("loginCatequista") {
                                    popUpTo(0) {
                                        inclusive = true
                                    }

                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSobreNosDialog) {
        AlertDialog(
            onDismissRequest = {
                showSobreNosDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSobreNosDialog = false
                    }
                ) {
                    Text(
                        text = "Entendido",
                        color = Crisma_Primary
                    )
                }
            },
            title = {
                Text(
                    text = "Sobre o CrismAPP",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "O CrismAPP foi idealizado para modernizar e fortalecer a comunicação na jornada espiritual da nossa Paróquia.\n\n. Desenvolvimento:\nEmanuel Barbosa\n(github.com/Emanuel-dev-silva)\n\n. Gestão de Requisitos:\nVictor Lima"
                )
            }
        )
    }

    if (showContatosDialog) {
        AlertDialog(
            onDismissRequest = {
                showContatosDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showContatosDialog = false
                    }
                ) {
                    Text(
                        text = "Fechar",
                        color = Crisma_Primary
                    )
                }
            },
            title = {
                Text(
                    text = "Contatos",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = ". Paróquia Santo Antônio\nTiúma, São Lourenço da Mata - PE\n\n. Secretaria e WhatsApp:\n(81) 9 8593-9076\n\n. Horário de Atendimento:\nTerça a Sábado: 08h às 12h"
                )
            }
        )
    }
}

@Composable
private fun PainelSairCard(
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(128.dp)
            .height(42.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFF0F0F0)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Sair",
                tint = Crisma_Primary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Sair",
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PainelIndicadorCard(
    valor: Int,
    titulo: String,
    detalhe: String,
    icone: ImageVector,
    @Suppress("UNUSED_PARAMETER")
    corDestaque: Color,
    @Suppress("UNUSED_PARAMETER")
    corFundo: Color,
    carregando: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(64.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFF0F0F0)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = Crisma_Primary,
                modifier = Modifier.size(21.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (carregando) "—" else valor.toString(),
                    color = Crisma_Primary,
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = titulo,
                    color = Color.Black,
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = detalhe,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}