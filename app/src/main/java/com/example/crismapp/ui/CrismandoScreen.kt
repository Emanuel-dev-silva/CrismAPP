package com.example.crismapp.ui

import android.app.Activity
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.crismapp.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)
private val customFont = FontFamily.Default

private const val TOTAL_PARCELAS_CARNE = 12

private fun converterDataFirebaseParaMillis(valor: Any?): Long {
    return when (valor) {
        is Timestamp -> valor.toDate().time
        is Number -> valor.toLong()
        else -> 0L
    }
}

private fun formatarDataPagamento(dataEmMillis: Long): String {
    if (dataEmMillis <= 0L) return ""

    val formato = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale("pt", "BR")
    )

    formato.timeZone = TimeZone.getTimeZone("America/Recife")

    return formato.format(Date(dataEmMillis))
}

data class FrequenciaItem(
    val title: String,
    val status: StatusFrequencia
)

data class CarneItem(
    val title: String,
    val isPaid: Boolean
)

data class DocumentoItem(
    val title: String,
    val status: StatusDocumento
)

private fun RegistroDocumento.ehDocumentoDoPadrinho(): Boolean {
    return id.contains("-PADRINHO-", ignoreCase = true) ||
            nome.contains("Padrinho", ignoreCase = true)
}

data class AvisoItem(
    val id: String,
    val text: String,
    val turmaId: String,
    val tipo: String = "TURMA",
    val linkUrl: String? = null,
    val dataCriacao: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrismandoScreen(navController: NavController) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val db = remember { FirebaseFirestore.getInstance() }

    /*
     * A matrícula chegou pela rota:
     * crismandoScreen?matricula=CX-1234
     */
    val backStackEntry by navController.currentBackStackEntryAsState()

    val matricula = remember(backStackEntry) {
        Uri.decode(
            backStackEntry
                ?.arguments
                ?.getString("matricula")
                .orEmpty()
        )
            .trim()
            .uppercase(Locale.ROOT)
    }

    // Dados do usuário autenticado.
    var userName by remember { mutableStateOf("Crismando") }
    var alunoId by remember { mutableStateOf("") }
    var turmaId by remember { mutableStateOf("") }
    var categoriaTurma by remember { mutableStateOf("") }

    // Estados dos diálogos.
    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }
    var showPresencasPopup by remember { mutableStateOf(false) }
    var showAvisosPopup by remember { mutableStateOf(false) }
    var showCarnePopup by remember { mutableStateOf(false) }
    var showDocumentosPopup by remember { mutableStateOf(false) }

    // Dados reais vindos do Firebase.
    var listaAvisosFirebase by remember { mutableStateOf(emptyList<AvisoItem>()) }
    var listaFrequenciasFirebase by remember { mutableStateOf(emptyList<RegistroFrequencia>()) }
    var listaEncontrosFirebase by remember { mutableStateOf(emptyList<RegistroEncontro>()) }
    var listaFinanceiroFirebase by remember { mutableStateOf(emptyList<RegistroFinanceiro>()) }
    var listaDocumentosFirebase by remember { mutableStateOf(emptyList<RegistroDocumento>()) }

    // Estados de carregamento das listagens.
    var carregandoFrequencias by remember { mutableStateOf(true) }
    var carregandoAvisos by remember { mutableStateOf(true) }
    var carregandoFinanceiro by remember { mutableStateOf(true) }
    var carregandoDocumentos by remember { mutableStateOf(true) }

    // Estados visuais existentes.
    var animarImagem by remember { mutableStateOf(false) }
    var animarTextos by remember { mutableStateOf(false) }
    var animarBotoesAcao by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window
        window.statusBarColor = Crisma_Primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

        delay(100)
        animarImagem = true

        delay(200)
        animarTextos = true

        delay(300)
        animarBotoesAcao = true
    }

    // =========================================================
    // 1. CARREGA O CRISMANDO QUE FEZ LOGIN
    // =========================================================

    LaunchedEffect(matricula) {
        if (matricula.isBlank()) {
            userName = "Crismando"
            alunoId = ""
            turmaId = ""
            return@LaunchedEffect
        }

        db.collection("usuarios")
            .document(matricula)
            .get()
            .addOnSuccessListener { documento ->
                if (!documento.exists()) {
                    userName = "Crismando"
                    alunoId = ""
                    turmaId = ""
                    return@addOnSuccessListener
                }

                userName = documento.getString("nome")
                    ?.takeIf { it.isNotBlank() }
                    ?: "Crismando"

                alunoId = documento.id
                turmaId = documento.getString("turmaId").orEmpty()
            }
            .addOnFailureListener {
                userName = "Crismando"
                alunoId = ""
                turmaId = ""
            }
    }

    // =========================================================
    // 2. DESCOBRE SE A TURMA É JOVEM OU ADULTA
    // =========================================================

    LaunchedEffect(turmaId) {
        if (turmaId.isBlank()) {
            categoriaTurma = ""
            return@LaunchedEffect
        }

        db.collection("turmas")
            .document(turmaId)
            .get()
            .addOnSuccessListener { documentoTurma ->
                categoriaTurma = documentoTurma
                    .getString("categoria")
                    .orEmpty()
                    .trim()
                    .lowercase(Locale.ROOT)
            }
            .addOnFailureListener {
                categoriaTurma = ""
            }
    }

    // =========================================================
    // 3. ENCONTROS DA TURMA DO CRISMANDO
    // =========================================================

    DisposableEffect(turmaId) {
        if (turmaId.isBlank()) {
            listaEncontrosFirebase = emptyList()
            onDispose { }
        } else {
            val listener = db.collection("encontros")
                .whereEqualTo("turmaId", turmaId)
                .addSnapshotListener { snapshot, erro ->
                    if (erro != null || snapshot == null) {
                        listaEncontrosFirebase = emptyList()
                        return@addSnapshotListener
                    }

                    listaEncontrosFirebase = snapshot.documents
                        .mapNotNull { documento ->
                            val numero = documento.getLong("numero")?.toInt() ?: 0

                            if (numero <= 0) {
                                null
                            } else {
                                RegistroEncontro(
                                    id = documento.id,
                                    numero = numero,
                                    dataManual = documento.getString("dataManual").orEmpty(),
                                    turmaId = documento.getString("turmaId").orEmpty(),
                                    dataCriacao = documento.getLong("dataCriacao") ?: 0L
                                )
                            }
                        }
                        .sortedBy { it.numero }
                }

            onDispose {
                listener.remove()
            }
        }
    }

    // =========================================================
    // 4. FREQUÊNCIA INDIVIDUAL DO CRISMANDO
    // =========================================================

    DisposableEffect(alunoId, turmaId) {
        if (alunoId.isBlank()) {
            listaFrequenciasFirebase = emptyList()
            carregandoFrequencias = false
            onDispose { }
        } else {
            carregandoFrequencias = true

            val listener = db.collection("frequencias")
                .whereEqualTo("alunoId", alunoId)
                .addSnapshotListener { snapshot, erro ->
                    carregandoFrequencias = false

                    if (erro != null || snapshot == null) {
                        listaFrequenciasFirebase = emptyList()
                        return@addSnapshotListener
                    }

                    listaFrequenciasFirebase = snapshot.documents
                        .map { documento ->
                            RegistroFrequencia(
                                id = documento.id,
                                alunoId = documento.getString("alunoId").orEmpty(),
                                turmaId = documento.getString("turmaId").orEmpty(),
                                encontro = documento.getLong("encontro")?.toInt() ?: 0,
                                status = documento.getString("status")
                                    ?: StatusFrequencia.NENHUM.name,
                                dataRegistro = documento.getLong("dataAtualizacao")
                                    ?: documento.getLong("dataRegistro")
                                    ?: 0L
                            )
                        }
                        .filter { registro ->
                            registro.encontro > 0 &&
                                    (registro.turmaId.isBlank() || registro.turmaId == turmaId)
                        }
                        .sortedBy { it.encontro }
                }

            onDispose {
                listener.remove()
            }
        }
    }

    // =========================================================
    // 5. FINANCEIRO INDIVIDUAL DO CRISMANDO
    // =========================================================

    DisposableEffect(alunoId, turmaId) {
        if (alunoId.isBlank()) {
            listaFinanceiroFirebase = emptyList()
            carregandoFinanceiro = false
            onDispose { }
        } else {
            carregandoFinanceiro = true

            val listener = db.collection("financeiro")
                .whereEqualTo("alunoId", alunoId)
                .addSnapshotListener { snapshot, erro ->
                    carregandoFinanceiro = false

                    if (erro != null || snapshot == null) {
                        listaFinanceiroFirebase = emptyList()
                        return@addSnapshotListener
                    }

                    listaFinanceiroFirebase = snapshot.documents
                        .map { documento ->
                            RegistroFinanceiro(
                                id = documento.id,
                                alunoId = documento.getString("alunoId").orEmpty(),
                                turmaId = documento.getString("turmaId").orEmpty(),
                                numeroParcela = documento.getLong("numeroParcela")?.toInt() ?: 0,
                                parcela = documento.getLong("parcela")?.toInt() ?: 0,
                                status = documento.getString("status")
                                    ?: StatusPagamento.PENDENTE.name,
                                statusPago = documento.getBoolean("statusPago") ?: false,
                                recebidoPor = documento.getString("recebidoPor").orEmpty(),
                                catequista = documento.getString("catequista").orEmpty(),
                                dataPagamento = converterDataFirebaseParaMillis(
                                    documento.get("dataPagamento")
                                ),
                                dataLancamento = converterDataFirebaseParaMillis(
                                    documento.get("dataLancamento")
                                )
                            )
                        }
                        .filter { registro ->
                            registro.obterNumeroParcela() in 1..TOTAL_PARCELAS_CARNE &&
                                    (registro.turmaId.isBlank() || registro.turmaId == turmaId)
                        }
                        .sortedBy { it.obterNumeroParcela() }
                }

            onDispose {
                listener.remove()
            }
        }
    }

    // =========================================================
    // 6. DOCUMENTOS INDIVIDUAIS DO CRISMANDO
    // A coleção será criada/ajustada na etapa do Firebase.
    // =========================================================

    DisposableEffect(alunoId, turmaId) {
        if (alunoId.isBlank()) {
            listaDocumentosFirebase = emptyList()
            carregandoDocumentos = false
            onDispose { }
        } else {
            carregandoDocumentos = true

            val listener = db.collection("documentos")
                .whereEqualTo("alunoId", alunoId)
                .addSnapshotListener { snapshot, erro ->
                    carregandoDocumentos = false

                    if (erro != null || snapshot == null) {
                        listaDocumentosFirebase = emptyList()
                        return@addSnapshotListener
                    }

                    listaDocumentosFirebase = snapshot.documents
                        .map { documento ->
                            RegistroDocumento(
                                id = documento.id,
                                alunoId = documento.getString("alunoId").orEmpty(),
                                turmaId = documento.getString("turmaId").orEmpty(),
                                nome = documento.getString("nome").orEmpty(),
                                tipo = documento.getString("tipo").orEmpty(),
                                status = documento.getString("status")
                                    ?: StatusDocumento.NAO_ENTREGUE.name,
                                dataAtualizacao = converterDataFirebaseParaMillis(
                                    documento.get("dataAtualizacao")
                                )
                            )
                        }
                        /*
                         * A matrícula identifica o crismando de forma única.
                         * Por isso, não filtramos pelo turmaId aqui. Assim os
                         * documentos continuam visíveis mesmo após transferência.
                         */
                        .sortedWith(
                            compareBy<RegistroDocumento> {
                                if (it.ehDocumentoDoPadrinho()) 1 else 0
                            }.thenBy {
                                it.nome.ifBlank { it.tipo }
                            }
                        )
                }

            onDispose {
                listener.remove()
            }
        }
    }

    // =========================================================
    // 7. AVISOS VISÍVEIS PARA O CRISMANDO
    //
    // O crismando recebe:
    // - avisos gerais;
    // - avisos destinados à categoria da sua turma;
    // - avisos destinados especificamente à sua turma.
    // =========================================================

    DisposableEffect(turmaId, categoriaTurma) {
        if (turmaId.isBlank()) {
            listaAvisosFirebase = emptyList()
            carregandoAvisos = false
            onDispose { }
        } else {
            carregandoAvisos = true

            val categoriaNormalizada = categoriaTurma
                .trim()
                .lowercase(Locale.ROOT)

            val destinoCategoria = when (
                categoriaNormalizada
            ) {
                "jovem" -> "CATEGORIA_JOVEM"
                "adulta", "adulto" ->
                    "CATEGORIA_ADULTA"
                else -> ""
            }

            // Compatibilidade com avisos antigos.
            val destinoLegado = when (
                categoriaNormalizada
            ) {
                "jovem" -> "turma_jovem"
                "adulta", "adulto" ->
                    "turma_adulta"
                else -> ""
            }

            val listener = db.collection("avisos")
                .addSnapshotListener { snapshot, erro ->
                    carregandoAvisos = false

                    if (erro != null || snapshot == null) {
                        listaAvisosFirebase = emptyList()
                        return@addSnapshotListener
                    }

                    listaAvisosFirebase = snapshot.documents
                        .mapNotNull { documento ->
                            val texto = documento
                                .getString("texto")
                                .orEmpty()
                                .trim()

                            val avisoTurmaId = documento
                                .getString("turmaId")
                                .orEmpty()
                                .trim()

                            val pertenceAoCrismando =
                                avisoTurmaId == turmaId ||
                                        avisoTurmaId.equals(
                                            "GERAL",
                                            ignoreCase = true
                                        ) ||
                                        (
                                                destinoCategoria
                                                    .isNotBlank() &&
                                                        avisoTurmaId.equals(
                                                            destinoCategoria,
                                                            ignoreCase = true
                                                        )
                                                ) ||
                                        (
                                                destinoLegado
                                                    .isNotBlank() &&
                                                        avisoTurmaId.equals(
                                                            destinoLegado,
                                                            ignoreCase = true
                                                        )
                                                ) ||
                                        avisoTurmaId.isBlank()

                            if (
                                texto.isBlank() ||
                                !pertenceAoCrismando
                            ) {
                                null
                            } else {
                                AvisoItem(
                                    id = documento.id,
                                    text = texto,
                                    turmaId = avisoTurmaId
                                        .ifBlank { "GERAL" },
                                    tipo = documento
                                        .getString("tipo")
                                        ?: "TURMA",
                                    linkUrl = documento
                                        .getString("linkUrl")
                                        ?.takeIf {
                                            it.isNotBlank()
                                        },
                                    dataCriacao = documento
                                        .getLong("dataCriacao")
                                        ?: 0L
                                )
                            }
                        }
                        .sortedByDescending {
                            it.dataCriacao
                        }
                }

            onDispose {
                listener.remove()
            }
        }
    }

    // =========================================================
    // VALORES CALCULADOS AUTOMATICAMENTE
    // =========================================================

    val totalPresencas = listaFrequenciasFirebase.count {
        it.obterStatus() == StatusFrequencia.PRESENTE
    }

    val totalFaltas = listaFrequenciasFirebase.count {
        it.obterStatus() == StatusFrequencia.FALTA
    }

    val totalJustificadas = listaFrequenciasFirebase.count {
        it.obterStatus() == StatusFrequencia.JUSTIFICADA
    }

    val parcelasPagas = listaFinanceiroFirebase
        .filter { it.estaPago() }
        .map { it.obterNumeroParcela() }
        .filter { it in 1..TOTAL_PARCELAS_CARNE }
        .toSet()

    val documentosDoCrismando = listaDocumentosFirebase
        .filterNot { it.ehDocumentoDoPadrinho() }

    val documentosDoPadrinho = listaDocumentosFirebase
        .filter { it.ehDocumentoDoPadrinho() }

    /*
     * NAO_POSSUI significa que aquele comprovante não é necessário,
     * como no caso de uma pessoa não casada. Portanto, ele não deixa
     * o indicador pendente.
     */
    val totalDocumentosEntregues = listaDocumentosFirebase.count {
        it.obterStatus() == StatusDocumento.ENTREGUE
    }

    val possuiDocumentoPendente = listaDocumentosFirebase.any {
        it.obterStatus() == StatusDocumento.NAO_ENTREGUE
    }

    val crismandoFoiCadastrado = documentosDoCrismando.isNotEmpty()
    val padrinhoFoiCadastrado = documentosDoPadrinho.isNotEmpty()

    val todosDocumentosNecessariosEntregues =
        crismandoFoiCadastrado &&
                padrinhoFoiCadastrado &&
                !possuiDocumentoPendente

    val corLedDocumentos = when {
        carregandoDocumentos -> Color.Gray

        // Nenhum comprovante entregue, ou documentação ainda não cadastrada.
        totalDocumentosEntregues == 0 -> Color(0xFFE53935)

        // As duas abas foram cadastradas e não existe documento obrigatório pendente.
        todosDocumentosNecessariosEntregues -> Color(0xFF4CAF50)

        // Pelo menos um foi entregue, mas ainda falta algo.
        else -> Color(0xFFFFB300)
    }

    val textoResumoDocumentos = when {
        carregandoDocumentos -> "Carregando documentos..."
        todosDocumentosNecessariosEntregues -> "Documentação completa"
        totalDocumentosEntregues == 0 -> "Nenhum documento entregue"
        else -> "$totalDocumentosEntregues documento(s) entregue(s)"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ÁREA SUPERIOR (65%)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
                    .background(Crisma_Primary)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
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
                        label = "Sobre o App"
                    ) {
                        showSobreNosDialog = true
                    }

                    UserIconWithLabel(
                        icon = Icons.Outlined.Phone,
                        label = "Contatos"
                    ) {
                        showContatosDialog = true
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 65.dp)
                ) {
                    AnimatedVisibility(
                        visible = animarImagem,
                        enter = fadeIn(tween(1200)) + scaleIn(initialScale = 0.9f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.imagem_crisma),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = animarTextos,
                        enter = fadeIn(tween(1200)) + slideInVertically { it / 3 }
                    ) {
                        Column {
                            Text(
                                text = "\nÁrea do Crismando",
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
                                text = "\"A Eucaristia é a minha rodovia para o Céu.\"\n" +
                                        "(S. Carlo Acutis)",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontFamily = customFont
                            )
                        }
                    }
                }
            }

            // BARRA CENTRAL INTERATIVA
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.08f)
                        .offset(y = -(screenHeight * 0.04f))
                        .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .background(Light_Gray_Darker),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "  Olá, $userName!",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = customFont
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Crisma_Gold)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.White)
                            .clickable { showDocumentosPopup = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = corLedDocumentos,
                                        shape = RoundedCornerShape(50)
                                    )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Documentos",
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                fontFamily = customFont
                            )
                        }
                    }
                }
            }

            // ÁREA INFERIOR (35%) - GRID 2x2
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
                )  {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmallMenuCard(
                                title = "Frequência",
                                icon = Icons.Outlined.DateRange,
                                modifier = Modifier.weight(1f)
                            ) {
                                showPresencasPopup = true
                            }

                            SmallMenuCard(
                                title = "Avisos",
                                icon = Icons.Outlined.Notifications,
                                modifier = Modifier.weight(1f)
                            ) {
                                showAvisosPopup = true
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmallMenuCard(
                                title = "Carnê",
                                icon = Icons.Outlined.Payments,
                                modifier = Modifier.weight(1f)
                            ) {
                                showCarnePopup = true
                            }

                            SmallMenuCard(
                                title = "Sair",
                                icon = Icons.Outlined.ArrowBack,
                                modifier = Modifier.weight(1f)
                            ) {
                                navController.navigate("crismandoLoginScreen") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // =========================================================
    // DIÁLOGOS PADRÃO
    // =========================================================

    if (showSobreNosDialog) {
        AlertDialog(
            onDismissRequest = { showSobreNosDialog = false },
            confirmButton = {
                TextButton(onClick = { showSobreNosDialog = false }) {
                    Text("Entendido", color = Crisma_Primary)
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
                    text = "O CrismAPP foi idealizado para modernizar e fortalecer " +
                            "a comunicação na jornada espiritual da nossa Paróquia.\n\n" +
                            ". Desenvolvimento:\n" +
                            "Emanuel Barbosa\n" +
                            "(github.com/Emanuel-dev-silva)\n\n" +
                            ". Gestão de Requisitos:\n" +
                            "Victor Lima"
                )
            }
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
            title = {
                Text(
                    text = "Contatos",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = ". Paróquia Santo Antônio\n" +
                                "Tiúma, São Lourenço da Mata - PE\n\n" +
                                ". Secretaria e WhatsApp:\n" +
                                "(81) 9 8593-9076\n\n" +
                                ". Horário de Atendimento:\n" +
                                "Terça a Sábado: 08h às 12h"
                    )
                }
            }
        )
    }

    // =========================================================
    // POPUP DE FREQUÊNCIA
    // =========================================================

    if (showPresencasPopup) {
        CustomPopup(
            title = "Presenças: $totalPresencas | Faltas: $totalFaltas | Just.: $totalJustificadas",
            onDismiss = { showPresencasPopup = false }
        ) {
            when {
                carregandoFrequencias -> {
                    item {
                        PopupLoadingItem(text = "Carregando frequência...")
                    }
                }

                listaFrequenciasFirebase.isEmpty() -> {
                    item {
                        PopupEmptyItem(
                            text = "Nenhuma frequência foi registrada para você."
                        )
                    }
                }

                else -> {
                    items(
                        items = listaFrequenciasFirebase,
                        key = { it.id }
                    ) { registro ->
                        val encontro = listaEncontrosFirebase
                            .firstOrNull { it.numero == registro.encontro }

                        val status = registro.obterStatus()

                        val textoStatus = when (status) {
                            StatusFrequencia.PRESENTE -> "Presença confirmada"
                            StatusFrequencia.FALTA -> "Falta"
                            StatusFrequencia.JUSTIFICADA -> "Falta justificada"
                            StatusFrequencia.NENHUM -> "Aguardando registro"
                        }

                        val dataEncontro = encontro
                            ?.dataManual
                            ?.takeIf { it.isNotBlank() }

                        val titulo = buildString {
                            append("Encontro ")
                            append(registro.encontro.toString().padStart(2, '0'))

                            if (dataEncontro != null) {
                                append(" - ")
                                append(dataEncontro)
                            }

                            append(" - ")
                            append(textoStatus)
                        }

                        FrequenciaCard(
                            item = FrequenciaItem(
                                title = titulo,
                                status = status
                            )
                        )
                    }
                }
            }
        }
    }

    // =========================================================
    // POPUP DE AVISOS
    // =========================================================

    if (showAvisosPopup) {
        val uriHandler = LocalUriHandler.current

        CustomPopup(
            title = "Avisos: ${listaAvisosFirebase.size}",
            onDismiss = { showAvisosPopup = false }
        ) {
            when {
                carregandoAvisos -> {
                    item {
                        PopupLoadingItem(text = "Carregando avisos...")
                    }
                }

                listaAvisosFirebase.isEmpty() -> {
                    item {
                        PopupEmptyItem(
                            text = "Nenhum aviso publicado no momento."
                        )
                    }
                }

                else -> {
                    items(
                        items = listaAvisosFirebase,
                        key = { it.id }
                    ) { item ->
                        AvisoCardComponent(
                            item = item,
                            uriHandler = uriHandler
                        )
                    }
                }
            }
        }
    }

    // =========================================================
    // POPUP DO CARNÊ
    // =========================================================

    if (showCarnePopup) {
        CustomPopup(
            title = "Carnê: ${parcelasPagas.size} de $TOTAL_PARCELAS_CARNE parcelas pagas",
            onDismiss = { showCarnePopup = false }
        ) {
            if (carregandoFinanceiro) {
                item {
                    PopupLoadingItem(text = "Carregando carnê...")
                }
            } else {
                items(
                    items = (1..TOTAL_PARCELAS_CARNE).toList(),
                    key = { it }
                ) { numeroParcela ->
                    val pagamento = listaFinanceiroFirebase
                        .filter { registro ->
                            registro.obterNumeroParcela() == numeroParcela &&
                                    registro.estaPago()
                        }
                        .maxByOrNull { registro ->
                            registro.obterDataPagamento()
                        }

                    val paga = pagamento != null
                    val dataPagamento = pagamento?.obterDataPagamento() ?: 0L

                    val situacao = when {
                        !paga -> "Pendente"
                        dataPagamento > 0L ->
                            "Paga em ${formatarDataPagamento(dataPagamento)}"
                        else -> "Paga"
                    }

                    CarneCard(
                        item = CarneItem(
                            title = "Parcela ${
                                numeroParcela.toString().padStart(2, '0')
                            } - $situacao",
                            isPaid = paga
                        )
                    )
                }
            }
        }
    }

    // =========================================================
    // POPUP DE DOCUMENTOS
    // =========================================================

    if (showDocumentosPopup) {
        CustomPopup(
            title = "Documentos: $textoResumoDocumentos",
            onDismiss = { showDocumentosPopup = false }
        ) {
            when {
                carregandoDocumentos -> {
                    item {
                        PopupLoadingItem(text = "Carregando documentos...")
                    }
                }

                listaDocumentosFirebase.isEmpty() -> {
                    item {
                        PopupEmptyItem(
                            text = "Nenhum documento foi cadastrado para você ainda."
                        )
                    }
                }

                else -> {
                    item {
                        DocumentoGrupoTitulo(
                            titulo = "Crismando",
                            cadastrado = crismandoFoiCadastrado
                        )
                    }

                    if (documentosDoCrismando.isEmpty()) {
                        item {
                            PopupEmptyItem(
                                text = "A documentação do crismando ainda não foi cadastrada."
                            )
                        }
                    } else {
                        items(
                            items = documentosDoCrismando,
                            key = { it.id }
                        ) { registro ->
                            DocumentoRegistroCard(registro)
                        }
                    }

                    item {
                        DocumentoGrupoTitulo(
                            titulo = "Padrinho",
                            cadastrado = padrinhoFoiCadastrado
                        )
                    }

                    if (documentosDoPadrinho.isEmpty()) {
                        item {
                            PopupEmptyItem(
                                text = "A documentação do padrinho ainda não foi cadastrada."
                            )
                        }
                    } else {
                        items(
                            items = documentosDoPadrinho,
                            key = { it.id }
                        ) { registro ->
                            DocumentoRegistroCard(registro)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// COMPONENTES DOS ITENS DOS POPUPS
// =============================================================

@Composable
private fun FrequenciaCard(item: FrequenciaItem) {
    val statusIcon = when (item.status) {
        StatusFrequencia.PRESENTE -> Icons.Outlined.CheckCircle
        StatusFrequencia.JUSTIFICADA -> Icons.Outlined.ErrorOutline
        StatusFrequencia.FALTA -> Icons.Outlined.Cancel
        StatusFrequencia.NENHUM -> Icons.Outlined.HelpOutline
    }

    val statusColor = when (item.status) {
        StatusFrequencia.PRESENTE -> Color(0xFF4CAF50)
        StatusFrequencia.JUSTIFICADA -> Color(0xFFFF9800)
        StatusFrequencia.FALTA -> Color(0xFFE53935)
        StatusFrequencia.NENHUM -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color = Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = item.title,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = customFont
            )
        }
    }
}

@Composable
private fun CarneCard(item: CarneItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color = Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.isPaid) {
                    Icons.Outlined.CheckCircle
                } else {
                    Icons.Outlined.Cancel
                },
                contentDescription = null,
                tint = if (item.isPaid) {
                    Color(0xFF4CAF50)
                } else {
                    Color(0xFFE53935)
                },
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = item.title,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = customFont
            )
        }
    }
}

@Composable
private fun DocumentoGrupoTitulo(
    titulo: String,
    cadastrado: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = customFont,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (cadastrado) "Cadastrado" else "Pendente",
            color = if (cadastrado) Color(0xFF2E7D32) else Color(0xFFE53935),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DocumentoRegistroCard(registro: RegistroDocumento) {
    val nomeDocumento = registro.nome
        .ifBlank { registro.tipo }
        .ifBlank { "Documento" }

    val status = registro.obterStatus()

    val textoStatus = when (status) {
        StatusDocumento.ENTREGUE -> "Entregue"
        StatusDocumento.NAO_ENTREGUE -> "Não entregue"
        StatusDocumento.NAO_POSSUI -> "Não necessário"
    }

    DocumentoCard(
        item = DocumentoItem(
            title = "$nomeDocumento: $textoStatus",
            status = status
        )
    )
}

@Composable
private fun DocumentoCard(item: DocumentoItem) {
    val statusIcon = when (item.status) {
        StatusDocumento.ENTREGUE -> Icons.Outlined.CheckCircle
        StatusDocumento.NAO_POSSUI -> Icons.Outlined.ErrorOutline
        StatusDocumento.NAO_ENTREGUE -> Icons.Outlined.Cancel
    }

    val statusColor = when (item.status) {
        StatusDocumento.ENTREGUE -> Color(0xFF4CAF50)
        StatusDocumento.NAO_POSSUI -> Color(0xFFFF9800)
        StatusDocumento.NAO_ENTREGUE -> Color(0xFFE53935)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color = Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = item.title,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = customFont
            )
        }
    }
}

@Composable
fun AvisoCardComponent(
    item: AvisoItem,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    val isLink = item.linkUrl != null

    val destino = item.turmaId
        .trim()
        .uppercase(Locale.ROOT)

    val tagTurma = when {
        destino == "GERAL" || destino.isBlank() ->
            "AVISO GERAL"

        destino == "CATEGORIA_JOVEM" ||
                destino == "TURMA_JOVEM" ->
            "TURMAS JOVENS"

        destino == "CATEGORIA_ADULTA" ||
                destino == "TURMA_ADULTA" ->
            "TURMAS ADULTAS"

        else -> "SUA TURMA"
    }

    val tagColor = when {
        destino == "GERAL" || destino.isBlank() ->
            Crisma_Gold

        destino == "CATEGORIA_JOVEM" ||
                destino == "CATEGORIA_ADULTA" ||
                destino == "TURMA_JOVEM" ||
                destino == "TURMA_ADULTA" ->
            Color(0xFF1976D2)

        else -> Crisma_Primary
    }

    val tagTextColor = if (tagColor == Crisma_Gold) {
        Color.Black
    } else {
        Color.White
    }

    val cardBackground = when {
        destino == "GERAL" || destino.isBlank() ->
            Color(0xFFFFF8D1)

        destino == "CATEGORIA_JOVEM" ||
                destino == "CATEGORIA_ADULTA" ||
                destino == "TURMA_JOVEM" ||
                destino == "TURMA_ADULTA" ->
            Color(0xFFE3F2FD)

        else -> Color(0xFFFFEBEE)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isLink) {
                    Modifier.clickable {
                        item.linkUrl?.let { link ->
                            uriHandler.openUri(link)
                        }
                    }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = tagColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = tagColor,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(
                        horizontal = 7.dp,
                        vertical = 3.dp
                    )
            ) {
                Text(
                    text = tagTurma,
                    color = tagTextColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isLink) {
                        Icons.Outlined.Link
                    } else {
                        Icons.Outlined.Notifications
                    },
                    contentDescription = null,
                    tint = tagColor,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = item.text,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = customFont
                )
            }
        }
    }
}

@Composable
private fun PopupLoadingItem(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                color = Crisma_Primary,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )

            Text(
                text = text,
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                fontFamily = customFont
            )
        }
    }
}

@Composable
private fun PopupEmptyItem(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            fontFamily = customFont,
            textAlign = TextAlign.Center
        )
    }
}

// =============================================================
// COMPONENTE POPUP
// =============================================================

@Composable
fun CustomPopup(
    title: String,
    onDismiss: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val formattedTitle = title
        .lowercase(Locale.getDefault())
        .replaceFirstChar { caractere ->
            if (caractere.isLowerCase()) {
                caractere.titlecase(Locale.getDefault())
            } else {
                caractere.toString()
            }
        }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.52f)
                .padding(horizontal = 4.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = 1.dp,
                    color = Crisma_Primary,
                    shape = RoundedCornerShape(4.dp)
                )
                .clip(RoundedCornerShape(4.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Crisma_Primary)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = formattedTitle,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFont,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(end = 32.dp)
                    )

                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar",
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.CenterEnd)
                            .clickable { onDismiss() }
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFF9F9F9))
                        .padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = 12.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content
                )
            }
        }
    }
}