package com.example.crismapp.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatequistaLoginScreen(navController: NavController) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }

    var animarImagem by remember { mutableStateOf(false) }
    var animarTextos by remember { mutableStateOf(false) }
    var animarFormulario by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window
        window.statusBarColor = Crisma_Primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

        delay(100); animarImagem = true
        delay(200); animarTextos = true
        delay(300); animarFormulario = true
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
                                "\nLogin do Catequista",
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
                                "\"Não fostes vós que me escolhestes, mas fui eu que vos escolhi.\"\n(Jo 15,16) ",
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
                            navController.navigate("crismandoLoginScreen") {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Crismando", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Box(Modifier.width(1.dp).fillMaxHeight().background(Crisma_Primary.copy(alpha = 0.3f)))

                    Button(
                        onClick = { /* Já está no Catequista */ },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Light_Gray_Darker),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Catequista", color = Crisma_Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- ÁREA INFERIOR (35%) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .background(Color.White),
                contentAlignment = Alignment.TopCenter
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = animarFormulario,
                    enter = fadeIn(tween(900)) + slideInVertically { 20 }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {

                        // Campo Usuário
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Usuário", fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = Crisma_Primary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp)
                                .border(2.dp, Crisma_Primary, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedLabelColor = Crisma_Primary,
                                unfocusedLabelColor = Crisma_Primary, // Rótulo em vermelho quando não focado
                                focusedTextColor = Crisma_Primary, // Texto digitado em vermelho quando focado
                                unfocusedTextColor = Crisma_Primary, // Texto digitado em vermelho quando não focado
                                cursorColor = Crisma_Primary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo Senha
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Senha", fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Crisma_Primary) },
                            trailingIcon = {
                                val icon = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = icon, contentDescription = null, tint = Crisma_Primary) // Ícone do olho em vermelho
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp)
                                .border(2.dp, Crisma_Primary, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedLabelColor = Crisma_Primary,
                                unfocusedLabelColor = Crisma_Primary, // Rótulo em vermelho quando não focado
                                focusedTextColor = Crisma_Primary, // Texto da senha em vermelho quando focado
                                unfocusedTextColor = Crisma_Primary, // Texto da senha em vermelho quando não focado
                                cursorColor = Crisma_Primary
                            ),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Botões de Ação
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("userSelection") {
                                        popUpTo("LoginCatequista") { inclusive = true }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Light_Gray_Darker),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    "Voltar",
                                    color = Crisma_Primary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    navController.navigate("catequistaOptions") {
                                        popUpTo("LoginCatequista") { inclusive = true }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    "Entrar",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
            text = { Text(". Paróquia Santo Antônio\nTiúma, São Lourenço da Mata - PE\n\n. Secretaria e WhatsApp:\n(81) 9 8593-9076\n\n. Horário de Atendimento:\nTerça a Sábado: 08h às 12h") }
        )
    }
}