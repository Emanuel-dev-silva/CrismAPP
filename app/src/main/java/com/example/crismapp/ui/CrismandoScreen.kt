package com.example.crismapp.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import java.util.Locale

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Primary_Light = Color(0xFFFF3333)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)
private val customFont = FontFamily.Default

enum class TipoPresenca { PRESENCA, FALTA_JUSTIFICADA, FALTA }
enum class TipoDocumento { ENTREGUE, NAO_POSSUI, NAO_ENTREGUE }

data class FrequenciaItem(val title: String, val status: TipoPresenca)
data class CarneItem(val title: String, val isPaid: Boolean)
data class DocumentoItem(val title: String, val status: TipoDocumento)
data class AvisoItem(val text: String, val linkUrl: String? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrismandoScreen(navController: NavController) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var userName by remember { mutableStateOf("Emanuel") }

    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }

    // Estados dos Popups
    var showPresencasPopup by remember { mutableStateOf(false) }
    var showAvisosPopup by remember { mutableStateOf(false) }
    var showCarnePopup by remember { mutableStateOf(false) }
    var showDocumentosPopup by remember { mutableStateOf(false) }

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

                    Box(
                        Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Crisma_Gold)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.White)
                            .clickable { showDocumentosPopup = true },
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
                                showPresencasPopup = true
                            }
                            SmallMenuCard(title = "Avisos", icon = Icons.Outlined.Notifications, modifier = Modifier.weight(1f)) {
                                showAvisosPopup = true
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmallMenuCard(title = "Carnê", icon = Icons.Outlined.Payments, modifier = Modifier.weight(1f)) {
                                showCarnePopup = true
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

    // --- POPUPS DE LISTAGEM ---

    if (showPresencasPopup) {
        CustomPopup(
            title = "Veja suas presenças:",
            onDismiss = { showPresencasPopup = false }
        ) {
            val presencas = listOf(
                FrequenciaItem("Encontro 01 - Presença confirmada", TipoPresenca.PRESENCA),
                FrequenciaItem("Encontro 02 - Presença confirmada", TipoPresenca.PRESENCA),
                FrequenciaItem("Encontro 03 - Presença confirmada", TipoPresenca.PRESENCA),
                FrequenciaItem("Encontro 04 - Falta justificada", TipoPresenca.FALTA_JUSTIFICADA),
                FrequenciaItem("Encontro 05 - Falta", TipoPresenca.FALTA)
            )
            items(presencas) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, color = Color(0xFFEEEEEE))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (statusIcon, statusColor) = when (item.status) {
                            TipoPresenca.PRESENCA -> Pair(Icons.Outlined.CheckCircle, Color(0xFF4CAF50))
                            TipoPresenca.FALTA_JUSTIFICADA -> Pair(Icons.Outlined.ErrorOutline, Color(0xFFFF9800))
                            TipoPresenca.FALTA -> Pair(Icons.Outlined.Cancel, Color(0xFFE53935))
                        }
                        Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = item.title, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = customFont)
                    }
                }
            }
        }
    }

    if (showAvisosPopup) {
        val uriHandler = LocalUriHandler.current
        CustomPopup(
            title = "Últimos avisos:",
            onDismiss = { showAvisosPopup = false }
        ) {
            val avisos = listOf(
                AvisoItem("Preseça necessária na missa de sábado"),
                AvisoItem("Não haverá encontro no sábado de aleluia."),
                AvisoItem("Trazer a bíblia e caderno no próximo encontro."),
                AvisoItem("Entre no grupo da WhatsApp da turma", "https://chat.whatsapp.com/ExemploCodigoDoGrupo")
            )
            items(avisos) { item ->
                val isLink = item.linkUrl != null
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isLink) Modifier.clickable { item.linkUrl?.let { uriHandler.openUri(it) } } else Modifier),
                    colors = CardDefaults.cardColors(containerColor = if (isLink) Color(0xFFE8F5E9) else Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, color = if (isLink) Color(0xFF81C784) else Color(0xFFEEEEEE))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLink) Icons.Outlined.Link else Icons.Outlined.Label,
                            contentDescription = null,
                            tint = if (isLink) Color(0xFF2E7D32) else Crisma_Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = item.text, color = if (isLink) Color(0xFF1B5E20) else Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = customFont)
                    }
                }
            }
        }
    }

    if (showCarnePopup) {
        CustomPopup(
            title = "Carnês pagos:",
            onDismiss = { showCarnePopup = false }
        ) {
            val carnes = listOf(
                CarneItem("Parcela 01 - Paga", isPaid = true),
                CarneItem("Parcela 02 - Paga", isPaid = true),
                CarneItem("Parcela 03 - Paga", isPaid = true),
                CarneItem("Parcela 04 - Pendente", isPaid = false)
            )
            items(carnes) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, color = Color(0xFFEEEEEE))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.isPaid) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                            contentDescription = null,
                            tint = if (item.isPaid) Color(0xFF4CAF50) else Color(0xFFE53935),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = item.title, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = customFont)
                    }
                }
            }
        }
    }

    if (showDocumentosPopup) {
        CustomPopup(
            title = "Situação dos documentos:",
            onDismiss = { showDocumentosPopup = false }
        ) {
            val documentos = listOf(
                DocumentoItem("Ficha do Padrinho: Não entregue", TipoDocumento.NAO_ENTREGUE),
                DocumentoItem("Lembrança do Batismo: Entregue", TipoDocumento.ENTREGUE),
                DocumentoItem("Lembrança da 1ª Eucaristia: Não possui", TipoDocumento.NAO_POSSUI)
            )
            items(documentos) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, color = Color(0xFFEEEEEE))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (statusIcon, statusColor) = when (item.status) {
                            TipoDocumento.ENTREGUE -> Pair(Icons.Outlined.CheckCircle, Color(0xFF4CAF50))
                            TipoDocumento.NAO_POSSUI -> Pair(Icons.Outlined.ErrorOutline, Color(0xFFFF9800))
                            TipoDocumento.NAO_ENTREGUE -> Pair(Icons.Outlined.Cancel, Color(0xFFE53935))
                        }
                        Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = item.title, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = customFont)
                    }
                }
            }
        }
    }
}

// --- COMPONENTE POPUP CORRIGIDO ---
@Composable
fun CustomPopup(
    title: String,
    onDismiss: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Capitaliza apenas a primeira letra para o título
    val formattedTitle = title.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.52f)
                .padding(horizontal = 4.dp)
                .background(Color.White, shape = RoundedCornerShape(4.dp))
                .border(1.dp, Crisma_Primary, shape = RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Crisma_Primary)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = formattedTitle, // Título agora com capitalização correta
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFont,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(end = 32.dp)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar",
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.CenterEnd)
                            .clickable { onDismiss() }
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFF9F9F9))
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content
                )
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun UserIconWithLabel(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
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
            fontWeight = FontWeight.Medium,
            fontFamily = customFont
        )
    }
}

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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, color = Color(0xFFF0F0F0))
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
                    tint = Crisma_Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = customFont,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}