import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sqrt

internal class Segment(
    val cumDistM: Double,
    val elapsedS: Double,
    val speedMpm: Double,
    val grade: Double,
    val hr: Int?,
    val elevationM: Double?,
)

private fun buildSegments(points: List<TrackPoint>): List<Segment> {
    val baseTime = points.firstNotNullOfOrNull { it.timeS }
    var cum = 0.0
    val segs = ArrayList<Segment>(points.size)
    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val curr = points[i]

        val dist = haversine(prev.lat, prev.lon, curr.lat, curr.lon)
        val eleDiff = if (curr.ele != null && prev.ele != null) curr.ele!! - prev.ele!! else 0.0
        val dt = if (curr.timeS != null && prev.timeS != null && curr.timeS!! > prev.timeS!!) {
            curr.timeS!! - prev.timeS!!
        } else {
            1.0
        }
        cum += dist
        val grade = if (dist > 0.5) clampF(eleDiff / dist, -0.50, 0.50) else 0.0
        val speedMpm = (dist / dt) * 60.0
        val elapsed = if (curr.timeS != null && baseTime != null) curr.timeS!! - baseTime else i.toDouble()

        segs.add(
            Segment(
                cumDistM = cum,
                elapsedS = elapsed,
                speedMpm = speedMpm,
                grade = grade,
                hr = curr.hr,
                elevationM = curr.ele,
            ),
        )
    }
    return segs
}

private data class WindowResult(val speedMpm: Double, val actualMin: Double)

private fun bestWindow(segs: List<Segment>, targetMin: Double): WindowResult? {
    val targetS = targetMin * 60.0
    var bestSpeed = 0.0
    var bestMin = 0.0
    var found = false
    for (startI in segs.indices) {
        val startElapsed = segs[startI].elapsedS
        val startDist = if (startI == 0) 0.0 else segs[startI - 1].cumDistM
        val targetElapsed = startElapsed + targetS

        var endI = -1
        for (k in segs.indices.reversed()) {
            if (k < startI) break
            if (segs[k].elapsedS <= targetElapsed) { endI = k; break }
        }
        if (endI <= startI) continue
        val actualS = segs[endI].elapsedS - startElapsed
        if (actualS < targetS * 0.6) continue
        val dist = segs[endI].cumDistM - startDist
        if (dist < 100.0) continue
        val sp = dist / actualS * 60.0
        if (sp > bestSpeed) { bestSpeed = sp; bestMin = actualS / 60.0; found = true }
    }
    return if (found) WindowResult(bestSpeed, bestMin) else null
}

private fun best1KmVo2(segs: List<Segment>, effectiveMaxHr: Double): PeakKmResult? {
    if (segs.size < 2) return null
    val vo2s = DoubleArray(segs.size) { i ->
        val g = if (segs[i].grade < 0) 0.0 else segs[i].grade
        acsmVo2(segs[i].speedMpm, g)
    }

    var best: PeakKmResult? = null
    var right = 0
    var vo2Sum = 0.0
    var hrSum = 0L
    var hrCount = 0

    for (left in segs.indices) {
        val leftDist = if (left == 0) 0.0 else segs[left - 1].cumDistM

        while (right < segs.size && segs[right].cumDistM - leftDist < 1000.0) {
            vo2Sum += vo2s[right]
            segs[right].hr?.let { hrSum += it; hrCount++ }
            right++
        }

        val rightIdx = min(right, segs.size - 1)
        val actualDist = segs[rightIdx].cumDistM - leftDist
        if (actualDist < 800.0) break
        val windowLen = right - left
        if (windowLen == 0) continue

        val avgVo2 = vo2Sum / windowLen
        if (best == null || avgVo2 > best.vo2Expressed) {
            val leftElapsed = if (left == 0) 0.0 else segs[left - 1].elapsedS
            val elapsed = segs[rightIdx].elapsedS - leftElapsed
            val sp = if (elapsed > 0) actualDist / elapsed * 60.0 else 0.0

            val leftEle = if (left == 0) segs[0].elevationM else segs[left - 1].elevationM
            val netEle = if (segs[rightIdx].elevationM != null && leftEle != null)
                segs[rightIdx].elevationM!! - leftEle
            else 0.0
            val avgGradePct = roundTo(netEle / actualDist * 100.0, 1)

            val avgHr = if (hrCount > 0) round(hrSum.toDouble() / hrCount).toInt() else null
            var vo2MaxEst: Double? = null
            if (effectiveMaxHr > 0 && avgHr != null && avgHr != 0) {
                val est = avgVo2 * effectiveMaxHr / avgHr
                if (est > 15.0 && est < 120.0) vo2MaxEst = roundTo(est, 1)
            }
            val pace = if (sp > 0) roundTo(1000.0 / sp, 2) else 0.0

            best = PeakKmResult(
                vo2Expressed = roundTo(avgVo2, 1),
                vo2MaxEst = vo2MaxEst,
                paceMinPerKm = pace,
                avgGradePct = avgGradePct,
                avgHr = avgHr,
                startDistanceKm = roundTo(leftDist / 1000.0, 2),
            )
        }

        vo2Sum -= vo2s[left]
        segs[left].hr?.let {
            hrSum = if (hrSum >= it) hrSum - it else 0
            if (hrCount > 0) hrCount--
        }
    }
    return best
}

private data class DriftResult(val drift: List<DriftPoint>, val decoupling: Double?)

private fun computeDrift(segs: List<Segment>): DriftResult {
    val skipS = 180.0
    val rawDist = ArrayList<Double>()
    val rawEff = ArrayList<Double>()
    for (s in segs) {
        if (s.elapsedS <= skipS || s.speedMpm <= 50.0) continue
        val hr = s.hr ?: continue
        if (hr <= 50) continue
        val eff = s.speedMpm / hr.toDouble()
        if (eff > 0) { rawDist.add(s.cumDistM); rawEff.add(eff) }
    }
    if (rawDist.size < 20) return DriftResult(emptyList(), null)

    val span = max(1.0, segs.lastOrNull()?.elapsedS ?: 1.0)
    val samplesPerSec = rawDist.size / span
    var win = round(samplesPerSec * 600.0).toInt()
    win = clampI(win, 5, 600)
    if (win > rawDist.size) win = rawDist.size
    val half = win / 2

    val smoothDist = DoubleArray(rawDist.size)
    val smoothEff = DoubleArray(rawDist.size)
    for (i in rawDist.indices) {
        val s = max(0, i - half)
        val e = min(rawDist.size, i + half + 1)
        var sum = 0.0
        for (k in s until e) sum += rawEff[k]
        smoothDist[i] = rawDist[i]
        smoothEff[i] = sum / (e - s)
    }

    val baseline = smoothEff[0]
    if (baseline <= 0) return DriftResult(emptyList(), null)

    val step = max(1, smoothEff.size / 300)
    val drift = ArrayList<DriftPoint>()
    var i = 0
    while (i < smoothEff.size) {
        drift.add(
            DriftPoint(
                distanceKm = roundTo(smoothDist[i] / 1000.0, 2),
                efficiency = roundTo(smoothEff[i] / baseline, 3),
            ),
        )
        i += step
    }

    val mid = smoothEff.size / 2
    var m1 = 0.0
    var m2 = 0.0
    for (j in 0 until mid) m1 += smoothEff[j]
    for (j in mid until smoothEff.size) m2 += smoothEff[j]
    m1 /= mid
    m2 /= smoothEff.size - mid
    val decoupling = if (m1 > 0) roundTo((m1 - m2) / m1 * 100.0, 1) else null
    return DriftResult(drift, decoupling)
}

private fun computeDescentPoints(segs: List<Segment>): List<DescentPoint> {
    val total = max(1.0, segs.lastOrNull()?.cumDistM ?: 1.0)
    val pts = ArrayList<DescentPoint>()
    for (s in segs) {
        if (s.grade >= -0.03 || s.speedMpm <= 30.0) continue
        val speedKmh = s.speedMpm * 60.0 / 1000.0
        if (speedKmh < 0.5 || speedKmh > 35.0) continue
        val gradePct = roundTo(s.grade * 100.0, 1)
        if (gradePct < -50.0) continue
        pts.add(
            DescentPoint(
                gradePct = gradePct,
                speedKmh = roundTo(speedKmh, 2),
                progress = roundTo(s.cumDistM / total, 3),
            ),
        )
    }
    if (pts.size <= 2000) return pts
    val step = pts.size / 2000
    val sampled = ArrayList<DescentPoint>(2001)
    var i = 0
    while (i < pts.size) { sampled.add(pts[i]); i += step }
    return sampled
}

private fun paceFormat(paceKm: Double): String {
    val paceMin = paceKm.toInt()
    val paceSec = ((paceKm - paceMin) * 60.0).toInt()
    val secPart = if (paceSec < 10) "0$paceSec" else paceSec.toString()
    return "$paceMin:$secPart"
}

internal fun analyze(points: List<TrackPoint>, @Suppress("UNUSED_PARAMETER") weightKg: Double, maxHrInput: Int): AnalysisResult {
    if (points.size < 10) {
        return AnalysisResult(
            pointCount = points.size,
            error = "Not enough track points (found ${points.size}, need at least 10). Is this a valid GPX file?",
        )
    }

    val segs = buildSegments(points)
    val totalDistM = segs.lastOrNull()?.cumDistM ?: 0.0
    val totalDistKm = totalDistM / 1000.0

    val hasTime = points.any { it.timeS != null }
    val totalDurationS = if (hasTime) {
        val start = points.firstNotNullOfOrNull { it.timeS }
        val end = points.asReversed().firstNotNullOfOrNull { it.timeS }
        if (start != null && end != null && end > start) end - start else segs.size.toDouble()
    } else {
        segs.size.toDouble()
    }
    val totalDurationMin = totalDurationS / 60.0
    val avgPace = if (totalDistKm > 0) totalDurationMin / totalDistKm else 0.0

    val hasElevation = points.any { it.ele != null }
    var elevationGain = 0.0
    for (i in 1 until segs.size) {
        val a = segs[i - 1].elevationM
        val b = segs[i].elevationM
        if (a != null && b != null && b > a) elevationGain += b - a
    }

    val hrVals = points.mapNotNull { it.hr }
    val hasHr = hrVals.isNotEmpty()
    val avgHrF = if (hasHr) hrVals.sumOf { it.toDouble() } / hrVals.size else null
    val maxHrRecorded = if (hasHr) hrVals.max() else null

    val effectiveMaxHr = when {
        maxHrInput > 0 -> maxHrInput.toDouble()
        maxHrRecorded != null -> maxHrRecorded * 1.05
        else -> 0.0
    }

    val (cardiacDrift, decouplingPct) = if (hasHr && hasTime) {
        val r = computeDrift(segs); Pair(r.drift, r.decoupling)
    } else {
        Pair(emptyList<DriftPoint>(), null)
    }
    val descentPoints = if (hasTime && hasElevation) computeDescentPoints(segs) else emptyList()

    val estimates = ArrayList<Vo2Estimate>(3)

    // Method 1: ACSM + HR (steady-state)
    if (hasHr && effectiveMaxHr > 0) {
        val skipS = 180.0
        val hrLo = effectiveMaxHr * 0.65
        val hrHi = effectiveMaxHr * 0.97
        val samples = ArrayList<Double>()
        for (s in segs) {
            if (s.elapsedS <= skipS || s.speedMpm <= 80.0) continue
            val hr = s.hr ?: continue
            val hf = hr.toDouble()
            if (hf < hrLo || hf > hrHi) continue
            val est = acsmVo2(s.speedMpm, s.grade) * effectiveMaxHr / hf
            if (est > 15.0 && est < 110.0) samples.add(est)
        }
        if (samples.size >= 5) {
            samples.sort()
            val median = samples[samples.size / 2]
            val mean = samples.average()
            val variance = samples.sumOf { (it - mean) * (it - mean) } / samples.size
            val cv = sqrt(variance) / mean
            val countScore = min(samples.size / 100.0, 1.0)
            val cvPenalty = min(cv / 0.25, 1.0) * 15.0
            val confidence = clampI(round(45.0 + countScore * 40.0 - cvPenalty).toInt(), 20, 85)

            estimates.add(
                Vo2Estimate(
                    method = "ACSM + Heart Rate (Steady-State)",
                    value = roundTo(median, 1),
                    confidencePct = confidence,
                    notes = "Uses ACSM running metabolic equation and HR/HRmax linearity. " +
                        "Based on ${samples.size} steady-state data points (HR 65–97% of max, after first 3 min).",
                ),
            )
        }
    }

    // Method 2: Jack Daniels — best effort windows
    if (hasTime && totalDurationMin > 2.0) {
        val effortPct = if (hasHr && effectiveMaxHr > 0 && avgHrF != null) avgHrF / effectiveMaxHr else null
        val targets = doubleArrayOf(20.0, 30.0, 10.0, 60.0, 5.0)
        var added = false
        for (dur in targets) {
            if (totalDurationMin < dur * 0.6) continue
            val w = bestWindow(segs, dur) ?: continue
            val vo2Max = danielsVo2Max(w.speedMpm, w.actualMin) ?: continue
            if (vo2Max <= 15.0 || vo2Max >= 110.0) continue

            val paceKm = if (w.speedMpm > 0) 1000.0 / w.speedMpm else 0.0
            val durBoost = min(dur / 60.0, 1.0)

            val (confidence, effortNote) = when {
                effortPct != null && effortPct < 0.75 -> Pair(
                    clampI(round(20.0 + durBoost * 20.0).toInt(), 15, 35),
                    "Avg HR was only ${formatFixed(effortPct * 100.0, 0)}% of max — this was an easy/Zone 2 run. " +
                        "Daniels assumes race-like effort, so this result will " +
                        "significantly underestimate your actual VO2 max.",
                )
                effortPct != null && effortPct < 0.85 -> Pair(
                    clampI(round(20.0 + durBoost * 40.0).toInt(), 25, 55),
                    "Avg HR was ${formatFixed(effortPct * 100.0, 0)}% of max — a moderate effort. Result will " +
                        "likely underestimate your VO2 max; most accurate when run " +
                        "at race or threshold intensity.",
                )
                effortPct != null -> Pair(
                    clampI(round(20.0 + durBoost * 52.0).toInt(), 35, 72),
                    "Avg HR was ${formatFixed(effortPct * 100.0, 0)}% of max — a hard effort. Result is " +
                        "reasonably accurate; may slightly overestimate if HR was " +
                        "elevated by heat or fatigue rather than pure intensity.",
                )
                else -> Pair(
                    clampI(round(20.0 + durBoost * 35.0).toInt(), 20, 55),
                    "No HR data — cannot assess effort level. Result is only accurate " +
                        "if this was a race or near-maximal effort.",
                )
            }

            estimates.add(
                Vo2Estimate(
                    method = "Jack Daniels — Best ${dur.toInt()}-min Effort",
                    value = roundTo(vo2Max, 1),
                    confidencePct = confidence,
                    notes = "Based on your fastest ${dur.toInt()}-minute segment (avg pace ${paceFormat(paceKm)} /km). $effortNote",
                ),
            )
            added = true
            break
        }

        if (!added && totalDistKm > 0.5) {
            val wholeSpeed = totalDistM / totalDurationS * 60.0
            val vo2Max = danielsVo2Max(wholeSpeed, totalDurationMin)
            if (vo2Max != null && vo2Max > 15.0 && vo2Max < 110.0) {
                estimates.add(
                    Vo2Estimate(
                        method = "Jack Daniels — Whole Activity",
                        value = roundTo(vo2Max, 1),
                        confidencePct = 18,
                        notes = "Based on average pace across the entire activity. Assumes race-like " +
                            "effort throughout — almost always an underestimate for training runs.",
                    ),
                )
            }
        }
    }

    // Method 3: Firstbeat — linear regression
    if (hasHr && effectiveMaxHr > 0) {
        val skipS = 180.0
        val hrLo = effectiveMaxHr * 0.50
        val hrHi = effectiveMaxHr * 0.97
        val pairs = ArrayList<Pair<Double, Double>>()
        for (s in segs) {
            if (s.elapsedS <= skipS || s.speedMpm <= 80.0) continue
            val hr = s.hr ?: continue
            val hf = hr.toDouble()
            if (hf < hrLo || hf > hrHi) continue
            val vo2 = acsmVo2(s.speedMpm, s.grade)
            if (vo2 > 0) pairs.add(hf to vo2)
        }
        if (pairs.size >= 10) {
            val n = pairs.size.toDouble()
            var sumX = 0.0; var sumY = 0.0; var sumXY = 0.0; var sumXX = 0.0
            for ((x, y) in pairs) { sumX += x; sumY += y; sumXY += x * y; sumXX += x * x }
            val denom = n * sumXX - sumX * sumX
            if (abs(denom) > 1e-10) {
                val m = (n * sumXY - sumX * sumY) / denom
                val b = (sumY - m * sumX) / n
                val vo2Max = m * effectiveMaxHr + b
                if (m > 0 && vo2Max > 15.0 && vo2Max < 110.0) {
                    val yMean = sumY / n
                    var ssRes = 0.0; var ssTot = 0.0
                    for ((x, y) in pairs) {
                        val r = y - (m * x + b)
                        ssRes += r * r
                        val d = y - yMean
                        ssTot += d * d
                    }
                    val rSquared = if (ssTot > 1e-10) max(0.0, 1.0 - ssRes / ssTot) else 0.0
                    val confidence = clampI(round(30.0 + rSquared * 53.0).toInt(), 20, 83)
                    estimates.add(
                        Vo2Estimate(
                            method = "Firstbeat (HR–VO2 Regression)",
                            value = roundTo(vo2Max, 1),
                            confidencePct = confidence,
                            notes = "Fits a linear regression across ${pairs.size} steady-state (HR, VO2) pairs " +
                                "and extrapolates to HRmax (R² = ${formatFixed(rSquared, 2)}). More robust than the " +
                                "point-by-point method as it uses all data together.",
                        ),
                    )
                }
            }
        }
    }

    var fitnessCat: String? = null
    var fitnessDesc: String? = null
    if (estimates.isNotEmpty()) {
        val best = estimates.maxByOrNull { it.confidencePct }!!
        val fc = fitnessCategory(best.value)
        fitnessCat = fc.name
        fitnessDesc = fc.description
    }

    val step = max(1, segs.size / 500)
    val chart = ArrayList<ChartPoint>()
    var i = 0
    while (i < segs.size) {
        val s = segs[i]
        chart.add(
            ChartPoint(
                distanceKm = roundTo(s.cumDistM / 1000.0, 2),
                paceMinPerKm = if (s.speedMpm > 10.0) roundTo(1000.0 / s.speedMpm, 2) else null,
                hr = s.hr,
                elevationM = s.elevationM,
            ),
        )
        i += step
    }

    return AnalysisResult(
        totalDistanceKm = roundTo(totalDistKm, 2),
        totalDurationMin = roundTo(totalDurationMin, 1),
        avgPaceMinPerKm = roundTo(avgPace, 2),
        elevationGainM = round(elevationGain),
        avgHr = avgHrF,
        maxHrRecorded = maxHrRecorded,
        hasHrData = hasHr,
        hasElevationData = hasElevation,
        hasTimeData = hasTime,
        pointCount = points.size,
        estimates = estimates,
        fitnessCategory = fitnessCat,
        fitnessDescription = fitnessDesc,
        peak1Km = if (hasTime) best1KmVo2(segs, effectiveMaxHr) else null,
        chartPoints = chart,
        cardiacDrift = cardiacDrift,
        decouplingPct = decouplingPct,
        descentPoints = descentPoints,
    )
}
