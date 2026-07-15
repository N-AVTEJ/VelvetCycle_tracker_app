package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class VelvetColors(
    val background: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val pinkAccent: Color,
    val border: Color,
    val tabBackground: Color
)

val LightVelvetColors = VelvetColors(
    background = Color(0xFFFFF0F5),      // LightPinkBg
    cardBackground = Color(0xFFFFFFFF),  // White
    textPrimary = Color(0xFF1A1A2E),     // DarkText
    textSecondary = Color(0xFF757575),   // GrayText
    pinkAccent = Color(0xFFD4537E),      // PrimaryPink
    border = Color(0xFFE0E0E0),          // BorderColor
    tabBackground = Color(0xFFFFFFFF)    // White
)

val DarkVelvetColors = VelvetColors(
    background = Color(0xFF0D0D0D),
    cardBackground = Color(0xFF1A1A1A),
    textPrimary = Color(0xFFF5F5F5),
    textSecondary = Color(0xFFAAAAAA),
    pinkAccent = Color(0xFFD4537E),
    border = Color(0xFF2A2A2A),
    tabBackground = Color(0xFF111111)
)

val LocalVelvetColors = staticCompositionLocalOf { LightVelvetColors }

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPink,
    secondary = SecondaryPink,
    tertiary = Teal,
    background = Color(0xFF0D0D0D),
    surface = Color(0xFF1A1A1A),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFF5F5F5),
    onBackground = Color(0xFFF5F5F5),
    onSurface = Color(0xFFF5F5F5)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPink,
    secondary = SecondaryPink,
    tertiary = Teal,
    background = LightPinkBg,
    surface = White,
    onPrimary = White,
    onSecondary = DarkText,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val velvetColors = if (darkTheme) DarkVelvetColors else LightVelvetColors

    CompositionLocalProvider(LocalVelvetColors provides velvetColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
