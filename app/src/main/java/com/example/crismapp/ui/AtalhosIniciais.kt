package com.example.crismapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
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

    fun lista(): List<AtalhoInicialConfiguracao> {
        return listOf(
            biblia(),
            catecismo()
        )
    }

    fun porId(id: String): AtalhoInicialConfiguracao? {
        return when (id.trim().uppercase()) {
            ID_BIBLIA -> biblia()
            ID_CATECISMO -> catecismo()
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

    var url by remember(
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
                url.trim().isNotBlank()

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
                .heightIn(max = 650.dp),
            color = Color(0xFFFAFAFA),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 6.dp
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
                    Icon(
                        imageVector = Icons.Outlined.Link,
                        contentDescription = null,
                        tint = Color(0xFFFF0000),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Editar acessos rápidos",
                            color = Color.Black,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Altere os botões da primeira tela",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        AtalhosIniciaisPadrao.ID_BIBLIA to "Primeiro botão",
                        AtalhosIniciaisPadrao.ID_CATECISMO to "Terceiro botão"
                    ).forEach { (id, rotulo) ->
                        val selecionado =
                            idSelecionado == id

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
                                    fontSize = 11.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (
                                        id ==
                                        AtalhosIniciaisPadrao.ID_BIBLIA
                                    ) {
                                        Icons.Outlined.MenuBook
                                    } else {
                                        Icons.Outlined.School
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = Color(0xFF555555),
                                iconColor = Color(0xFFFF0000),
                                selectedContainerColor =
                                Color(0xFFFFF3F3),
                                selectedLabelColor =
                                Color(0xFFB00000),
                                selectedLeadingIconColor =
                                Color(0xFFFF0000),
                                disabledContainerColor =
                                Color(0xFFF3F3F3),
                                disabledLabelColor =
                                Color(0xFF999999),
                                disabledLeadingIconColor =
                                Color(0xFF999999)
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

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it.take(18)
                    },
                    enabled = !carregando && !salvando,
                    label = {
                        Text("Nome no botão")
                    },
                    supportingText = {
                        Text("${titulo.length}/18")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color(0xFFF7F7F7),
                        focusedBorderColor = Color(0xFFFF0000),
                        unfocusedBorderColor = Color(0xFFD8D8D8),
                        disabledBorderColor = Color(0xFFE5E5E5),
                        focusedLabelColor = Color(0xFF555555),
                        unfocusedLabelColor = Color(0xFF707070),
                        disabledLabelColor = Color(0xFF999999),
                        focusedTextColor = Color(0xFF333333),
                        unfocusedTextColor = Color(0xFF333333),
                        disabledTextColor = Color(0xFF888888),
                        cursorColor = Color(0xFFFF0000)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = descricao,
                    onValueChange = {
                        descricao = it.take(24)
                    },
                    enabled = !carregando && !salvando,
                    label = {
                        Text("Texto pequeno abaixo")
                    },
                    supportingText = {
                        Text("${descricao.length}/24")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color(0xFFF7F7F7),
                        focusedBorderColor = Color(0xFFFF0000),
                        unfocusedBorderColor = Color(0xFFD8D8D8),
                        disabledBorderColor = Color(0xFFE5E5E5),
                        focusedLabelColor = Color(0xFF555555),
                        unfocusedLabelColor = Color(0xFF707070),
                        disabledLabelColor = Color(0xFF999999),
                        focusedTextColor = Color(0xFF333333),
                        unfocusedTextColor = Color(0xFF333333),
                        disabledTextColor = Color(0xFF888888),
                        cursorColor = Color(0xFFFF0000)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it.trimStart()
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
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color(0xFFF7F7F7),
                        focusedBorderColor = Color(0xFFFF0000),
                        unfocusedBorderColor = Color(0xFFD8D8D8),
                        disabledBorderColor = Color(0xFFE5E5E5),
                        focusedLabelColor = Color(0xFF555555),
                        unfocusedLabelColor = Color(0xFF707070),
                        disabledLabelColor = Color(0xFF999999),
                        focusedTextColor = Color(0xFF333333),
                        unfocusedTextColor = Color(0xFF333333),
                        disabledTextColor = Color(0xFF888888),
                        cursorColor = Color(0xFFFF0000)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Ícone do botão",
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
                                            imageVector =
                                            opcao.icone,
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
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF555555),
                            disabledContentColor = Color(0xFFAAAAAA)
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
                                    descricao =
                                    descricao.trim(),
                                    url = url.trim(),
                                    iconeCodigo =
                                    iconeCodigo
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