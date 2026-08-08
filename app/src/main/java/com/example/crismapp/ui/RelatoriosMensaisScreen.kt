package com.example.crismapp.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val RelatorioPrimary = Color(0xFFFF0000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatoriosMensaisScreen(
    navController: NavController
) {
    val context = LocalContext.current

    var referencia by remember {
        mutableStateOf(
            RelatorioMensalRepository
                .obterReferenciaMesAnterior()
        )
    }

    var carregando by remember {
        mutableStateOf(false)
    }

    var relatorioJaSalvo by remember {
        mutableStateOf(false)
    }

    var resultadoPendente by remember {
        mutableStateOf<ResultadoRelatorioMensal?>(null)
    }

    val referenciaAtual = remember {
        RelatorioMensalRepository.obterReferenciaMesAtual()
    }

    val administrador =
        FirebaseAuthRepository.catequistaAtual
            ?.possuiPermissaoTotal() == true

    val launcherSalvarTxt = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "text/plain"
        )
    ) { uri ->
        val resultado = resultadoPendente

        if (
            uri == null ||
            resultado == null
        ) {
            resultadoPendente = null
            return@rememberLauncherForActivityResult
        }

        try {
            val bytes = resultado.conteudo
                .toByteArray(Charsets.UTF_8)

            context.contentResolver
                .openOutputStream(uri)
                ?.use { saida ->
                    saida.write(bytes)
                    saida.flush()
                }
                ?: throw IllegalStateException(
                    "Não foi possível abrir o arquivo escolhido."
                )

            RelatorioMensalRepository
                .marcarRelatorioComoSalvo(
                    resultado = resultado,
                    tamanhoBytes = bytes.size,
                    onSuccess = {
                        relatorioJaSalvo = true
                        resultadoPendente = null

                        Toast.makeText(
                            context,
                            "Relatório TXT salvo com sucesso.",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onError = { erro ->
                        resultadoPendente = null

                        Toast.makeText(
                            context,
                            "O TXT foi salvo, mas o Firebase não " +
                                    "registrou a geração: " +
                                    (
                                            erro.message
                                                ?: "erro desconhecido"
                                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
        } catch (erro: Exception) {
            resultadoPendente = null

            Toast.makeText(
                context,
                erro.message
                    ?: "Não foi possível salvar o TXT.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(referencia.chave, administrador) {
        if (administrador) {
            RelatorioMensalRepository
                .verificarRelatorioGerado(
                    referencia = referencia,
                    onSuccess = {
                        relatorioJaSalvo = it
                    },
                    onError = {
                        relatorioJaSalvo = false
                    }
                )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Relatórios mensais",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector =
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults
                    .topAppBarColors(
                        containerColor =
                        RelatorioPrimary
                    )
            )
        }
    ) { paddingValues ->
        if (!administrador) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment =
                    Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = RelatorioPrimary,
                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Acesso restrito",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Somente administradores podem " +
                                "gerar e salvar relatórios mensais.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }

            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                    Color(0xFFFFF8F8)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFFFD7D7)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment =
                        Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector =
                            Icons.Outlined.Description,
                            contentDescription = null,
                            tint = RelatorioPrimary,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )

                        Column {
                            Text(
                                text = "Relatório completo em TXT",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Um arquivo único por mês",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "O arquivo reúne as turmas e todos " +
                                "os crismandos, mostrando a situação " +
                                "acumulada de pagamentos, frequência " +
                                "e documentos, além de todas as " +
                                "alterações registradas no mês.",
                        color = Color(0xFF444444),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Mês do relatório",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFE8E8E8)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 6.dp),
                    verticalAlignment =
                    Alignment.CenterVertically,
                    horizontalArrangement =
                    Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            referencia =
                                RelatorioMensalRepository
                                    .moverReferencia(
                                        referencia,
                                        -1
                                    )
                        }
                    ) {
                        Icon(
                            imageVector =
                            Icons.Outlined.ChevronLeft,
                            contentDescription =
                            "Mês anterior",
                            tint = RelatorioPrimary
                        )
                    }

                    Row(
                        verticalAlignment =
                        Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector =
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = RelatorioPrimary,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text = referencia.titulo,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        enabled =
                        referencia.chave <
                                referenciaAtual.chave,
                        onClick = {
                            referencia =
                                RelatorioMensalRepository
                                    .moverReferencia(
                                        referencia,
                                        1
                                    )
                        }
                    ) {
                        Icon(
                            imageVector =
                            Icons.Outlined.ChevronRight,
                            contentDescription =
                            "Próximo mês",
                            tint = if (
                                referencia.chave <
                                referenciaAtual.chave
                            ) {
                                RelatorioPrimary
                            } else {
                                Color.LightGray
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment =
                Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = if (relatorioJaSalvo) {
                        Color(0xFF2E7D32)
                    } else {
                        Color.Gray
                    },
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = if (relatorioJaSalvo) {
                        "Este mês já possui um TXT registrado. " +
                                "Você pode gerar outra cópia."
                    } else {
                        "Ainda não há TXT registrado para este mês."
                    },
                    color = if (relatorioJaSalvo) {
                        Color(0xFF2E7D32)
                    } else {
                        Color.Gray
                    },
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(
                color = Color(0xFFECECEC)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "O TXT conterá",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• resumo geral da paróquia\n" +
                        "• turmas separadas por categoria\n" +
                        "• lista completa de crismandos\n" +
                        "• parcelas pagas, pendentes, " +
                        "reembolsadas e estornadas\n" +
                        "• presenças, faltas e justificadas\n" +
                        "• situação de cada documento\n" +
                        "• todas as alterações do mês com data",
                color = Color(0xFF444444),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    carregando = true

                    RelatorioMensalRepository
                        .gerarRelatorio(
                            referencia = referencia,
                            onSuccess = { resultado ->
                                carregando = false
                                resultadoPendente = resultado
                                launcherSalvarTxt.launch(
                                    resultado.nomeArquivo
                                )
                            },
                            onError = { erro ->
                                carregando = false

                                Toast.makeText(
                                    context,
                                    erro.message
                                        ?: "Não foi possível gerar o relatório.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                },
                enabled = !carregando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RelatorioPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (carregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Text(
                        text = "Montando o relatório...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector =
                        Icons.Outlined.Download,
                        contentDescription = null,
                        tint = Color.White
                    )

                    Spacer(
                        modifier = Modifier.width(9.dp)
                    )

                    Text(
                        text = "Gerar e salvar TXT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ao tocar no botão, o Android abrirá a " +
                        "janela para você escolher a pasta e " +
                        "confirmar o nome do arquivo.",
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
