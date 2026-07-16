package com.example.crismapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density

/*
 * Fonte padrão atual do CrismAPP.
 *
 * FontFamily.Default corresponde à fonte padrão usada atualmente
 * nas telas do aplicativo.
 */
val CrismAppFontFamily: FontFamily = FontFamily.Default

/*
 * Aplica a mesma família de fonte em todos os estilos do Material 3.
 *
 * Os tamanhos já definidos diretamente nas telas, como 11.sp,
 * 14.sp, 16.sp e 24.sp, continuam exatamente como estão.
 */
private fun TextStyle.comFonteCrismApp(): TextStyle {
    return copy(fontFamily = CrismAppFontFamily)
}

private fun Typography.comFonteCrismApp(): Typography {
    return copy(
        displayLarge = displayLarge.comFonteCrismApp(),
        displayMedium = displayMedium.comFonteCrismApp(),
        displaySmall = displaySmall.comFonteCrismApp(),
        headlineLarge = headlineLarge.comFonteCrismApp(),
        headlineMedium = headlineMedium.comFonteCrismApp(),
        headlineSmall = headlineSmall.comFonteCrismApp(),
        titleLarge = titleLarge.comFonteCrismApp(),
        titleMedium = titleMedium.comFonteCrismApp(),
        titleSmall = titleSmall.comFonteCrismApp(),
        bodyLarge = bodyLarge.comFonteCrismApp(),
        bodyMedium = bodyMedium.comFonteCrismApp(),
        bodySmall = bodySmall.comFonteCrismApp(),
        labelLarge = labelLarge.comFonteCrismApp(),
        labelMedium = labelMedium.comFonteCrismApp(),
        labelSmall = labelSmall.comFonteCrismApp()
    )
}

/*
 * Envolva todo o NavGraph com este componente.
 *
 * fontScale = 1f impede que a configuração de tamanho de fonte
 * do aparelho aumente ou diminua os textos do aplicativo.
 *
 * A densidade física da tela continua normal; somente a escala
 * tipográfica fica fixa.
 */
@Composable
fun CrismAppPadraoVisual(
    content: @Composable () -> Unit
) {
    val densidadeAtual = LocalDensity.current

    val densidadeComFonteFixa = remember(
        densidadeAtual.density
    ) {
        Density(
            density = densidadeAtual.density,
            fontScale = 1f
        )
    }

    /*
     * Mantém exatamente os tamanhos e pesos do tema que o projeto
     * já utiliza, trocando apenas a família para a fonte padrão
     * escolhida pelo CrismAPP.
     */
    val tipografiaAtual = MaterialTheme.typography

    val tipografiaPadronizada = remember(
        tipografiaAtual
    ) {
        tipografiaAtual.comFonteCrismApp()
    }

    /*
     * Substitui as cores roxas padrão do Material 3 pela paleta
     * oficial usada no CrismAPP.
     *
     * Componentes que não receberam cor manualmente, como chips,
     * campos, botões, indicadores e seletores, passam a usar
     * vermelho, branco e cinza automaticamente.
     */
    val esquemaAtual = MaterialTheme.colorScheme

    val esquemaCrismApp = remember(esquemaAtual) {
        esquemaAtual.copy(
            primary = Color(0xFFFF0000),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFEEEE),
            onPrimaryContainer = Color(0xFF3A0000),

            secondary = Color(0xFFB00000),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFF3F3),
            onSecondaryContainer = Color(0xFF3A0000),

            tertiary = Color(0xFFFFD700),
            onTertiary = Color.Black,
            tertiaryContainer = Color(0xFFFFF8D1),
            onTertiaryContainer = Color(0xFF3A3200),

            background = Color.White,
            onBackground = Color(0xFF1F1F1F),
            surface = Color.White,
            onSurface = Color(0xFF1F1F1F),

            /*
             * Impede o Material 3 de aplicar a tonalidade da cor
             * primária sobre cartões elevados. Sem isso, cartões
             * brancos podem parecer levemente azulados ou rosados.
             */
            surfaceTint = Color.Transparent,

            surfaceVariant = Color(0xFFF7F7F7),
            onSurfaceVariant = Color(0xFF555555),
            outline = Color(0xFFD8D8D8),
            outlineVariant = Color(0xFFECECEC),
            error = Color(0xFFFF0000)
        )
    }

    CompositionLocalProvider(
        LocalDensity provides densidadeComFonteFixa
    ) {
        MaterialTheme(
            colorScheme = esquemaCrismApp,
            typography = tipografiaPadronizada,
            shapes = MaterialTheme.shapes,
            content = content
        )
    }
}