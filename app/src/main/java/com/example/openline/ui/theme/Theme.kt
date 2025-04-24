package com.example.openline.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.example.app.ui.theme.AgreeGreen
import com.example.app.ui.theme.CardBackground
import com.example.app.ui.theme.ColorOnPrimary
import com.example.app.ui.theme.ColorPrimary

import com.example.app.ui.theme.TextPrimary

// 1) Your color palette
private val LightColors = lightColorScheme(
    primary            = ColorPrimary,
    onPrimary          = ColorOnPrimary,
    secondary          = AgreeGreen,
    onSecondary        = ColorOnPrimary,
    background         = CardBackground,
    onBackground       = TextPrimary,
    surface            = CardBackground,
    onSurface          = TextPrimary,
)

private val DarkColors = darkColorScheme(
    primary            = ColorPrimary,
    onPrimary          = ColorOnPrimary,
    primaryContainer   = ColorPrimary,
    secondary          = AgreeGreen,
    onSecondary        = ColorOnPrimary,
    background         = TextPrimary,
    onBackground       = CardBackground,
    surface            = TextPrimary,
    onSurface          = CardBackground,
)

// 2) Default typography
private val AppTypography = Typography()

// 3) Default shapes
private val AppShapes = Shapes()

@Composable
fun OpenLineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = LightColors

    MaterialTheme(
        colorScheme = colors,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}
