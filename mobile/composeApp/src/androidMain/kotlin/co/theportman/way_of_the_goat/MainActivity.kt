package co.theportman.way_of_the_goat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import co.theportman.way_of_the_goat.data.cache.ServingsDataManager
import co.theportman.way_of_the_goat.data.database.DatabaseDriverFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize ServingsDataManager with Android context
        val driverFactory = DatabaseDriverFactory(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            ServingsDataManager.instance.initialize(driverFactory)
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
