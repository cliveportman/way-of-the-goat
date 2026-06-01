package co.theportman.way_of_the_goat

import android.content.Context

/**
 * Persists the first-run onboarding flag.
 *
 * The KMP build always showed [co.theportman.way_of_the_goat.screens.IntroFlowScreen]
 * after the splash (it had a TODO for this). The native rewrite gates onboarding to
 * the first launch only, backed by a tiny [android.content.SharedPreferences] file so
 * no Room schema change is required.
 */
class OnboardingPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** True until [markComplete] has been called at least once. */
    val isFirstLaunch: Boolean
        get() = !prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    /** Record that the user has finished the intro flow. */
    fun markComplete() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    private companion object {
        const val PREFS_NAME = "way_of_the_goat_prefs"
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}
