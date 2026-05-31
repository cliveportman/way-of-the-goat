package vo2

import "math"

// ACSM Running Metabolic Equation.
// VO2 (mL/kg/min) = 0.2 × S + 0.9 × S × G + 3.5
// S = speed in m/min, G = fractional grade.
func acsmVo2(speedMPM, grade float64) float64 {
	return 0.2*speedMPM + 0.9*speedMPM*grade + 3.5
}

// Jack Daniels / Gilbert performance formula.
// velocityMPM in m/min, timeMin in minutes. Returns VO2max in mL/kg/min.
func danielsVo2Max(velocityMPM, timeMin float64) (float64, bool) {
	if velocityMPM < 60.0 || timeMin < 1.0 {
		return 0, false
	}
	pct := 0.8 +
		0.1894393*math.Exp(-0.012778*timeMin) +
		0.2989558*math.Exp(-0.1932605*timeMin)
	vo2 := -4.60 + 0.182258*velocityMPM + 0.000104*velocityMPM*velocityMPM
	if pct <= 0 || vo2 <= 0 {
		return 0, false
	}
	return vo2 / pct, true
}

func fitnessCategory(vo2max float64) (string, string) {
	switch {
	case vo2max < 30.0:
		return "Poor", "Below average cardiovascular fitness"
	case vo2max < 40.0:
		return "Fair", "Average cardiovascular fitness"
	case vo2max < 50.0:
		return "Good", "Above average cardiovascular fitness"
	case vo2max < 60.0:
		return "Excellent", "High cardiovascular fitness"
	case vo2max < 75.0:
		return "Superior", "Very high cardiovascular fitness"
	default:
		return "Elite", "Elite-level cardiovascular fitness"
	}
}

func roundTo(v float64, places int) float64 {
	factor := math.Pow(10, float64(places))
	return math.Round(v*factor) / factor
}

func clampF(v, lo, hi float64) float64 {
	if v < lo {
		return lo
	}
	if v > hi {
		return hi
	}
	return v
}

func clampI(v, lo, hi int) int {
	if v < lo {
		return lo
	}
	if v > hi {
		return hi
	}
	return v
}
