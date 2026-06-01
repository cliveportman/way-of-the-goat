package co.theportman.way_of_the_goat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalGoatColors = staticCompositionLocalOf { GoatDarkColorScheme }

val MaterialTheme.goatColors: GoatColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalGoatColors.current

private val M3DarkColorScheme = darkColorScheme(
    primary = GoatPalette.Lime400,
    onPrimary = GoatPalette.Slate950,
    primaryContainer = GoatPalette.Slate900,
    onPrimaryContainer = GoatPalette.Slate50,
    secondary = GoatPalette.Slate400,
    onSecondary = GoatPalette.Slate950,
    background = GoatPalette.Slate950,
    onBackground = GoatPalette.Slate50,
    surface = GoatPalette.Slate900,
    onSurface = GoatPalette.Slate50,
    surfaceVariant = GoatPalette.Slate800,
    onSurfaceVariant = GoatPalette.Slate400,
    outline = GoatPalette.Slate400,
    error = GoatPalette.Red500,
    onError = GoatPalette.Slate50,
)

private val M3LightColorScheme = lightColorScheme(
    primary = GoatPalette.Lime400,
    onPrimary = GoatPalette.Slate950,
    primaryContainer = GoatPalette.Slate100,
    onPrimaryContainer = GoatPalette.Slate950,
    secondary = GoatPalette.Slate500,
    onSecondary = GoatPalette.Slate50,
    background = GoatPalette.Slate50,
    onBackground = GoatPalette.Slate950,
    surface = GoatPalette.Slate100,
    onSurface = GoatPalette.Slate950,
    surfaceVariant = GoatPalette.Slate200,
    onSurfaceVariant = GoatPalette.Slate500,
    outline = GoatPalette.Slate500,
    error = GoatPalette.Red500,
    onError = GoatPalette.Slate50,
)

@Composable
fun WayOfTheGoatTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val goatColors = if (darkTheme) GoatDarkColorScheme else GoatLightColorScheme
    val m3ColorScheme = if (darkTheme) M3DarkColorScheme else M3LightColorScheme

    CompositionLocalProvider(LocalGoatColors provides goatColors) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = GoatTypography,
            content = content
        )
    }
}
