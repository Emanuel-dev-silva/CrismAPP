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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
fun CatequistaLoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }

    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }

    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var animarImagem by remember { mutableStateOf(false) }
    var animarTextos by remember { mutableStateOf(false) }
    var animarCampos by remember { mutableStateOf(false) }
    var animarIconesTopo by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window
        window.statusBarColor = Crisma_Primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

        delay(100)
        animarImagem = true
        delay(200)
        animarTextos = true
        delay(400)
        animarIconesTopo = true
        delay(200)
        animarCampos = true
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
                        enter = fadeIn(tween(1200)) + scaleIn(initialScale = 0.9f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.imagem_crisma),
                            contentDescription = "Logo CrismAPP",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(top = 10.dp)
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = animarTextos,
                        enter = fadeIn(tween(200)) + slideInVertically { it / 3 }
                    ) {
                        Column {
                            Text("\nLogin do Catequista", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = customFont)
                            HorizontalDivider(
                                color = Crisma_Gold,
                                thickness = 2.dp,
                                modifier = Modifier.fillMaxWidth(0.76f).padding(vertical = 12.dp)
                            )
                            Text("Acesse para gerenciar sua turma.", fontSize = 16.sp, color = Color.White, fontFamily = customFont)
                        }
                    }
                }
            }

            // --- ÂNCORA DA BARRA CENTRAL ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.08f)
                        .offset(y = -(screenHeight * 0.04f))
                        .background(Crisma_Primary)
                ) {
                    Button(
                        onClick = {
                            navController.navigate("crismandoScreen") {
                                popUpTo("LoginCatequista") { inclusive = true }
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Crismando", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = customFont)
                    }

                    Box(Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.3f)))

                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Light_Gray_Darker),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Catequista", color = Crisma_Primary, fontWeight = FontWeight.Bold, fontFamily = customFont)
                    }
                }
            }

            // ÁREA INFERIOR (35%) - Ocupada pelos inputs de login
            Box(
                modifier = Modifier.fillMaxWidth().weight(0.35f).background(Color.White),
                contentAlignment = Alignment.TopCenter
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = animarCampos,
                    enter = fadeIn(tween(200)) + slideInVertically { 20 },
                    modifier = Modifier.padding(top = 0.dp) // Padding para não colar na barra central
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Usuário", fontFamily = customFont) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Crisma_Primary,
                                focusedLabelColor = Crisma_Primary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Senha", fontFamily = customFont) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Crisma_Primary,
                                focusedLabelColor = Crisma_Primary
                            )
                        )

                        if (loginError) {
                            Text("Credenciais inválidas", color = Color.Red, fontSize = 12.sp, fontFamily = customFont, modifier = Modifier.padding(top = 4.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("userSelection") {
                                        popUpTo("LoginCatequista") { inclusive = true }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Light_Gray_Darker)
                            ) {
                                Text("Voltar", color = Crisma_Primary, fontWeight = FontWeight.Bold, fontFamily = customFont)
                            }

                            Button(
                                onClick = { if(username.isEmpty()) loginError = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary)
                            ) {
                                Text("Entrar", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = customFont)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---
    if (showSobreNosDialog) {
        AlertDialog(
            onDismissRequest = { showSobreNosDialog = false },
            confirmButton = { TextButton(onClick = { showSobreNosDialog = false }) { Text("Entendido") } },
            title = { Text("Sobre o CrismAPP") },
            text = { Text(
                "O CrismAPP foi idealizado para modernizar e fortalecer a comunicação na jornada espiritual da nossa Paróquia.\n\n" +
                        ". Desenvolvimento:\nEmanuel Barbosa\n(github.com/Emanuel-dev-silva)\n\n" +
                        ". Gestão de Requisitos:\nVictor Lima"
            ) }
        )
    }
    if (showContatosDialog) {
        AlertDialog(
            onDismissRequest = { showContatosDialog = false },
            confirmButton = { TextButton(onClick = { showContatosDialog = false }) { Text("Fechar") } },
            title = { Text("Contatos da Paróquia") },
            text = { Text(
                ". Paróquia Santo Antônio\nTiúma, São Lourenço da Mata - PE\n\n. Secretaria e WhatsApp:\n(81) 9 8593-9076\n\n. Horário de Atendimento:\nTerça a Sábado: 08h às 12h"
            ) }
        )
    }
}

