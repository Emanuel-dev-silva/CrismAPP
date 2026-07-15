package com.example.crismapp.ui

import android.app.Activity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.crismapp.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)
private val Card_Border = Color(0xFFF0F0F0)
private val customFont = FontFamily.Default

private const val LINK_CATECISMO =
    "https://www.vatican.va/archive/ccc/index_po.htm"

private const val LINK_BIBLIA_AVE_MARIA =
    "https://claretianos.com.br/biblia-ave-maria-online/"

private data class AvisoGeralInicial(
    val id: String,
    val texto: String,
    val dataCriacao: Long
)

@Composable
fun UserSelectionScreen(
    onCrismandoSelected: () -> Unit,
    onCatequistaSelected: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val uriHandler = LocalUriHandler.current
    val db = remember { FirebaseFirestore.getInstance() }

    SideEffect {
        val window = (view.context as Activity).window

        window.statusBarColor = Crisma_Primary.toArgb()

        WindowCompat
            .getInsetsController(window, view)
            .isAppearanceLightStatusBars = false
    }

    var selectedOption by remember { mutableStateOf("") }

    var showSobreNosDialog by remember {
        mutableStateOf(false)
    }

    var showContatosDialog by remember {
        mutableStateOf(false)
    }

    var showAvisoCompletoDialog by remember {
        mutableStateOf(false)
    }

    var animarImagem by remember {
        mutableStateOf(false)
    }

    var animarTextosSuperior by remember {
        mutableStateOf(false)
    }

    var animarLabelsBotoes by remember {
        mutableStateOf(false)
    }

    var animarConteudoInferior by remember {
        mutableStateOf(false)
    }

    var avisosGerais by remember {
        mutableStateOf(emptyList<AvisoGeralInicial>())
    }

    var carregandoAviso by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {
        delay(100)
        animarImagem = true

        delay(200)
        animarTextosSuperior = true

        delay(300)
        animarLabelsBotoes = true

        delay(400)
        animarConteudoInferior = true
    }

    /*
     * Carrega os cinco avisos gerais mais recentes.
     *
     * Compatibilidade:
     * - turmaId = "GERAL";
     * - destino = "GERAL";
     * - tipo = "GERAL";
     * - turmaId vazio, usado por registros antigos.
     */
    DisposableEffect(Unit) {
        val listener = db.collection("avisos")
            .addSnapshotListener { snapshot, erro ->
                carregandoAviso = false

                if (erro != null || snapshot == null) {
                    avisosGerais = emptyList()
                    return@addSnapshotListener
                }

                avisosGerais = snapshot.documents
                    .mapNotNull { documento ->
                        val texto = documento
                            .getString("texto")
                            .orEmpty()
                            .trim()
                            .ifBlank {
                                documento
                                    .getString("conteudo")
                                    .orEmpty()
                                    .trim()
                            }

                        val turmaId = documento
                            .getString("turmaId")
                            .orEmpty()
                            .trim()

                        val destino = documento
                            .getString("destino")
                            .orEmpty()
                            .trim()

                        val tipo = documento
                            .getString("tipo")
                            .orEmpty()
                            .trim()

                        val ehGeral =
                            turmaId.equals("GERAL", ignoreCase = true) ||
                                    destino.equals("GERAL", ignoreCase = true) ||
                                    tipo.equals("GERAL", ignoreCase = true) ||
                                    turmaId.isBlank()

                        if (!ehGeral || texto.isBlank()) {
                            null
                        } else {
                            fun obterDataMillis(): Long {
                                val campos = listOf(
                                    "dataCriacao",
                                    "dataHora",
                                    "criadoEm",
                                    "data"
                                )

                                campos.forEach { campo ->
                                    when (val valor = documento.get(campo)) {
                                        is Number -> {
                                            return valor.toLong()
                                        }

                                        is Timestamp -> {
                                            return valor.toDate().time
                                        }

                                        is java.util.Date -> {
                                            return valor.time
                                        }
                                    }
                                }

                                return 0L
                            }

                            AvisoGeralInicial(
                                id = documento.id,
                                texto = texto,
                                dataCriacao = obterDataMillis()
                            )
                        }
                    }
                    .sortedByDescending { aviso ->
                        aviso.dataCriacao
                    }
                    .take(5)
            }

        onDispose {
            listener.remove()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // =====================================================
            // ÁREA SUPERIOR
            // =====================================================

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 65.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = animarImagem,
                        enter =
                        fadeIn(tween(1200)) +
                                scaleIn(initialScale = 0.9f),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Image(
                            painter = painterResource(
                                id = R.drawable.imagem_crisma
                            ),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = animarTextosSuperior,
                        enter =
                        fadeIn(tween(1000)) +
                                slideInVertically { it / 4 }
                    ) {
                        Column {
                            Text(
                                text = "\nOlá, bem-vindo ao CrismAPP",
                                fontSize = 22.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            HorizontalDivider(
                                color = Crisma_Gold,
                                thickness = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .padding(vertical = 12.dp)
                            )

                            Text(
                                text = "Selecione seu perfil para continuar sua jornada espiritual.",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // =====================================================
            // ABAS DE ACESSO
            // =====================================================

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
                            selectedOption = "Crismando"
                            onCrismandoSelected()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (
                                selectedOption == "Crismando"
                            ) {
                                Light_Gray_Darker
                            } else {
                                Crisma_Primary
                            }
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = "Crismando",
                            color = if (
                                selectedOption == "Crismando"
                            ) {
                                Crisma_Primary
                            } else {
                                Color.White
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha =
                                        if (animarLabelsBotoes) {
                                            1f
                                        } else {
                                            0f
                                        }
                                }
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
                            selectedOption = "Catequista"
                            onCatequistaSelected()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (
                                selectedOption == "Catequista"
                            ) {
                                Light_Gray_Darker
                            } else {
                                Crisma_Primary
                            }
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = "Catequista",
                            color = if (
                                selectedOption == "Catequista"
                            ) {
                                Crisma_Primary
                            } else {
                                Color.White
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha =
                                        if (animarLabelsBotoes) {
                                            1f
                                        } else {
                                            0f
                                        }
                                }
                        )
                    }
                }
            }

            // =====================================================
            // CONTEÚDO ÚTIL DA PRIMEIRA TELA
            // =====================================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .offset(y = -(screenHeight * 0.04f))
                    .background(Color.White),
                contentAlignment = Alignment.TopCenter
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = animarConteudoInferior,
                    enter =
                    fadeIn(tween(700)) +
                            slideInVertically { it / 6 }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = 14.dp,
                                end = 14.dp,
                                top = 8.dp,
                                bottom = 12.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AjudaCoordenacaoCabecalho(
                            onClick = {
                                showContatosDialog = true
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Acesso rápido",
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                            Arrangement.spacedBy(7.dp)
                        ) {
                            AtalhoInicialCard(
                                titulo = "Bíblia",
                                descricao = "Ave-Maria",
                                icone = Icons.Outlined.MenuBook,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    runCatching {
                                        uriHandler.openUri(
                                            LINK_BIBLIA_AVE_MARIA
                                        )
                                    }
                                }
                            )

                            AtalhoInicialCard(
                                titulo = "Avisos",
                                descricao = "Últimos gerais",
                                icone = Icons.Outlined.Notifications,
                                carregando = carregandoAviso,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    showAvisoCompletoDialog = true
                                }
                            )

                            AtalhoInicialCard(
                                titulo = "Catecismo",
                                descricao = "Igreja Católica",
                                icone = Icons.Outlined.School,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    runCatching {
                                        uriHandler.openUri(
                                            LINK_CATECISMO
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ExitMenuCard(
                            title = "Sair",
                            icon =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            modifier = Modifier.width(128.dp),
                            onClick = {
                                if (context is Activity) {
                                    context.finish()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // =====================================================
    // DIÁLOGO DO AVISO GERAL
    // =====================================================

    if (showAvisoCompletoDialog) {
        AlertDialog(
            onDismissRequest = {
                showAvisoCompletoDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAvisoCompletoDialog = false
                    }
                ) {
                    Text(
                        text = "Fechar",
                        color = Crisma_Primary
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = Crisma_Primary
                )
            },
            title = {
                Text(
                    text = "Últimos avisos",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                when {
                    carregandoAviso -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Crisma_Primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    avisosGerais.isEmpty() -> {
                        Text(
                            text = "Nenhum aviso geral no momento.",
                            color = Color(0xFF555555)
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            avisosGerais.forEachIndexed { indice, aviso ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment =
                                    Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = Crisma_Primary
                                                    .copy(alpha = 0.08f),
                                                shape =
                                                RoundedCornerShape(7.dp)
                                            ),
                                        contentAlignment =
                                        Alignment.Center
                                    ) {
                                        Text(
                                            text = "${indice + 1}",
                                            color = Crisma_Primary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(
                                        modifier = Modifier.width(8.dp)
                                    )

                                    Text(
                                        text = aviso.texto,
                                        modifier = Modifier.weight(1f),
                                        color = Color(0xFF3F3F3F),
                                        fontSize = 13.sp,
                                        lineHeight = 17.sp
                                    )
                                }

                                if (indice < avisosGerais.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(
                                            vertical = 10.dp
                                        ),
                                        color = Color(0xFFECECEC)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    // =====================================================
    // DIÁLOGOS INSTITUCIONAIS
    // =====================================================

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
                    text = "Contatos da Paróquia",
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
private fun AtalhoInicialCard(
    titulo: String,
    descricao: String,
    icone: ImageVector,
    modifier: Modifier = Modifier,
    carregando: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = !carregando,
        modifier = modifier.height(68.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            disabledContainerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Card_Border
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 5.dp,
                    vertical = 7.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (carregando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Crisma_Primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icone,
                    contentDescription = null,
                    tint = Crisma_Primary,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = titulo,
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AjudaCoordenacaoCabecalho(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    color = Crisma_Primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.HelpOutline,
                contentDescription = null,
                tint = Crisma_Primary,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(9.dp))

        Column {
            Text(
                text = "Em caso de dúvidas",
                color = Color.Black,
                fontSize = 16.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Contate a coordenação da Crisma",
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 12.sp
            )
        }
    }
}

/**
 * Botão Sair no mesmo padrão visual dos demais cartões.
 */
@Composable
fun ExitMenuCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Card_Border
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Crisma_Primary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = customFont
            )
        }
    }
}