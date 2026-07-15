package com.example.crismapp.ui

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

private const val PREFS_LOGIN_CRISMANDO = "prefs_login_crismando"
private const val CHAVE_TENTATIVAS_INVALIDAS = "tentativas_invalidas"
private const val CHAVE_BLOQUEADO_ATE = "bloqueado_ate"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrismandoLoginScreen(
    navController: NavController
) {
    val view = LocalView.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    /*
     * O bloqueio é salvo no aparelho para continuar valendo mesmo
     * depois de fechar e abrir o aplicativo.
     *
     * Escalonamento:
     * - 3 erros: 1 minuto;
     * - mais 2 erros: 5 minutos;
     * - mais 2 erros: 1 hora.
     *
     * Após cumprir o bloqueio de 1 hora, a contagem recomeça do zero.
     * Um login correto também limpa toda a contagem.
     */
    val preferenciasLogin = remember {
        context.getSharedPreferences(
            PREFS_LOGIN_CRISMANDO,
            Context.MODE_PRIVATE
        )
    }

    var tentativasInvalidas by remember {
        mutableStateOf(
            preferenciasLogin.getInt(
                CHAVE_TENTATIVAS_INVALIDAS,
                0
            )
        )
    }

    var bloqueadoAte by remember {
        mutableStateOf(
            preferenciasLogin.getLong(
                CHAVE_BLOQUEADO_ATE,
                0L
            )
        )
    }

    var segundosRestantesBloqueio by remember {
        mutableStateOf(0)
    }

    val bloqueioAtivo =
        segundosRestantesBloqueio > 0

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

    fun formatarTempoBloqueio(segundosTotais: Int): String {
        val horas = segundosTotais / 3600
        val minutos = (segundosTotais % 3600) / 60
        val segundos = segundosTotais % 60

        return when {
            horas > 0 ->
                String.format("%02d:%02d:%02d", horas, minutos, segundos)

            else ->
                String.format("%02d:%02d", minutos, segundos)
        }
    }

    fun registrarTentativaInvalida() {
        val novaQuantidade = tentativasInvalidas + 1
        tentativasInvalidas = novaQuantidade

        preferenciasLogin.edit()
            .putInt(
                CHAVE_TENTATIVAS_INVALIDAS,
                novaQuantidade
            )
            .apply()

        val duracaoBloqueio = when (novaQuantidade) {
            3 -> 60_000L
            5 -> 5 * 60_000L
            7 -> 60 * 60_000L
            else -> 0L
        }

        if (duracaoBloqueio > 0L) {
            val novoBloqueioAte =
                System.currentTimeMillis() + duracaoBloqueio

            bloqueadoAte = novoBloqueioAte

            preferenciasLogin.edit()
                .putLong(
                    CHAVE_BLOQUEADO_ATE,
                    novoBloqueioAte
                )
                .apply()

            mensagemErro = null
        } else {
            mensagemErro = when (novaQuantidade) {
                1 ->
                    "Código inválido. Restam 2 tentativas antes do bloqueio."

                2 ->
                    "Código inválido. Resta 1 tentativa antes do bloqueio de 1 minuto."

                4 ->
                    "Código inválido. Resta 1 tentativa antes do bloqueio de 5 minutos."

                6 ->
                    "Código inválido. Resta 1 tentativa antes do bloqueio de 1 hora."

                else ->
                    "Código inválido ou não cadastrado."
            }
        }
    }

    fun limparBloqueioETentativas() {
        tentativasInvalidas = 0
        bloqueadoAte = 0L
        segundosRestantesBloqueio = 0

        preferenciasLogin.edit()
            .remove(CHAVE_TENTATIVAS_INVALIDAS)
            .remove(CHAVE_BLOQUEADO_ATE)
            .apply()
    }

    /*
     * Mantém um contador regressivo visível.
     * Nos bloqueios de 1 e 5 minutos, a quantidade de erros é mantida.
     * Quando termina o bloqueio de 1 hora, a contagem é zerada.
     */
    LaunchedEffect(bloqueadoAte) {
        while (bloqueadoAte > 0L) {
            val restanteMillis =
                bloqueadoAte - System.currentTimeMillis()

            if (restanteMillis <= 0L) {
                segundosRestantesBloqueio = 0

                preferenciasLogin.edit()
                    .remove(CHAVE_BLOQUEADO_ATE)
                    .apply()

                bloqueadoAte = 0L

                if (tentativasInvalidas >= 7) {
                    tentativasInvalidas = 0

                    preferenciasLogin.edit()
                        .remove(CHAVE_TENTATIVAS_INVALIDAS)
                        .apply()
                }

                break
            }

            segundosRestantesBloqueio =
                ((restanteMillis + 999L) / 1000L).toInt()

            delay(1_000L)
        }
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
                            navController.navigate("loginCatequista") {
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

            /*
             * A parte branca sobe metade da altura das abas.
             * Assim, o formulário começa logo abaixo dos botões,
             * sem a faixa vazia que aparecia anteriormente.
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .offset(y = -(screenHeight * 0.04f))
                    .background(Color.White)
                    .imePadding()
                    .navigationBarsPadding(),
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
                                bottom = 10.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
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
                                    imageVector = Icons.Outlined.Key,
                                    contentDescription = null,
                                    tint = Crisma_Primary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(9.dp))

                            Column {
                                Text(
                                    text = "Acesso do crismando",
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Entre com seu código de matrícula",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    lineHeight = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = codigoMatricula,
                            onValueChange = { textoDigitado ->
                                /*
                                 * O prefixo CX- fica fixo no campo.
                                 * O usuário digita somente os números.
                                 */
                                codigoMatricula = textoDigitado
                                    .filter { it.isDigit() }
                                    .take(8)

                                mensagemErro = null
                            },
                            label = {
                                Text(
                                    text = "Código de matrícula",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                color = Color(0xFF4F4F4F),
                                fontWeight = FontWeight.Bold
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
                                        imageVector = Icons.Outlined.Key,
                                        contentDescription = null,
                                        tint = Crisma_Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            prefix = {
                                Text(
                                    text = "CX-",
                                    color = Color(0xFF4F4F4F),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "1234",
                                    color = Color(0xFF8A8A8A),
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFFFDFD),
                                unfocusedContainerColor = Color(0xFFFFFDFD),
                                focusedBorderColor = Crisma_Primary,
                                unfocusedBorderColor = Color(0xFFECECEC),
                                focusedLabelColor = Color(0xFF5F5F5F),
                                unfocusedLabelColor = Color(0xFF707070),
                                cursorColor = Crisma_Primary
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            enabled = !carregando && !bloqueioAtivo
                        )

                        if (bloqueioAtivo) {
                            Text(
                                text =
                                "Muitas tentativas incorretas. Tente novamente em ${
                                    formatarTempoBloqueio(
                                        segundosRestantesBloqueio
                                    )
                                }.",
                                color = Crisma_Primary,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            mensagemErro?.let { erro ->
                                Text(
                                    text = erro,
                                    color = Crisma_Primary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                onClick = {
                                    navController.navigate("userSelection") {
                                        popUpTo("crismandoLoginScreen") {
                                            inclusive = true
                                        }

                                        launchSingleTop = true
                                    }
                                },
                                enabled = !carregando,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White,
                                    disabledContainerColor = Color(0xFFF7F7F7)
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 1.dp
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Color(0xFFF0F0F0)
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
                                    if (bloqueioAtivo) {
                                        return@Card
                                    }

                                    val numeroMatricula =
                                        codigoMatricula.trim()

                                    if (numeroMatricula.isBlank()) {
                                        mensagemErro =
                                            "Digite o número da sua matrícula."

                                        return@Card
                                    }

                                    val matriculaNormalizada =
                                        "CX-$numeroMatricula"

                                    carregando = true
                                    mensagemErro = null

                                    db.collection("usuarios")
                                        .document(matriculaNormalizada)
                                        .get()
                                        .addOnSuccessListener { documento ->
                                            if (!documento.exists()) {
                                                carregando = false
                                                registrarTentativaInvalida()

                                                return@addOnSuccessListener
                                            }

                                            val usuarioAtivo =
                                                documento.getBoolean("ativo") ?: true

                                            if (!usuarioAtivo) {
                                                carregando = false
                                                mensagemErro =
                                                    "Este cadastro está desativado. Procure a coordenação."

                                                return@addOnSuccessListener
                                            }

                                            val matriculaCodificada =
                                                Uri.encode(matriculaNormalizada)

                                            carregando = false
                                            limparBloqueioETentativas()

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
                                enabled = !carregando && !bloqueioAtivo,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Crisma_Primary,
                                    disabledContainerColor = Color(0xFFE7E7E7)
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
                                    if (carregando) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Entrar",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Icon(
                                            imageVector =
                                            Icons.AutoMirrored.Outlined.Login,
                                            contentDescription = null,
                                            tint = Color.White,
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