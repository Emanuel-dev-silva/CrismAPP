package com.example.crismapp.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.example.crismapp.R
import kotlinx.coroutines.delay

private val Crisma_Primary = Color(0xFFFF0000)
private val Crisma_Gold = Color(0xFFFFD700)
private val Light_Gray_Darker = Color(0xFFE0E0E0)

enum class StatusFrequencia { PRESENTE, FALTA, JUSTIFICADA, NENHUM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurmaJovemScreen(navController: NavController) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Estados de Controle de Visibilidade
    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }
    var showDadosPopup by remember { mutableStateOf(false) }
    var showAvisosPopup by remember { mutableStateOf(false) }
    var showFinanceiroPopup by remember { mutableStateOf(false) }
    var showFrequenciaPopup by remember { mutableStateOf(false) }
    var showTurmasPopup by remember { mutableStateOf(false) }

    // Estados de Navegação Interna
    var turmaSelecionada by remember { mutableStateOf<String?>(null) }
    var encontroSelecionado by remember { mutableStateOf<Int?>(null) }
    var crismandoSelecionado by remember { mutableStateOf<String?>(null) }

    // Estados para Edição de Turmas
    var modoCriarTurma by remember { mutableStateOf(false) }
    var novoNomeTurma by remember { mutableStateOf("") }
    var novoNomeCrismando by remember { mutableStateOf("") }

    // Dados Mockados reativos
    val dadosGerais = remember {
        mutableStateMapOf(
            "Matriz" to mutableStateMapOf("Fulaninho 1" to List(10) { false }, "Fulaninho 2" to List(10) { it < 2 }),
            "Comunidade" to mutableStateMapOf("Fulaninho 3" to List(10) { false }, "Fulaninho 4" to List(10) { false })
        )
    }

    val frequenciaPorEncontro = remember { mutableStateMapOf<String, StatusFrequencia>() }
    var novoAvisoTexto by remember { mutableStateOf("") }
    val listaAvisosAtivos = remember { mutableStateListOf("Reunião com pais na Matriz", "Levar autorização para o retiro") }

    // Estados de Animação
    var animarImagem by remember { mutableStateOf(false) }
    var animarTextos by remember { mutableStateOf(false) }
    var animarBotoesAcao by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window
        window.statusBarColor = Crisma_Primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        delay(100); animarImagem = true
        delay(200); animarTextos = true
        delay(300); animarBotoesAcao = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- CABEÇALHO ---
            Box(modifier = Modifier.fillMaxWidth().weight(0.65f).background(Crisma_Primary).padding(horizontal = 16.dp, vertical = 24.dp)) {
                Row(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    UserIconWithLabel(Icons.Outlined.Info, "Sobre o App") { showSobreNosDialog = true }
                    UserIconWithLabel(Icons.Outlined.Phone, "Contatos") { showContatosDialog = true }
                }
                Column(modifier = Modifier.fillMaxSize().padding(top = 65.dp)) {
                    AnimatedVisibility(visible = animarImagem, enter = fadeIn(tween(1200)) + scaleIn(initialScale = 0.9f)) {
                        Image(painter = painterResource(id = R.drawable.imagem_crisma), contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp))
                    }
                    AnimatedVisibility(visible = animarTextos, enter = fadeIn(tween(1200)) + slideInVertically { it / 3 }) {
                        Column {
                            Text("\nGestão: Turma Jovem", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = Crisma_Gold, thickness = 2.dp, modifier = Modifier.fillMaxWidth(0.76f).padding(vertical = 12.dp))
                            Text("Administração e Pastoral", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }

            // --- SELETOR DE TURMA ---
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(modifier = Modifier.fillMaxWidth().height(screenHeight * 0.08f).offset(y = -(screenHeight * 0.04f)).background(Color.White)) {
                    Button(onClick = { }, modifier = Modifier.weight(1f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = Light_Gray_Darker), shape = RoundedCornerShape(0.dp)) {
                        Text("Turma Jovem", color = Crisma_Primary, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Crisma_Primary.copy(alpha = 0.3f)))
                    Button(onClick = { navController.navigate("turmaAdultaScreen") { launchSingleTop = true } }, modifier = Modifier.weight(1f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary), shape = RoundedCornerShape(0.dp)) {
                        Text("Turma Adulta", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- MENU ---
            Box(modifier = Modifier.fillMaxWidth().weight(0.35f).background(Color.White), contentAlignment = Alignment.TopCenter) {
                androidx.compose.animation.AnimatedVisibility(visible = animarBotoesAcao, enter = fadeIn(tween(900)) + slideInVertically { 20 }) {
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallMenuCard(title = "Frequência", icon = Icons.Outlined.CheckCircle, modifier = Modifier.weight(1f)) {
                                turmaSelecionada = null
                                showFrequenciaPopup = true
                            }
                            SmallMenuCard(title = "Turmas", icon = Icons.Outlined.Groups, modifier = Modifier.weight(1f)) {
                                turmaSelecionada = null
                                modoCriarTurma = false
                                showTurmasPopup = true
                            }
                            SmallMenuCard(title = "Avisos", icon = Icons.Outlined.Notifications, modifier = Modifier.weight(1f)) { showAvisosPopup = true }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallMenuCard(title = "Financeiro", icon = Icons.Outlined.Payments, modifier = Modifier.weight(1f)) {
                                turmaSelecionada = null
                                crismandoSelecionado = null
                                showFinanceiroPopup = true
                            }
                            SmallMenuCard(title = "Dados", icon = Icons.Outlined.BarChart, modifier = Modifier.weight(1f)) {
                                turmaSelecionada = null
                                showDadosPopup = true
                            }
                            SmallMenuCard(title = "Voltar", icon = Icons.Outlined.ArrowBack, modifier = Modifier.weight(1f)) {
                                navController.navigate("catequistaOptions") { popUpTo("turmaJovemScreen") { inclusive = true } }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- POPUP TURMAS ---
    if (showTurmasPopup) {
        val titTurma = when {
            modoCriarTurma -> "Nova Turma"
            turmaSelecionada != null -> "Editar: $turmaSelecionada"
            else -> "Gerenciar Turmas"
        }
        CustomPopup(title = titTurma, onDismiss = { showTurmasPopup = false }) {
            if (modoCriarTurma) {
                item {
                    OutlinedTextField(value = novoNomeTurma, onValueChange = { novoNomeTurma = it }, label = { Text("Nome da Turma") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { if (novoNomeTurma.isNotBlank()) { dadosGerais[novoNomeTurma] = mutableStateMapOf(); novoNomeTurma = ""; modoCriarTurma = false } }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary)) { Text("Criar Turma", fontWeight = FontWeight.Bold) }
                    TextButton(onClick = { modoCriarTurma = false }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar", color = Color.Gray) }
                }
            } else if (turmaSelecionada == null) {
                items(dadosGerais.keys.toList()) { nomeTurma ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(nomeTurma, fontWeight = FontWeight.Bold, color = Crisma_Primary, modifier = Modifier.weight(1f))
                            IconButton(onClick = { turmaSelecionada = nomeTurma }) { Icon(Icons.Outlined.Edit, "Editar", tint = Color.Gray) }
                            IconButton(onClick = { dadosGerais.remove(nomeTurma) }) { Icon(Icons.Outlined.Delete, "Excluir", tint = Color.Red.copy(0.7f)) }
                        }
                    }
                }
                item {
                    Button(onClick = { modoCriarTurma = true }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary), shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text("Nova Turma", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                item {
                    TextButton(onClick = { turmaSelecionada = null }) { Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary); Text(" Voltar", color = Crisma_Primary) }
                    OutlinedTextField(value = novoNomeCrismando, onValueChange = { novoNomeCrismando = it }, placeholder = { Text("Nome do Crismando...") }, modifier = Modifier.fillMaxWidth(), trailingIcon = {
                        IconButton(onClick = { if (novoNomeCrismando.isNotBlank()) { dadosGerais[turmaSelecionada!!]!![novoNomeCrismando] = List(10) { false }; novoNomeCrismando = "" } }) { Icon(Icons.Outlined.AddCircle, null, tint = Crisma_Primary) }
                    })
                    Spacer(Modifier.height(16.dp))
                }
                items(dadosGerais[turmaSelecionada]!!.keys.toList()) { nome ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(nome, modifier = Modifier.weight(1f), fontSize = 14.sp)
                            IconButton(onClick = { dadosGerais[turmaSelecionada!!]!!.remove(nome) }) { Icon(Icons.Outlined.Delete, null, tint = Color.Red.copy(0.6f)) }
                        }
                    }
                }
            }
        }
    }

    // --- POPUP FREQUÊNCIA ---
    if (showFrequenciaPopup) {
        val titFreq = when {
            encontroSelecionado != null -> "${encontroSelecionado}º Encontro: $turmaSelecionada"
            turmaSelecionada != null -> "Encontros: $turmaSelecionada"
            else -> "Frequência - Turmas"
        }
        CustomPopup(title = titFreq, onDismiss = { showFrequenciaPopup = false }) {
            if (turmaSelecionada == null) {
                items(dadosGerais.keys.toList()) { local ->
                    Card(onClick = { turmaSelecionada = local }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(local, fontWeight = FontWeight.Bold, color = Crisma_Primary); Icon(Icons.Outlined.ArrowForwardIos, null, modifier = Modifier.size(14.dp), tint = Crisma_Primary) }
                    }
                }
            } else if (encontroSelecionado == null) {
                item { TextButton(onClick = { turmaSelecionada = null }) { Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary); Text(" Voltar", color = Crisma_Primary) } }
                items((1..3).toList()) { num ->
                    Card(onClick = { encontroSelecionado = num }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("${num}º Encontro", color = Crisma_Primary); Icon(Icons.Outlined.ChevronRight, null, tint = Crisma_Primary) }
                    }
                }
            } else {
                item { TextButton(onClick = { encontroSelecionado = null }) { Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary); Text(" Voltar", color = Crisma_Primary) } }
                items(dadosGerais[turmaSelecionada]!!.keys.toList()) { nome ->
                    val chave = "Encontro_${encontroSelecionado}_$nome"
                    val status = frequenciaPorEncontro[chave] ?: StatusFrequencia.NENHUM
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(nome, fontWeight = FontWeight.Bold, color = Crisma_Primary)
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(onClick = { frequenciaPorEncontro[chave] = StatusFrequencia.FALTA }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(status == StatusFrequencia.FALTA) Color.Red else Color(0xFFFFEBEE))) { Text("FALTA", fontSize = 9.sp, color = if(status == StatusFrequencia.FALTA) Color.White else Color.Red) }
                                Button(onClick = { frequenciaPorEncontro[chave] = StatusFrequencia.PRESENTE }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(status == StatusFrequencia.PRESENTE) Color(0xFF2E7D32) else Color(0xFFE8F5E9))) { Text("PRESENTE", fontSize = 9.sp, color = if(status == StatusFrequencia.PRESENTE) Color.White else Color(0xFF2E7D32)) }
                                Button(onClick = { frequenciaPorEncontro[chave] = StatusFrequencia.JUSTIFICADA }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(status == StatusFrequencia.JUSTIFICADA) Color(0xFFF57C00) else Color(0xFFFFF3E0))) { Text("JUST.", fontSize = 9.sp, color = if(status == StatusFrequencia.JUSTIFICADA) Color.White else Color(0xFFF57C00)) }
                            }
                        }
                    }
                }
                item { Button(onClick = { showFrequenciaPopup = false }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary), shape = RoundedCornerShape(8.dp)) { Text("Salvar Chamada", fontWeight = FontWeight.Bold) } }
            }
        }
    }

    // --- POPUP FINANCEIRO ---
    if (showFinanceiroPopup) {
        CustomPopup(title = "Financeiro", onDismiss = { showFinanceiroPopup = false; turmaSelecionada = null; crismandoSelecionado = null }) {
            if (turmaSelecionada == null) {
                items(dadosGerais.keys.toList()) { local ->
                    Card(onClick = { turmaSelecionada = local }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(local, fontWeight = FontWeight.Bold, color = Crisma_Primary); Icon(Icons.Outlined.ArrowForwardIos, null, modifier = Modifier.size(14.dp), tint = Crisma_Primary) }
                    }
                }
            } else if (crismandoSelecionado == null) {
                item { TextButton(onClick = { turmaSelecionada = null }) { Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary); Text(" Voltar", color = Crisma_Primary) } }
                items(dadosGerais[turmaSelecionada]!!.keys.toList()) { nome ->
                    Card(onClick = { crismandoSelecionado = nome }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(nome, color = Crisma_Primary); Icon(Icons.Outlined.ChevronRight, null, tint = Crisma_Primary) }
                    }
                }
            } else {
                item { TextButton(onClick = { crismandoSelecionado = null }) { Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary); Text(" Voltar", color = Crisma_Primary) } }
                val listaPagamento = dadosGerais[turmaSelecionada]!![crismandoSelecionado]!!
                items(10) { i ->
                    val pago = listaPagamento[i]
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = if(pago) Color(0xFFF1F8E9) else Color.White)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Parcela ${i+1}", modifier = Modifier.weight(1f))
                            Button(onClick = {
                                val nova = listaPagamento.toMutableList()
                                nova[i] = !pago
                                dadosGerais[turmaSelecionada!!]!![crismandoSelecionado!!] = nova
                            }, colors = ButtonDefaults.buttonColors(containerColor = if(pago) Color(0xFF2E7D32) else Color(0xFFE0E0E0))) { Text(if(pago) "PAGO" else "PAGAR", fontSize = 10.sp) }
                        }
                    }
                }
            }
        }
    }

    // --- POPUP DADOS (COM DETALHAMENTO DE TURMA) ---
    if (showDadosPopup) {
        val tituloDados = if (turmaSelecionada == null) "Estatísticas Gerais" else "Faltas: $turmaSelecionada"

        CustomPopup(title = tituloDados, onDismiss = { showDadosPopup = false; turmaSelecionada = null }) {
            if (turmaSelecionada == null) {
                // VISÃO GERAL: Médias por Turma
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Faltas Médias do App", fontSize = 14.sp)
                            Text("13.3%", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Clique na turma para ver crismandos:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                }
                items(dadosGerais.keys.toList()) { nomeTurma ->
                    Card(
                        onClick = { turmaSelecionada = nomeTurma },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(nomeTurma, fontWeight = FontWeight.Bold, color = Crisma_Primary)
                                Text("Média: 12%", fontSize = 12.sp, color = Color.Gray)
                            }
                            Icon(Icons.Outlined.BarChart, null, tint = Crisma_Primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            } else {
                // VISÃO DETALHADA: Lista de Crismandos da Turma
                item {
                    TextButton(onClick = { turmaSelecionada = null }) {
                        Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Crisma_Primary)
                        Text(" Voltar para Visão Geral", color = Crisma_Primary)
                    }
                }
                val componentes = dadosGerais[turmaSelecionada]!!.keys.toList()
                items(componentes) { nomeCrismando ->
                    // Mock de porcentagem de faltas (Pode ser calculada dinamicamente depois)
                    val percentualFalta = (0..30).random()
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF5F5F5))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(nomeCrismando, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                            Text(
                                "$percentualFalta%",
                                fontWeight = FontWeight.Bold,
                                color = if (percentualFalta > 20) Color.Red else Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }
    }

    // --- POPUP AVISOS ---
    if (showAvisosPopup) {
        CustomPopup(title = "Avisos", onDismiss = { showAvisosPopup = false }) {
            item {
                OutlinedTextField(value = novoAvisoTexto, onValueChange = { novoAvisoTexto = it }, placeholder = { Text("Novo aviso...") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { if(novoAvisoTexto.isNotBlank()) { listaAvisosAtivos.add(0, novoAvisoTexto); novoAvisoTexto = "" } }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Crisma_Primary), shape = RoundedCornerShape(8.dp)) { Text("Publicar", fontWeight = FontWeight.Bold) }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }
            items(listaAvisosAtivos) { aviso ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(aviso, modifier = Modifier.weight(1f), fontSize = 13.sp)
                        IconButton(onClick = { listaAvisosAtivos.remove(aviso) }) { Icon(Icons.Outlined.Delete, null, tint = Color.Red.copy(0.6f)) }
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS PADRÃO ---
    if (showSobreNosDialog) AlertDialog(onDismissRequest = { showSobreNosDialog = false }, confirmButton = { TextButton(onClick = { showSobreNosDialog = false }) { Text("OK", color = Crisma_Primary, fontWeight = FontWeight.Bold) } }, title = { Text("Sobre") }, text = { Text("CrismAPP - Gestão Catequética.") })
    if (showContatosDialog) AlertDialog(onDismissRequest = { showContatosDialog = false }, confirmButton = { TextButton(onClick = { showContatosDialog = false }) { Text("OK", color = Crisma_Primary, fontWeight = FontWeight.Bold) } }, title = { Text("Contatos") }, text = { Text("Paróquia: (81) 98593-9076") })
}