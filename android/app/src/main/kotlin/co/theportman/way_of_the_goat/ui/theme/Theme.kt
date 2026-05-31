package co.theportman.way_of_the_goat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Minimal app theme for the native Android scaffold.
 *
 * Phase 5 replaces these default Material 3 colour schemes with the Goat design
 * tokens ported from `references/kmp` (GoatColorScheme, GoatSpacing, GoatTypography,
 * and the `MaterialTheme.goatColors` extension) per the android-conventions skill.
 */
@Composable
fun WayOfTheGoatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}
