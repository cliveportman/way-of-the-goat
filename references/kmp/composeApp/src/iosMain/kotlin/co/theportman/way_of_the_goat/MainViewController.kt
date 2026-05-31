package co.theportman.way_of_the_goat

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import co.theportman.way_of_the_goat.data.cache.ServingsDataManager
import co.theportman.way_of_the_goat.data.database.DatabaseDriverFactory
import co.theportman.way_of_the_goat.util.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

fun MainViewController() = ComposeUIViewController {
    LaunchedEffect(Unit) {
        val driverFactory = DatabaseDriverFactory()
        withContext(Dispatchers.IO) {
            ServingsDataManager.instance.initialize(driverFactory)
        }.onFailure { error ->
            logError("MainViewController", "ServingsDataManager initialization failed: ${error.message}")
        }
    }

    App()
}
