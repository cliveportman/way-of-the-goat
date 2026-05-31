// Parses ISO 8601 like "2024-01-15T10:30:00Z" or "2024-01-15T10:30:00.000Z"
// into seconds since 2000-01-01. Returns null on any parse / range error.

private fun isLeap(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

private val MONTH_DAYS = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

internal fun parseTimestamp(input: String): Double? {
    var s = input.trim()
    if (s.endsWith('Z')) s = s.dropLast(1)
    val t = s.indexOf('T')
    if (t < 0) return null
    val dateStr = s.substring(0, t)
    var timeStr = s.substring(t + 1)

    val dp = dateStr.split('-')
    if (dp.size != 3) return null
    val year = dp[0].toIntOrNull() ?: return null
    val month = dp[1].toIntOrNull() ?: return null
    val day = dp[2].toIntOrNull() ?: return null

    val dot = timeStr.indexOf('.')
    if (dot >= 0) timeStr = timeStr.substring(0, dot)
    val tp = timeStr.split(':')
    if (tp.size != 3) return null
    val hour = tp[0].toDoubleOrNull() ?: return null
    val min = tp[1].toDoubleOrNull() ?: return null
    val sec = tp[2].toDoubleOrNull() ?: return null

    if (year !in 2000..2100) return null
    if (month !in 1..12) return null
    val maxDay = when (month) {
        2 -> if (isLeap(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    if (day !in 1..maxDay) return null
    if (hour < 0 || hour > 23 || min < 0 || min > 59 || sec < 0 || sec > 59) return null

    var days = 0
    for (y in 2000 until year) days += if (isLeap(y)) 366 else 365
    for (m in 0 until (month - 1)) {
        days += MONTH_DAYS[m]
        if (m == 1 && isLeap(year)) days += 1
    }
    days += day - 1

    return days * 86400.0 + hour * 3600.0 + min * 60.0 + sec
}
