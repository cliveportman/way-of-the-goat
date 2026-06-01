package co.theportman.way_of_the_goat.util

import android.util.Log

/** Log an error. Native Android replacement for the KMP expect/actual logger. */
fun logError(tag: String, message: String) {
    Log.e(tag, message)
}
