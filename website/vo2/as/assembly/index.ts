import { parseGpx } from './gpx';
import { analyze } from './analyze';
import { serializeResult } from './serialize';
import { AnalysisResult } from './types';

export function analyze_gpx(content: string, weightKg: f64, maxHr: i32): string {
  const parsed = parseGpx(content);
  let result: AnalysisResult;
  if (parsed.error != null) {
    result = new AnalysisResult();
    result.error = 'Failed to parse GPX: ' + parsed.error!;
  } else {
    result = analyze(parsed.points, weightKg, maxHr);
  }
  return serializeResult(result);
}
