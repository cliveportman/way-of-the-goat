package vo2

type TrackPoint struct {
	Lat    float64
	Lon    float64
	Ele    *float64
	TimeS  *float64
	HR     *uint32
}

type ChartPoint struct {
	DistanceKM    float64  `json:"distance_km"`
	PaceMinPerKM  *float64 `json:"pace_min_per_km"`
	HR            *uint32  `json:"hr"`
	ElevationM    *float64 `json:"elevation_m"`
}

type Vo2Estimate struct {
	Method        string  `json:"method"`
	Value         float64 `json:"value"`
	ConfidencePct uint32  `json:"confidence_pct"`
	Notes         string  `json:"notes"`
}

type PeakKmResult struct {
	Vo2Expressed     float64  `json:"vo2_expressed"`
	Vo2MaxEst        *float64 `json:"vo2max_est"`
	PaceMinPerKM     float64  `json:"pace_min_per_km"`
	AvgGradePct      float64  `json:"avg_grade_pct"`
	AvgHR            *uint32  `json:"avg_hr"`
	StartDistanceKM  float64  `json:"start_distance_km"`
}

type DriftPoint struct {
	DistanceKM float64 `json:"distance_km"`
	Efficiency float64 `json:"efficiency"`
}

type DescentPoint struct {
	GradePct float64 `json:"grade_pct"`
	SpeedKMH float64 `json:"speed_kmh"`
	Progress float64 `json:"progress"`
}

type AnalysisResult struct {
	TotalDistanceKM    float64        `json:"total_distance_km"`
	TotalDurationMin   float64        `json:"total_duration_min"`
	AvgPaceMinPerKM    float64        `json:"avg_pace_min_per_km"`
	ElevationGainM     float64        `json:"elevation_gain_m"`
	AvgHR              *float64       `json:"avg_hr"`
	MaxHRRecorded      *uint32        `json:"max_hr_recorded"`
	HasHRData          bool           `json:"has_hr_data"`
	HasElevationData   bool           `json:"has_elevation_data"`
	HasTimeData        bool           `json:"has_time_data"`
	PointCount         int            `json:"point_count"`
	Estimates          []Vo2Estimate  `json:"estimates"`
	FitnessCategory    *string        `json:"fitness_category"`
	FitnessDescription *string        `json:"fitness_description"`
	Peak1km            *PeakKmResult  `json:"peak_1km"`
	ChartPoints        []ChartPoint   `json:"chart_points"`
	CardiacDrift       []DriftPoint   `json:"cardiac_drift"`
	DecouplingPct      *float64       `json:"decoupling_pct"`
	DescentPoints      []DescentPoint `json:"descent_points"`
	Error              *string        `json:"error"`
}
