package com.example.crismapp.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun TurmaJovemScreen(navController: NavController) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }

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
                        Image(
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
                                "\nGestão: Turma Jovem",
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
                                "\"Ensina o jovem no caminho em que deve andar.\" \n(Pr 22,6)",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontFamily = customFont
                            )
                        }
                    }
                }
            }

            // --- BARRA CENTRAL ---
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
                        onClick = { },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Light_Gray_Darker),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Turma Jovem", color = Crisma_Primary, fontWeight = FontWeight.Bold)
                    }

                    Box(Modifier.width(1.dp).fillMaxHeight().background(Crisma_Primary.copy(alpha = 0.3f)))

                    Button(
                        onClick = {
                            navController.navigate("turmaAdultaScreen") {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Turma Adulta", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ÁREA INFERIOR (35%)
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
                        // Linha 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmallMenuCard(title = "Frequência", icon = Icons.Outlined.CheckCircle, modifier = Modifier.weight(1f)) { }
                            SmallMenuCard(title = "Turmas", icon = Icons.Outlined.Groups, modifier = Modifier.weight(1f)) { }
                            SmallMenuCard(title = "Avisos", icon = Icons.Outlined.Notifications, modifier = Modifier.weight(1f)) { }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Linha 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmallMenuCard(title = "Financeiro", icon = Icons.Outlined.Payments, modifier = Modifier.weight(1f)) { }
                            SmallMenuCard(title = "Dados", icon = Icons.Outlined.BarChart, modifier = Modifier.weight(1f)) { }
                            SmallMenuCard(title = "Voltar", icon = Icons.Outlined.ArrowBack, modifier = Modifier.weight(1f)) {
                                // Correção aqui: utilizando a rota exata "catequistaOptions" definida no seu NavGraph
                                navController.navigate("catequistaOptions") {
                                    popUpTo("turmaJovemScreen") { inclusive = true }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---
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
            text = { Text(". Paróquia Santo Antônio\nTiúma, São Lourenço da Mata - PE\n\n. Secretaria e WhatsApp:\n(81) 9 8593-9076\n\n. Horário de Atendimento:\nTerça a Sábado: 08h às 12h")
    }
        )
    }
}