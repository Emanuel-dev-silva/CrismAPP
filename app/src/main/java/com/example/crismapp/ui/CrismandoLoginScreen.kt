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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
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

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)
private val customFont = FontFamily.Default

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrismandoLoginScreen(navController: NavController) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val db = FirebaseFirestore.getInstance()

    // Estado único para a matrícula (Ex: CX-1234)
    var codigoMatricula by remember { mutableStateOf("") }

    // Estados de feedback do Firebase
    var carregando by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }

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
                    if (animarImagem) {
                        Image(
                            painter = painterResource(id = R.drawable.imagem_crisma),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .align(Alignment.CenterHorizontally)
                                .clickable {
                                    // Ajustado para a rota simples padrão aceita pelo seu NavGraph
                                    navController.navigate("crismandoScreen") {
                                        popUpTo("crismandoLoginScreen") { inclusive = true }
                                    }
                                }
                        )
                    }

                    if (animarTextos) {
                        Column {
                            Text(
                                "\nLogin do Crismando",
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
                                "\"Recebereis a força do Espírito Santo.\" \n(At 1,8)",
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
                        onClick = { /* Já está no Crismando */ },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Light_Gray_Darker),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Crismando", color = Crisma_Primary, fontWeight = FontWeight.Bold)
                    }

                    Box(Modifier.width(1.dp).fillMaxHeight().background(Crisma_Primary.copy(alpha = 0.3f)))

                    Button(
                        onClick = {
                            navController.navigate("LoginCatequista") {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Catequista", color = Color.White, fontWeight = FontWeight.Bold)
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
                if (animarFormulario) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        OutlinedTextField(
                            value = codigoMatricula,
                            onValueChange = { input ->
                                codigoMatricula = input.uppercase().replace(" ", "")
                                mensagemErro = null
                            },
                            label = { Text("Código de Matrícula (Ex: CX-1234)", fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null, tint = Crisma_Primary) },
                            placeholder = { Text("CX-XXXX") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(4.dp)),
                            shape = RoundedCornerShape(4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Crisma_Primary,
                                unfocusedBorderColor = Color(0xFFF0F0F0),
                                focusedLabelColor = Crisma_Primary,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = Crisma_Primary
                            ),
                            singleLine = true,
                            enabled = !carregando
                        )

                        if (mensagemErro != null) {
                            Text(
                                text = mensagemErro!!,
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("userSelection") {
                                        popUpTo("crismandoLoginScreen") { inclusive = true }
                                    }
                                },
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp),
                                modifier = Modifier.weight(1f).height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Light_Gray_Darker),
                                shape = RoundedCornerShape(4.dp),
                                enabled = !carregando
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Crisma_Primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Voltar", color = Crisma_Primary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (codigoMatricula.isBlank()) {
                                        mensagemErro = "Insira o seu código de acesso."
                                        return@Button
                                    }

                                    if (!codigoMatricula.startsWith("CX-")) {
                                        mensagemErro = "Formato inválido. O código deve iniciar com 'CX-'"
                                        return@Button
                                    }

                                    carregando = true
                                    mensagemErro = null

                                    db.collection("usuarios").document(codigoMatricula)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            carregando = false
                                            if (document != null && document.exists()) {
                                                // Ajustado para navegar de forma direta casando perfeitamente com a rota do NavGraph
                                                navController.navigate("crismandoScreen") {
                                                    popUpTo("crismandoLoginScreen") { inclusive = true }
                                                }
                                            } else {
                                                mensagemErro = "Código inválido ou não cadastrado."
                                            }
                                        }
                                        .addOnFailureListener {
                                            carregando = false
                                            mensagemErro = "Falha na conexão. Verifique sua internet."
                                        }
                                },
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp),
                                modifier = Modifier.weight(1f).height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                                shape = RoundedCornerShape(4.dp),
                                enabled = !carregando
                            ) {
                                if (carregando) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Entrar", color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.AutoMirrored.Outlined.Login, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
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
