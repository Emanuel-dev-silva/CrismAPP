package com.example.crismapp.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.crismapp.R
import kotlinx.coroutines.delay

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)
private val customFont = FontFamily.Default

@Composable
fun UserSelectionScreen(onCrismandoSelected: () -> Unit, onCatequistaSelected: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Crisma_Primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }

    var selectedOption by remember { mutableStateOf("") }
    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }

    var animarImagem by remember { mutableStateOf(false) }
    var animarTextosSuperior by remember { mutableStateOf(false) }
    var animarLabelsBotoes by remember { mutableStateOf(false) }
    var animarBotaoSair by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100); animarImagem = true
        delay(200); animarTextosSuperior = true
        delay(300); animarLabelsBotoes = true
        delay(400); animarBotaoSair = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ÁREA SUPERIOR (65%)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
                    .background(Crisma_Primary)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
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
                        enter = fadeIn(tween(1200)) + scaleIn(initialScale = 0.9f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.imagem_crisma),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxWidth().height(180.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = animarTextosSuperior,
                        enter = fadeIn(tween(1000)) + slideInVertically { it / 4 }
                    ) {
                        Column {
                            Text("\nOlá, bem-vindo ao CrismAPP", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = Crisma_Gold, thickness = 2.dp, modifier = Modifier.fillMaxWidth(0.7f).padding(vertical = 12.dp))
                            Text("Selecione seu perfil para continuar sua jornada espiritual.", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }

            // --- BARRA CENTRAL ---
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.08f)
                        .offset(y = -(screenHeight * 0.04f))
                        .background(Color.White)
                ) {
                    Button(
                        onClick = { selectedOption = "Crismando"; onCrismandoSelected() },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedOption == "Crismando") Light_Gray_Darker else Crisma_Primary),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Crismando", color = if (selectedOption == "Crismando") Crisma_Primary else Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.alpha(if (animarLabelsBotoes) 1f else 0f))
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Crisma_Primary.copy(alpha = 0.3f)))
                    Button(
                        onClick = { selectedOption = "Catequista"; onCatequistaSelected() },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedOption == "Catequista") Light_Gray_Darker else Crisma_Primary),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Catequista", color = if (selectedOption == "Catequista") Crisma_Primary else Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.alpha(if (animarLabelsBotoes) 1f else 0f))
                    }
                }
            }

            // --- ÁREA INFERIOR (35%) ---
            Box(
                modifier = Modifier.fillMaxWidth().weight(0.35f).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (animarBotaoSair) {
                    // BOTÃO SAIR RETANGULAR COM ÍCONE AO LADO DO NOME
                    ExitMenuCard(
                        title = "Sair",
                        icon = Icons.Outlined.ArrowBack,
                        modifier = Modifier.width(170.dp),
                        onClick = { if (context is Activity) context.finish() }
                    )
                }
            }
        }
    }

    // Diálogos
    if (showSobreNosDialog) {
        AlertDialog(
            onDismissRequest = { showSobreNosDialog = false },
            confirmButton = { TextButton(onClick = { showSobreNosDialog = false }) { Text("Entendido", color = Crisma_Primary) } },
            title = { Text("Sobre o CrismAPP", fontWeight = FontWeight.Bold) },
            text = { Text("O CrismAPP foi idealizado para modernizar e fortalecer a comunicação na jornada espiritual da nossa Paróquia.\n\n. Desenvolvimento:\nEmanuel Barbosa\n(github.com/Emanuel-dev-silva)\n\n. Gestão de Requisitos:\nVictor Lima") }
        )
    }

    if (showContatosDialog) {
        AlertDialog(
            onDismissRequest = { showContatosDialog = false },
            confirmButton = { TextButton(onClick = { showContatosDialog = false }) { Text("Fechar", color = Crisma_Primary) } },
            title = { Text("Contatos da Paróquia", fontWeight = FontWeight.Bold) },
            text = { Text(". Paróquia Santo Antônio\nTiúma, São Lourenço da Mata - PE\n\n. Secretaria e WhatsApp:\n(81) 9 8593-9076\n\n. Horário de Atendimento:\nTerça a Sábado: 08h às 12h") }
        )
    }
}

/**
 * Componente exclusivo para o botão Sair desta tela.
 * Ícone e texto lado a lado, formato retangular (4.dp).
 */
@Composable
fun ExitMenuCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(65.dp) // Altura menor para combinar com a linha única
            .clickable { onClick() }
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, color = Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Crisma_Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
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