import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sign

// ACSM Running Metabolic Equation.
// VO2 (mL/kg/min) = 0.2 × S + 0.9 × S × G + 3.5
internal fun acsmVo2(speedMpm: Double, grade: Double): Double =
    0.2 * speedMpm + 0.9 * speedMpm * grade + 3.5

internal fun danielsVo2Max(velocityMpm: Double, timeMin: Double): Double? {
    if (velocityMpm < 60.0 || timeMin < 1.0) return null
    val pct = 0.8 +
        0.1894393 * exp(-0.012778 * timeMin) +
        0.2989558 * exp(-0.1932605 * timeMin)
    val vo2 = -4.60 + 0.182258 * velocityMpm + 0.000104 * velocityMpm * velocityMpm
    if (pct <= 0.0 || vo2 <= 0.0) return null
    return vo2 / pct
}

internal data class FitnessCategory(val name: String, val description: String)

internal fun fitnessCategory(vo2Max: Double): FitnessCategory = when {
    vo2Max < 30.0 -> FitnessCategory("Poor", "Below average cardiovascular fitness")
    vo2Max < 40.0 -> FitnessCategory("Fair", "Average cardiovascular fitness")
    vo2Max < 50.0 -> FitnessCategory("Good", "Above average cardiovascular fitness")
    vo2Max < 60.0 -> FitnessCategory("Excellent", "High cardiovascular fitness")
    vo2Max < 75.0 -> FitnessCategory("Superior", "Very high cardiovascular fitness")
    else -> FitnessCategory("Elite", "Elite-level cardiovascular fitness")
}

// Half-away-from-zero rounding, matching the Rust/Go/.NET/JS/AS ports.
// Kotlin's Math.round is half-to-positive-infinity, so we sign-correct.
internal fun roundTo(v: Double, places: Int): Double {
    val factor = 10.0.pow(places)
    return sign(v) * round(abs(v) * factor) / factor
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun clampF(v: Double, lo: Double, hi: Double): Double =
    if (v < lo) lo else if (v > hi) hi else v

@Suppress("NOTHING_TO_INLINE")
internal inline fun clampI(v: Int, lo: Int, hi: Int): Int =
    if (v < lo) lo else if (v > hi) hi else v

// Equivalent to JS Number.prototype.toFixed.
internal fun formatFixed(v: Double, places: Int): String {
    val factor = 10.0.pow(places)
    val rounded = sign(v) * round(abs(v) * factor) / factor
    if (places <= 0) return rounded.toLong().toString()

    val negative = rounded < 0.0
    val abs = abs(rounded)
    val whole = abs.toLong()
    val fracInt = round((abs - whole) * factor).toLong()
    val frac = fracInt.toString().padStart(places, '0')
    return (if (negative) "-" else "") + whole.toString() + "." + frac
}
