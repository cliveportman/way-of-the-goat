package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.theportman.way_of_the_goat.isDebugBuild
import co.theportman.way_of_the_goat.ui.theme.goatColors

@Composable
fun HelpScreen(
    onNavigateToDesignTokens: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Help",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.goatColors.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Get help and support here. This is sample text to demonstrate the Help screen layout.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.goatColors.onSurface
        )

        if (isDebugBuild) {
            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(
                color = MaterialTheme.goatColors.outline,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Developer",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.goatColors.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToDesignTokens() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Design tokens",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.goatColors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "\u2192",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.goatColors.onSurfaceVariant
                )
            }
        }
    }
}
