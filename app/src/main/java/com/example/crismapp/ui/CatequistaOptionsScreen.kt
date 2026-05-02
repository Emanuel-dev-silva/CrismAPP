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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.example.crismapp.R
import kotlinx.coroutines.delay

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)
private val customFont = FontFamily.Default

@Composable
fun CatequistaOptionsScreen(navController: NavController) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Estados para Diálogos
    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }

    // Estados de Animação
    var animarImagem by remember { mutableStateOf(false) }
    var animarTextos by remember { mutableStateOf(false) }
    var animarBotoes by remember { mutableStateOf(false) }
    var animarIconesTopo by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window
        window.statusBarColor = Crisma_Primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

        delay(100); animarImagem = true
        delay(200); animarIconesTopo = true
        delay(400); animarTextos = true
        delay(600); animarBotoes = true
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
                // ÍCONES DE TOPO
                androidx.compose.animation.AnimatedVisibility(
                    visible = animarIconesTopo,
                    enter = fadeIn(tween(1200)),
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        UserIconWithLabel(Icons.Outlined.Info, "Sobre o App") { showSobreNosDialog = true }
                        UserIconWithLabel(Icons.Outlined.Phone, "Contatos") { showContatosDialog = true }
                    }
                }

                Column(modifier = Modifier.fillMaxSize().padding(top = 65.dp)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = animarImagem,
                        enter = fadeIn(tween(1500)) + scaleIn(initialScale = 0.9f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.imagem_crisma),
                            contentDescription = "Logo CrismAPP",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = animarTextos,
                        enter = fadeIn(tween(1200)) + slideInVertically { it / 3 }
                    ) {
                        Column {
                            Text(
                                "\nPainel do Catequista",
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
                                "Selecione a turma que deseja gerenciar hoje.",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontFamily = customFont
                            )
                        }
                    }
                }
            }

            // --- BARRA CENTRAL COM FUNDO BRANCO ---
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
                            navController.navigate("turmaJovemScreen")
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Turma Jovem", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Box(Modifier.width(1.dp).fillMaxHeight().background(Crisma_Primary.copy(alpha = 0.3f)))

                    Button(
                        onClick = {
                            navController.navigate("turmaAdultaScreen")
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Turma Adulta", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- ÁREA INFERIOR (35%) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .background(Color.White),
                contentAlignment = Alignment.Center // Centraliza o conteúdo do Box
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = animarBotoes,
                    enter = fadeIn(tween(900)) + slideInVertically { 20 }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center // Centraliza os itens verticalmente na Column
                    ) {
                        // BOTÃO RETANGULAR ESTILIZADO COM ÍCONE + TEXTO CENTRALIZADO
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    navController.navigate("loginCatequista") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Light_Gray_Darker),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowBack,
                                    contentDescription = "Sair",
                                    tint = Crisma_Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Sair",
                                    color = Crisma_Primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = customFont
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGOS
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
            title = { Text("Contatos", fontWeight = FontWeight.Bold) },
            text = { Text(". Paróquia Santo Antônio\nTiúma, São Lourenço da Mata - PE\n\n. Secretaria e WhatsApp:\n(81) 9 8593-9076\n\n. Horário de Atendimento:\nTerça a Sábado: 08h às 12h") }
        )
    }
}