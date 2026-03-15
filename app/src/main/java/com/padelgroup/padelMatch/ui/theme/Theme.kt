package com.padelgroup.padelMatch.ui.theme

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

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B6E3C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB0F0C8),
    secondary = Color(0xFF3D6B52),
    tertiary = Color(0xFF1A6B5A),
    background = Color(0xFFF6FBF4),
    surface = Color(0xFFF6FBF4)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7DDBA0),
    onPrimary = Color(0xFF003919),
    primaryContainer = Color(0xFF00522C),
    secondary = Color(0xFF9DD4B1),
    tertiary = Color(0xFF80D9C1),
    background = Color(0xFF0E1510),
    surface = Color(0xFF0E1510)
)

@Composable
fun PadelMatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
