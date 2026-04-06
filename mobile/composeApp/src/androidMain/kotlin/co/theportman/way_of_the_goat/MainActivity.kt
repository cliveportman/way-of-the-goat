package co.theportman.way_of_the_goat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import co.theportman.way_of_the_goat.data.cache.ServingsDataManager
import co.theportman.way_of_the_goat.data.database.DatabaseDriverFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize ServingsDataManager with Android context
        val driverFactory = DatabaseDriverFactory(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            ServingsDataManager.instance.initialize(driverFactory)
                .onFailure { error ->
                    println("ServingsDataManager initialization failed: ${error.message}")
                }
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
private fun AppAndroidPreview() {
    App()
}
