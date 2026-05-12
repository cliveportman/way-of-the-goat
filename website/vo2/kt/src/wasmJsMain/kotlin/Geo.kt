import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_M = 6_371_000.0

private fun Double.toRadians(): Double = this * PI / 180.0

internal fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = (lat2 - lat1).toRadians()
    val dLon = (lon2 - lon1).toRadians()
    val sLat = sin(dLat / 2)
    val sLon = sin(dLon / 2)
    val a = sLat * sLat + cos(lat1.toRadians()) * cos(lat2.toRadians()) * sLon * sLon
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_M * c
}
