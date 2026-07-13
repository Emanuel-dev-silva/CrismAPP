package com.example.crismapp.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
private val customFont = FontFamily.Default

@Composable
fun CatequistaOptionsScreen(navController: NavController) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

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

    var animarBotoes by remember {
        mutableStateOf(false)
    }

    var animarIconesTopo by remember {
        mutableStateOf(true)
    }

    val catequista = FirebaseAuthRepository.catequistaAtual

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window

        window.statusBarColor = Crisma_Primary.toArgb()

        WindowCompat.getInsetsController(
            window,
            view
        ).isAppearanceLightStatusBars = false

        /*
         * Não basta existir um usuário no Firebase Auth.
         * O perfil também precisa existir e estar ativo.
         */
        if (FirebaseAuthRepository.catequistaAtual == null) {
            FirebaseAuthRepository.restaurarSessao(
                onSuccess = {},
                onSemSessao = {
                    navController.navigate("loginCatequista") {
                        popUpTo(0) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                },
                onError = { erro ->
                    Toast.makeText(
                        context,
                        erro.message ?: "Acesso não autorizado.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.navigate("loginCatequista") {
                        popUpTo(0) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        delay(100)
        animarImagem = true

        delay(200)
        animarIconesTopo = true

        delay(400)
        animarTextos = true

        delay(600)
        animarBotoes = true
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
                if (animarIconesTopo) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(top = 20.dp),
                        horizontalArrangement =
                        Arrangement.SpaceBetween
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
                            contentDescription = "Logo CrismAPP",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    if (animarTextos) {
                        Column {
                            Text(
                                text = "\nPainel do Catequista",
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

                            val nome = catequista
                                ?.nome
                                .orEmpty()
                                .ifBlank { "Catequista" }

                            val nivel = if (
                                catequista?.possuiPermissaoTotal() == true
                            ) {
                                "Permissão total"
                            } else {
                                "Permissão comum"
                            }

                            Text(
                                text = "Olá, $nome — $nivel",
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
                                "turmaJovemScreen"
                            )
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
                            text = "Turma Jovem",
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
                        onClick = {
                            navController.navigate(
                                "turmaAdultaScreen"
                            )
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
                            text = "Turma Adulta",
                            color = Color.White,
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
                contentAlignment = Alignment.Center
            ) {
                if (animarBotoes) {
                    ExitMenuCard(
                        title = "Sair",
                        icon = Icons.Outlined.ArrowBack,
                        modifier = Modifier.width(160.dp),
                        onClick = {
                            FirebaseAuthRepository.sair()

                            navController.navigate(
                                "loginCatequista"
                            ) {
                                popUpTo(0) {
                                    inclusive = true
                                }

                                launchSingleTop = true
                            }
                        }
                    )
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