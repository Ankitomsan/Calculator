package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = Color.Black,
    primaryContainer = OperatorButtonDark,
    onPrimaryContainer = OperatorButtonTextDark,
    secondary = FunctionButtonTextDark,
    onSecondary = Color.Black,
    secondaryContainer = FunctionButtonDark,
    onSecondaryContainer = FunctionButtonTextDark,
    tertiary = ScientificButtonTextDark,
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF2C2F42)
)

private val LightColorScheme = lightColorScheme(
    primary = AccentOrangeDark,
    onPrimary = Color.White,
    primaryContainer = OperatorButtonLight,
    onPrimaryContainer = OperatorButtonTextLight,
    secondary = FunctionButtonTextLight,
    onSecondary = Color.White,
    secondaryContainer = FunctionButtonLight,
    onSecondaryContainer = FunctionButtonTextLight,
    tertiary = Color(0xFF2563EB),
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
