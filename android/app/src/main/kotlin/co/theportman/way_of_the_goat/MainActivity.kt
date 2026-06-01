package co.theportman.way_of_the_goat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import co.theportman.way_of_the_goat.data.cache.ServingsDataManager
import co.theportman.way_of_the_goat.ui.theme.WayOfTheGoatTheme
import co.theportman.way_of_the_goat.ui.theme.goatColors
import co.theportman.way_of_the_goat.util.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialise the Room-backed servings cache off the main thread.
        lifecycleScope.launch(Dispatchers.IO) {
            ServingsDataManager.instance.initialize(applicationContext)
                .onFailure { error ->
                    logError("MainActivity", "ServingsDataManager init failed: ${error.message}")
                }
        }

        val onboardingPreferences = OnboardingPreferences(this)

        setContent {
            WayOfTheGoatTheme {
                // Capture the first-launch decision once so recomposition (or completing
                // onboarding) doesn't re-evaluate the navigation start destination.
                val showOnboarding = remember { onboardingPreferences.isFirstLaunch }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.goatColors.surface,
                ) {
                    WayOfTheGoatApp(
                        showOnboarding = showOnboarding,
                        onOnboardingComplete = { onboardingPreferences.markComplete() },
                    )
                }
            }
        }
    }
}
