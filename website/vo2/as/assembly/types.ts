// Output types. Nullable primitives use NaN (f64) or -1 (i32) as sentinels.

export class ChartPoint {
  distance_km: f64 = 0;
  pace_min_per_km: f64 = NaN;
  hr: i32 = -1;
  elevation_m: f64 = NaN;
}

export class Vo2Estimate {
  method: string = '';
  value: f64 = 0;
  confidence_pct: i32 = 0;
  notes: string = '';
}

export class PeakKmResult {
  vo2_expressed: f64 = 0;
  vo2max_est: f64 = NaN;
  pace_min_per_km: f64 = 0;
  avg_grade_pct: f64 = 0;
  avg_hr: i32 = -1;
  start_distance_km: f64 = 0;
}

export class DriftPoint {
  distance_km: f64 = 0;
  efficiency: f64 = 0;
}

export class DescentPoint {
  grade_pct: f64 = 0;
  speed_kmh: f64 = 0;
  progress: f64 = 0;
}

export class AnalysisResult {
  total_distance_km: f64 = 0;
  total_duration_min: f64 = 0;
  avg_pace_min_per_km: f64 = 0;
  elevation_gain_m: f64 = 0;
  avg_hr: f64 = NaN;
  max_hr_recorded: i32 = -1;
  has_hr_data: bool = false;
  has_elevation_data: bool = false;
  has_time_data: bool = false;
  point_count: i32 = 0;
  estimates: Vo2Estimate[] = [];
  fitness_category: string | null = null;
  fitness_description: string | null = null;
  peak_1km: PeakKmResult | null = null;
  chart_points: ChartPoint[] = [];
  cardiac_drift: DriftPoint[] = [];
  decoupling_pct: f64 = NaN;
  descent_points: DescentPoint[] = [];
  error: string | null = null;
}
