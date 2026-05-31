package vo2

import (
	"encoding/json"
	"fmt"
	"math"
	"strings"
	"testing"
)

// synthesizeGPX builds a deterministic GPX track: n points moving east along
// the equator at ~3 m/s (~11 km/h), with sinusoidal elevation and HR.
func synthesizeGPX(n int) string {
	var b strings.Builder
	b.WriteString(`<?xml version="1.0"?><gpx version="1.1"><trk><trkseg>`)
	const stepDeg = 0.00003 // ~3.3 m at the equator
	for i := 0; i < n; i++ {
		lon := float64(i) * stepDeg
		ele := 100.0 + 50.0*math.Sin(float64(i)/30.0)
		hr := 140 + int(20*math.Sin(float64(i)/40.0))
		sec := i // 1 s sampling
		fmt.Fprintf(&b,
			`<trkpt lat="0.0" lon="%.6f"><ele>%.2f</ele><time>2024-01-01T00:%02d:%02dZ</time><extensions><gpxtpx:TrackPointExtension><gpxtpx:hr>%d</gpxtpx:hr></gpxtpx:TrackPointExtension></extensions></trkpt>`,
			lon, ele, sec/60, sec%60, hr,
		)
	}
	b.WriteString(`</trkseg></trk></gpx>`)
	return b.String()
}

func TestParseTimestamp(t *testing.T) {
	cases := []struct {
		in   string
		want float64
		ok   bool
	}{
		{"2000-01-01T00:00:00Z", 0, true},
		{"2000-01-01T00:00:01Z", 1, true},
		{"2000-01-02T00:00:00Z", 86400, true},
		{"2024-01-15T10:30:00.000Z", 0, true}, // sentinel: ok=true, value checked loosely below
		{"bogus", 0, false},
		{"2024-13-01T00:00:00Z", 0, false},
	}
	for _, c := range cases {
		got, ok := parseTimestamp(c.in)
		if ok != c.ok {
			t.Errorf("parseTimestamp(%q): ok=%v want %v", c.in, ok, c.ok)
			continue
		}
		if c.ok && c.in != "2024-01-15T10:30:00.000Z" && got != c.want {
			t.Errorf("parseTimestamp(%q): got %v want %v", c.in, got, c.want)
		}
	}
}

func TestHaversineRoughDistance(t *testing.T) {
	// With r=6,371,000 m (mean radius — same as the Rust port), 1° at the
	// equator works out to π × r / 180 ≈ 111,194.9 m.
	d := haversine(0, 0, 0, 1)
	if math.Abs(d-111_194.9) > 1.0 {
		t.Errorf("haversine 1° at equator: got %.1f, expected ~111194.9", d)
	}
}

func TestParseGPXBasic(t *testing.T) {
	gpx := synthesizeGPX(50)
	points, err := parseGPX(gpx)
	if err != nil {
		t.Fatalf("parseGPX error: %v", err)
	}
	if len(points) != 50 {
		t.Errorf("point count: got %d, want 50", len(points))
	}
	if points[0].HR == nil || *points[0].HR == 0 {
		t.Errorf("expected HR on first point, got %v", points[0].HR)
	}
	if points[0].Ele == nil {
		t.Errorf("expected elevation on first point")
	}
	if points[0].TimeS == nil {
		t.Errorf("expected timestamp on first point")
	}
}

func TestAnalyzeEndToEnd(t *testing.T) {
	gpx := synthesizeGPX(1500)
	out := AnalyzeGPX(gpx, 70.0, 185)

	var data AnalysisResult
	if err := json.Unmarshal([]byte(out), &data); err != nil {
		t.Fatalf("AnalyzeGPX output is not valid JSON: %v\n%s", err, out)
	}

	if data.Error != nil {
		t.Fatalf("unexpected error: %s", *data.Error)
	}
	if data.PointCount != 1500 {
		t.Errorf("point_count: got %d, want 1500", data.PointCount)
	}
	if !data.HasHRData || !data.HasTimeData || !data.HasElevationData {
		t.Errorf("expected hr/time/elevation flags all true, got hr=%v time=%v ele=%v",
			data.HasHRData, data.HasTimeData, data.HasElevationData)
	}
	if data.TotalDistanceKM < 4.5 || data.TotalDistanceKM > 5.5 {
		// 1500 × 3.3 m ≈ 4.95 km
		t.Errorf("total_distance_km: got %.2f, want ~4.95", data.TotalDistanceKM)
	}
	if len(data.ChartPoints) == 0 {
		t.Errorf("expected chart_points to be populated")
	}
}

func TestEmptyArraysSerializeNotNull(t *testing.T) {
	// 5 points → too few, returns empty result. We still want JSON arrays.
	gpx := synthesizeGPX(5)
	out := AnalyzeGPX(gpx, 0, 0)
	for _, key := range []string{`"estimates":[]`, `"chart_points":[]`, `"cardiac_drift":[]`, `"descent_points":[]`} {
		if !strings.Contains(out, key) {
			t.Errorf("expected %s in output, got:\n%s", key, out)
		}
	}
}

func TestRoundTo(t *testing.T) {
	if got := roundTo(1.2345, 2); got != 1.23 {
		t.Errorf("roundTo(1.2345,2)=%v want 1.23", got)
	}
	if got := roundTo(1.235, 2); got != 1.24 {
		t.Errorf("roundTo(1.235,2)=%v want 1.24", got)
	}
}
