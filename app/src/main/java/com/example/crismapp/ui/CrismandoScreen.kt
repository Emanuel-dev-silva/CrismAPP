package com.example.crismapp.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.example.crismapp.R
import kotlinx.coroutines.delay

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Primary_Light = Color(0xFFFF3333)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)
private val customFont = FontFamily.Default

// Enums e Data Classes para estruturação de dados
enum class TipoPresenca {
    PRESENCA,
    FALTA_JUSTIFICADA,
    FALTA
}

enum class TipoDocumento {
    ENTREGUE,
    NAO_POSSUI,
    NAO_ENTREGUE
}

data class FrequenciaItem(val title: String, val status: TipoPresenca)
data class CarneItem(val title: String, val isPaid: Boolean)
data class DocumentoItem(val title: String, val status: TipoDocumento)
data class AvisoItem(val text: String, val linkUrl: String? = null)

@Composable
fun CrismandoScreen(navController: NavController) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val uriHandler = LocalUriHandler.current
    val screenHeight = configuration.screenHeightDp.dp

    var userName by remember { mutableStateOf("Emanuel") }

    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }

    var showPresencasDialog by remember { mutableStateOf(false) }
    var showAvisosDialog by remember { mutableStateOf(false) }
    var showCarneDialog by remember { mutableStateOf(false) }
    var showDocumentosDialog by remember { mutableStateOf(false) }

    var animarImagem by remember { mutableStateOf(false) }
    var animarTextos by remember { mutableStateOf(false) }
    var animarBotoesAcao by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window
        window.statusBarColor = Crisma_Primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

        delay(100); animarImagem = true
        delay(200); animarTextos = true
        delay(300); animarBotoesAcao = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ÁREA SUPERIOR (65%)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
                    .background(Crisma_Primary)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    UserIconWithLabel(Icons.Outlined.Info, "Sobre o App") { showSobreNosDialog = true }
                    UserIconWithLabel(Icons.Outlined.Phone, "Contatos") { showContatosDialog = true }
                }

                Column(modifier = Modifier.fillMaxSize().padding(top = 65.dp)) {
                    AnimatedVisibility(
                        visible = animarImagem,
                        enter = fadeIn(tween(1200)) + scaleIn(initialScale = 0.9f)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.imagem_crisma),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxWidth().height(180.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = animarTextos,
                        enter = fadeIn(tween(1200)) + slideInVertically { it / 3 }
                    ) {
                        Column {
                            Text(
                                "\nÁrea do Crismando",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = customFont
                            )
                            HorizontalDivider(
                                color = Crisma_Gold,
                                thickness = 2.dp,
                                modifier = Modifier.fillMaxWidth(0.76f).padding(vertical = 12.dp)
                            )
                            Text(
                                "\"A Eucaristia é a minha rodovia para o Céu.\"\n" +
                                        "(S. Carlo Acutis)",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontFamily = customFont
                            )
                        }
                    }
                }
            }

            // --- BARRA CENTRAL INTERATIVA ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.08f)
                        .offset(y = -(screenHeight * 0.04f))
                        .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lado Esquerdo: Saudação
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .background(Light_Gray_Darker),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "  Olá, $userName!",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = customFont
                        )
                    }

                    // Divisor Dourado
                    Box(
                        Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Crisma_Gold)
                    )

                    // Lado Direito: Botão de Documentos com status visual
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.White)
                            .clickable { showDocumentosDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFFFB300), shape = RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Documentos",
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                fontFamily = customFont
                            )
                        }
                    }
                }
            }

            // ÁREA INFERIOR (35%) - GRID 2x2
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .background(Color.White),
                contentAlignment = Alignment.TopCenter
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = animarBotoesAcao,
                    enter = fadeIn(tween(900)) + slideInVertically { 20 }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmallMenuCard(title = "Frequência", icon = Icons.Outlined.DateRange, modifier = Modifier.weight(1f)) {
                                showPresencasDialog = true
                            }
                            SmallMenuCard(title = "Avisos", icon = Icons.Outlined.Notifications, modifier = Modifier.weight(1f)) {
                                showAvisosDialog = true
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmallMenuCard(title = "Carnê", icon = Icons.Outlined.Payments, modifier = Modifier.weight(1f)) {
                                showCarneDialog = true
                            }
                            SmallMenuCard(title = "Sair", icon = Icons.Outlined.ArrowBack, modifier = Modifier.weight(1f)) {
                                navController.navigate("crismandoLoginScreen") {
                                    popUpTo("crismandoScreen") { inclusive = true }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS DE INFORMAÇÃO PADRÃO ---
    if (showSobreNosDialog) {
        AlertDialog(
            onDismissRequest = { showSobreNosDialog = false },
            confirmButton = {
                TextButton(onClick = { showSobreNosDialog = false }) {
                    Text("Entendido", color = Crisma_Primary)
                }
            },
            title = { Text("Sobre o CrismAPP", fontWeight = FontWeight.Bold) },
            text = { Text("O CrismAPP foi idealizado para modernizar e fortalecer a comunicação na jornada espiritual da nossa Paróquia.\n\n. Desenvolvimento:\nEmanuel Barbosa\n(github.com/Emanuel-dev-silva)\n\n. Gestão de Requisitos:\nVictor Lima") }
        )
    }

    if (showContatosDialog) {
        AlertDialog(
            onDismissRequest = { showContatosDialog = false },
            confirmButton = {
                TextButton(onClick = { showContatosDialog = false }) {
                    Text("Fechar", color = Crisma_Primary)
                }
            },
            title = { Text("Contatos", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(". Paróquia Santo Antônio\nTiúma, São Lourenço da Mata - PE\n\n. Secretaria e WhatsApp:\n(81) 9 8593-9076\n\n. Horário de Atendimento:\nTerça a Sábado: 08h às 12h")
                }
            }
        )
    }

    // --- POPUPS DE INFORMAÇÃO DO CRISMANDO ---

    if (showPresencasDialog) {
        FrequenciaPopupScreen(
            title = "veja suas presenças",
            onDismiss = { showPresencasDialog = false },
            items = listOf(
                FrequenciaItem("Encontro 01 - Presença confirmada", TipoPresenca.PRESENCA),
                FrequenciaItem("Encontro 02 - Presença confirmada", TipoPresenca.PRESENCA),
                FrequenciaItem("Encontro 03 - Presença confirmada", TipoPresenca.PRESENCA),
                FrequenciaItem("Encontro 04 - Falta justificada", TipoPresenca.FALTA_JUSTIFICADA),
                FrequenciaItem("Encontro 05 - Falta", TipoPresenca.FALTA)
            )
        )
    }

    // 2. Popup de Avisos (Link do WhatsApp integrado aqui agora)
    if (showAvisosDialog) {
        AvisosPopupScreen(
            title = "últimos avisos",
            onDismiss = { showAvisosDialog = false },
            items = listOf(
                AvisoItem("Missa de entrega no próximo domingo às 19h."),
                AvisoItem("Não haverá encontro no feriado de Tiradentes."),
                AvisoItem("Trazer a bíblia e caderno no próximo encontro."),
                AvisoItem("Entre no grupo oficial do WhatsApp da turma", "https://chat.whatsapp.com/ExemploCodigoDoGrupo")
            )
        )
    }

    if (showCarneDialog) {
        CarnePopupScreen(
            title = "carnês pagos",
            onDismiss = { showCarneDialog = false },
            items = listOf(
                CarneItem("Parcela 01 - Paga", isPaid = true),
                CarneItem("Parcela 02 - Paga", isPaid = true),
                CarneItem("Parcela 03 - Paga", isPaid = true),
                CarneItem("Parcela 04 - Pendente", isPaid = false)
            )
        )
    }

    if (showDocumentosDialog) {
        DocumentosPopupScreen(
            title = "situação de documentos",
            onDismiss = { showDocumentosDialog = false },
            items = listOf(
                DocumentoItem("Ficha do Padrinho/Madrinha: Não entregue", TipoDocumento.NAO_ENTREGUE),
                DocumentoItem("Lembrança do Batismo: Entregue", TipoDocumento.ENTREGUE),
                DocumentoItem("Lembrança da 1ª Eucaristia: Não possui", TipoDocumento.NAO_POSSUI)
            )
        )
    }
}

// Popup especializado para Avisos (Suporta textos normais e links clicáveis)
@Composable
fun AvisosPopupScreen(
    title: String,
    onDismiss: () -> Unit,
    items: List<AvisoItem>
) {
    val uriHandler = LocalUriHandler.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, color = Light_Gray_Darker.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Crisma_Primary_Light, Crisma_Primary)
                            )
                        )
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFont,
                        textAlign = TextAlign.Center
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFF9F9F9))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items) { item ->
                        val isLink = item.linkUrl != null
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.15f),
                                    spotColor = Color.Black.copy(alpha = 0.2f)
                                )
                                .then(
                                    if (isLink) Modifier.clickable { uriHandler.openUri(item.linkUrl!!) }
                                    else Modifier
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLink) Color(0xFFE8F5E9) else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isLink) Color(0xFF81C784) else Color(0xFFEEEEEE)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isLink) Icons.Outlined.Link else Icons.Outlined.Label,
                                    contentDescription = null,
                                    tint = if (isLink) Color(0xFF2E7D32) else Crisma_Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.text,
                                    color = if (isLink) Color(0xFF1B5E20) else Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = customFont
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Light_Gray_Darker, thickness = 1.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA))
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FECHAR",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFont
                    )
                }
            }
        }
    }
}

// Popup especializado para Documentos
@Composable
fun DocumentosPopupScreen(
    title: String,
    onDismiss: () -> Unit,
    items: List<DocumentoItem>
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, color = Light_Gray_Darker.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Crisma_Primary_Light, Crisma_Primary)
                            )
                        )
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFont,
                        textAlign = TextAlign.Center
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFF9F9F9))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.15f),
                                    spotColor = Color.Black.copy(alpha = 0.2f)
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, color = Color(0xFFEEEEEE))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val (statusIcon, statusColor) = when (item.status) {
                                    TipoDocumento.ENTREGUE -> Pair(Icons.Outlined.CheckCircle, Color(0xFF4CAF50))
                                    TipoDocumento.NAO_POSSUI -> Pair(Icons.Outlined.ErrorOutline, Color(0xFFFF9800))
                                    TipoDocumento.NAO_ENTREGUE -> Pair(Icons.Outlined.Cancel, Color(0xFFE53935))
                                }

                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.title,
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = customFont
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Light_Gray_Darker, thickness = 1.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA))
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FECHAR",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFont
                    )
                }
            }
        }
    }
}

// Popup especializado para Frequência
@Composable
fun FrequenciaPopupScreen(
    title: String,
    onDismiss: () -> Unit,
    items: List<FrequenciaItem>
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, color = Light_Gray_Darker.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Crisma_Primary_Light, Crisma_Primary)
                            )
                        )
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFont,
                        textAlign = TextAlign.Center
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFF9F9F9))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.15f),
                                    spotColor = Color.Black.copy(alpha = 0.2f)
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, color = Color(0xFFEEEEEE))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val (statusIcon, statusColor) = when (item.status) {
                                    TipoPresenca.PRESENCA -> Pair(Icons.Outlined.CheckCircle, Color(0xFF4CAF50))
                                    TipoPresenca.FALTA_JUSTIFICADA -> Pair(Icons.Outlined.ErrorOutline, Color(0xFFFF9800))
                                    TipoPresenca.FALTA -> Pair(Icons.Outlined.Cancel, Color(0xFFE53935))
                                }

                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.title,
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = customFont
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Light_Gray_Darker, thickness = 1.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA))
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FECHAR",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFont
                    )
                }
            }
        }
    }
}

// Popup especializado para Carnê
@Composable
fun CarnePopupScreen(
    title: String,
    onDismiss: () -> Unit,
    items: List<CarneItem>
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, color = Light_Gray_Darker.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Crisma_Primary_Light, Crisma_Primary)
                            )
                        )
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFont,
                        textAlign = TextAlign.Center
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFF9F9F9))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.15f),
                                    spotColor = Color.Black.copy(alpha = 0.2f)
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, color = Color(0xFFEEEEEE))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val statusIcon = if (item.isPaid) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline
                                val statusColor = if (item.isPaid) Color(0xFF4CAF50) else Color(0xFFFF9800)

                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.title,
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = customFont
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Light_Gray_Darker, thickness = 1.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA))
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FECHAR",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFont
                    )
                }
            }
        }
    }
}

@Composable
fun SmallMenuCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Crisma_Primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun UserIconWithLabel(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}