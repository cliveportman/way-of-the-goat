package co.cliveportman.wayofthegoat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import co.cliveportman.wayofthegoat.ui.theme.WayOfTheGoatTheme
import co.cliveportman.wayofthegoat.shared.WayOfTheGoatApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WayOfTheGoatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WayOfTheGoatApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WayOfTheGoatAppPreview() {
    WayOfTheGoatTheme {
        WayOfTheGoatApp()
    }
}