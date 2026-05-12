import { parseGpx } from './gpx.js';
import { analyze } from './analyze.js';

// Public entry: GPX text in, result object out. Mirrors the WASM
// analyze_gpx signature but skips the JSON stringify/parse round-trip —
// the frontend can consume the object directly.
export function analyzeGpx(gpxContent, weightKg, maxHr) {
  try {
    const points = parseGpx(gpxContent);
    return analyze(points, weightKg, maxHr | 0);
  } catch (e) {
    return {
      error: 'Failed to parse GPX: ' + (e.message || e),
      estimates: [],
      chart_points: [],
      cardiac_drift: [],
      descent_points: [],
    };
  }
}
