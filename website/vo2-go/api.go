package vo2

import "encoding/json"

// AnalyzeGPX is the public entry point: GPX text in, JSON string out. Matches
// the Rust analyze_gpx signature so the same frontend can drive either WASM.
func AnalyzeGPX(gpxContent string, weightKg float64, maxHR uint32) string {
	points, err := parseGPX(gpxContent)
	if err != nil {
		errStr := "Failed to parse GPX: " + err.Error()
		out, _ := json.Marshal(map[string]any{
			"error":        errStr,
			"estimates":    []any{},
			"chart_points": []any{},
		})
		return string(out)
	}
	result := analyze(points, weightKg, maxHR)
	buf, err := json.Marshal(result)
	if err != nil {
		errStr := "Serialization failed"
		empty := AnalysisResult{
			Estimates:     []Vo2Estimate{},
			ChartPoints:   []ChartPoint{},
			CardiacDrift:  []DriftPoint{},
			DescentPoints: []DescentPoint{},
			Error:         &errStr,
		}
		out, _ := json.Marshal(empty)
		return string(out)
	}
	return string(buf)
}
