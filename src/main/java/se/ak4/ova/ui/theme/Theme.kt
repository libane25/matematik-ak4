package se.ak4.ova.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF2E7D32)
private val GreenDark = Color(0xFF1B5E20)
private val Blue = Color(0xFF1565C0)
private val Correct = Color(0xFF2E7D4F)
private val Wrong = Color(0xFFC62828)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = GreenDark,
    secondary = Blue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E8FA),
    tertiary = Correct,
    background = Color(0xFFF4F7F4),
    surface = Color.White,
    surfaceVariant = Color(0xFFE7F0E8),
    error = Wrong
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF0D3312),
    primaryContainer = GreenDark,
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF0D2137),
    tertiary = Color(0xFF81C784),
    background = Color(0xFF102016),
    surface = Color(0xFF173024),
    surfaceVariant = Color(0xFF234233),
    error = Color(0xFFFF8A80)
)

@Composable
fun Ak4Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
