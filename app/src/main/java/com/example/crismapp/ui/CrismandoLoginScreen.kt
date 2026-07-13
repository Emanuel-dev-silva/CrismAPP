package com.example.crismapp.ui

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Phone
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
import java.util.Locale

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)
private val customFont = FontFamily.Default

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrismandoLoginScreen(
    navController: NavController
) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    // Código usado para localizar o documento na coleção "usuarios".
    var codigoMatricula by remember {
        mutableStateOf("")
    }

    // Estados de carregamento e feedback.
    var carregando by remember {
        mutableStateOf(false)
    }

    var mensagemErro by remember {
        mutableStateOf<String?>(null)
    }

    var showSobreNosDialog by remember {
        mutableStateOf(false)
    }

    var showContatosDialog by remember {
        mutableStateOf(false)
    }

    var animarImagem by remember {
        mutableStateOf(false)
    }

    var animarTextos by remember {
        mutableStateOf(false)
    }

    var animarFormulario by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window

        window.statusBarColor = Crisma_Primary.toArgb()

        WindowCompat
            .getInsetsController(window, view)
            .isAppearanceLightStatusBars = false

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
            modifier = Modifier.fillMaxSize()
        ) {

            // =====================================================
            // ÁREA SUPERIOR
            // =====================================================

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
                        icon = Icons.Outlined.Info,
                        label = "Sobre o App",
                        onClick = {
                            showSobreNosDialog = true
                        }
                    )

                    UserIconWithLabel(
                        icon = Icons.Outlined.Phone,
                        label = "Contatos",
                        onClick = {
                            showContatosDialog = true
                        }
                    )
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
                                text = "\nLogin do Crismando",
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
                                text = "\"Recebereis a força do Espírito Santo.\" \n(At 1,8)",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontFamily = customFont
                            )
                        }
                    }
                }
            }

            // =====================================================
            // BARRA CENTRAL
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
                            // Já está na área de login do crismando.
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Light_Gray_Darker
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = "Crismando",
                            color = Crisma_Primary,
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
                        onClick = {
                            navController.navigate("LoginCatequista") {
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
                            text = "Catequista",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // =====================================================
            // ÁREA INFERIOR
            // =====================================================

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
                            onValueChange = { textoDigitado ->
                                codigoMatricula = textoDigitado
                                    .uppercase(Locale.ROOT)
                                    .replace(" ", "")
                                    .trim()

                                mensagemErro = null
                            },
                            label = {
                                Text(
                                    text = "Código de Matrícula (Ex: CX-1234)",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Key,
                                    contentDescription = null,
                                    tint = Crisma_Primary
                                )
                            },
                            placeholder = {
                                Text(text = "CX-XXXX")
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
                            enabled = !carregando
                        )

                        mensagemErro?.let { erro ->
                            Text(
                                text = erro,
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("userSelection") {
                                        popUpTo("crismandoLoginScreen") {
                                            inclusive = true
                                        }

                                        launchSingleTop = true
                                    }
                                },
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 1.dp
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Light_Gray_Darker
                                ),
                                shape = RoundedCornerShape(4.dp),
                                enabled = !carregando
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = null,
                                    tint = Crisma_Primary,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Text(
                                    text = "Voltar",
                                    color = Crisma_Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    val matriculaNormalizada = codigoMatricula
                                        .uppercase(Locale.ROOT)
                                        .replace(" ", "")
                                        .trim()

                                    if (matriculaNormalizada.isBlank()) {
                                        mensagemErro =
                                            "Insira o seu código de acesso."

                                        return@Button
                                    }

                                    if (!matriculaNormalizada.startsWith("CX-")) {
                                        mensagemErro =
                                            "Formato inválido. O código deve iniciar com 'CX-'"

                                        return@Button
                                    }

                                    carregando = true
                                    mensagemErro = null

                                    db.collection("usuarios")
                                        .document(matriculaNormalizada)
                                        .get()
                                        .addOnSuccessListener { documento ->
                                            if (!documento.exists()) {
                                                carregando = false
                                                mensagemErro =
                                                    "Código inválido ou não cadastrado."

                                                return@addOnSuccessListener
                                            }

                                            /*
                                             * O campo "ativo" é opcional.
                                             *
                                             * Alunos antigos que não possuem esse campo
                                             * serão considerados ativos normalmente.
                                             */
                                            val usuarioAtivo =
                                                documento.getBoolean("ativo") ?: true

                                            if (!usuarioAtivo) {
                                                carregando = false
                                                mensagemErro =
                                                    "Este cadastro está desativado. Procure a coordenação."

                                                return@addOnSuccessListener
                                            }

                                            /*
                                             * A matrícula é enviada na rota.
                                             *
                                             * A CrismandoScreen usará esse valor para:
                                             *
                                             * - buscar o nome do crismando;
                                             * - descobrir sua turma;
                                             * - carregar avisos da turma;
                                             * - carregar frequência individual;
                                             * - carregar carnês e documentos.
                                             */
                                            val matriculaCodificada =
                                                Uri.encode(matriculaNormalizada)

                                            carregando = false

                                            navController.navigate(
                                                "crismandoScreen?matricula=$matriculaCodificada"
                                            ) {
                                                popUpTo("crismandoLoginScreen") {
                                                    inclusive = true
                                                }

                                                launchSingleTop = true
                                            }
                                        }
                                        .addOnFailureListener { erro ->
                                            carregando = false

                                            mensagemErro =
                                                if (
                                                    erro.message
                                                        ?.contains(
                                                            other = "PERMISSION_DENIED",
                                                            ignoreCase = true
                                                        ) == true
                                                ) {
                                                    "Sem permissão para acessar os cadastros."
                                                } else {
                                                    "Falha na conexão. Verifique sua internet."
                                                }
                                        }
                                },
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 1.dp
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Crisma_Primary
                                ),
                                shape = RoundedCornerShape(4.dp),
                                enabled = !carregando
                            ) {
                                if (carregando) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Entrar",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(
                                        modifier = Modifier.width(8.dp)
                                    )

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Login,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =====================================================
    // DIÁLOGO SOBRE O APLICATIVO
    // =====================================================

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
                    text = "O CrismAPP foi idealizado para modernizar e fortalecer a comunicação na jornada espiritual da nossa Paróquia.\n\n" +
                            ". Desenvolvimento:\n" +
                            "Emanuel Barbosa\n" +
                            "(github.com/Emanuel-dev-silva)\n\n" +
                            ". Gestão de Requisitos:\n" +
                            "Victor Lima"
                )
            }
        )
    }

    // =====================================================
    // DIÁLOGO DE CONTATOS
    // =====================================================

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
                    text = ". Paróquia Santo Antônio\n" +
                            "Tiúma, São Lourenço da Mata - PE\n\n" +
                            ". Secretaria e WhatsApp:\n" +
                            "(81) 9 8593-9076\n\n" +
                            ". Horário de Atendimento:\n" +
                            "Terça a Sábado: 08h às 12h"
                )
            }
        )
    }
}