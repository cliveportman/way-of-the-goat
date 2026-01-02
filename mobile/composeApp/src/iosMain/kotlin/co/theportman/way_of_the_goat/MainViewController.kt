package co.theportman.way_of_the_goat

import androidx.compose.ui.window.ComposeUIViewController
import co.theportman.way_of_the_goat.data.cache.ServingsDataManager
import co.theportman.way_of_the_goat.data.database.DatabaseDriverFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

fun MainViewController() = ComposeUIViewController {
    // Initialize ServingsDataManager (iOS doesn't need context)
    val driverFactory = DatabaseDriverFactory()
    CoroutineScope(Dispatchers.IO).launch {
        ServingsDataManager.instance.initialize(driverFactory)
    }

    App()
}
