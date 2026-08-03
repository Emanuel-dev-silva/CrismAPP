package com.example.crismapp.ui

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.example.crismapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)
private val customFont = FontFamily.Default

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun CatequistaLoginScreen(navController: NavController) {
    val view = LocalView.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val usernameBringIntoViewRequester = remember {
        BringIntoViewRequester()
    }

    val passwordBringIntoViewRequester = remember {
        BringIntoViewRequester()
    }

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
         * Sempre inicia sem uma sessão antiga de catequista.
         */
        FirebaseAuthRepository.sair()

        delay(100)
        animarImagem = true

        delay(200)
        animarTextos = true

        delay(300)
        animarFormulario = true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding()
        ) {

            // =====================================================
            // CABEÇALHO
            // =====================================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.617f)
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

            // =====================================================
            // ABAS CRISMANDO / CATEQUISTA
            //
            // Mesma estrutura usada nas demais telas do aplicativo:
            // os botões possuem a altura completa e ficam metade
            // sobre o vermelho e metade sobre a área branca.
            // =====================================================

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
                            FirebaseAuthRepository.sair()

                            navController.navigate(
                                "crismandoLoginScreen"
                            ) {
                                popUpTo("loginCatequista") {
                                    inclusive = true
                                }

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

            // =====================================================
            // FORMULÁRIO
            // =====================================================

            /*
             * O formulário sobe exatamente a metade da altura das abas.
             * Isso ocupa o espaço que antes aparecia como uma faixa branca
             * vazia, sem cortar nem descentralizar os botões.
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = screenHeight * 0.35f)
                    .offset(y = -(screenHeight * 0.04f))
                    .background(Color.White),
                contentAlignment = Alignment.TopCenter
            ) {
                if (animarFormulario) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 28.dp,
                                end = 28.dp,
                                top = 8.dp,
                                bottom = 6.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        color = Crisma_Primary.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = Crisma_Primary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(9.dp))

                            Column {
                                Text(
                                    text = "Acesso do catequista",
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Entre com seu usuário e PIN",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    lineHeight = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username =
                                    FirebaseAuthRepository.normalizarLogin(it)

                                mensagemErro = ""
                            },
                            label = {
                                Text(
                                    text = "Nome de usuário",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                color = Color(0xFF4F4F4F)
                            ),
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = Crisma_Primary.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(9.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = Crisma_Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(
                                    usernameBringIntoViewRequester
                                )
                                .onFocusChanged { estadoFoco ->
                                    if (estadoFoco.isFocused) {
                                        coroutineScope.launch {
                                            delay(250L)
                                            usernameBringIntoViewRequester
                                                .bringIntoView()
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFBFBFB),
                                unfocusedContainerColor = Color(0xFFFBFBFB),
                                focusedBorderColor = Crisma_Primary,
                                unfocusedBorderColor = Color(0xFFECECEC),
                                focusedLabelColor = Color(0xFF5F5F5F),
                                unfocusedLabelColor = Color(0xFF707070),
                                cursorColor = Crisma_Primary
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    focusManager.moveFocus(
                                        FocusDirection.Down
                                    )
                                }
                            ),
                            singleLine = true,
                            enabled = !carregandoLogin
                        )

                        Spacer(modifier = Modifier.height(7.dp))

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
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                color = Color(0xFF4F4F4F)
                            ),
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = Crisma_Primary.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(9.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = Crisma_Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            trailingIcon = {
                                val icone = if (passwordVisible) {
                                    Icons.Outlined.Visibility
                                } else {
                                    Icons.Outlined.VisibilityOff
                                }

                                IconButton(
                                    onClick = {
                                        passwordVisible = !passwordVisible
                                    }
                                ) {
                                    Icon(
                                        imageVector = icone,
                                        contentDescription = null,
                                        tint = Crisma_Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(
                                    passwordBringIntoViewRequester
                                )
                                .onFocusChanged { estadoFoco ->
                                    if (estadoFoco.isFocused) {
                                        coroutineScope.launch {
                                            delay(250L)
                                            passwordBringIntoViewRequester
                                                .bringIntoView()
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFBFBFB),
                                unfocusedContainerColor = Color(0xFFFBFBFB),
                                focusedBorderColor = Crisma_Primary,
                                unfocusedBorderColor = Color(0xFFECECEC),
                                focusedLabelColor = Color(0xFF5F5F5F),
                                unfocusedLabelColor = Color(0xFF707070),
                                cursorColor = Crisma_Primary
                            ),
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            singleLine = true,
                            enabled = !carregandoLogin
                        )

                        if (mensagemErro.isNotBlank()) {
                            Text(
                                text = mensagemErro,
                                color = Crisma_Primary,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(9.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                onClick = {
                                    navController.navigate(
                                        "userSelection"
                                    ) {
                                        popUpTo("loginCatequista") {
                                            inclusive = true
                                        }
                                    }
                                },
                                enabled = !carregandoLogin,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF8F8F8),
                                    disabledContainerColor = Color(0xFFF5F5F5)
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 1.dp
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Color(0xFFEEEEEE)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector =
                                        Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = null,
                                        tint = Crisma_Primary,
                                        modifier = Modifier.size(17.dp)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = "Voltar",
                                        color = Crisma_Primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Card(
                                onClick = {
                                    focusManager.clearFocus()
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
                                enabled =
                                username.isNotBlank() &&
                                        password.length == 6 &&
                                        !carregandoLogin,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Crisma_Primary,
                                    disabledContainerColor = Color(0xFFF1F1F1)
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 1.dp
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (carregandoLogin) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Entrar",
                                            color = if (
                                                username.isNotBlank() &&
                                                password.length == 6
                                            ) {
                                                Color.White
                                            } else {
                                                Color.Gray
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Icon(
                                            imageVector =
                                            Icons.AutoMirrored.Outlined.Login,
                                            contentDescription = null,
                                            tint = if (
                                                username.isNotBlank() &&
                                                password.length == 6
                                            ) {
                                                Color.White
                                            } else {
                                                Color.Gray
                                            },
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
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