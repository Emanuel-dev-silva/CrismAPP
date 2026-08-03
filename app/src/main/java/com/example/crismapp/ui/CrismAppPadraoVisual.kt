package com.example.crismapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp

/*
 * Padrão tipográfico global do CrismAPP.
 *
 * FontFamily.SansSerif mantém uma fonte sem serifa consistente
 * em todas as telas, evitando variações causadas por fontes
 * decorativas configuradas no aparelho.
 */
val CrismAppFontFamily: FontFamily = FontFamily.SansSerif

/*
 * Todos os tamanhos, alturas de linha e espaçamentos foram
 * definidos manualmente.
 *
 * Textos que já possuem fontSize diretamente nas telas mantêm
 * aquele tamanho, mas continuam protegidos pelo fontScale = 1f.
 */
private val CrismAppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CrismAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.sp
    )
)

@Composable
fun CrismAppPadraoVisual(
    content: @Composable () -> Unit
) {
    val densidadeAtual = LocalDensity.current

    /*
     * Impede que o tamanho de fonte configurado no aparelho
     * aumente ou diminua os textos do aplicativo.
     */
    val densidadeComFonteFixa = remember(
        densidadeAtual.density
    ) {
        Density(
            density = densidadeAtual.density,
            fontScale = 1f
        )
    }

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
            typography = CrismAppTypography,
            shapes = MaterialTheme.shapes,
            content = content
        )
    }
}