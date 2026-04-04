package co.theportman.way_of_the_goat.util

/**
 * Formats a distance in km to a display string.
 *
 * Whole numbers are shown without a decimal (e.g. "8"),
 * otherwise one decimal place is shown (e.g. "8.3").
 */
fun formatDistance(km: Double): String {
    // Round to 1 decimal place
    val rounded = (km * 10).toInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}
