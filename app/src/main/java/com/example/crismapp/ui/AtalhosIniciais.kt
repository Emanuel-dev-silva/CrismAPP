package com.example.crismapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class AtalhoInicialConfiguracao(
    val id: String,
    val titulo: String,
    val descricao: String,
    val url: String,
    val iconeCodigo: String
)

data class OpcaoIconeAtalho(
    val codigo: String,
    val nome: String,
    val icone: ImageVector
)

object AtalhosIniciaisPadrao {
    const val ID_BIBLIA = "BIBLIA"
    const val ID_CATECISMO = "CATECISMO"
    const val ID_AJUDA = "AJUDA"
    const val ID_SOBRE = "SOBRE"
    const val ID_CONTATOS = "CONTATOS"

    fun biblia(): AtalhoInicialConfiguracao {
        return AtalhoInicialConfiguracao(
            id = ID_BIBLIA,
            titulo = "Bíblia",
            descricao = "Ave-Maria",
            url = "https://claretianos.com.br/biblia-ave-maria-online/",
            iconeCodigo = "MENU_BOOK"
        )
    }

    fun catecismo(): AtalhoInicialConfiguracao {
        return AtalhoInicialConfiguracao(
            id = ID_CATECISMO,
            titulo = "Catecismo",
            descricao = "Igreja Católica",
            url = "https://www.vatican.va/archive/ccc/index_po.htm",
            iconeCodigo = "SCHOOL"
        )
    }

    /*
     * Para a configuração AJUDA, o campo "url" é reutilizado
     * internamente como a mensagem exibida no popup.
     */
    fun ajuda(): AtalhoInicialConfiguracao {
        return AtalhoInicialConfiguracao(
            id = ID_AJUDA,
            titulo = "Em caso de dúvidas",
            descricao = "Contate a coordenação da Crisma",
            url = "Em caso de dúvidas sobre a Crisma, entre em contato com a coordenação para receber orientação.",
            iconeCodigo = "HELP"
        )
    }

    fun sobre(): AtalhoInicialConfiguracao {
        return AtalhoInicialConfiguracao(
            id = ID_SOBRE,
            titulo = "Sobre o App",
            descricao = "Conheça o CrismAPP",
            url = "O CrismAPP foi idealizado para modernizar e fortalecer a comunicação na jornada espiritual da nossa Paróquia.\n\nDesenvolvimento:\nEmanuel Barbosa\n(github.com/Emanuel-dev-silva)\n\nGestão de Requisitos:\nVictor Lima",
            iconeCodigo = "INFO"
        )
    }

    fun contatos(): AtalhoInicialConfiguracao {
        return AtalhoInicialConfiguracao(
            id = ID_CONTATOS,
            titulo = "Contatos",
            descricao = "Paróquia e secretaria",
            url = "Paróquia Santo Antônio\nTiúma, São Lourenço da Mata - PE\n\nSecretaria e WhatsApp:\n(81) 9 8593-9076\n\nHorário de Atendimento:\nTerça a Sábado: 08h às 12h",
            iconeCodigo = "PHONE"
        )
    }

    fun lista(): List<AtalhoInicialConfiguracao> {
        return listOf(
            biblia(),
            catecismo(),
            ajuda(),
            sobre(),
            contatos()
        )
    }

    fun porId(id: String): AtalhoInicialConfiguracao? {
        return when (id.trim().uppercase()) {
            ID_BIBLIA -> biblia()
            ID_CATECISMO -> catecismo()
            ID_AJUDA -> ajuda()
            ID_SOBRE -> sobre()
            ID_CONTATOS -> contatos()
            else -> null
        }
    }
}

object IconesAtalhoInicial {
    val opcoes: List<OpcaoIconeAtalho> = listOf(
        OpcaoIconeAtalho(
            codigo = "MENU_BOOK",
            nome = "Livro",
            icone = Icons.Outlined.MenuBook
        ),
        OpcaoIconeAtalho(
            codigo = "SCHOOL",
            nome = "Estudo",
            icone = Icons.Outlined.School
        ),
        OpcaoIconeAtalho(
            codigo = "DESCRIPTION",
            nome = "Documento",
            icone = Icons.Outlined.Description
        ),
        OpcaoIconeAtalho(
            codigo = "PUBLIC",
            nome = "Mundo",
            icone = Icons.Outlined.Public
        ),
        OpcaoIconeAtalho(
            codigo = "FAVORITE",
            nome = "Coração",
            icone = Icons.Outlined.Favorite
        ),
        OpcaoIconeAtalho(
            codigo = "LIGHTBULB",
            nome = "Luz",
            icone = Icons.Outlined.Lightbulb
        )
,
        OpcaoIconeAtalho(
            codigo = "HELP",
            nome = "Ajuda",
            icone = Icons.Outlined.HelpOutline
        ),
        OpcaoIconeAtalho(
            codigo = "PHONE",
            nome = "Telefone",
            icone = Icons.Outlined.Phone
        ),
        OpcaoIconeAtalho(
            codigo = "INFO",
            nome = "Informação",
            icone = Icons.Outlined.Info
        )
    )

    val codigosPermitidos: Set<String> =
        opcoes.map { it.codigo }.toSet()
}

fun iconeAtalhoInicial(codigo: String): ImageVector {
    return IconesAtalhoInicial.opcoes
        .firstOrNull {
            it.codigo.equals(
                codigo,
                ignoreCase = true
            )
        }
        ?.icone
        ?: Icons.Outlined.Link
}

@Composable
fun ConteudoInstitucionalDialog(
    configuracao: AtalhoInicialConfiguracao,
    botaoTexto: String = "Entendido",
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFAFAFA),
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 0.dp,
            shadowElevation = 7.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                color = Color(0xFFFF0000).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconeAtalhoInicial(configuracao.iconeCodigo),
                            contentDescription = null,
                            tint = Color(0xFFFF0000),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(11.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = configuracao.titulo,
                            color = Color.Black,
                            fontSize = 17.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = configuracao.descricao,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFECECEC), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFECECEC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = configuracao.url,
                        modifier = Modifier.padding(14.dp),
                        color = Color(0xFF3F3F3F),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF0000)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = botaoTexto,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EditorAtalhosIniciaisDialog(
    configuracoes: List<AtalhoInicialConfiguracao>,
    carregando: Boolean,
    salvando: Boolean,
    onDismiss: () -> Unit,
    onSalvar: (AtalhoInicialConfiguracao) -> Unit
) {
    var idSelecionado by remember {
        mutableStateOf(AtalhosIniciaisPadrao.ID_BIBLIA)
    }

    val configuracaoAtual = configuracoes
        .firstOrNull {
            it.id.equals(
                idSelecionado,
                ignoreCase = true
            )
        }
        ?: AtalhosIniciaisPadrao.porId(idSelecionado)
        ?: AtalhosIniciaisPadrao.biblia()

    val editandoConteudo =
        idSelecionado in setOf(
            AtalhosIniciaisPadrao.ID_AJUDA,
            AtalhosIniciaisPadrao.ID_SOBRE,
            AtalhosIniciaisPadrao.ID_CONTATOS
        )

    val limiteTitulo = 18
    val limiteDescricao = if (editandoConteudo) 36 else 24
    val limiteMensagem = 400

    var titulo by remember(
        idSelecionado,
        configuracaoAtual
    ) {
        mutableStateOf(configuracaoAtual.titulo)
    }

    var descricao by remember(
        idSelecionado,
        configuracaoAtual
    ) {
        mutableStateOf(configuracaoAtual.descricao)
    }

    var urlOuMensagem by remember(
        idSelecionado,
        configuracaoAtual
    ) {
        mutableStateOf(configuracaoAtual.url)
    }

    var iconeCodigo by remember(
        idSelecionado,
        configuracaoAtual
    ) {
        mutableStateOf(configuracaoAtual.iconeCodigo)
    }

    val podeSalvar =
        !carregando &&
                !salvando &&
                titulo.trim().isNotBlank() &&
                descricao.trim().isNotBlank() &&
                urlOuMensagem.trim().isNotBlank()

    Dialog(
        onDismissRequest = {
            if (!salvando) {
                onDismiss()
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 690.dp),
            color = Color(0xFFFAFAFA),
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 0.dp,
            shadowElevation = 7.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color(0xFFFF0000)
                                    .copy(alpha = 0.08f),
                                shape = RoundedCornerShape(11.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = Color(0xFFFF0000),
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Editar tela inicial",
                            color = Color.Black,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Links rápidos e mensagem de ajuda",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalDivider(
                    color = Color(0xFFECECEC),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple(
                            AtalhosIniciaisPadrao.ID_BIBLIA,
                            "Bíblia",
                            Icons.Outlined.MenuBook
                        ),
                        Triple(
                            AtalhosIniciaisPadrao.ID_CATECISMO,
                            "Catecismo",
                            Icons.Outlined.School
                        ),
                        Triple(
                            AtalhosIniciaisPadrao.ID_AJUDA,
                            "Ajuda",
                            Icons.Outlined.HelpOutline
                        )
,
                        Triple(
                            AtalhosIniciaisPadrao.ID_SOBRE,
                            "Sobre",
                            Icons.Outlined.Info
                        ),
                        Triple(
                            AtalhosIniciaisPadrao.ID_CONTATOS,
                            "Contatos",
                            Icons.Outlined.Phone
                        )
                    ).forEach { (id, rotulo, icone) ->
                        val selecionado = idSelecionado == id

                        FilterChip(
                            selected = selecionado,
                            onClick = {
                                if (!salvando) {
                                    idSelecionado = id
                                }
                            },
                            label = {
                                Text(
                                    text = rotulo,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = icone,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor =
                                    Color(0xFFFF0000)
                                        .copy(alpha = 0.10f),
                                selectedLabelColor =
                                    Color(0xFFB00000),
                                selectedLeadingIconColor =
                                    Color(0xFFFF0000)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selecionado,
                                borderColor = Color(0xFFE5E5E5),
                                selectedBorderColor =
                                    Color(0xFFFF0000)
                                        .copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (editandoConteudo) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF7F7F7)
                        ),
                        border = BorderStroke(
                            1.dp,
                            Color(0xFFEAEAEA)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        ),
                        shape = RoundedCornerShape(11.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        color = Color(0xFFFF0000)
                                            .copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector =
                                        iconeAtalhoInicial(iconeCodigo),
                                    contentDescription = null,
                                    tint = Color(0xFFFF0000),
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(9.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = titulo.ifBlank {
                                        "Título da ajuda"
                                    },
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = descricao.ifBlank {
                                        "Texto pequeno da primeira tela"
                                    },
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    lineHeight = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it.take(limiteTitulo)
                    },
                    enabled = !carregando && !salvando,
                    label = {
                        Text(
                            if (editandoConteudo) {
                                "Título da ajuda"
                            } else {
                                "Nome no botão"
                            }
                        )
                    },
                    supportingText = {
                        Text("${titulo.length}/$limiteTitulo")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF4F4F4F),
                        unfocusedTextColor = Color(0xFF4F4F4F),
                        focusedBorderColor = Color(0xFFFF0000),
                        focusedLabelColor = Color(0xFF555555),
                        cursorColor = Color(0xFFFF0000)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = descricao,
                    onValueChange = {
                        descricao = it.take(limiteDescricao)
                    },
                    enabled = !carregando && !salvando,
                    label = {
                        Text("Texto pequeno abaixo")
                    },
                    supportingText = {
                        Text("${descricao.length}/$limiteDescricao")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF4F4F4F),
                        unfocusedTextColor = Color(0xFF4F4F4F),
                        focusedBorderColor = Color(0xFFFF0000),
                        focusedLabelColor = Color(0xFF555555),
                        cursorColor = Color(0xFFFF0000)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (editandoConteudo) {
                    OutlinedTextField(
                        value = urlOuMensagem,
                        onValueChange = {
                            urlOuMensagem = it.take(limiteMensagem)
                        },
                        enabled = !carregando && !salvando,
                        label = {
                            Text("Mensagem do popup")
                        },
                        supportingText = {
                            Text(
                                "${urlOuMensagem.length}/$limiteMensagem"
                            )
                        },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF4F4F4F),
                            unfocusedTextColor = Color(0xFF4F4F4F),
                            focusedBorderColor = Color(0xFFFF0000),
                            focusedLabelColor = Color(0xFF555555),
                            cursorColor = Color(0xFFFF0000)
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = urlOuMensagem,
                        onValueChange = {
                            urlOuMensagem = it.trimStart()
                        },
                        enabled = !carregando && !salvando,
                        label = {
                            Text("Link completo")
                        },
                        placeholder = {
                            Text("https://...")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF4F4F4F),
                            unfocusedTextColor = Color(0xFF4F4F4F),
                            focusedBorderColor = Color(0xFFFF0000),
                            focusedLabelColor = Color(0xFF555555),
                            cursorColor = Color(0xFFFF0000)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (editandoConteudo) {
                        "Ícone do conteúdo"
                    } else {
                        "Ícone do botão"
                    },
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(7.dp))

                IconesAtalhoInicial.opcoes
                    .chunked(3)
                    .forEach { linha ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(7.dp)
                        ) {
                            linha.forEach { opcao ->
                                val selecionado =
                                    iconeCodigo == opcao.codigo

                                Card(
                                    onClick = {
                                        if (!salvando) {
                                            iconeCodigo =
                                                opcao.codigo
                                        }
                                    },
                                    enabled =
                                        !carregando && !salvando,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(62.dp),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                                if (selecionado) {
                                                    Color(0xFFFFF3F3)
                                                } else {
                                                    Color.White
                                                }
                                        ),
                                    border = BorderStroke(
                                        width =
                                            if (selecionado) {
                                                1.5.dp
                                            } else {
                                                1.dp
                                            },
                                        color =
                                            if (selecionado) {
                                                Color(0xFFFF0000)
                                                    .copy(alpha = 0.55f)
                                            } else {
                                                Color(0xFFECECEC)
                                            }
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(
                                        modifier =
                                            Modifier.fillMaxSize(),
                                        horizontalAlignment =
                                            Alignment.CenterHorizontally,
                                        verticalArrangement =
                                            Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = opcao.icone,
                                            contentDescription =
                                                opcao.nome,
                                            tint = Color(0xFFFF0000),
                                            modifier =
                                                Modifier.size(20.dp)
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(4.dp)
                                        )

                                        Text(
                                            text = opcao.nome,
                                            color = Color(0xFF444444),
                                            fontSize = 9.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            repeat(3 - linha.size) {
                                Spacer(
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))
                    }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !salvando,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            Color(0xFFE5E5E5)
                        )
                    ) {
                        Text(
                            text = "Cancelar",
                            color = Color(0xFF555555),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onSalvar(
                                AtalhoInicialConfiguracao(
                                    id = idSelecionado,
                                    titulo = titulo.trim(),
                                    descricao = descricao.trim(),
                                    url = urlOuMensagem.trim(),
                                    iconeCodigo = iconeCodigo
                                )
                            )
                        },
                        enabled = podeSalvar,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF0000),
                            disabledContainerColor =
                                Color(0xFFE2E2E2)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (salvando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Salvar",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
