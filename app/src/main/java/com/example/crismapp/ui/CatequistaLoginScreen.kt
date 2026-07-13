package com.example.crismapp.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var carregandoLogin by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf("") }

    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }

    var animarImagem by remember { mutableStateOf(false) }
    var animarTextos by remember { mutableStateOf(false) }
    var animarFormulario by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window
        window.statusBarColor = Crisma_Primary.toArgb()
        WindowCompat.getInsetsController(
            window,
            view
        ).isAppearanceLightStatusBars = false

        /*
         * Caso o catequista já tenha entrado anteriormente,
         * validamos novamente o perfil antes de abrir o painel.
         */
        if (FirebaseAuthRepository.possuiSessaoFirebase()) {
            FirebaseAuthRepository.restaurarSessao(
                onSuccess = {
                    navController.navigate("catequistaOptions") {
                        popUpTo("loginCatequista") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onSemSessao = {},
                onError = {}
            )
        }

        delay(100)
        animarImagem = true

        delay(200)
        animarTextos = true

        delay(300)
        animarFormulario = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

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
                    if (animarImagem) {
                        Image(
                            painter = painterResource(
                                id = R.drawable.imagem_crisma
                            ),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    if (animarTextos) {
                        Column {
                            Text(
                                text = "\nLogin do Catequista",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = customFont
                            )

                            HorizontalDivider(
                                color = Crisma_Gold,
                                thickness = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth(0.76f)
                                    .padding(vertical = 12.dp)
                            )

                            Text(
                                text = "\"Não fostes vós que me escolhestes, mas fui eu que vos escolhi.\"\n(Jo 15,16)",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontFamily = customFont
                            )
                        }
                    }
                }
            }

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
                            navController.navigate(
                                "crismandoLoginScreen"
                            ) {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Crisma_Primary
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = "Crismando",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
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
                        onClick = {},
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Light_Gray_Darker
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = "Catequista",
                            color = Crisma_Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username =
                                    FirebaseAuthRepository
                                        .normalizarLogin(it)
                                mensagemErro = ""
                            },
                            label = {
                                Text(
                                    text = "Nome de usuário",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Crisma_Primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(4.dp)
                                ),
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
                            enabled = !carregandoLogin
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { novoValor ->
                                password = novoValor
                                    .filter { it.isDigit() }
                                    .take(6)
                                mensagemErro = ""
                            },
                            label = {
                                Text(
                                    text = "PIN de 6 dígitos",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = Crisma_Primary
                                )
                            },
                            trailingIcon = {
                                val icone = if (passwordVisible) {
                                    Icons.Outlined.Visibility
                                } else {
                                    Icons.Outlined.VisibilityOff
                                }

                                IconButton(
                                    onClick = {
                                        passwordVisible =
                                            !passwordVisible
                                    }
                                ) {
                                    Icon(
                                        imageVector = icone,
                                        contentDescription = null,
                                        tint = Crisma_Primary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(4.dp)
                                ),
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
                            visualTransformation = if (
                                passwordVisible
                            ) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword
                            ),
                            singleLine = true,
                            enabled = !carregandoLogin
                        )

                        if (mensagemErro.isNotBlank()) {
                            Text(
                                text = mensagemErro,
                                color = Crisma_Primary,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate(
                                        "userSelection"
                                    ) {
                                        popUpTo("loginCatequista") {
                                            inclusive = true
                                        }
                                    }
                                },
                                elevation =
                                ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 1.dp
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Light_Gray_Darker
                                ),
                                shape = RoundedCornerShape(4.dp),
                                enabled = !carregandoLogin
                            ) {
                                Icon(
                                    imageVector =
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = null,
                                    tint = Crisma_Primary,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Voltar",
                                    color = Crisma_Primary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    carregandoLogin = true
                                    mensagemErro = ""

                                    FirebaseAuthRepository
                                        .entrarComLoginEPin(
                                            login = username,
                                            pin = password,
                                            onSuccess = {
                                                carregandoLogin = false

                                                navController.navigate(
                                                    "catequistaOptions"
                                                ) {
                                                    popUpTo(
                                                        "loginCatequista"
                                                    ) {
                                                        inclusive = true
                                                    }

                                                    launchSingleTop = true
                                                }
                                            },
                                            onError = { erro ->
                                                carregandoLogin = false
                                                mensagemErro =
                                                    erro.message
                                                        ?: "Não foi possível entrar."
                                            }
                                        )
                                },
                                elevation =
                                ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 1.dp
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Crisma_Primary
                                ),
                                shape = RoundedCornerShape(4.dp),
                                enabled =
                                username.isNotBlank() &&
                                        password.length == 6 &&
                                        !carregandoLogin
                            ) {
                                if (carregandoLogin) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Entrar",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Icon(
                                        imageVector =
                                        Icons.AutoMirrored.Outlined.Login,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "O acesso com Google será ativado na próxima etapa.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Crisma_Primary
                            ),
                            colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = Crisma_Primary
                            ),
                            shape = RoundedCornerShape(4.dp),
                            enabled = !carregandoLogin
                        ) {
                            Text(
                                text = "Entrar com Google — em breve",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

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
                    text = "Contatos",
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