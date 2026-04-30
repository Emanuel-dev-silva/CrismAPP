package com.example.crismapp.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.crismapp.R
import kotlinx.coroutines.delay

// --- COR ALTERADA PARA VERMELHO VIVO ---
private val Crisma_Primary = Color(0xFFFF0000) // Vermelho Vivo (Puro)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)
private val customFont = FontFamily.Default

@Composable
fun UserIconWithLabel(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp, fontFamily = customFont)
    }
}

@Composable
fun UserSelectionScreen(onCrismandoSelected: () -> Unit, onCatequistaSelected: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Ajuste automático da barra de status para o vermelho vivo
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
    var animarIconesTopo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        animarImagem = true
        animarBotaoSair = true
        delay(300)
        animarTextosSuperior = true
        delay(200)
        animarLabelsBotoes = true
        delay(200)
        animarIconesTopo = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ÁREA SUPERIOR (65%) - Vermelho Vivo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
                    .background(Crisma_Primary)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    Box(modifier = Modifier.fillMaxWidth().height(70.dp)) {
                        // Prefixo explícito para evitar erro de sublinhamento
                        androidx.compose.animation.AnimatedVisibility(
                            visible = animarIconesTopo,
                            enter = fadeIn(tween(800))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                UserIconWithLabel(Icons.Outlined.Info, "Sobre o App") { showSobreNosDialog = true }
                                UserIconWithLabel(Icons.Outlined.Phone, "Paróquia") { showContatosDialog = true }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = animarImagem,
                            enter = fadeIn(tween(1000)) + scaleIn(initialScale = 0.9f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.imagem_crisma),
                                contentDescription = "Logo Crisma",
                                modifier = Modifier.fillMaxWidth().height(180.dp)
                            )
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = animarTextosSuperior,
                        enter = fadeIn(tween(1000)) + slideInVertically { it / 4 }
                    ) {
                        Column {
                            Text("\nOlá, bem-vindo ao CrismAPP!", fontSize = 22.sp, color = Color.White, fontFamily = customFont, fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = Crisma_Gold, thickness = 2.dp, modifier = Modifier.fillMaxWidth(0.7f).padding(vertical = 12.dp))
                            Text("Selecione seu perfil para continuar sua jornada espiritual.", fontSize = 16.sp, color = Color.White, fontFamily = customFont)
                        }
                    }
                }
            }

            // ÁREA INFERIOR (35%) - Branca
            Box(
                modifier = Modifier.fillMaxWidth().weight(0.35f).background(Color.White),
                contentAlignment = Alignment.BottomCenter
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = animarBotaoSair,
                    enter = fadeIn(tween(1000)) + slideInVertically { 40 }
                ) {
                    Surface(
                        onClick = { if (context is Activity) context.finish() },
                        modifier = Modifier.padding(bottom = 110.dp).height(48.dp).fillMaxWidth(0.45f),
                        shape = RoundedCornerShape(12.dp),
                        color = Light_Gray_Darker,
                        shadowElevation = 2.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(
                                text = "Sair ",
                                color = Crisma_Primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // BARRA CENTRAL FLUTUANTE (Lógica CirculAPP)
        val density = LocalDensity.current
        val offsetY = with(density) { (screenHeight * 0.575f).roundToPx() }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.08f)
                .offset { IntOffset(0, offsetY) }
                .background(Crisma_Primary)
        ) {
            Button(
                onClick = { selectedOption = "Crismando"; onCrismandoSelected() },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedOption == "Crismando") Color(0xFFE0E0E0) else Color.Transparent),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text(
                    "Crismando",
                    color = if (selectedOption == "Crismando") Crisma_Primary else Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(if (animarLabelsBotoes) 1f else 0f)
                )
            }
            Box(Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.3f)))
            Button(
                onClick = { selectedOption = "Catequista"; onCatequistaSelected() },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedOption == "Catequista") Color(0xFFE0E0E0) else Color.Transparent),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text(
                    "Catequista",
                    color = if (selectedOption == "Catequista") Crisma_Primary else Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(if (animarLabelsBotoes) 1f else 0f)
                )
            }
        }
    }

    // Diálogos (Mantidos como estavam)
    if (showSobreNosDialog) {
        AlertDialog(
            onDismissRequest = { showSobreNosDialog = false },
            confirmButton = { TextButton(onClick = { showSobreNosDialog = false }) { Text("Entendido") } },
            title = { Text("Sobre o CrismAPP") },
            text = { Text(
                "O CrismAPP foi idealizado para modernizar e fortalecer a comunicação na jornada espiritual da nossa Paróquia.\n\n" +
                        ". Desenvolvimento:\nEmanuel Barbosa\n(github.com/Emanuel-dev-silva)\n\n" +
                        ". Gestão de Requisitos:\nVictor Lima\n\n"

            ) }
        )
    }
    if (showContatosDialog) {
        AlertDialog(
            onDismissRequest = { showContatosDialog = false },
            confirmButton = { TextButton(onClick = { showContatosDialog = false }) { Text("Fechar") } },
            title = { Text("Contatos da Paróquia") },
            text = { Text(
                ". Paróquia Santo Antônio\n" +
                        "Tiúma, São Lourenço da Mata - PE\n\n" +
                        ". Secretaria e WhatsApp:\n" +
                        "(81) 9 8593-9076\n\n" +
                        ". Horário de Atendimento:\n" +
                        "Terça a Sábado: 08h às 12h"
            ) }
        )
    }
}