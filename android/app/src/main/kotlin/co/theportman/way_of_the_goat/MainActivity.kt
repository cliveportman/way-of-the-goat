package co.theportman.way_of_the_goat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.theportman.way_of_the_goat.ui.theme.WayOfTheGoatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WayOfTheGoatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Placeholder launch surface. The Scores screen + navigation
                    // graph are ported in Phase 5 of the native Android rewrite.
                    Placeholder()
                }
            }
        }
    }
}

@Composable
private fun Placeholder(modifier: Modifier = Modifier) {
    Text(
        text = "Way of the Goat",
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize()
            .padding(16.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderPreview() {
    WayOfTheGoatTheme {
        Placeholder()
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PlaceholderPreviewDark() {
    WayOfTheGoatTheme(darkTheme = true) {
        Placeholder()
    }
}
