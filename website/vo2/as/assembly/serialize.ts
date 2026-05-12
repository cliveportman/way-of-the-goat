import {
  AnalysisResult, ChartPoint, Vo2Estimate, PeakKmResult, DriftPoint, DescentPoint,
} from './types';
import { jsonEscape, jsonNumber, jsonNumberOrNull, jsonIntOrNull } from './json';

function chartPointToJson(p: ChartPoint): string {
  return '{'
    + '"distance_km":' + jsonNumber(p.distance_km)
    + ',"pace_min_per_km":' + jsonNumberOrNull(p.pace_min_per_km)
    + ',"hr":' + jsonIntOrNull(p.hr)
    + ',"elevation_m":' + jsonNumberOrNull(p.elevation_m)
    + '}';
}

function estimateToJson(e: Vo2Estimate): string {
  return '{'
    + '"method":' + jsonEscape(e.method)
    + ',"value":' + jsonNumber(e.value)
    + ',"confidence_pct":' + e.confidence_pct.toString()
    + ',"notes":' + jsonEscape(e.notes)
    + '}';
}

function peakKmToJson(p: PeakKmResult): string {
  return '{'
    + '"vo2_expressed":' + jsonNumber(p.vo2_expressed)
    + ',"vo2max_est":' + jsonNumberOrNull(p.vo2max_est)
    + ',"pace_min_per_km":' + jsonNumber(p.pace_min_per_km)
    + ',"avg_grade_pct":' + jsonNumber(p.avg_grade_pct)
    + ',"avg_hr":' + jsonIntOrNull(p.avg_hr)
    + ',"start_distance_km":' + jsonNumber(p.start_distance_km)
    + '}';
}

function driftPointToJson(p: DriftPoint): string {
  return '{"distance_km":' + jsonNumber(p.distance_km)
       + ',"efficiency":' + jsonNumber(p.efficiency) + '}';
}

function descentPointToJson(p: DescentPoint): string {
  return '{"grade_pct":' + jsonNumber(p.grade_pct)
       + ',"speed_kmh":' + jsonNumber(p.speed_kmh)
       + ',"progress":' + jsonNumber(p.progress) + '}';
}

function arrayJson<T>(arr: T[], toJson: (v: T) => string): string {
  const parts = new Array<string>(arr.length);
  for (let i = 0; i < arr.length; i++) parts[i] = toJson(arr[i]);
  return '[' + parts.join(',') + ']';
}

export function serializeResult(r: AnalysisResult): string {
  const out: string[] = [];
  out.push('{');
  out.push('"total_distance_km":' + jsonNumber(r.total_distance_km));
  out.push(',"total_duration_min":' + jsonNumber(r.total_duration_min));
  out.push(',"avg_pace_min_per_km":' + jsonNumber(r.avg_pace_min_per_km));
  out.push(',"elevation_gain_m":' + jsonNumber(r.elevation_gain_m));
  out.push(',"avg_hr":' + jsonNumberOrNull(r.avg_hr));
  out.push(',"max_hr_recorded":' + jsonIntOrNull(r.max_hr_recorded));
  out.push(',"has_hr_data":' + (r.has_hr_data ? 'true' : 'false'));
  out.push(',"has_elevation_data":' + (r.has_elevation_data ? 'true' : 'false'));
  out.push(',"has_time_data":' + (r.has_time_data ? 'true' : 'false'));
  out.push(',"point_count":' + r.point_count.toString());
  out.push(',"estimates":' + arrayJson<Vo2Estimate>(r.estimates, estimateToJson));
  out.push(',"fitness_category":' + (r.fitness_category != null ? jsonEscape(r.fitness_category!) : 'null'));
  out.push(',"fitness_description":' + (r.fitness_description != null ? jsonEscape(r.fitness_description!) : 'null'));
  out.push(',"peak_1km":' + (r.peak_1km != null ? peakKmToJson(r.peak_1km!) : 'null'));
  out.push(',"chart_points":' + arrayJson<ChartPoint>(r.chart_points, chartPointToJson));
  out.push(',"cardiac_drift":' + arrayJson<DriftPoint>(r.cardiac_drift, driftPointToJson));
  out.push(',"decoupling_pct":' + jsonNumberOrNull(r.decoupling_pct));
  out.push(',"descent_points":' + arrayJson<DescentPoint>(r.descent_points, descentPointToJson));
  out.push(',"error":' + (r.error != null ? jsonEscape(r.error!) : 'null'));
  out.push('}');
  return out.join('');
}
