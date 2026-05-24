package com.example.crismapp.ui

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.example.crismapp.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlin.random.Random

// Declarada localmente com nome único para evitar conflitos de escopo no pacote
data class EncontroCatequeseAdulto(val id: String, val numero: Int, val dataManual: String, val turmaId: String)

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)

class MascaraDataTransformationAdulta : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val out = StringBuilder()

        for (i in originalText.indices) {
            out.append(originalText[i])
            if (i == 1 || i == 3) {
                out.append("/")
            }
        }

        val dataMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 4) return offset + 1
                return offset + 2
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return offset - 2
            }
        }

        return TransformedText(AnnotatedString(out.toString()), dataMapping)
    }
}

fun validarDigitosDataAdulta(puros: String): Boolean {
    if (puros.length >= 2) {
        val dia = puros.substring(0, 2).toIntOrNull() ?: 0
        if (dia < 1 || dia > 31) return false
    }
    if (puros.length >= 4) {
        val mes = puros.substring(2, 4).toIntOrNull() ?: 0
        if (mes < 1 || mes > 12) return false
    }
    return true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurmaAdultaScreen(navController: NavController) {
    val view = LocalView.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val db = FirebaseFirestore.getInstance()

    // Estados de Controle de Popups
    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }
    var showDadosPopup by remember { mutableStateOf(false) }
    var showAvisosPopup by remember { mutableStateOf(false) }
    var showFinanceiroPopup by remember { mutableStateOf(false) }
    var showFrequenciaPopup by remember { mutableStateOf(false) }
    var showTurmasPopup by remember { mutableStateOf(false) }

    // Estados de Navegação Interna
    var idTurmaSelecionada by remember { mutableStateOf<String?>(null) }
    var nomeTurmaSelecionada by remember { mutableStateOf<String?>(null) }
    var encontroSelecionado by remember { mutableStateOf<Int?>(null) }
    var crismandoSelecionado by remember { mutableStateOf<String?>(null) }

    // Estados para Edição/Criação
    var modoCriarTurma by remember { mutableStateOf(false) }
    var novoNomeTurma by remember { mutableStateOf("") }
    var novoNomeCrismando by remember { mutableStateOf("") }
    var novoAvisoTexto by remember { mutableStateOf("") }

    var novaDataEncontroInput by remember { mutableStateOf("") }

    var idEncontroEmEdicao by remember { mutableStateOf<String?>(null) }
    var dataEncontroEdicaoInput by remember { mutableStateOf("") }

    // Estados para controle do Alerta de Exclusão com Delay
    var idEncontroParaExcluir by remember { mutableStateOf<String?>(null) }
    var numeroEncontroParaExcluir by remember { mutableStateOf(0) }
    var liberarBotoesConfirmacaoExcluir by remember { mutableStateOf(false) }

    var idTurmaParaExcluir by remember { mutableStateOf<String?>(null) }
    var nomeTurmaParaExcluir by remember { mutableStateOf("") }
    var liberarBotoesConfirmacaoExcluirTurma by remember { mutableStateOf(false) }

    // Estados para controle do Alerta de Exclusão de Aviso com Delay
    var idAvisoParaExcluir by remember { mutableStateOf<String?>(null) }
    var textoAvisoParaExcluir by remember { mutableStateOf("") }
    var liberarBotoesConfirmacaoExcluirAviso by remember { mutableStateOf(false) }

    var modoPreenchimentoAutomatico by remember { mutableStateOf(false) }
    var exibirPorcentagemFalta by remember { mutableStateOf(false) }

    val frequenciaPorEncontro = remember { mutableStateMapOf<String, StatusFrequencia>() }

    var listaAvisosAtivos by remember { mutableStateOf(listOf<Aviso>()) }
    var listaTurmasFirestore by remember { mutableStateOf(listOf<Turma>()) }
    var listaCrismandosFirestore by remember { mutableStateOf(listOf<Crismando>()) }

    var listaEncontrosFirestore by remember { mutableStateOf(listOf<EncontroCatequeseAdulto>()) }
    var todasFrequenciasGeraisByTurma by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    var animarImagem by remember { mutableStateOf(false) }
    var animarTextos by remember { mutableStateOf(false) }
    var animarBotoesAcao by remember { mutableStateOf(false) }

    val visualTransformationData = remember { MascaraDataTransformationAdulta() }

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window
        window.statusBarColor = Crisma_Primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        delay(100); animarImagem = true
        delay(200); animarTextos = true
        delay(300); animarBotoesAcao = true
    }

    LaunchedEffect(idEncontroParaExcluir) {
        if (idEncontroParaExcluir != null) {
            liberarBotoesConfirmacaoExcluir = false
            delay(2000)
            liberarBotoesConfirmacaoExcluir = true
        }
    }

    LaunchedEffect(idTurmaParaExcluir) {
        if (idTurmaParaExcluir != null) {
            liberarBotoesConfirmacaoExcluirTurma = false
            delay(2000)
            liberarBotoesConfirmacaoExcluirTurma = true
        }
    }

    // Temporizador controlado para a exclusão de avisos
    LaunchedEffect(idAvisoParaExcluir) {
        if (idAvisoParaExcluir != null) {
            liberarBotoesConfirmacaoExcluirAviso = false
            delay(2000)
            liberarBotoesConfirmacaoExcluirAviso = true
        }
    }

    // 1. Ouvinte em tempo real para carregar as TURMAS do banco (Apenas Adultas)
    LaunchedEffect(Unit) {
        db.collection("turmas")
            .whereEqualTo("categoria", "adulta")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    listaTurmasFirestore = snapshot.documents.mapNotNull { doc ->
                        val nome = doc.getString("nome") ?: ""
                        if (nome.isNotEmpty()) Turma(id = doc.id, nome = nome) else null
                    }.sortedBy { it.nome }
                }
            }
    }

    // 2. Ouvinte em tempo real para carregar os CRISMANDOS ADULTOS da turma selecionada
    LaunchedEffect(idTurmaSelecionada) {
        if (idTurmaSelecionada == null) {
            listaCrismandosFirestore = emptyList()
            return@LaunchedEffect
        }
        db.collection("usuarios")
            .whereEqualTo("turmaId", idTurmaSelecionada)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    listaCrismandosFirestore = snapshot.documents.mapNotNull { doc ->
                        val nome = doc.getString("nome") ?: ""
                        val tId = doc.getString("turmaId") ?: ""
                        if (nome.isNotEmpty()) Crismando(id = doc.id, nome = nome, turmaId = tId) else null
                    }.sortedBy { it.nome }
                }
            }
    }

    // 2.B Ouvinte reativo dos encontros da turma selecionada
    LaunchedEffect(idTurmaSelecionada) {
        if (idTurmaSelecionada == null) {
            listaEncontrosFirestore = emptyList()
            return@LaunchedEffect
        }
        db.collection("encontros")
            .whereEqualTo("turmaId", idTurmaSelecionada)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                listaEncontrosFirestore = snapshot.documents.mapNotNull { doc ->
                    val num = doc.getLong("numero")?.toInt() ?: 0
                    val dataMan = doc.getString("dataManual") ?: ""
                    val tId = doc.getString("turmaId") ?: ""
                    if (num > 0) EncontroCatequeseAdulto(id = doc.id, numero = num, dataManual = dataMan, turmaId = tId) else null
                }.sortedBy { it.numero }
            }
    }

    // 2.C Ouvinte reativo das frequências da sala para cálculo estatístico
    LaunchedEffect(idTurmaSelecionada) {
        if (idTurmaSelecionada == null) {
            todasFrequenciasGeraisByTurma = emptyList()
            return@LaunchedEffect
        }
        db.collection("frequencias")
            .whereEqualTo("turmaId", idTurmaSelecionada)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    todasFrequenciasGeraisByTurma = snapshot.documents.map { doc -> doc.data ?: emptyMap() }
                }
            }
    }

    // 3. Ouvinte em tempo real para Avisos da Turma Adulta
    LaunchedEffect(Unit) {
        db.collection("avisos")
            .whereEqualTo("turmaId", "turma_adulta")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                listaAvisosAtivos = snapshot.documents.mapNotNull { doc ->
                    val txt = doc.getString("texto") ?: ""
                    val tp = doc.getString("tipo") ?: "gerais"
                    val tId = doc.getString("turmaId") ?: ""
                    val data = doc.getLong("dataCriacao") ?: 0L
                    if (txt.isNotEmpty()) Aviso(id = doc.id, texto = txt, tipo = tp, turmaId = tId, dataCriacao = data) else null
                }.sortedByDescending { it.dataCriacao }
            }
    }

    // 4. Ouvinte reativo global para a frequência em nuvem (Adultos)
    LaunchedEffect(idTurmaSelecionada, encontroSelecionado) {
        if (idTurmaSelecionada == null || encontroSelecionado == null) return@LaunchedEffect

        db.collection("frequencias")
            .whereEqualTo("turmaId", idTurmaSelecionada)
            .whereEqualTo("encontro", encontroSelecionado)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                snapshot.documents.forEach { doc ->
                    val alunoId = doc.getString("alunoId") ?: ""
                    val statusString = doc.getString("status") ?: "NENHUM"

                    if (alunoId.isNotEmpty()) {
                        val apiKeyMap = "Adu_${idTurmaSelecionada}_${encontroSelecionado}_${alunoId}"
                        frequenciaPorEncontro[apiKeyMap] = StatusFrequencia.valueOf(statusString)
                    }
                }
            }
    }

    // Lógica do Switch de preenchimento automático
    LaunchedEffect(modoPreenchimentoAutomatico, idTurmaSelecionada, encontroSelecionado) {
        if (idTurmaSelecionada == null || encontroSelecionado == null) return@LaunchedEffect

        listaCrismandosFirestore.forEach { crismando ->
            val chave = "Adu_${idTurmaSelecionada}_${encontroSelecionado}_${crismando.id}"
            if (modoPreenchimentoAutomatico) {
                if (frequenciaPorEncontro[chave] == null || frequenciaPorEncontro[chave] == StatusFrequencia.NENHUM) {
                    frequenciaPorEncontro[chave] = StatusFrequencia.PRESENTE
                }
            } else {
                if (frequenciaPorEncontro[chave] == StatusFrequencia.PRESENTE) {
                    frequenciaPorEncontro[chave] = StatusFrequencia.NENHUM
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- CABEÇALHO ---
            Box(modifier = Modifier.fillMaxWidth().weight(0.65f).background(Crisma_Primary).padding(horizontal = 16.dp, vertical = 24.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        UserIconWithLabelAdulta(Icons.Outlined.Info, "Sobre o App") { showSobreNosDialog = true }
                        UserIconWithLabelAdulta(Icons.Outlined.Phone, "Contatos") { showContatosDialog = true }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (animarImagem) {
                        Image(painter = painterResource(id = R.drawable.imagem_crisma), contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp))
                    }

                    if (animarTextos) {
                        Column {
                            Text("\nGestão: Turma Adulta", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = Crisma_Gold, thickness = 2.dp, modifier = Modifier.fillMaxWidth(0.76f).padding(vertical = 12.dp))
                            Text("Formação para Adultos", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }

            // --- BARRA CENTRAL ---
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(modifier = Modifier.fillMaxWidth().height(screenHeight * 0.08f).offset(y = -(screenHeight * 0.04f)).background(Color.White)) {
                    Button(onClick = { navController.navigate("turmaJovemScreen") { launchSingleTop = true } }, modifier = Modifier.weight(1f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary), shape = RoundedCornerShape(0.dp)) {
                        Text("Turma Jovem", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Crisma_Primary.copy(alpha = 0.3f)))
                    Button(onClick = { }, modifier = Modifier.weight(1f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = Light_Gray_Darker), shape = RoundedCornerShape(0.dp)) {
                        Text("Turma Adulta", color = Crisma_Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- MENU ---
            Box(modifier = Modifier.fillMaxWidth().weight(0.35f).background(Color.White), contentAlignment = Alignment.TopCenter) {
                if (animarBotoesAcao) {
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallMenuCardAdulta(title = "Frequência", icon = Icons.Outlined.CheckCircle, modifier = Modifier.weight(1f)) {
                                idTurmaSelecionada = null
                                encontroSelecionado = null
                                modoPreenchimentoAutomatico = false
                                showFrequenciaPopup = true
                            }
                            SmallMenuCardAdulta(title = "Turmas", icon = Icons.Outlined.Groups, modifier = Modifier.weight(1f)) { idTurmaSelecionada = null; nomeTurmaSelecionada = null; modoCriarTurma = false; showTurmasPopup = true }
                            SmallMenuCardAdulta(title = "Avisos", icon = Icons.Outlined.Notifications, modifier = Modifier.weight(1f)) { showAvisosPopup = true }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallMenuCardAdulta(title = "Financeiro", icon = Icons.Outlined.Payments, modifier = Modifier.weight(1f)) { idTurmaSelecionada = null; crismandoSelecionado = null; showFinanceiroPopup = true }
                            SmallMenuCardAdulta(title = "Dados", icon = Icons.Outlined.BarChart, modifier = Modifier.weight(1f)) {
                                idTurmaSelecionada = null
                                exibirPorcentagemFalta = false
                                showDadosPopup = true
                            }
                            SmallMenuCardAdulta(title = "Voltar", icon = Icons.Outlined.ArrowBack, modifier = Modifier.weight(1f)) {
                                navController.navigate("catequistaOptions") { popUpTo("turmaAdultaScreen") { inclusive = true } }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- POPUP TURMAS ---
    if (showTurmasPopup) {
        val titT = when {
            modoCriarTurma -> "Nova Turma Adulta"
            idTurmaSelecionada != null -> "Membros: $nomeTurmaSelecionada"
            else -> "Gerenciar Turmas"
        }
        CustomPopupAdulta(title = titT, onDismiss = { showTurmasPopup = false }) {
            if (modoCriarTurma) {
                item {
                    OutlinedTextField(value = novoNomeTurma, onValueChange = { novoNomeTurma = it }, label = { Text("Ex: Matriz, Comunidade X...") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (novoNomeTurma.isNotBlank()) {
                                val novaTurmaMap = hashMapOf("nome" to novoNomeTurma, "categoria" to "adulta")
                                db.collection("turmas").add(novaTurmaMap).addOnSuccessListener {
                                    Toast.makeText(context, "Turma Adulta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                    novoNomeTurma = ""
                                    modoCriarTurma = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                        shape = RoundedCornerShape(4.dp)
                    ) { Text("Criar", fontWeight = FontWeight.Bold) }
                    TextButton(onClick = { modoCriarTurma = false }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar", color = Color.Gray) }
                }
            } else if (idTurmaSelecionada == null) {
                items(listaTurmasFirestore) { turma ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(turma.nome, fontWeight = FontWeight.Bold, color = Crisma_Primary, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                idTurmaSelecionada = turma.id
                                nomeTurmaSelecionada = turma.nome
                            }) { Icon(Icons.Outlined.Edit, "Membros", tint = Color.Gray) }

                            IconButton(onClick = {
                                idTurmaParaExcluir = turma.id
                                nomeTurmaParaExcluir = turma.nome
                            }) {
                                Icon(Icons.Outlined.Delete, "Excluir", tint = Color.Red.copy(0.7f))
                            }
                        }
                    }
                }
                item { Button(onClick = { modoCriarTurma = true }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary), shape = RoundedCornerShape(8.dp)) { Text("Nova Turma Adulta", fontWeight = FontWeight.Bold) } }
            } else {
                item {
                    TextButton(onClick = { idTurmaSelecionada = null; nomeTurmaSelecionada = null }) { Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary); Text(" Voltar", color = Crisma_Primary) }
                    OutlinedTextField(
                        value = novoNomeCrismando,
                        onValueChange = { novoNomeCrismando = it },
                        placeholder = { Text("Nome do Adulto...") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (novoNomeCrismando.isNotBlank()) {
                                    val numeroAleatorio = Random.nextInt(1000, 9999)
                                    val codigoGerado = "CX-$numeroAleatorio"
                                    val novoUsuarioMap = hashMapOf("nome" to novoNomeCrismando, "turmaId" to idTurmaSelecionada!!, "matricula" to codigoGerado)
                                    db.collection("usuarios").document(codigoGerado).set(novoUsuarioMap).addOnSuccessListener {
                                        Toast.makeText(context, "Membro cadastrado! Código: $codigoGerado", Toast.LENGTH_LONG).show()
                                        novoNomeCrismando = ""
                                    }
                                }
                            }) { Icon(Icons.Outlined.AddCircle, null, tint = Crisma_Primary) }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
                // 💡 COMPILADOR BLINDADO AQUI: Ajuste do cabeçalho da listagem corrigindo a sintaxe do Compose
                items(listaCrismandosFirestore) { crismando ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(crismando.nome, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Matrícula: ${crismando.id}", fontSize = 11.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { db.collection("usuarios").document(crismando.id).delete() }) { Icon(Icons.Outlined.Delete, null, tint = Color.Red.copy(0.6f)) }
                        }
                    }
                }
            }
        }
    }

    // --- POPUP FREQUÊNCIA ---
    if (showFrequenciaPopup) {
        val titFreq = when {
            encontroSelecionado != null -> "Encontro $encontroSelecionado - ${listaEncontrosFirestore.find { it.numero == encontroSelecionado }?.dataManual ?: ""}"
            idTurmaSelecionada != null -> "Encontros: $nomeTurmaSelecionada"
            else -> "Frequência - Selecione a Turma"
        }
        CustomPopupAdulta(title = titFreq, onDismiss = { showFrequenciaPopup = false; idTurmaSelecionada = null; encontroSelecionado = null; idEncontroEmEdicao = null; modoPreenchimentoAutomatico = false }) {
            if (idTurmaSelecionada == null) {
                items(listaTurmasFirestore) { turma ->
                    Card(
                        onClick = {
                            idTurmaSelecionada = turma.id
                            nomeTurmaSelecionada = turma.nome
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(turma.nome, fontWeight = FontWeight.Bold, color = Crisma_Primary)
                            Icon(Icons.Outlined.ArrowForwardIos, null, modifier = Modifier.size(14.dp), tint = Crisma_Primary)
                        }
                    }
                }
            } else if (encontroSelecionado == null) {
                item {
                    TextButton(onClick = { idTurmaSelecionada = null; nomeTurmaSelecionada = null }) {
                        Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary)
                        Text(" Voltar para Turmas", color = Crisma_Primary)
                    }
                }

                items(listaEncontrosFirestore) { encontro ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f).clickable { encontroSelecionado = encontro.numero }) {
                                Text("Encontro ${encontro.numero} - ${encontro.dataManual}", color = Crisma_Primary, fontWeight = FontWeight.Medium)
                            }

                            Row {
                                IconButton(onClick = {
                                    idEncontroEmEdicao = encontro.id
                                    dataEncontroEdicaoInput = encontro.dataManual.filter { it.isDigit() }
                                }) {
                                    Icon(Icons.Outlined.Edit, "Editar Data", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = {
                                    idEncontroParaExcluir = encontro.id
                                    numeroEncontroParaExcluir = encontro.numero
                                }) {
                                    Icon(Icons.Outlined.Delete, "Excluir Encontro", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    if (idEncontroEmEdicao == encontro.id) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
                            border = BorderStroke(1.dp, Crisma_Gold),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = dataEncontroEdicaoInput,
                                    onValueChange = { newValue ->
                                        val puros = newValue.filter { it.isDigit() }
                                        if (puros.length <= 8 && validarDigitosDataAdulta(puros)) {
                                            dataEncontroEdicaoInput = puros
                                        }
                                    },
                                    placeholder = { Text("Nova data (DDMMAAAA)...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    visualTransformation = visualTransformationData
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (dataEncontroEdicaoInput.length == 8) {
                                            val anoInserido = dataEncontroEdicaoInput.substring(4, 8).toIntOrNull() ?: 0
                                            if (anoInserido < 2026) {
                                                Toast.makeText(context, "O ano não pode ser menor que 2026!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val dataComBarras = StringBuilder(dataEncontroEdicaoInput)
                                                    .insert(2, "/").insert(5, "/").toString()

                                                db.collection("encontros").document(encontro.id)
                                                    .update("dataManual", dataComBarras)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(context, "Data updated!", Toast.LENGTH_SHORT).show()
                                                        idEncontroEmEdicao = null
                                                    }
                                            }
                                        } else {
                                            Toast.makeText(context, "Digite os 8 números da data!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Salvar", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        OutlinedTextField(
                            value = novaDataEncontroInput,
                            onValueChange = { newValue ->
                                val puros = newValue.filter { it.isDigit() }
                                if (puros.length <= 8 && validarDigitosDataAdulta(puros)) {
                                    novaDataEncontroInput = puros
                                }
                            },
                            placeholder = { Text("Digite a data (Ex: 22062026)...") },
                            label = { Text("Data do Encontro") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = visualTransformationData
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (novaDataEncontroInput.length == 8) {
                                    val anoInserido = novaDataEncontroInput.substring(4, 8).toIntOrNull() ?: 0
                                    if (anoInserido < 2026) {
                                        Toast.makeText(context, "O ano não pode ser menor que 2026!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val proximoNumero = listaEncontrosFirestore.size + 1
                                        val dataComBarras = StringBuilder(novaDataEncontroInput)
                                            .insert(2, "/").insert(5, "/").toString()

                                        val novoEncontroMap = hashMapOf(
                                            "numero" to proximoNumero,
                                            "dataManual" to dataComBarras,
                                            "turmaId" to idTurmaSelecionada!!,
                                            "dataCriacao" to System.currentTimeMillis()
                                        )
                                        db.collection("encontros").add(novoEncontroMap).addOnSuccessListener {
                                            Toast.makeText(context, "Encontro $proximoNumero adicionado!", Toast.LENGTH_SHORT).show()
                                            novaDataEncontroInput = ""
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Digite a data completa com 8 números!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(Icons.Outlined.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Adicionar Encontro", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { encontroSelecionado = null; modoPreenchimentoAutomatico = false }) {
                            Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary)
                            Text(" Voltar para Encontros", color = Crisma_Primary)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Edit, null, tint = Crisma_Primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Preencher", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Switch(
                                checked = modoPreenchimentoAutomatico,
                                onCheckedChange = { modoPreenchimentoAutomatico = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2E7D32))
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                items(listaCrismandosFirestore) { crismando ->
                    val chave = "Adu_${idTurmaSelecionada}_${encontroSelecionado}_${crismando.id}"
                    val status = frequenciaPorEncontro[chave] ?: StatusFrequencia.NENHUM

                    val factorOpacity = if (modoPreenchimentoAutomatico) 1.0f else 0.4f

                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF0F0F0)), shape = RoundedCornerShape(4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(crismando.nome, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = if (modoPreenchimentoAutomatico) 1.0f else 0.6f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { frequenciaPorEncontro[chave] = StatusFrequencia.PRESENTE },
                                    enabled = modoPreenchimentoAutomatico,
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(status == StatusFrequencia.PRESENTE) Color(0xFF2E7D32) else Color(0xFFE8F5E9),
                                        contentColor = if(status == StatusFrequencia.PRESENTE) Color.White else Color(0xFF2E7D32),
                                        disabledContainerColor = if(status == StatusFrequencia.PRESENTE) Color(0xFF2E7D32).copy(alpha = factorOpacity) else Color(0xFFE8F5E9).copy(alpha = factorOpacity),
                                        disabledContentColor = if(status == StatusFrequencia.PRESENTE) Color.White.copy(alpha = factorOpacity) else Color(0xFF2E7D32).copy(alpha = factorOpacity)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("PRESENTE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { frequenciaPorEncontro[chave] = StatusFrequencia.FALTA },
                                    enabled = modoPreenchimentoAutomatico,
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(status == StatusFrequencia.FALTA) Color(0xFFFF0000) else Color(0xFFFFEBEE),
                                        contentColor = if(status == StatusFrequencia.FALTA) Color.White else Color(0xFFFF0000),
                                        disabledContainerColor = if(status == StatusFrequencia.FALTA) Color(0xFFFF0000).copy(alpha = factorOpacity) else Color(0xFFFFEBEE).copy(alpha = factorOpacity),
                                        disabledContentColor = if(status == StatusFrequencia.FALTA) Color.White.copy(alpha = factorOpacity) else Color(0xFFFF0000).copy(alpha = factorOpacity)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("FALTA", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { frequenciaPorEncontro[chave] = StatusFrequencia.JUSTIFICADA },
                                    enabled = modoPreenchimentoAutomatico,
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(status == StatusFrequencia.JUSTIFICADA) Color(0xFFF57C00) else Color(0xFFFFF3E0),
                                        contentColor = if(status == StatusFrequencia.JUSTIFICADA) Color.White else Color(0xFFF57C00),
                                        disabledContainerColor = if(status == StatusFrequencia.JUSTIFICADA) Color(0xFFF57C00).copy(alpha = factorOpacity) else Color(0xFFFFF3E0).copy(alpha = factorOpacity),
                                        disabledContentColor = if(status == StatusFrequencia.JUSTIFICADA) Color.White.copy(alpha = factorOpacity) else Color(0xFFF57C00).copy(alpha = factorOpacity)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("JUST.", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            var salvasComSucesso = 0
                            listaCrismandosFirestore.forEach { crismando ->
                                val chave = "Adu_${idTurmaSelecionada}_${encontroSelecionado}_${crismando.id}"
                                val statusParaSalvar = frequenciaPorEncontro[chave] ?: StatusFrequencia.NENHUM

                                val docIdFrequencia = "FREQ_T-${idTurmaSelecionada}_E-${encontroSelecionado}_A-${crismando.id}"

                                val dadosFrequenciaMap = hashMapOf(
                                    "turmaId" to idTurmaSelecionada!!,
                                    "encontro" to encontroSelecionado!!,
                                    "alunoId" to crismando.id,
                                    "alunoNome" to crismando.nome,
                                    "status" to statusParaSalvar.name,
                                    "dataAtualizacao" to System.currentTimeMillis()
                                )

                                db.collection("frequencias")
                                    .document(docIdFrequencia)
                                    .set(dadosFrequenciaMap)
                                    .addOnSuccessListener {
                                        salvasComSucesso++
                                        if (salvasComSucesso == listaCrismandosFirestore.size) {
                                            Toast.makeText(context, "Chamada de Adultos sincronizada com sucesso!", Toast.LENGTH_SHORT).show()
                                            modoPreenchimentoAutomatico = false
                                            showFrequenciaPopup = false
                                        }
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Sincronizar no Firebase", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showFinanceiroPopup) {
        CustomPopupAdulta(title = "Financeiro Adultos", onDismiss = { showFinanceiroPopup = false; idTurmaSelecionada = null; crismandoSelecionado = null }) {
            if (idTurmaSelecionada == null) {
                items(listaTurmasFirestore) { turma ->
                    Card(onClick = { idTurmaSelecionada = turma.id }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(turma.nome, fontWeight = FontWeight.Bold, color = Crisma_Primary)
                            Icon(Icons.Outlined.Payments, null, tint = Crisma_Primary)
                        }
                    }
                }
            } else if (crismandoSelecionado == null) {
                item { TextButton(onClick = { idTurmaSelecionada = null }) { Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary); Text(" Voltar", color = Crisma_Primary) } }
                items(listaCrismandosFirestore) { crismando ->
                    Card(onClick = { crismandoSelecionado = crismando.nome }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(crismando.nome, color = Crisma_Primary)
                            Icon(Icons.Outlined.ChevronRight, null, tint = Crisma_Primary)
                        }
                    }
                }
            } else {
                item { TextButton(onClick = { crismandoSelecionado = null }) { Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary); Text(" Voltar", color = Crisma_Primary) } }
                items(10) { i ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Parcela ${i+1}", modifier = Modifier.weight(1f))
                            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), shape = RoundedCornerShape(4.dp)) {
                                Text("PAGO", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDadosPopup) {
        val tituloD = if (idTurmaSelecionada == null) "Estatísticas Adultos" else "Métricas: $nomeTurmaSelecionada"
        CustomPopupAdulta(title = tituloD, onDismiss = { showDadosPopup = false; idTurmaSelecionada = null }) {
            if (idTurmaSelecionada == null) {
                items(listaTurmasFirestore) { turma ->
                    Card(onClick = { idTurmaSelecionada = turma.id; nomeTurmaSelecionada = turma.nome }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(turma.nome, fontWeight = FontWeight.Bold, color = Crisma_Primary)
                            Icon(Icons.Outlined.BarChart, null, tint = Crisma_Primary)
                        }
                    }
                }
            } else {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { idTurmaSelecionada = null }) {
                            Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary)
                            Text(" Voltar para Salas", color = Crisma_Primary)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (exibirPorcentagemFalta) Icons.Outlined.TrendingUp else Icons.Outlined.Assessment,
                                    contentDescription = null,
                                    tint = Crisma_Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (exibirPorcentagemFalta) "Visualizando Faltas (%)" else "Visualizando Presenças (%)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                            Switch(
                                checked = exibirPorcentagemFalta,
                                onCheckedChange = { exibirPorcentagemFalta = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color.Red)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                items(listaCrismandosFirestore) { crismando ->
                    val totalEncontrosTurma = listaEncontrosFirestore.size
                    val totalPresencasAluno = todasFrequenciasGeraisByTurma.count {
                        it["alunoId"] == crismando.id && it["status"] == "PRESENTE"
                    }
                    val totalFaltasAluno = todasFrequenciasGeraisByTurma.count {
                        it["alunoId"] == crismando.id && it["status"] == "FALTA"
                    }
                    val totalJustificadasAluno = todasFrequenciasGeraisByTurma.count {
                        it["alunoId"] == crismando.id && it["status"] == "JUSTIFICADA"
                    }

                    val porcentagemCalculada = if (totalEncontrosTurma > 0) {
                        if (exibirPorcentagemFalta) {
                            (totalFaltasAluno.toFloat() / totalEncontrosTurma.toFloat()) * 100f
                        } else {
                            (totalPresencasAluno.toFloat() / totalEncontrosTurma.toFloat()) * 100f
                        }
                    } else 0f

                    val textoPorcentagem = String.format("%.1f%%", porcentagemCalculada)

                    val corAlertaMétrica = if (exibirPorcentagemFalta) {
                        if (porcentagemCalculada > 25f) Color.Red else Color(0xFF2E7D32)
                    } else {
                        if (porcentagemCalculada < 75f) Color.Red else Color(0xFF2E7D32)
                    }

                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(crismando.nome, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "P: $totalPresencasAluno | F: $totalFaltasAluno | J: $totalJustificadasAluno | Total de Aulas: $totalEncontrosTurma",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = textoPorcentagem,
                                color = corAlertaMétrica,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAvisosPopup) {
        CustomPopupAdulta(title = "Avisos Adultos", onDismiss = { showAvisosPopup = false }) {
            item {
                OutlinedTextField(value = novoAvisoTexto, onValueChange = { novoAvisoTexto = it }, placeholder = { Text("Novo aviso...") }, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        if (novoAvisoTexto.isNotBlank()) {
                            val novoAvisoMap = hashMapOf(
                                "texto" to novoAvisoTexto,
                                "tipo" to "gerais",
                                "turmaId" to "turma_adulta",
                                "dataCriacao" to System.currentTimeMillis()
                            )
                            db.collection("avisos").add(novoAvisoMap)
                            novoAvisoTexto = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary),
                    shape = RoundedCornerShape(4.dp)
                ) { Text("Publicar", fontWeight = FontWeight.Bold) }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }
            items(listaAvisosAtivos) { aviso ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = aviso.texto, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color.DarkGray)

                        // 💡 EXCLUSÃO DE AVISO ADULTO: Configura os estados reativos para acionar o alerta temporizado de 2 segundos
                        IconButton(onClick = {
                            idAvisoParaExcluir = aviso.id
                            textoAvisoParaExcluir = aviso.texto
                        }) {
                            Icon(Icons.Outlined.Delete, null, tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }

    if (showSobreNosDialog) AlertDialog(onDismissRequest = { showSobreNosDialog = false }, confirmButton = { TextButton(onClick = { showSobreNosDialog = false }) { Text("OK", color = Crisma_Primary, fontWeight = FontWeight.Bold) } }, title = { Text("Sobre") }, text = { Text("CrismAPP - Gestão Catequética.") })
    if (showContatosDialog) AlertDialog(onDismissRequest = { showContatosDialog = false }, confirmButton = { TextButton(onClick = { showContatosDialog = false }) { Text("OK", color = Crisma_Primary, fontWeight = FontWeight.Bold) } }, title = { Text("Contatos") }, text = { Text("Paróquia: (81) 98593-9076") })

    if (idEncontroParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { idEncontroParaExcluir = null },
            title = { Text("Confirmar Exclusão", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja mesmo excluir o ${numeroEncontroParaExcluir}º Encontro? Essa ação é irreversível.") },
            dismissButton = {
                TextButton(onClick = { idEncontroParaExcluir = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            confirmButton = {
                AnimatedVisibility(
                    visible = liberarBotoesConfirmacaoExcluir,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Button(
                        onClick = {
                            db.collection("encontros").document(idEncontroParaExcluir!!).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Encontro removido!", Toast.LENGTH_SHORT).show()
                                    idEncontroParaExcluir = null
                                }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Sim, Excluir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    if (idTurmaParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { idTurmaParaExcluir = null },
            title = { Text("Excluir Turma Adulta", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja mesmo excluir permanentemente a turma \"$nomeTurmaParaExcluir\"? Alunos e dados serão desconectados.") },
            dismissButton = {
                TextButton(onClick = { idTurmaParaExcluir = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            confirmButton = {
                AnimatedVisibility(
                    visible = liberarBotoesConfirmacaoExcluirTurma,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Button(
                        onClick = {
                            db.collection("turmas").document(idTurmaParaExcluir!!).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Turma excluída com sucesso!", Toast.LENGTH_SHORT).show()
                                    idTurmaParaExcluir = null
                                }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Confirmar Exclusão", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // 💡 DIÁLOGO DE EXCLUSÃO DE AVISOS COM TRAVA DE 2 SEGUNDOS ADICIONADO PARA ADULTOS
    if (idAvisoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { idAvisoParaExcluir = null },
            title = { Text("Excluir Aviso Adulto", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja mesmo excluir permanentemente o aviso: \"$textoAvisoParaExcluir\"?") },
            dismissButton = {
                TextButton(onClick = { idAvisoParaExcluir = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            confirmButton = {
                AnimatedVisibility(
                    visible = liberarBotoesConfirmacaoExcluirAviso,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Button(
                        onClick = {
                            db.collection("avisos").document(idAvisoParaExcluir!!).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Aviso removido com sucesso!", Toast.LENGTH_SHORT).show()
                                    idAvisoParaExcluir = null
                                }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Confirmar Exclusão", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

@Composable
fun UserIconWithLabelAdulta(icon: ImageVector, label: String, onClick: () -> Unit) {
    Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp).clickable { onClick() })
}

@Composable
fun SmallMenuCardAdulta(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), // 💡 Fundo unificado com o cinza sutil dos encontros/avisos
        shape = RoundedCornerShape(4.dp), // 💡 Bordas modernas menos arredondadas
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Crisma_Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CustomPopupAdulta(title: String, onDismiss: () -> Unit, content: LazyListScope.() -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().height(screenHeight * 0.52f).background(Color.White, shape = RoundedCornerShape(2.dp)).border(width = 1.dp, color = Crisma_Primary, shape = RoundedCornerShape(2.dp))) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().background(Crisma_Primary).padding(12.dp)) {
                    Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.align(Alignment.CenterEnd).clickable { onDismiss() })
                }
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
            }
        }
    }
}