package vo2

import (
	"fmt"
	"math"
	"sort"
)

type segment struct {
	cumDistM   float64
	elapsedS   float64
	speedMPM   float64
	grade      float64
	hr         *uint32
	elevationM *float64
}

func buildSegments(points []TrackPoint) []segment {
	var baseTime *float64
	for i := range points {
		if points[i].TimeS != nil {
			baseTime = points[i].TimeS
			break
		}
	}

	cumDist := 0.0
	segments := make([]segment, 0, len(points))
	for i := 1; i < len(points); i++ {
		prev := &points[i-1]
		curr := &points[i]

		dist := haversine(prev.Lat, prev.Lon, curr.Lat, curr.Lon)
		var eleDiff float64
		if curr.Ele != nil && prev.Ele != nil {
			eleDiff = *curr.Ele - *prev.Ele
		}

		dt := 1.0
		if curr.TimeS != nil && prev.TimeS != nil && *curr.TimeS > *prev.TimeS {
			dt = *curr.TimeS - *prev.TimeS
		}

		cumDist += dist

		grade := 0.0
		if dist > 0.5 {
			grade = clampF(eleDiff/dist, -0.50, 0.50)
		}

		speedMPM := (dist / dt) * 60.0

		var elapsed float64
		if curr.TimeS != nil && baseTime != nil {
			elapsed = *curr.TimeS - *baseTime
		} else {
			elapsed = float64(i)
		}

		segments = append(segments, segment{
			cumDistM:   cumDist,
			elapsedS:   elapsed,
			speedMPM:   speedMPM,
			grade:      grade,
			hr:         curr.HR,
			elevationM: curr.Ele,
		})
	}
	return segments
}

// bestWindow finds the best average speed (m/min) over any window of ~targetMin minutes.
// Returns (speedMPM, actualMin, ok). Mirrors the Rust rposition-based scan.
func bestWindow(segs []segment, targetMin float64) (float64, float64, bool) {
	targetS := targetMin * 60.0
	bestSpeed := 0.0
	bestMin := 0.0
	found := false

	for startI := 0; startI < len(segs); startI++ {
		startElapsed := segs[startI].elapsedS
		startDist := 0.0
		if startI > 0 {
			startDist = segs[startI-1].cumDistM
		}
		targetElapsed := startElapsed + targetS

		// Last index in [startI..) with elapsed <= targetElapsed
		endI := -1
		for k := len(segs) - 1; k >= startI; k-- {
			if segs[k].elapsedS <= targetElapsed {
				endI = k
				break
			}
		}
		if endI <= startI {
			continue
		}

		actualS := segs[endI].elapsedS - startElapsed
		if actualS < targetS*0.6 {
			continue
		}
		dist := segs[endI].cumDistM - startDist
		if dist < 100.0 {
			continue
		}

		speed := dist / actualS * 60.0
		if speed > bestSpeed {
			bestSpeed = speed
			bestMin = actualS / 60.0
			found = true
		}
	}
	return bestSpeed, bestMin, found
}

// best1kmVo2 finds the 1 km window with the highest avg expressed ACSM VO2.
// O(n) sliding window keyed on cumulative distance.
func best1kmVo2(segs []segment, effectiveMaxHR float64) *PeakKmResult {
	if len(segs) < 2 {
		return nil
	}
	vo2s := make([]float64, len(segs))
	for i, s := range segs {
		g := s.grade
		if g < 0 {
			g = 0
		}
		vo2s[i] = acsmVo2(s.speedMPM, g)
	}

	var best *PeakKmResult
	right := 0
	vo2Sum := 0.0
	hrSum := uint64(0)
	hrCount := 0

	for left := 0; left < len(segs); left++ {
		leftDist := 0.0
		if left > 0 {
			leftDist = segs[left-1].cumDistM
		}

		// Extend right to cover ≥ 1000 m
		for right < len(segs) && segs[right].cumDistM-leftDist < 1000.0 {
			vo2Sum += vo2s[right]
			if segs[right].hr != nil {
				hrSum += uint64(*segs[right].hr)
				hrCount++
			}
			right++
		}

		rightIdx := right
		if rightIdx > len(segs)-1 {
			rightIdx = len(segs) - 1
		}
		actualDist := segs[rightIdx].cumDistM - leftDist
		if actualDist < 800.0 {
			break
		}
		windowLen := right - left
		if windowLen == 0 {
			continue
		}

		avgVo2 := vo2Sum / float64(windowLen)
		if best == nil || avgVo2 > best.Vo2Expressed {
			leftElapsed := 0.0
			if left > 0 {
				leftElapsed = segs[left-1].elapsedS
			}
			elapsed := segs[rightIdx].elapsedS - leftElapsed
			speedMPM := 0.0
			if elapsed > 0 {
				speedMPM = actualDist / elapsed * 60.0
			}

			var leftEle *float64
			if left == 0 {
				leftEle = segs[0].elevationM
			} else {
				leftEle = segs[left-1].elevationM
			}
			netEle := 0.0
			if segs[rightIdx].elevationM != nil && leftEle != nil {
				netEle = *segs[rightIdx].elevationM - *leftEle
			}
			avgGradePct := roundTo(netEle/actualDist*100.0, 1)

			var avgHR *uint32
			if hrCount > 0 {
				v := uint32(math.Round(float64(hrSum) / float64(hrCount)))
				avgHR = &v
			}

			var vo2MaxEst *float64
			if effectiveMaxHR > 0 && avgHR != nil && *avgHR != 0 {
				est := avgVo2 * effectiveMaxHR / float64(*avgHR)
				if est > 15.0 && est < 120.0 {
					r := roundTo(est, 1)
					vo2MaxEst = &r
				}
			}

			pace := 0.0
			if speedMPM > 0 {
				pace = roundTo(1000.0/speedMPM, 2)
			}

			best = &PeakKmResult{
				Vo2Expressed:    roundTo(avgVo2, 1),
				Vo2MaxEst:       vo2MaxEst,
				PaceMinPerKM:    pace,
				AvgGradePct:     avgGradePct,
				AvgHR:           avgHR,
				StartDistanceKM: roundTo(leftDist/1000.0, 2),
			}
		}

		// Slide left forward
		vo2Sum -= vo2s[left]
		if segs[left].hr != nil {
			if hrSum >= uint64(*segs[left].hr) {
				hrSum -= uint64(*segs[left].hr)
			} else {
				hrSum = 0
			}
			if hrCount > 0 {
				hrCount--
			}
		}
	}

	return best
}

// computeDrift returns a smoothed speed/HR efficiency series and the aerobic
// decoupling percentage between the first and second halves.
func computeDrift(segs []segment) ([]DriftPoint, *float64) {
	const skipS = 180.0

	type pair struct{ dist, eff float64 }
	raw := make([]pair, 0, len(segs))
	for _, s := range segs {
		if s.elapsedS <= skipS || s.speedMPM <= 50.0 {
			continue
		}
		if s.hr == nil || *s.hr <= 50 {
			continue
		}
		eff := s.speedMPM / float64(*s.hr)
		if eff > 0 {
			raw = append(raw, pair{dist: s.cumDistM, eff: eff})
		}
	}
	if len(raw) < 20 {
		return []DriftPoint{}, nil
	}

	span := 1.0
	if len(segs) > 0 {
		span = segs[len(segs)-1].elapsedS
		if span < 1 {
			span = 1
		}
	}
	samplesPerSec := float64(len(raw)) / span
	win := int(math.Round(samplesPerSec * 600.0))
	win = clampI(win, 5, 600)
	if win > len(raw) {
		win = len(raw)
	}
	half := win / 2

	smoothed := make([]pair, len(raw))
	for i := 0; i < len(raw); i++ {
		s := i - half
		if s < 0 {
			s = 0
		}
		e := i + half + 1
		if e > len(raw) {
			e = len(raw)
		}
		sum := 0.0
		for k := s; k < e; k++ {
			sum += raw[k].eff
		}
		smoothed[i] = pair{dist: raw[i].dist, eff: sum / float64(e-s)}
	}

	baseline := smoothed[0].eff
	if baseline <= 0 {
		return []DriftPoint{}, nil
	}

	step := len(smoothed) / 300
	if step < 1 {
		step = 1
	}
	drift := make([]DriftPoint, 0, len(smoothed)/step+1)
	for i := 0; i < len(smoothed); i += step {
		drift = append(drift, DriftPoint{
			DistanceKM: roundTo(smoothed[i].dist/1000.0, 2),
			Efficiency: roundTo(smoothed[i].eff/baseline, 3),
		})
	}

	mid := len(smoothed) / 2
	var m1, m2 float64
	for i := 0; i < mid; i++ {
		m1 += smoothed[i].eff
	}
	for i := mid; i < len(smoothed); i++ {
		m2 += smoothed[i].eff
	}
	m1 /= float64(mid)
	m2 /= float64(len(smoothed) - mid)
	var decoupling *float64
	if m1 > 0 {
		v := roundTo((m1-m2)/m1*100.0, 1)
		decoupling = &v
	}

	return drift, decoupling
}

// computeDescentPoints extracts downhill segments for the scatter chart.
func computeDescentPoints(segs []segment) []DescentPoint {
	totalDist := 1.0
	if len(segs) > 0 {
		totalDist = segs[len(segs)-1].cumDistM
		if totalDist < 1 {
			totalDist = 1
		}
	}

	pts := make([]DescentPoint, 0)
	for _, s := range segs {
		if s.grade >= -0.03 || s.speedMPM <= 30.0 {
			continue
		}
		speedKMH := s.speedMPM * 60.0 / 1000.0
		if speedKMH < 0.5 || speedKMH > 35.0 {
			continue
		}
		gradePct := roundTo(s.grade*100.0, 1)
		if gradePct < -50.0 {
			continue
		}
		pts = append(pts, DescentPoint{
			GradePct: gradePct,
			SpeedKMH: roundTo(speedKMH, 2),
			Progress: roundTo(s.cumDistM/totalDist, 3),
		})
	}

	if len(pts) > 2000 {
		step := len(pts) / 2000
		sampled := make([]DescentPoint, 0, 2000+1)
		for i := 0; i < len(pts); i += step {
			sampled = append(sampled, pts[i])
		}
		pts = sampled
	}
	return pts
}

func emptyResult(pointCount int, errMsg string) AnalysisResult {
	var errPtr *string
	if errMsg != "" {
		errPtr = &errMsg
	}
	return AnalysisResult{
		PointCount:    pointCount,
		Estimates:     []Vo2Estimate{},
		ChartPoints:   []ChartPoint{},
		CardiacDrift:  []DriftPoint{},
		DescentPoints: []DescentPoint{},
		Error:         errPtr,
	}
}

// analyze runs the full pipeline. weightKg is accepted for parity with the
// Rust signature (and a future energy-expenditure calc) but is currently unused.
func analyze(points []TrackPoint, _ float64, maxHRInput uint32) AnalysisResult {
	if len(points) < 10 {
		return emptyResult(len(points), fmt.Sprintf(
			"Not enough track points (found %d, need at least 10). Is this a valid GPX file?",
			len(points),
		))
	}

	segs := buildSegments(points)

	// ---- Activity stats ----
	totalDistM := 0.0
	if len(segs) > 0 {
		totalDistM = segs[len(segs)-1].cumDistM
	}
	totalDistKM := totalDistM / 1000.0

	hasTime := false
	for i := range points {
		if points[i].TimeS != nil {
			hasTime = true
			break
		}
	}
	totalDurationS := float64(len(segs))
	if hasTime {
		var startT, endT *float64
		for i := 0; i < len(points); i++ {
			if points[i].TimeS != nil {
				startT = points[i].TimeS
				break
			}
		}
		for i := len(points) - 1; i >= 0; i-- {
			if points[i].TimeS != nil {
				endT = points[i].TimeS
				break
			}
		}
		if startT != nil && endT != nil && *endT > *startT {
			totalDurationS = *endT - *startT
		}
	}
	totalDurationMin := totalDurationS / 60.0
	avgPace := 0.0
	if totalDistKM > 0 {
		avgPace = totalDurationMin / totalDistKM
	}

	hasElevation := false
	for i := range points {
		if points[i].Ele != nil {
			hasElevation = true
			break
		}
	}
	elevationGain := 0.0
	for i := 1; i < len(segs); i++ {
		a := segs[i-1].elevationM
		b := segs[i].elevationM
		if a != nil && b != nil && *b > *a {
			elevationGain += *b - *a
		}
	}

	hrVals := make([]uint32, 0, len(points))
	for i := range points {
		if points[i].HR != nil {
			hrVals = append(hrVals, *points[i].HR)
		}
	}
	hasHR := len(hrVals) > 0
	var avgHR *float64
	var maxHRRecorded *uint32
	if hasHR {
		sum := 0.0
		max := uint32(0)
		for _, h := range hrVals {
			sum += float64(h)
			if h > max {
				max = h
			}
		}
		v := sum / float64(len(hrVals))
		avgHR = &v
		maxHRRecorded = &max
	}

	effectiveMaxHR := 0.0
	switch {
	case maxHRInput > 0:
		effectiveMaxHR = float64(maxHRInput)
	case maxHRRecorded != nil:
		effectiveMaxHR = float64(*maxHRRecorded) * 1.05
	}

	// ---- Movement analysis ----
	var cardiacDrift []DriftPoint
	var decouplingPct *float64
	if hasHR && hasTime {
		cardiacDrift, decouplingPct = computeDrift(segs)
	} else {
		cardiacDrift = []DriftPoint{}
	}

	var descentPoints []DescentPoint
	if hasTime && hasElevation {
		descentPoints = computeDescentPoints(segs)
	} else {
		descentPoints = []DescentPoint{}
	}

	// ---- VO2max estimates ----
	estimates := make([]Vo2Estimate, 0, 3)

	// Method 1: ACSM + HR (steady-state)
	if hasHR && effectiveMaxHR > 0 {
		const skipS = 180.0
		hrLo := effectiveMaxHR * 0.65
		hrHi := effectiveMaxHR * 0.97

		samples := make([]float64, 0, len(segs))
		for _, s := range segs {
			if s.elapsedS <= skipS || s.speedMPM <= 80.0 || s.hr == nil {
				continue
			}
			hf := float64(*s.hr)
			if hf < hrLo || hf > hrHi {
				continue
			}
			vo2 := acsmVo2(s.speedMPM, s.grade)
			est := vo2 * effectiveMaxHR / hf
			if est > 15.0 && est < 110.0 {
				samples = append(samples, est)
			}
		}

		if len(samples) >= 5 {
			sort.Float64s(samples)
			median := samples[len(samples)/2]

			mean := 0.0
			for _, x := range samples {
				mean += x
			}
			mean /= float64(len(samples))
			variance := 0.0
			for _, x := range samples {
				d := x - mean
				variance += d * d
			}
			variance /= float64(len(samples))
			cv := math.Sqrt(variance) / mean
			countScore := math.Min(float64(len(samples))/100.0, 1.0)
			cvPenalty := math.Min(cv/0.25, 1.0) * 15.0
			confidence := clampI(int(math.Round(45.0+countScore*40.0-cvPenalty)), 20, 85)

			estimates = append(estimates, Vo2Estimate{
				Method:        "ACSM + Heart Rate (Steady-State)",
				Value:         roundTo(median, 1),
				ConfidencePct: uint32(confidence),
				Notes: fmt.Sprintf(
					"Uses ACSM running metabolic equation and HR/HRmax linearity. "+
						"Based on %d steady-state data points (HR 65–97%% of max, after first 3 min).",
					len(samples),
				),
			})
		}
	}

	// Method 2: Jack Daniels — best effort windows
	if hasTime && totalDurationMin > 2.0 {
		var effortPct *float64
		if hasHR && effectiveMaxHR > 0 && avgHR != nil {
			v := *avgHR / effectiveMaxHR
			effortPct = &v
		}

		targets := []float64{20.0, 30.0, 10.0, 60.0, 5.0}
		added := false
		for _, dur := range targets {
			if totalDurationMin < dur*0.6 {
				continue
			}
			speed, actualMin, ok := bestWindow(segs, dur)
			if !ok {
				continue
			}
			vo2max, ok := danielsVo2Max(speed, actualMin)
			if !ok || vo2max <= 15.0 || vo2max >= 110.0 {
				continue
			}
			paceKm := 0.0
			if speed > 0 {
				paceKm = 1000.0 / speed
			}
			paceMin := uint32(paceKm)
			paceSec := uint32((paceKm - float64(paceMin)) * 60.0)

			var confidence int
			var effortNote string
			durBoost := math.Min(dur/60.0, 1.0)
			switch {
			case effortPct != nil && *effortPct < 0.75:
				confidence = clampI(int(math.Round(20.0+durBoost*20.0)), 15, 35)
				effortNote = fmt.Sprintf(
					"Avg HR was only %.0f%% of max — this was an easy/Zone 2 run. "+
						"Daniels assumes race-like effort, so this result will "+
						"significantly underestimate your actual VO2 max.",
					*effortPct*100.0,
				)
			case effortPct != nil && *effortPct < 0.85:
				confidence = clampI(int(math.Round(20.0+durBoost*40.0)), 25, 55)
				effortNote = fmt.Sprintf(
					"Avg HR was %.0f%% of max — a moderate effort. Result will "+
						"likely underestimate your VO2 max; most accurate when run "+
						"at race or threshold intensity.",
					*effortPct*100.0,
				)
			case effortPct != nil:
				confidence = clampI(int(math.Round(20.0+durBoost*52.0)), 35, 72)
				effortNote = fmt.Sprintf(
					"Avg HR was %.0f%% of max — a hard effort. Result is "+
						"reasonably accurate; may slightly overestimate if HR was "+
						"elevated by heat or fatigue rather than pure intensity.",
					*effortPct*100.0,
				)
			default:
				confidence = clampI(int(math.Round(20.0+durBoost*35.0)), 20, 55)
				effortNote = "No HR data — cannot assess effort level. Result is only accurate " +
					"if this was a race or near-maximal effort."
			}

			estimates = append(estimates, Vo2Estimate{
				Method:        fmt.Sprintf("Jack Daniels — Best %.0f-min Effort", dur),
				Value:         roundTo(vo2max, 1),
				ConfidencePct: uint32(confidence),
				Notes: fmt.Sprintf(
					"Based on your fastest %.0f-minute segment (avg pace %d:%02d /km). %s",
					dur, paceMin, paceSec, effortNote,
				),
			})
			added = true
			break
		}

		if !added && totalDistKM > 0.5 {
			wholeSpeed := totalDistM / totalDurationS * 60.0
			if vo2max, ok := danielsVo2Max(wholeSpeed, totalDurationMin); ok && vo2max > 15.0 && vo2max < 110.0 {
				estimates = append(estimates, Vo2Estimate{
					Method:        "Jack Daniels — Whole Activity",
					Value:         roundTo(vo2max, 1),
					ConfidencePct: 18,
					Notes: "Based on average pace across the entire activity. Assumes race-like " +
						"effort throughout — almost always an underestimate for training runs.",
				})
			}
		}
	}

	// Method 3: Firstbeat — linear regression of HR vs VO2
	if hasHR && effectiveMaxHR > 0 {
		const skipS = 180.0
		hrLo := effectiveMaxHR * 0.50
		hrHi := effectiveMaxHR * 0.97

		type hrVo2 struct{ hr, vo2 float64 }
		pairs := make([]hrVo2, 0, len(segs))
		for _, s := range segs {
			if s.elapsedS <= skipS || s.speedMPM <= 80.0 || s.hr == nil {
				continue
			}
			hf := float64(*s.hr)
			if hf < hrLo || hf > hrHi {
				continue
			}
			vo2 := acsmVo2(s.speedMPM, s.grade)
			if vo2 > 0 {
				pairs = append(pairs, hrVo2{hr: hf, vo2: vo2})
			}
		}

		if len(pairs) >= 10 {
			n := float64(len(pairs))
			var sumX, sumY, sumXY, sumXX float64
			for _, p := range pairs {
				sumX += p.hr
				sumY += p.vo2
				sumXY += p.hr * p.vo2
				sumXX += p.hr * p.hr
			}
			denom := n*sumXX - sumX*sumX
			if math.Abs(denom) > 1e-10 {
				m := (n*sumXY - sumX*sumY) / denom
				b := (sumY - m*sumX) / n
				vo2max := m*effectiveMaxHR + b
				if m > 0 && vo2max > 15.0 && vo2max < 110.0 {
					yMean := sumY / n
					var ssRes, ssTot float64
					for _, p := range pairs {
						r := p.vo2 - (m*p.hr + b)
						ssRes += r * r
						d := p.vo2 - yMean
						ssTot += d * d
					}
					rSquared := 0.0
					if ssTot > 1e-10 {
						rSquared = 1.0 - ssRes/ssTot
						if rSquared < 0 {
							rSquared = 0
						}
					}
					confidence := clampI(int(math.Round(30.0+rSquared*53.0)), 20, 83)
					estimates = append(estimates, Vo2Estimate{
						Method:        "Firstbeat (HR–VO2 Regression)",
						Value:         roundTo(vo2max, 1),
						ConfidencePct: uint32(confidence),
						Notes: fmt.Sprintf(
							"Fits a linear regression across %d steady-state (HR, VO2) pairs "+
								"and extrapolates to HRmax (R² = %.2f). More robust than the "+
								"point-by-point method as it uses all data together.",
							len(pairs), rSquared,
						),
					})
				}
			}
		}
	}

	// Fitness category from best (highest-confidence) estimate
	var fitnessCat, fitnessDesc *string
	if len(estimates) > 0 {
		bestIdx := 0
		for i := 1; i < len(estimates); i++ {
			if estimates[i].ConfidencePct > estimates[bestIdx].ConfidencePct {
				bestIdx = i
			}
		}
		c, d := fitnessCategory(estimates[bestIdx].Value)
		fitnessCat = &c
		fitnessDesc = &d
	}

	// ---- Chart data (≤500 points) ----
	step := len(segs) / 500
	if step < 1 {
		step = 1
	}
	chart := make([]ChartPoint, 0, len(segs)/step+1)
	for i := 0; i < len(segs); i += step {
		s := segs[i]
		var pace *float64
		if s.speedMPM > 10.0 {
			v := roundTo(1000.0/s.speedMPM, 2)
			pace = &v
		}
		chart = append(chart, ChartPoint{
			DistanceKM:   roundTo(s.cumDistM/1000.0, 2),
			PaceMinPerKM: pace,
			HR:           s.hr,
			ElevationM:   s.elevationM,
		})
	}

	var peak1km *PeakKmResult
	if hasTime {
		peak1km = best1kmVo2(segs, effectiveMaxHR)
	}

	return AnalysisResult{
		TotalDistanceKM:    roundTo(totalDistKM, 2),
		TotalDurationMin:   roundTo(totalDurationMin, 1),
		AvgPaceMinPerKM:    roundTo(avgPace, 2),
		ElevationGainM:     math.Round(elevationGain),
		AvgHR:              avgHR,
		MaxHRRecorded:      maxHRRecorded,
		HasHRData:          hasHR,
		HasElevationData:   hasElevation,
		HasTimeData:        hasTime,
		PointCount:         len(points),
		Estimates:          estimates,
		FitnessCategory:    fitnessCat,
		FitnessDescription: fitnessDesc,
		Peak1km:            peak1km,
		ChartPoints:        chart,
		CardiacDrift:       cardiacDrift,
		DecouplingPct:      decouplingPct,
		DescentPoints:      descentPoints,
		Error:              nil,
	}
}
