package com.example.crismapp.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
private val customFont = FontFamily.Default

private const val TOTAL_PARCELAS_PAINEL = 12

private data class AlunoPainel(
    val id: String,
    val nome: String,
    val turmaId: String,
    val categoria: String,
    val ativo: Boolean
)

private data class TurmaPainel(
    val id: String,
    val nome: String,
    val categoria: String
)

private data class GrupoTurmaPainel<T>(
    val turmaId: String,
    val turmaNome: String,
    val itens: List<T>
)

private data class GrupoCategoriaPainel<T>(
    val categoria: String,
    val titulo: String,
    val turmas: List<GrupoTurmaPainel<T>>
)

private data class EncontroPainel(
    val turmaId: String
)

private data class FrequenciaPainel(
    val alunoId: String,
    val turmaId: String,
    val status: String
)

private data class AlunoComFrequenciaBaixa(
    val aluno: AlunoPainel,
    val quantidadeFaltas: Int,
    val totalEncontros: Int,
    val percentualFaltas: Float
)

private data class PagamentoPainel(
    val alunoId: String,
    val numeroParcela: Int,
    val pago: Boolean
)

private data class AlunoComParcelasPendentes(
    val aluno: AlunoPainel,
    val quantidadeRestante: Int
)

private data class DocumentoPainel(
    val alunoId: String,
    val perfil: String,
    val status: String
)

private data class ResumoTurmaPainel(
    val turmaId: String,
    val turmaNome: String,
    val categoria: String,
    val totalAtivos: Int,
    val totalDocumentacaoPendente: Int,
    val totalParcelasPendentes: Int,
    val totalFrequenciaAlerta: Int
)

private data class GrupoResumoTurmasPainel(
    val categoria: String,
    val titulo: String,
    val resumos: List<ResumoTurmaPainel>
)

private fun normalizarCategoriaPainel(valor: String): String {
    val categoria = valor
        .trim()
        .lowercase(Locale.ROOT)

    return when {
        categoria.contains("adult") -> "adulta"
        categoria.contains("jov") -> "jovem"
        else -> categoria
    }
}

private fun tituloCategoriaPainel(categoria: String): String {
    return when (normalizarCategoriaPainel(categoria)) {
        "adulta" -> "Adultos"
        "jovem" -> "Jovens"
        else -> "Sem categoria"
    }
}

private fun <T> agruparPorCategoriaETurma(
    itens: List<T>,
    turmas: List<TurmaPainel>,
    obterAluno: (T) -> AlunoPainel,
    comparadorItens: Comparator<T>? = null
): List<GrupoCategoriaPainel<T>> {
    val localeBrasil = Locale("pt", "BR")
    val turmasPorId = turmas.associateBy { it.id }

    val itensPorCategoria = itens.groupBy { item ->
        val aluno = obterAluno(item)
        val turma = turmasPorId[aluno.turmaId]

        normalizarCategoriaPainel(
            turma?.categoria
                .orEmpty()
                .ifBlank { aluno.categoria }
        )
    }

    val ordemCategorias = buildList {
        add("adulta")
        add("jovem")

        itensPorCategoria.keys
            .filter {
                it != "adulta" &&
                        it != "jovem"
            }
            .sorted()
            .forEach(::add)
    }

    return ordemCategorias.mapNotNull { categoria ->
        val itensDaCategoria =
            itensPorCategoria[categoria].orEmpty()

        if (itensDaCategoria.isEmpty()) {
            null
        } else {
            val gruposTurma = itensDaCategoria
                .groupBy { item ->
                    obterAluno(item).turmaId
                }
                .map { (turmaId, itensDaTurma) ->
                    val turma = turmasPorId[turmaId]

                    GrupoTurmaPainel(
                        turmaId = turmaId,
                        turmaNome = turma
                            ?.nome
                            .orEmpty()
                            .ifBlank {
                                turmaId.ifBlank {
                                    "Turma não identificada"
                                }
                            },
                        itens = if (comparadorItens != null) {
                            itensDaTurma.sortedWith(comparadorItens)
                        } else {
                            itensDaTurma.sortedBy { item ->
                                obterAluno(item)
                                    .nome
                                    .lowercase(localeBrasil)
                            }
                        }
                    )
                }
                .sortedBy {
                    it.turmaNome.lowercase(localeBrasil)
                }

            GrupoCategoriaPainel(
                categoria = categoria,
                titulo = tituloCategoriaPainel(categoria),
                turmas = gruposTurma
            )
        }
    }
}

@Composable
fun CatequistaOptionsScreen(navController: NavController) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val db = remember { FirebaseFirestore.getInstance() }

    var showSobreNosDialog by remember { mutableStateOf(false) }
    var showContatosDialog by remember { mutableStateOf(false) }

    /*
     * Configurações compartilhadas da tela inicial.
     * Título, ícone e conteúdo dos popups acompanham em tempo real
     * o que o administrador configurar em Sobre e Contatos.
     */
    var atalhosIniciais by remember {
        mutableStateOf(
            AtalhosIniciaisPadrao.lista()
        )
    }

    val sobreInicial = atalhosIniciais
        .firstOrNull {
            it.id == AtalhosIniciaisPadrao.ID_SOBRE
        }
        ?: AtalhosIniciaisPadrao.sobre()

    val contatosInicial = atalhosIniciais
        .firstOrNull {
            it.id == AtalhosIniciaisPadrao.ID_CONTATOS
        }
        ?: AtalhosIniciaisPadrao.contatos()
    var showDocumentacaoPendenteDialog by remember {
        mutableStateOf(false)
    }
    var showParcelasPendentesDialog by remember {
        mutableStateOf(false)
    }
    var showFrequenciaBaixaDialog by remember {
        mutableStateOf(false)
    }
    var showResumoTurmasDialog by remember {
        mutableStateOf(false)
    }
    var showRelatorioMensalPendenteDialog by remember {
        mutableStateOf(false)
    }
    var animarImagem by remember { mutableStateOf(false) }
    var animarTextos by remember { mutableStateOf(false) }
    var animarBotoes by remember { mutableStateOf(false) }
    var animarIconesTopo by remember { mutableStateOf(true) }

    var alunosPainel by remember { mutableStateOf(emptyList<AlunoPainel>()) }
    var turmasPainel by remember { mutableStateOf(emptyList<TurmaPainel>()) }
    var encontrosPainel by remember { mutableStateOf(emptyList<EncontroPainel>()) }
    var frequenciasPainel by remember { mutableStateOf(emptyList<FrequenciaPainel>()) }
    var pagamentosPainel by remember { mutableStateOf(emptyList<PagamentoPainel>()) }
    var documentosPainel by remember { mutableStateOf(emptyList<DocumentoPainel>()) }

    var carregandoAlunos by remember { mutableStateOf(true) }
    var carregandoTurmas by remember { mutableStateOf(true) }
    var carregandoEncontros by remember { mutableStateOf(true) }
    var carregandoFrequencias by remember { mutableStateOf(true) }
    var carregandoPagamentos by remember { mutableStateOf(true) }
    var carregandoDocumentos by remember { mutableStateOf(true) }

    val carregandoPainel =
        carregandoAlunos ||
                carregandoTurmas ||
                carregandoEncontros ||
                carregandoFrequencias ||
                carregandoPagamentos ||
                carregandoDocumentos

    val alunosAtivos by remember(alunosPainel) {
        derivedStateOf {
            alunosPainel.filter { aluno ->
                aluno.ativo &&
                        aluno.id.isNotBlank() &&
                        aluno.turmaId.isNotBlank()
            }
        }
    }

    val totalCrismandosAtivos by remember(alunosAtivos) {
        derivedStateOf {
            alunosAtivos.map { it.id }.distinct().size
        }
    }

    val alunosComDocumentacaoPendente by remember(
        alunosAtivos,
        documentosPainel
    ) {
        derivedStateOf {
            alunosAtivos
                .filter { aluno ->
                    val documentosDoAluno = documentosPainel.filter {
                        it.alunoId.equals(
                            aluno.id,
                            ignoreCase = true
                        )
                    }

                    val perfisConfigurados = documentosDoAluno
                        .map {
                            it.perfil.uppercase(Locale.ROOT)
                        }
                        .toSet()

                    documentosDoAluno.isEmpty() ||
                            "CRISMANDO" !in perfisConfigurados ||
                            "PADRINHO" !in perfisConfigurados ||
                            documentosDoAluno.any {
                                it.status.equals(
                                    "NAO_ENTREGUE",
                                    ignoreCase = true
                                )
                            }
                }
                .sortedBy {
                    it.nome.lowercase(Locale("pt", "BR"))
                }
        }
    }

    val totalDocumentacaoPendente by remember(
        alunosComDocumentacaoPendente
    ) {
        derivedStateOf {
            alunosComDocumentacaoPendente.size
        }
    }

    val alunosComFrequenciaBaixa by remember(
        alunosAtivos,
        encontrosPainel,
        frequenciasPainel
    ) {
        derivedStateOf {
            val localeBrasil = Locale("pt", "BR")

            alunosAtivos
                .mapNotNull { aluno ->
                    val totalEncontrosDaTurma = encontrosPainel.count {
                        it.turmaId == aluno.turmaId
                    }

                    if (totalEncontrosDaTurma <= 0) {
                        null
                    } else {
                        /*
                         * Para o percentual de faltas, tanto FALTA quanto
                         * JUSTIFICADA contam como encontro não frequentado.
                         * Registros NENHUM não entram como falta.
                         */
                        val quantidadeFaltas =
                            frequenciasPainel.count { frequencia ->
                                frequencia.alunoId.equals(
                                    aluno.id,
                                    ignoreCase = true
                                ) &&
                                        frequencia.turmaId == aluno.turmaId &&
                                        (
                                                frequencia.status.equals(
                                                    "FALTA",
                                                    ignoreCase = true
                                                ) ||
                                                        frequencia.status.equals(
                                                            "JUSTIFICADA",
                                                            ignoreCase = true
                                                        )
                                                )
                            }
                                .coerceAtMost(totalEncontrosDaTurma)

                        val percentualFaltas =
                            quantidadeFaltas.toFloat() /
                                    totalEncontrosDaTurma.toFloat() *
                                    100f

                        if (percentualFaltas > 20f) {
                            AlunoComFrequenciaBaixa(
                                aluno = aluno,
                                quantidadeFaltas = quantidadeFaltas,
                                totalEncontros = totalEncontrosDaTurma,
                                percentualFaltas = percentualFaltas
                            )
                        } else {
                            null
                        }
                    }
                }
                .sortedWith(
                    compareByDescending<AlunoComFrequenciaBaixa> {
                        it.percentualFaltas
                    }.thenBy {
                        it.aluno.nome.lowercase(localeBrasil)
                    }
                )
        }
    }

    val totalFrequenciaBaixa by remember(
        alunosComFrequenciaBaixa
    ) {
        derivedStateOf {
            alunosComFrequenciaBaixa.size
        }
    }

    val alunosComParcelasPendentes by remember(
        alunosAtivos,
        pagamentosPainel
    ) {
        derivedStateOf {
            alunosAtivos
                .mapNotNull { aluno ->
                    val parcelasPagas = pagamentosPainel
                        .filter {
                            it.alunoId.equals(
                                aluno.id,
                                ignoreCase = true
                            ) &&
                                    it.pago &&
                                    it.numeroParcela in
                                    1..TOTAL_PARCELAS_PAINEL
                        }
                        .map { it.numeroParcela }
                        .distinct()
                        .size

                    val quantidadeRestante =
                        (TOTAL_PARCELAS_PAINEL - parcelasPagas)
                            .coerceAtLeast(0)

                    if (quantidadeRestante > 0) {
                        AlunoComParcelasPendentes(
                            aluno = aluno,
                            quantidadeRestante = quantidadeRestante
                        )
                    } else {
                        null
                    }
                }
                .sortedBy {
                    it.aluno.nome.lowercase(
                        Locale("pt", "BR")
                    )
                }
        }
    }

    val totalParcelasPendentes by remember(
        alunosComParcelasPendentes
    ) {
        derivedStateOf {
            alunosComParcelasPendentes.sumOf {
                it.quantidadeRestante
            }
        }
    }

    val documentacaoPendenteAgrupada by remember(
        alunosComDocumentacaoPendente,
        turmasPainel
    ) {
        derivedStateOf {
            agruparPorCategoriaETurma(
                itens = alunosComDocumentacaoPendente,
                turmas = turmasPainel,
                obterAluno = { aluno -> aluno }
            )
        }
    }

    val parcelasPendentesAgrupadas by remember(
        alunosComParcelasPendentes,
        turmasPainel
    ) {
        derivedStateOf {
            agruparPorCategoriaETurma(
                itens = alunosComParcelasPendentes,
                turmas = turmasPainel,
                obterAluno = { pendencia ->
                    pendencia.aluno
                }
            )
        }
    }

    val frequenciaBaixaAgrupada by remember(
        alunosComFrequenciaBaixa,
        turmasPainel
    ) {
        derivedStateOf {
            val localeBrasil = Locale("pt", "BR")

            agruparPorCategoriaETurma(
                itens = alunosComFrequenciaBaixa,
                turmas = turmasPainel,
                obterAluno = { frequenciaBaixa ->
                    frequenciaBaixa.aluno
                },
                comparadorItens =
                compareByDescending<AlunoComFrequenciaBaixa> {
                    it.percentualFaltas
                }.thenBy {
                    it.aluno.nome.lowercase(localeBrasil)
                }
            )
        }
    }

    /*
     * Resumo geral por turma usado ao tocar em "Crismandos ativos".
     *
     * Regras:
     * - ativos: quantidade de crismandos ativos;
     * - documentos: quantidade de crismandos com documentação incompleta;
     * - parcelas: soma de todas as parcelas ainda pendentes;
     * - frequência: quantidade de crismandos com mais de 20% de faltas.
     */
    val resumosTurmas by remember(
        alunosAtivos,
        turmasPainel,
        alunosComDocumentacaoPendente,
        alunosComParcelasPendentes,
        alunosComFrequenciaBaixa
    ) {
        derivedStateOf {
            val localeBrasil = Locale("pt", "BR")
            val turmasPorId = turmasPainel.associateBy { it.id }

            val idsDocumentacaoPendente =
                alunosComDocumentacaoPendente
                    .map { it.id.lowercase(Locale.ROOT) }
                    .toSet()

            val parcelasPendentesPorAluno =
                alunosComParcelasPendentes.associate {
                    it.aluno.id.lowercase(Locale.ROOT) to
                            it.quantidadeRestante
                }

            val idsFrequenciaAlerta =
                alunosComFrequenciaBaixa
                    .map {
                        it.aluno.id.lowercase(Locale.ROOT)
                    }
                    .toSet()

            alunosAtivos
                .groupBy { it.turmaId }
                .map { (turmaId, alunosDaTurma) ->
                    val turma = turmasPorId[turmaId]

                    val categoria = normalizarCategoriaPainel(
                        turma?.categoria
                            .orEmpty()
                            .ifBlank {
                                alunosDaTurma
                                    .firstOrNull()
                                    ?.categoria
                                    .orEmpty()
                            }
                    )

                    ResumoTurmaPainel(
                        turmaId = turmaId,
                        turmaNome = turma
                            ?.nome
                            .orEmpty()
                            .ifBlank {
                                turmaId.ifBlank {
                                    "Turma não identificada"
                                }
                            },
                        categoria = categoria,
                        totalAtivos = alunosDaTurma.size,
                        totalDocumentacaoPendente =
                        alunosDaTurma.count { aluno ->
                            aluno.id
                                .lowercase(Locale.ROOT) in
                                    idsDocumentacaoPendente
                        },
                        totalParcelasPendentes =
                        alunosDaTurma.sumOf { aluno ->
                            parcelasPendentesPorAluno[
                                aluno.id.lowercase(Locale.ROOT)
                            ] ?: 0
                        },
                        totalFrequenciaAlerta =
                        alunosDaTurma.count { aluno ->
                            aluno.id
                                .lowercase(Locale.ROOT) in
                                    idsFrequenciaAlerta
                        }
                    )
                }
                .sortedWith(
                    compareBy<ResumoTurmaPainel> {
                        when (it.categoria) {
                            "adulta" -> 0
                            "jovem" -> 1
                            else -> 2
                        }
                    }.thenBy {
                        it.turmaNome.lowercase(localeBrasil)
                    }
                )
        }
    }

    val resumosTurmasAgrupados by remember(resumosTurmas) {
        derivedStateOf {
            val gruposPorCategoria =
                resumosTurmas.groupBy { it.categoria }

            val ordemCategorias = buildList {
                add("adulta")
                add("jovem")

                gruposPorCategoria.keys
                    .filter {
                        it != "adulta" &&
                                it != "jovem"
                    }
                    .sorted()
                    .forEach(::add)
            }

            ordemCategorias.mapNotNull { categoria ->
                val resumos =
                    gruposPorCategoria[categoria].orEmpty()

                if (resumos.isEmpty()) {
                    null
                } else {
                    GrupoResumoTurmasPainel(
                        categoria = categoria,
                        titulo =
                        tituloCategoriaPainel(categoria),
                        resumos = resumos
                    )
                }
            }
        }
    }

    val catequista = FirebaseAuthRepository.catequistaAtual

    val referenciaMesAnterior = remember {
        RelatorioMensalRepository.obterReferenciaMesAnterior()
    }

    DisposableEffect(Unit) {
        val listener =
            FirebaseRepository.ouvirAtalhosIniciais(
                onUpdate = { configuracoes ->
                    atalhosIniciais = configuracoes
                },
                onError = {
                    atalhosIniciais =
                        AtalhosIniciaisPadrao.lista()
                }
            )

        onDispose {
            listener.remove()
        }
    }

    LaunchedEffect(catequista?.uid) {
        if (
            catequista?.possuiPermissaoTotal() == true
        ) {
            val preferencias = context.getSharedPreferences(
                "crismapp_relatorios",
                android.content.Context.MODE_PRIVATE
            )

            val chaveLembrete =
                "lembrete_${referenciaMesAnterior.chave}"

            val lembreteJaExibido =
                preferencias.getBoolean(
                    chaveLembrete,
                    false
                )

            if (!lembreteJaExibido) {
                RelatorioMensalRepository
                    .verificarRelatorioGerado(
                        referencia = referenciaMesAnterior,
                        onSuccess = { jaGerado ->
                            preferencias.edit()
                                .putBoolean(
                                    chaveLembrete,
                                    true
                                )
                                .apply()

                            if (!jaGerado) {
                                showRelatorioMensalPendenteDialog =
                                    true
                            }
                        },
                        onError = {
                            // O erro da verificação não bloqueia o painel.
                        }
                    )
            }
        }
    }

    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window

        window.statusBarColor = Crisma_Primary.toArgb()

        WindowCompat.getInsetsController(
            window,
            view
        ).isAppearanceLightStatusBars = false

        if (FirebaseAuthRepository.catequistaAtual == null) {
            FirebaseAuthRepository.restaurarSessao(
                onSuccess = {},
                onSemSessao = {
                    navController.navigate("LoginCatequista") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onError = { erro ->
                    Toast.makeText(
                        context,
                        erro.message ?: "Acesso não autorizado.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.navigate("LoginCatequista") {
                        popUpTo(0) { inclusive = true }
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

    DisposableEffect(Unit) {
        val listenerTurmas = db.collection("turmas")
            .addSnapshotListener { snapshot, erro ->
                carregandoTurmas = false

                if (erro != null || snapshot == null) {
                    turmasPainel = emptyList()
                    return@addSnapshotListener
                }

                turmasPainel = snapshot.documents.map { documento ->
                    TurmaPainel(
                        id = documento.id,
                        nome = documento
                            .getString("nome")
                            .orEmpty()
                            .trim()
                            .ifBlank { documento.id },
                        categoria = normalizarCategoriaPainel(
                            documento
                                .getString("categoria")
                                .orEmpty()
                        )
                    )
                }
            }

        val listenerAlunos = db.collection("usuarios")
            .addSnapshotListener { snapshot, erro ->
                carregandoAlunos = false

                if (erro != null || snapshot == null) {
                    alunosPainel = emptyList()
                    return@addSnapshotListener
                }

                alunosPainel = snapshot.documents.mapNotNull { documento ->
                    val nome = documento.getString("nome")
                        .orEmpty()
                        .trim()

                    if (nome.isBlank()) {
                        null
                    } else {
                        AlunoPainel(
                            id = documento.id,
                            nome = nome,
                            turmaId = documento
                                .getString("turmaId")
                                .orEmpty(),
                            categoria = normalizarCategoriaPainel(
                                documento
                                    .getString("categoria")
                                    .orEmpty()
                            ),
                            ativo = documento
                                .getBoolean("ativo")
                                ?: true
                        )
                    }
                }
            }

        val listenerEncontros = db.collection("encontros")
            .addSnapshotListener { snapshot, erro ->
                carregandoEncontros = false

                if (erro != null || snapshot == null) {
                    encontrosPainel = emptyList()
                    return@addSnapshotListener
                }

                encontrosPainel = snapshot.documents.mapNotNull { documento ->
                    val turmaId = documento.getString("turmaId").orEmpty()

                    if (turmaId.isBlank()) {
                        null
                    } else {
                        EncontroPainel(turmaId = turmaId)
                    }
                }
            }

        val listenerFrequencias = db.collection("frequencias")
            .addSnapshotListener { snapshot, erro ->
                carregandoFrequencias = false

                if (erro != null || snapshot == null) {
                    frequenciasPainel = emptyList()
                    return@addSnapshotListener
                }

                frequenciasPainel = snapshot.documents.mapNotNull { documento ->
                    val alunoId = documento.getString("alunoId").orEmpty()

                    if (alunoId.isBlank()) {
                        null
                    } else {
                        FrequenciaPainel(
                            alunoId = alunoId,
                            turmaId = documento
                                .getString("turmaId")
                                .orEmpty(),
                            status = documento
                                .getString("status")
                                .orEmpty()
                        )
                    }
                }
            }

        val listenerPagamentos = db.collection("financeiro")
            .addSnapshotListener { snapshot, erro ->
                carregandoPagamentos = false

                if (erro != null || snapshot == null) {
                    pagamentosPainel = emptyList()
                    return@addSnapshotListener
                }

                pagamentosPainel = snapshot.documents.mapNotNull { documento ->
                    val alunoId = documento.getString("alunoId").orEmpty()

                    val numeroParcela =
                        documento.getLong("parcela")?.toInt()
                            ?: documento.getLong("numeroParcela")?.toInt()
                            ?: 0

                    val status = documento
                        .getString("status")
                        .orEmpty()
                        .trim()
                        .uppercase(Locale.ROOT)

                    val pago =
                        status == "PAGO" ||
                                (
                                        status.isBlank() &&
                                                documento.getBoolean("statusPago") == true
                                        )

                    if (
                        alunoId.isBlank() ||
                        numeroParcela !in 1..TOTAL_PARCELAS_PAINEL
                    ) {
                        null
                    } else {
                        PagamentoPainel(
                            alunoId = alunoId,
                            numeroParcela = numeroParcela,
                            pago = pago
                        )
                    }
                }
            }

        val listenerDocumentos = db.collection("documentos")
            .addSnapshotListener { snapshot, erro ->
                carregandoDocumentos = false

                if (erro != null || snapshot == null) {
                    documentosPainel = emptyList()
                    return@addSnapshotListener
                }

                documentosPainel = snapshot.documents.mapNotNull { documento ->
                    val alunoId = documento.getString("alunoId").orEmpty()

                    val perfilSalvo = documento
                        .getString("perfil")
                        .orEmpty()
                        .trim()
                        .uppercase(Locale.ROOT)

                    val perfil = when {
                        perfilSalvo.isNotBlank() -> perfilSalvo

                        documento.id.contains(
                            "-PADRINHO-",
                            ignoreCase = true
                        ) -> "PADRINHO"

                        documento.id.contains(
                            "-CRISMANDO-",
                            ignoreCase = true
                        ) -> "CRISMANDO"

                        else -> ""
                    }

                    if (alunoId.isBlank()) {
                        null
                    } else {
                        DocumentoPainel(
                            alunoId = alunoId,
                            perfil = perfil,
                            status = documento.getString("status").orEmpty()
                        )
                    }
                }
            }

        onDispose {
            listenerTurmas.remove()
            listenerAlunos.remove()
            listenerEncontros.remove()
            listenerFrequencias.remove()
            listenerPagamentos.remove()
            listenerDocumentos.remove()
        }
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
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (animarIconesTopo) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            horizontalArrangement =
                            Arrangement.SpaceBetween
                        ) {
                            UserIconWithLabel(
                                icon = iconeAtalhoInicial(
                                    sobreInicial.iconeCodigo
                                ),
                                label = sobreInicial.titulo
                            ) {
                                showSobreNosDialog = true
                            }

                            UserIconWithLabel(
                                icon = iconeAtalhoInicial(
                                    contatosInicial.iconeCodigo
                                ),
                                label = contatosInicial.titulo
                            ) {
                                showContatosDialog = true
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (animarImagem) {
                        Image(
                            painter = painterResource(
                                id = R.drawable.imagem_crisma
                            ),
                            contentDescription = "Logo CrismAPP",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
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
                    .offset(y = -(screenHeight * 0.04f))
                    .background(Color.White),
                contentAlignment = Alignment.TopCenter
            ) {
                if (animarBotoes) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = 14.dp,
                                end = 14.dp,
                                bottom = 10.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .requiredWidth(screenWidth)
                                .height(54.dp)
                                .background(Color.White),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(
                                        if (
                                            catequista
                                                ?.possuiPermissaoTotal() ==
                                            true
                                        ) {
                                            1.35f
                                        } else {
                                            1f
                                        }
                                    )
                                    .fillMaxHeight()
                                    .background(
                                        Color(0xFFE0E0E0)
                                    )
                                    .padding(horizontal = 10.dp),
                                contentAlignment =
                                Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment =
                                    Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text =
                                            "Visão geral da paróquia",
                                            fontSize = 14.sp,
                                            lineHeight = 15.sp,
                                            color = Color.Black,
                                            fontWeight =
                                            FontWeight.Bold
                                        )

                                        Text(
                                            text =
                                            "Indicadores em tempo real",
                                            fontSize = 10.sp,
                                            lineHeight = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    if (carregandoPainel) {
                                        CircularProgressIndicator(
                                            modifier =
                                            Modifier.size(16.dp),
                                            color = Crisma_Primary,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }

                            if (
                                catequista
                                    ?.possuiPermissaoTotal() == true
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .fillMaxHeight()
                                        .background(Crisma_Gold)
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(0.85f)
                                        .fillMaxHeight()
                                        .background(Color.White)
                                        .clickable {
                                            navController.navigate(
                                                "relatoriosMensaisScreen"
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment =
                                        Alignment.CenterVertically,
                                        horizontalArrangement =
                                        Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector =
                                            Icons.Outlined.Download,
                                            contentDescription =
                                            "Gerar relatório TXT",
                                            tint = Crisma_Primary,
                                            modifier =
                                            Modifier.size(16.dp)
                                        )

                                        Spacer(
                                            modifier =
                                            Modifier.width(6.dp)
                                        )

                                        Text(
                                            text = "Relatórios",
                                            color = Color.Gray,
                                            fontWeight =
                                            FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PainelIndicadorCard(
                                valor = totalCrismandosAtivos,
                                titulo = "Crismandos ativos",
                                detalhe = "Resumo por turma",
                                icone = Icons.Outlined.Groups,
                                corDestaque = Crisma_Primary,
                                corFundo = Color(0xFFFFFCFC),
                                carregando = carregandoPainel,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    showResumoTurmasDialog = true
                                }
                            )

                            PainelIndicadorCard(
                                valor = totalDocumentacaoPendente,
                                titulo = "Documentação",
                                detalhe = "Toque para visualizar",
                                icone = Icons.Outlined.Description,
                                corDestaque = Crisma_Primary,
                                corFundo = Color(0xFFFFFCFC),
                                carregando =
                                carregandoAlunos ||
                                        carregandoDocumentos,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    showDocumentacaoPendenteDialog = true
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PainelIndicadorCard(
                                valor = totalFrequenciaBaixa,
                                titulo = "Frequência baixa",
                                detalhe = "Mais de 20% de faltas",
                                icone = Icons.Outlined.Assessment,
                                corDestaque = Crisma_Primary,
                                corFundo = Color(0xFFFFFCFC),
                                carregando =
                                carregandoAlunos ||
                                        carregandoTurmas ||
                                        carregandoEncontros ||
                                        carregandoFrequencias,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    showFrequenciaBaixaDialog = true
                                }
                            )

                            PainelIndicadorCard(
                                valor = totalParcelasPendentes,
                                titulo = "Parcelas pendentes",
                                detalhe = "Toque para visualizar",
                                icone = Icons.Outlined.Payments,
                                corDestaque = Crisma_Primary,
                                corFundo = Color(0xFFFFFCFC),
                                carregando =
                                carregandoAlunos ||
                                        carregandoPagamentos,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    showParcelasPendentesDialog = true
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        PainelSairCard(
                            onClick = {
                                FirebaseAuthRepository.sair()

                                navController.navigate("LoginCatequista") {
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
    }

    if (showRelatorioMensalPendenteDialog) {
        AlertDialog(
            onDismissRequest = {
                showRelatorioMensalPendenteDialog = false
            },
            containerColor = Color(0xFFFAFAFA),
            tonalElevation = 0.dp,
            icon = {
                Icon(
                    imageVector =
                    Icons.Outlined.Description,
                    contentDescription = null,
                    tint = Crisma_Primary
                )
            },
            title = {
                Text(
                    text = "Relatório mensal pendente",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "O relatório de " +
                            referenciaMesAnterior.titulo +
                            " ainda não foi registrado. " +
                            "Deseja gerar o TXT agora?"
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRelatorioMensalPendenteDialog =
                            false
                    }
                ) {
                    Text(
                        text = "Depois",
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRelatorioMensalPendenteDialog =
                            false

                        navController.navigate(
                            "relatoriosMensaisScreen"
                        )
                    }
                ) {
                    Text(
                        text = "Gerar",
                        color = Crisma_Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    if (showResumoTurmasDialog) {
        AlertDialog(
            onDismissRequest = {
                showResumoTurmasDialog = false
            },
            containerColor = Color(0xFFFAFAFA),
            tonalElevation = 0.dp,
            confirmButton = {
                TextButton(
                    onClick = {
                        showResumoTurmasDialog = false
                    }
                ) {
                    Text(
                        text = "Fechar",
                        color = Crisma_Primary
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = null,
                    tint = Crisma_Primary
                )
            },
            title = {
                Text(
                    text = "Resumo das turmas",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                when {
                    carregandoPainel -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Crisma_Primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    resumosTurmas.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment =
                            Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector =
                                Icons.Outlined.Groups,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(34.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text =
                                "Nenhuma turma possui crismandos ativos.",
                                color = Color(0xFF444444),
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 470.dp)
                                .verticalScroll(
                                    rememberScrollState()
                                )
                        ) {
                            Text(
                                text =
                                "$totalCrismandosAtivos crismando(s) ativo(s) em ${resumosTurmas.size} turma(s)",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            resumosTurmasAgrupados.forEach {
                                    grupoCategoria ->

                                CabecalhoCategoriaPainel(
                                    titulo =
                                    grupoCategoria.titulo,
                                    quantidade =
                                    grupoCategoria.resumos
                                        .sumOf {
                                            it.totalAtivos
                                        }
                                )

                                grupoCategoria.resumos.forEach {
                                        resumo ->

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                vertical = 4.dp
                                            ),
                                        colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                            Color.White
                                        ),
                                        elevation =
                                        CardDefaults
                                            .cardElevation(
                                                defaultElevation =
                                                1.dp
                                            ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color =
                                            Color(0xFFECECEC)
                                        ),
                                        shape =
                                        RoundedCornerShape(
                                            10.dp
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier =
                                                Modifier.fillMaxWidth(),
                                                verticalAlignment =
                                                Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier =
                                                    Modifier
                                                        .size(32.dp)
                                                        .background(
                                                            color =
                                                            Crisma_Primary
                                                                .copy(
                                                                    alpha =
                                                                    0.08f
                                                                ),
                                                            shape =
                                                            RoundedCornerShape(
                                                                9.dp
                                                            )
                                                        ),
                                                    contentAlignment =
                                                    Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector =
                                                        Icons.Outlined
                                                            .Groups,
                                                        contentDescription =
                                                        null,
                                                        tint =
                                                        Crisma_Primary,
                                                        modifier =
                                                        Modifier
                                                            .size(
                                                                18.dp
                                                            )
                                                    )
                                                }

                                                Spacer(
                                                    modifier =
                                                    Modifier.width(
                                                        9.dp
                                                    )
                                                )

                                                Column(
                                                    modifier =
                                                    Modifier.weight(
                                                        1f
                                                    )
                                                ) {
                                                    Text(
                                                        text =
                                                        resumo.turmaNome,
                                                        color =
                                                        Color.Black,
                                                        fontSize = 13.sp,
                                                        fontWeight =
                                                        FontWeight.Bold
                                                    )

                                                    Text(
                                                        text =
                                                        if (
                                                            resumo.totalAtivos ==
                                                            1
                                                        ) {
                                                            "1 crismando ativo"
                                                        } else {
                                                            "${resumo.totalAtivos} crismandos ativos"
                                                        },
                                                        color =
                                                        Color.Gray,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            HorizontalDivider(
                                                modifier =
                                                Modifier.padding(
                                                    vertical = 8.dp
                                                ),
                                                color =
                                                Color(0xFFF0F0F0),
                                                thickness = 1.dp
                                            )

                                            Row(
                                                modifier =
                                                Modifier.fillMaxWidth(),
                                                horizontalArrangement =
                                                Arrangement.spacedBy(
                                                    6.dp
                                                )
                                            ) {
                                                MetricaResumoTurma(
                                                    valor =
                                                    resumo.totalAtivos,
                                                    titulo = "Ativos",
                                                    icone =
                                                    Icons.Outlined
                                                        .Person,
                                                    modifier =
                                                    Modifier.weight(
                                                        1f
                                                    )
                                                )

                                                MetricaResumoTurma(
                                                    valor =
                                                    resumo
                                                        .totalDocumentacaoPendente,
                                                    titulo =
                                                    "Documentos",
                                                    icone =
                                                    Icons.Outlined
                                                        .Description,
                                                    modifier =
                                                    Modifier.weight(
                                                        1f
                                                    )
                                                )
                                            }

                                            Spacer(
                                                modifier =
                                                Modifier.height(
                                                    6.dp
                                                )
                                            )

                                            Row(
                                                modifier =
                                                Modifier.fillMaxWidth(),
                                                horizontalArrangement =
                                                Arrangement.spacedBy(
                                                    6.dp
                                                )
                                            ) {
                                                MetricaResumoTurma(
                                                    valor =
                                                    resumo
                                                        .totalParcelasPendentes,
                                                    titulo =
                                                    "Parcelas",
                                                    icone =
                                                    Icons.Outlined
                                                        .Payments,
                                                    modifier =
                                                    Modifier.weight(
                                                        1f
                                                    )
                                                )

                                                MetricaResumoTurma(
                                                    valor =
                                                    resumo
                                                        .totalFrequenciaAlerta,
                                                    titulo =
                                                    "Em alerta",
                                                    icone =
                                                    Icons.Outlined
                                                        .Assessment,
                                                    modifier =
                                                    Modifier.weight(
                                                        1f
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(
                                    modifier =
                                    Modifier.height(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    if (showFrequenciaBaixaDialog) {
        AlertDialog(
            onDismissRequest = {
                showFrequenciaBaixaDialog = false
            },
            containerColor = Color(0xFFFAFAFA),
            tonalElevation = 0.dp,
            confirmButton = {
                TextButton(
                    onClick = {
                        showFrequenciaBaixaDialog = false
                    }
                ) {
                    Text(
                        text = "Fechar",
                        color = Crisma_Primary
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Assessment,
                    contentDescription = null,
                    tint = Crisma_Primary
                )
            },
            title = {
                Text(
                    text = "Frequência baixa",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                when {
                    carregandoAlunos ||
                            carregandoTurmas ||
                            carregandoEncontros ||
                            carregandoFrequencias -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Crisma_Primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    alunosComFrequenciaBaixa.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment =
                            Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector =
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(34.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Nenhum crismando ativo possui mais de 20% de faltas.",
                                color = Color(0xFF444444),
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 455.dp)
                                .verticalScroll(
                                    rememberScrollState()
                                )
                        ) {
                            Text(
                                text = "${alunosComFrequenciaBaixa.size} crismando(s) acima de 20% de faltas",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            frequenciaBaixaAgrupada.forEach {
                                    grupoCategoria ->

                                CabecalhoCategoriaPainel(
                                    titulo = grupoCategoria.titulo,
                                    quantidade = grupoCategoria.turmas
                                        .sumOf { it.itens.size }
                                )

                                grupoCategoria.turmas.forEach {
                                        grupoTurma ->

                                    CabecalhoTurmaPainel(
                                        nome = grupoTurma.turmaNome,
                                        quantidade = grupoTurma.itens.size
                                    )

                                    grupoTurma.itens.forEach {
                                            frequenciaBaixa ->

                                        val aluno =
                                            frequenciaBaixa.aluno

                                        val percentualFormatado =
                                            String.format(
                                                Locale("pt", "BR"),
                                                "%.0f%%",
                                                frequenciaBaixa
                                                    .percentualFaltas
                                            )

                                        val textoFaltas =
                                            if (
                                                frequenciaBaixa
                                                    .quantidadeFaltas == 1
                                            ) {
                                                "1 falta"
                                            } else {
                                                "${frequenciaBaixa.quantidadeFaltas} faltas"
                                            }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            colors =
                                            CardDefaults.cardColors(
                                                containerColor =
                                                Color.White
                                            ),
                                            elevation =
                                            CardDefaults.cardElevation(
                                                defaultElevation = 1.dp
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = Color(0xFFEEEEEE)
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        horizontal = 10.dp,
                                                        vertical = 9.dp
                                                    ),
                                                verticalAlignment =
                                                Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(
                                                            color =
                                                            Crisma_Primary
                                                                .copy(
                                                                    alpha =
                                                                    0.08f
                                                                ),
                                                            shape =
                                                            RoundedCornerShape(
                                                                9.dp
                                                            )
                                                        ),
                                                    contentAlignment =
                                                    Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector =
                                                        Icons.Outlined
                                                            .Assessment,
                                                        contentDescription =
                                                        null,
                                                        tint = Crisma_Primary,
                                                        modifier =
                                                        Modifier.size(18.dp)
                                                    )
                                                }

                                                Spacer(
                                                    modifier =
                                                    Modifier.width(9.dp)
                                                )

                                                Column(
                                                    modifier =
                                                    Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = aluno.nome,
                                                        color = Color.Black,
                                                        fontSize = 13.sp,
                                                        fontWeight =
                                                        FontWeight.Bold
                                                    )

                                                    Text(
                                                        text =
                                                        "Matrícula: ${aluno.id}",
                                                        color = Color.Gray,
                                                        fontSize = 10.sp
                                                    )
                                                }

                                                Column(
                                                    horizontalAlignment =
                                                    Alignment.End
                                                ) {
                                                    Text(
                                                        text =
                                                        percentualFormatado,
                                                        color =
                                                        Crisma_Primary,
                                                        fontSize = 11.sp,
                                                        fontWeight =
                                                        FontWeight.Bold
                                                    )

                                                    Text(
                                                        text =
                                                        "$textoFaltas de ${frequenciaBaixa.totalEncontros}",
                                                        color = Color.Gray,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(
                                        modifier = Modifier.height(5.dp)
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    if (showDocumentacaoPendenteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDocumentacaoPendenteDialog = false
            },
            containerColor = Color(0xFFFAFAFA),
            tonalElevation = 0.dp,
            confirmButton = {
                TextButton(
                    onClick = {
                        showDocumentacaoPendenteDialog = false
                    }
                ) {
                    Text(
                        text = "Fechar",
                        color = Crisma_Primary
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = Crisma_Primary
                )
            },
            title = {
                Text(
                    text = "Documentação pendente",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                when {
                    carregandoAlunos || carregandoTurmas || carregandoDocumentos -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Crisma_Primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    alunosComDocumentacaoPendente.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment =
                            Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector =
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(34.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Todos os crismandos ativos estão com a documentação completa.",
                                color = Color(0xFF444444),
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 455.dp)
                                .verticalScroll(
                                    rememberScrollState()
                                )
                        ) {
                            Text(
                                text = "${alunosComDocumentacaoPendente.size} crismando(s) com pendência",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            documentacaoPendenteAgrupada.forEach {
                                    grupoCategoria ->

                                CabecalhoCategoriaPainel(
                                    titulo = grupoCategoria.titulo,
                                    quantidade = grupoCategoria.turmas
                                        .sumOf { it.itens.size }
                                )

                                grupoCategoria.turmas.forEach {
                                        grupoTurma ->

                                    CabecalhoTurmaPainel(
                                        nome = grupoTurma.turmaNome,
                                        quantidade = grupoTurma.itens.size
                                    )

                                    grupoTurma.itens.forEach { aluno ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            colors =
                                            CardDefaults.cardColors(
                                                containerColor =
                                                Color.White
                                            ),
                                            elevation =
                                            CardDefaults.cardElevation(
                                                defaultElevation = 1.dp
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = Color(0xFFEEEEEE)
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        horizontal = 10.dp,
                                                        vertical = 9.dp
                                                    ),
                                                verticalAlignment =
                                                Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(
                                                            color =
                                                            Crisma_Primary
                                                                .copy(
                                                                    alpha =
                                                                    0.08f
                                                                ),
                                                            shape =
                                                            RoundedCornerShape(
                                                                9.dp
                                                            )
                                                        ),
                                                    contentAlignment =
                                                    Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector =
                                                        Icons.Outlined
                                                            .Description,
                                                        contentDescription =
                                                        null,
                                                        tint = Crisma_Primary,
                                                        modifier =
                                                        Modifier.size(18.dp)
                                                    )
                                                }

                                                Spacer(
                                                    modifier =
                                                    Modifier.width(9.dp)
                                                )

                                                Column(
                                                    modifier =
                                                    Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = aluno.nome,
                                                        color = Color.Black,
                                                        fontSize = 13.sp,
                                                        fontWeight =
                                                        FontWeight.Bold
                                                    )

                                                    Text(
                                                        text =
                                                        "Matrícula: ${aluno.id}",
                                                        color = Color.Gray,
                                                        fontSize = 10.sp
                                                    )
                                                }

                                                Text(
                                                    text = "Pendente",
                                                    color = Crisma_Primary,
                                                    fontSize = 10.sp,
                                                    fontWeight =
                                                    FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(
                                        modifier = Modifier.height(5.dp)
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    if (showParcelasPendentesDialog) {
        AlertDialog(
            onDismissRequest = {
                showParcelasPendentesDialog = false
            },
            containerColor = Color(0xFFFAFAFA),
            tonalElevation = 0.dp,
            confirmButton = {
                TextButton(
                    onClick = {
                        showParcelasPendentesDialog = false
                    }
                ) {
                    Text(
                        text = "Fechar",
                        color = Crisma_Primary
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = Crisma_Primary
                )
            },
            title = {
                Text(
                    text = "Parcelas pendentes",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                when {
                    carregandoAlunos || carregandoTurmas || carregandoPagamentos -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Crisma_Primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    alunosComParcelasPendentes.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment =
                            Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector =
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(34.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Todos os crismandos ativos estão com as parcelas pagas.",
                                color = Color(0xFF444444),
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 455.dp)
                                .verticalScroll(
                                    rememberScrollState()
                                )
                        ) {
                            Text(
                                text = "${alunosComParcelasPendentes.size} crismando(s) com parcela(s) pendente(s)",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            parcelasPendentesAgrupadas.forEach {
                                    grupoCategoria ->

                                CabecalhoCategoriaPainel(
                                    titulo = grupoCategoria.titulo,
                                    quantidade = grupoCategoria.turmas
                                        .sumOf { it.itens.size }
                                )

                                grupoCategoria.turmas.forEach {
                                        grupoTurma ->

                                    CabecalhoTurmaPainel(
                                        nome = grupoTurma.turmaNome,
                                        quantidade = grupoTurma.itens.size
                                    )

                                    grupoTurma.itens.forEach {
                                            pendencia ->

                                        val aluno = pendencia.aluno
                                        val quantidade =
                                            pendencia.quantidadeRestante

                                        val textoRestante =
                                            if (quantidade == 1) {
                                                "resta 1"
                                            } else {
                                                "restam $quantidade"
                                            }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            colors =
                                            CardDefaults.cardColors(
                                                containerColor =
                                                Color.White
                                            ),
                                            elevation =
                                            CardDefaults.cardElevation(
                                                defaultElevation = 1.dp
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = Color(0xFFEEEEEE)
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        horizontal = 10.dp,
                                                        vertical = 9.dp
                                                    ),
                                                verticalAlignment =
                                                Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(
                                                            color =
                                                            Crisma_Primary
                                                                .copy(
                                                                    alpha =
                                                                    0.08f
                                                                ),
                                                            shape =
                                                            RoundedCornerShape(
                                                                9.dp
                                                            )
                                                        ),
                                                    contentAlignment =
                                                    Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector =
                                                        Icons.Outlined
                                                            .Payments,
                                                        contentDescription =
                                                        null,
                                                        tint = Crisma_Primary,
                                                        modifier =
                                                        Modifier.size(18.dp)
                                                    )
                                                }

                                                Spacer(
                                                    modifier =
                                                    Modifier.width(9.dp)
                                                )

                                                Column(
                                                    modifier =
                                                    Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = aluno.nome,
                                                        color = Color.Black,
                                                        fontSize = 13.sp,
                                                        fontWeight =
                                                        FontWeight.Bold
                                                    )

                                                    Text(
                                                        text =
                                                        "Matrícula: ${aluno.id}",
                                                        color = Color.Gray,
                                                        fontSize = 10.sp
                                                    )
                                                }

                                                Text(
                                                    text = textoRestante,
                                                    color = Crisma_Primary,
                                                    fontSize = 10.sp,
                                                    fontWeight =
                                                    FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(
                                        modifier = Modifier.height(5.dp)
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    if (showSobreNosDialog) {
        ConteudoInstitucionalDialog(
            configuracao = sobreInicial,
            botaoTexto = "Entendido",
            onDismiss = {
                showSobreNosDialog = false
            }
        )
    }


    if (showContatosDialog) {
        ConteudoInstitucionalDialog(
            configuracao = contatosInicial,
            botaoTexto = "Fechar",
            onDismiss = {
                showContatosDialog = false
            }
        )
    }

}

@Composable
private fun CabecalhoCategoriaPainel(
    titulo: String,
    quantidade: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 6.dp)
            .background(
                color = Color(0xFFEEEEEE),
                shape = RoundedCornerShape(9.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(22.dp)
                .background(
                    color = Crisma_Primary,
                    shape = RoundedCornerShape(4.dp)
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = titulo,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = quantidade.toString(),
            color = Crisma_Primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CabecalhoTurmaPainel(
    nome: String,
    quantidade: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 4.dp,
                end = 4.dp,
                top = 5.dp,
                bottom = 2.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Groups,
            contentDescription = null,
            tint = Crisma_Primary,
            modifier = Modifier.size(15.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = nome,
            color = Color(0xFF3F3F3F),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (quantidade == 1) {
                "1 nome"
            } else {
                "$quantidade nomes"
            },
            color = Color.Gray,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun MetricaResumoTurma(
    valor: Int,
    titulo: String,
    icone: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = Color(0xFFF7F7F7),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(
                horizontal = 8.dp,
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = Crisma_Primary,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = valor.toString(),
                color = Crisma_Primary,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = titulo,
                color = Color(0xFF555555),
                fontSize = 9.sp,
                lineHeight = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PainelSairCard(
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(118.dp)
            .height(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFEDEDED)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Sair",
                tint = Crisma_Primary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Sair",
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PainelIndicadorCard(
    valor: Int,
    titulo: String,
    detalhe: String,
    icone: ImageVector,
    @Suppress("UNUSED_PARAMETER")
    corDestaque: Color,
    @Suppress("UNUSED_PARAMETER")
    corFundo: Color,
    carregando: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val modifierCard = modifier
        .height(59.dp)
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        )

    Card(
        modifier = modifierCard,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFEDEDED)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = Crisma_Primary,
                modifier = Modifier.size(19.dp)
            )

            Spacer(modifier = Modifier.width(7.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (carregando) "—" else valor.toString(),
                    color = Crisma_Primary,
                    fontSize = 19.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = titulo,
                    color = Color.Black,
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = detalhe,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}