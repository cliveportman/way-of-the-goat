import { TrackPoint } from './gpx';
import { haversine } from './geo';
import { acsmVo2, danielsVo2Max, fitnessCategory, roundTo, clampF, clampI, formatFixed } from './vo2-formulas';
import {
  AnalysisResult, ChartPoint, Vo2Estimate, PeakKmResult, DriftPoint, DescentPoint,
} from './types';

class Segment {
  cumDistM: f64 = 0;
  elapsedS: f64 = 0;
  speedMpm: f64 = 0;
  grade: f64 = 0;
  hr: i32 = -1;        // -1 = no hr
  elevationM: f64 = NaN;
}

function buildSegments(points: TrackPoint[]): Segment[] {
  let baseTime: f64 = NaN;
  for (let i = 0; i < points.length; i++) {
    if (!isNaN(points[i].timeS)) { baseTime = points[i].timeS; break; }
  }

  let cum: f64 = 0;
  const segs: Segment[] = [];
  for (let i = 1; i < points.length; i++) {
    const prev = points[i - 1];
    const curr = points[i];

    const dist = haversine(prev.lat, prev.lon, curr.lat, curr.lon);
    let eleDiff: f64 = 0;
    if (!isNaN(curr.ele) && !isNaN(prev.ele)) eleDiff = curr.ele - prev.ele;

    let dt: f64 = 1;
    if (!isNaN(curr.timeS) && !isNaN(prev.timeS) && curr.timeS > prev.timeS) {
      dt = curr.timeS - prev.timeS;
    }
    cum += dist;
    const grade = dist > 0.5 ? clampF(eleDiff / dist, -0.50, 0.50) : 0.0;
    const speedMpm = (dist / dt) * 60.0;
    const elapsed: f64 = !isNaN(curr.timeS) && !isNaN(baseTime)
      ? curr.timeS - baseTime
      : <f64>i;

    const s = new Segment();
    s.cumDistM = cum;
    s.elapsedS = elapsed;
    s.speedMpm = speedMpm;
    s.grade = grade;
    s.hr = curr.hr;
    s.elevationM = curr.ele;
    segs.push(s);
  }
  return segs;
}

class WindowResult {
  speedMpm: f64;
  actualMin: f64;
  found: bool;
  constructor(speedMpm: f64, actualMin: f64, found: bool) {
    this.speedMpm = speedMpm; this.actualMin = actualMin; this.found = found;
  }
}

function bestWindow(segs: Segment[], targetMin: f64): WindowResult {
  const targetS = targetMin * 60.0;
  let bestSpeed: f64 = 0;
  let bestMin: f64 = 0;
  let found = false;
  for (let startI = 0; startI < segs.length; startI++) {
    const startElapsed = segs[startI].elapsedS;
    const startDist = startI == 0 ? 0.0 : segs[startI - 1].cumDistM;
    const targetElapsed = startElapsed + targetS;

    let endI = -1;
    for (let k = segs.length - 1; k >= startI; k--) {
      if (segs[k].elapsedS <= targetElapsed) { endI = k; break; }
    }
    if (endI <= startI) continue;

    const actualS = segs[endI].elapsedS - startElapsed;
    if (actualS < targetS * 0.6) continue;
    const dist = segs[endI].cumDistM - startDist;
    if (dist < 100.0) continue;

    const sp = dist / actualS * 60.0;
    if (sp > bestSpeed) { bestSpeed = sp; bestMin = actualS / 60.0; found = true; }
  }
  return new WindowResult(bestSpeed, bestMin, found);
}

function best1KmVo2(segs: Segment[], effectiveMaxHr: f64): PeakKmResult | null {
  if (segs.length < 2) return null;
  const vo2s = new Array<f64>(segs.length);
  for (let i = 0; i < segs.length; i++) {
    const g = segs[i].grade < 0 ? 0 : segs[i].grade;
    vo2s[i] = acsmVo2(segs[i].speedMpm, g);
  }

  let best: PeakKmResult | null = null;
  let right = 0;
  let vo2Sum: f64 = 0;
  let hrSum: i64 = 0;
  let hrCount = 0;

  for (let left = 0; left < segs.length; left++) {
    const leftDist = left == 0 ? 0.0 : segs[left - 1].cumDistM;

    while (right < segs.length && segs[right].cumDistM - leftDist < 1000.0) {
      vo2Sum += vo2s[right];
      if (segs[right].hr >= 0) { hrSum += <i64>segs[right].hr; hrCount++; }
      right++;
    }

    const rightIdx = right > segs.length - 1 ? segs.length - 1 : right;
    const actualDist = segs[rightIdx].cumDistM - leftDist;
    if (actualDist < 800.0) break;
    const windowLen = right - left;
    if (windowLen == 0) continue;

    const avgVo2 = vo2Sum / <f64>windowLen;
    if (best == null || avgVo2 > best.vo2_expressed) {
      const leftElapsed = left == 0 ? 0.0 : segs[left - 1].elapsedS;
      const elapsed = segs[rightIdx].elapsedS - leftElapsed;
      const sp = elapsed > 0 ? actualDist / elapsed * 60.0 : 0.0;

      const leftEle = left == 0 ? segs[0].elevationM : segs[left - 1].elevationM;
      let netEle: f64 = 0;
      if (!isNaN(segs[rightIdx].elevationM) && !isNaN(leftEle)) {
        netEle = segs[rightIdx].elevationM - leftEle;
      }
      const avgGradePct = roundTo(netEle / actualDist * 100.0, 1);

      const avgHr: i32 = hrCount > 0 ? <i32>Math.round(<f64>hrSum / <f64>hrCount) : -1;
      let vo2MaxEst: f64 = NaN;
      if (effectiveMaxHr > 0 && avgHr > 0) {
        const est = avgVo2 * effectiveMaxHr / <f64>avgHr;
        if (est > 15.0 && est < 120.0) vo2MaxEst = roundTo(est, 1);
      }
      const pace = sp > 0 ? roundTo(1000.0 / sp, 2) : 0.0;

      const p = new PeakKmResult();
      p.vo2_expressed = roundTo(avgVo2, 1);
      p.vo2max_est = vo2MaxEst;
      p.pace_min_per_km = pace;
      p.avg_grade_pct = avgGradePct;
      p.avg_hr = avgHr;
      p.start_distance_km = roundTo(leftDist / 1000.0, 2);
      best = p;
    }

    vo2Sum -= vo2s[left];
    if (segs[left].hr >= 0) {
      const h = <i64>segs[left].hr;
      hrSum = hrSum >= h ? hrSum - h : 0;
      if (hrCount > 0) hrCount--;
    }
  }
  return best;
}

class DriftResult {
  drift: DriftPoint[];
  decoupling: f64;
  constructor(drift: DriftPoint[], decoupling: f64) {
    this.drift = drift; this.decoupling = decoupling;
  }
}

function computeDrift(segs: Segment[]): DriftResult {
  const SKIP_S: f64 = 180.0;
  const rawDist: f64[] = [];
  const rawEff: f64[] = [];
  for (let i = 0; i < segs.length; i++) {
    const s = segs[i];
    if (s.elapsedS <= SKIP_S || s.speedMpm <= 50.0) continue;
    if (s.hr <= 50) continue;
    const eff = s.speedMpm / <f64>s.hr;
    if (eff > 0) { rawDist.push(s.cumDistM); rawEff.push(eff); }
  }
  if (rawDist.length < 20) return new DriftResult([], NaN);

  let span: f64 = segs.length > 0 ? segs[segs.length - 1].elapsedS : 1.0;
  if (span < 1.0) span = 1.0;
  const samplesPerSec = <f64>rawDist.length / span;
  let win = <i32>Math.round(samplesPerSec * 600.0);
  win = clampI(win, 5, 600);
  if (win > rawDist.length) win = rawDist.length;
  const half = win / 2;

  const smoothDist = new Array<f64>(rawDist.length);
  const smoothEff = new Array<f64>(rawDist.length);
  for (let i = 0; i < rawDist.length; i++) {
    const s = i - half < 0 ? 0 : i - half;
    const eEnd = i + half + 1 > rawDist.length ? rawDist.length : i + half + 1;
    let sum: f64 = 0;
    for (let k = s; k < eEnd; k++) sum += rawEff[k];
    smoothDist[i] = rawDist[i];
    smoothEff[i] = sum / <f64>(eEnd - s);
  }

  const baseline = smoothEff[0];
  if (baseline <= 0) return new DriftResult([], NaN);

  let step = smoothEff.length / 300;
  if (step < 1) step = 1;
  const drift: DriftPoint[] = [];
  for (let i = 0; i < smoothEff.length; i += step) {
    const dp = new DriftPoint();
    dp.distance_km = roundTo(smoothDist[i] / 1000.0, 2);
    dp.efficiency = roundTo(smoothEff[i] / baseline, 3);
    drift.push(dp);
  }

  const mid = smoothEff.length / 2;
  let m1: f64 = 0, m2: f64 = 0;
  for (let i = 0; i < mid; i++) m1 += smoothEff[i];
  for (let i = mid; i < smoothEff.length; i++) m2 += smoothEff[i];
  m1 /= <f64>mid;
  m2 /= <f64>(smoothEff.length - mid);
  const decoupling = m1 > 0 ? roundTo((m1 - m2) / m1 * 100.0, 1) : NaN;
  return new DriftResult(drift, decoupling);
}

function computeDescentPoints(segs: Segment[]): DescentPoint[] {
  let total: f64 = segs.length > 0 ? segs[segs.length - 1].cumDistM : 1.0;
  if (total < 1.0) total = 1.0;

  let pts: DescentPoint[] = [];
  for (let i = 0; i < segs.length; i++) {
    const s = segs[i];
    if (s.grade >= -0.03 || s.speedMpm <= 30.0) continue;
    const speedKmh = s.speedMpm * 60.0 / 1000.0;
    if (speedKmh < 0.5 || speedKmh > 35.0) continue;
    const gradePct = roundTo(s.grade * 100.0, 1);
    if (gradePct < -50.0) continue;
    const dp = new DescentPoint();
    dp.grade_pct = gradePct;
    dp.speed_kmh = roundTo(speedKmh, 2);
    dp.progress = roundTo(s.cumDistM / total, 3);
    pts.push(dp);
  }

  if (pts.length > 2000) {
    const step = pts.length / 2000;
    const sampled: DescentPoint[] = [];
    for (let i = 0; i < pts.length; i += step) sampled.push(pts[i]);
    pts = sampled;
  }
  return pts;
}

function paceFormat(paceKm: f64): string {
  const paceMin = <i32>paceKm;
  const paceSec = <i32>((paceKm - <f64>paceMin) * 60.0);
  let s = paceMin.toString() + ':';
  if (paceSec < 10) s += '0';
  s += paceSec.toString();
  return s;
}

export function analyze(points: TrackPoint[], _weightKg: f64, maxHrInput: i32): AnalysisResult {
  if (points.length < 10) {
    const empty = new AnalysisResult();
    empty.point_count = points.length;
    empty.error = 'Not enough track points (found ' + points.length.toString()
                + ', need at least 10). Is this a valid GPX file?';
    return empty;
  }

  const segs = buildSegments(points);

  let totalDistM: f64 = segs.length > 0 ? segs[segs.length - 1].cumDistM : 0.0;
  const totalDistKm = totalDistM / 1000.0;

  let hasTime = false;
  for (let i = 0; i < points.length; i++) {
    if (!isNaN(points[i].timeS)) { hasTime = true; break; }
  }
  let totalDurationS: f64 = <f64>segs.length;
  if (hasTime) {
    let startT: f64 = NaN, endT: f64 = NaN;
    for (let i = 0; i < points.length; i++) {
      if (!isNaN(points[i].timeS)) { startT = points[i].timeS; break; }
    }
    for (let i = points.length - 1; i >= 0; i--) {
      if (!isNaN(points[i].timeS)) { endT = points[i].timeS; break; }
    }
    if (!isNaN(startT) && !isNaN(endT) && endT > startT) totalDurationS = endT - startT;
  }
  const totalDurationMin = totalDurationS / 60.0;
  const avgPace = totalDistKm > 0 ? totalDurationMin / totalDistKm : 0.0;

  let hasElevation = false;
  for (let i = 0; i < points.length; i++) {
    if (!isNaN(points[i].ele)) { hasElevation = true; break; }
  }

  let elevationGain: f64 = 0;
  for (let i = 1; i < segs.length; i++) {
    const a = segs[i - 1].elevationM;
    const b = segs[i].elevationM;
    if (!isNaN(a) && !isNaN(b) && b > a) elevationGain += b - a;
  }

  const hrVals: i32[] = [];
  for (let i = 0; i < points.length; i++) {
    if (points[i].hr >= 0) hrVals.push(points[i].hr);
  }
  const hasHr = hrVals.length > 0;
  let avgHrF: f64 = NaN;
  let maxHrRecorded: i32 = -1;
  if (hasHr) {
    let sum: f64 = 0;
    let mx: i32 = 0;
    for (let i = 0; i < hrVals.length; i++) {
      sum += <f64>hrVals[i];
      if (hrVals[i] > mx) mx = hrVals[i];
    }
    avgHrF = sum / <f64>hrVals.length;
    maxHrRecorded = mx;
  }

  let effectiveMaxHr: f64 = 0;
  if (maxHrInput > 0) effectiveMaxHr = <f64>maxHrInput;
  else if (maxHrRecorded >= 0) effectiveMaxHr = <f64>maxHrRecorded * 1.05;

  let cardiacDrift: DriftPoint[] = [];
  let decouplingPct: f64 = NaN;
  if (hasHr && hasTime) {
    const r = computeDrift(segs);
    cardiacDrift = r.drift;
    decouplingPct = r.decoupling;
  }
  const descentPoints = hasTime && hasElevation ? computeDescentPoints(segs) : [];

  const estimates: Vo2Estimate[] = [];

  // Method 1: ACSM + HR (steady-state)
  if (hasHr && effectiveMaxHr > 0) {
    const SKIP_S: f64 = 180.0;
    const hrLo = effectiveMaxHr * 0.65;
    const hrHi = effectiveMaxHr * 0.97;
    const samples: f64[] = [];
    for (let i = 0; i < segs.length; i++) {
      const s = segs[i];
      if (s.elapsedS <= SKIP_S || s.speedMpm <= 80.0 || s.hr < 0) continue;
      const hf = <f64>s.hr;
      if (hf < hrLo || hf > hrHi) continue;
      const vo2 = acsmVo2(s.speedMpm, s.grade);
      const est = vo2 * effectiveMaxHr / hf;
      if (est > 15.0 && est < 110.0) samples.push(est);
    }
    if (samples.length >= 5) {
      samples.sort((a, b) => a < b ? -1 : a > b ? 1 : 0);
      const median = samples[samples.length / 2];
      let mean: f64 = 0;
      for (let i = 0; i < samples.length; i++) mean += samples[i];
      mean /= <f64>samples.length;
      let variance: f64 = 0;
      for (let i = 0; i < samples.length; i++) {
        const d = samples[i] - mean;
        variance += d * d;
      }
      variance /= <f64>samples.length;
      const cv = Math.sqrt(variance) / mean;
      const countScore: f64 = <f64>samples.length / 100.0 < 1.0 ? <f64>samples.length / 100.0 : 1.0;
      const cvPenalty = (cv / 0.25 < 1.0 ? cv / 0.25 : 1.0) * 15.0;
      const confidence = clampI(<i32>Math.round(45.0 + countScore * 40.0 - cvPenalty), 20, 85);

      const est = new Vo2Estimate();
      est.method = 'ACSM + Heart Rate (Steady-State)';
      est.value = roundTo(median, 1);
      est.confidence_pct = confidence;
      est.notes = 'Uses ACSM running metabolic equation and HR/HRmax linearity. '
                + 'Based on ' + samples.length.toString()
                + ' steady-state data points (HR 65–97% of max, after first 3 min).';
      estimates.push(est);
    }
  }

  // Method 2: Jack Daniels — best effort windows
  if (hasTime && totalDurationMin > 2.0) {
    const haveEffort = hasHr && effectiveMaxHr > 0 && !isNaN(avgHrF);
    const effortPct: f64 = haveEffort ? avgHrF / effectiveMaxHr : NaN;

    const targets: StaticArray<f64> = [20.0, 30.0, 10.0, 60.0, 5.0];
    let added = false;
    for (let ti = 0; ti < targets.length; ti++) {
      const dur = targets[ti];
      if (totalDurationMin < dur * 0.6) continue;
      const w = bestWindow(segs, dur);
      if (!w.found) continue;
      const vo2Max = danielsVo2Max(w.speedMpm, w.actualMin);
      if (isNaN(vo2Max) || vo2Max <= 15.0 || vo2Max >= 110.0) continue;

      const paceKm = w.speedMpm > 0 ? 1000.0 / w.speedMpm : 0.0;

      const durBoost: f64 = dur / 60.0 < 1.0 ? dur / 60.0 : 1.0;
      let confidence: i32;
      let effortNote: string;
      if (haveEffort && effortPct < 0.75) {
        confidence = clampI(<i32>Math.round(20.0 + durBoost * 20.0), 15, 35);
        effortNote = 'Avg HR was only ' + formatFixed(effortPct * 100.0, 0)
                   + '% of max — this was an easy/Zone 2 run. '
                   + 'Daniels assumes race-like effort, so this result will '
                   + 'significantly underestimate your actual VO2 max.';
      } else if (haveEffort && effortPct < 0.85) {
        confidence = clampI(<i32>Math.round(20.0 + durBoost * 40.0), 25, 55);
        effortNote = 'Avg HR was ' + formatFixed(effortPct * 100.0, 0)
                   + '% of max — a moderate effort. Result will '
                   + 'likely underestimate your VO2 max; most accurate when run '
                   + 'at race or threshold intensity.';
      } else if (haveEffort) {
        confidence = clampI(<i32>Math.round(20.0 + durBoost * 52.0), 35, 72);
        effortNote = 'Avg HR was ' + formatFixed(effortPct * 100.0, 0)
                   + '% of max — a hard effort. Result is '
                   + 'reasonably accurate; may slightly overestimate if HR was '
                   + 'elevated by heat or fatigue rather than pure intensity.';
      } else {
        confidence = clampI(<i32>Math.round(20.0 + durBoost * 35.0), 20, 55);
        effortNote = 'No HR data — cannot assess effort level. Result is only accurate '
                   + 'if this was a race or near-maximal effort.';
      }

      const est = new Vo2Estimate();
      est.method = 'Jack Daniels — Best ' + (<i32>dur).toString() + '-min Effort';
      est.value = roundTo(vo2Max, 1);
      est.confidence_pct = confidence;
      est.notes = 'Based on your fastest ' + (<i32>dur).toString()
                + '-minute segment (avg pace ' + paceFormat(paceKm) + ' /km). '
                + effortNote;
      estimates.push(est);
      added = true;
      break;
    }

    if (!added && totalDistKm > 0.5) {
      const wholeSpeed = totalDistM / totalDurationS * 60.0;
      const vo2Max = danielsVo2Max(wholeSpeed, totalDurationMin);
      if (!isNaN(vo2Max) && vo2Max > 15.0 && vo2Max < 110.0) {
        const est = new Vo2Estimate();
        est.method = 'Jack Daniels — Whole Activity';
        est.value = roundTo(vo2Max, 1);
        est.confidence_pct = 18;
        est.notes = 'Based on average pace across the entire activity. Assumes race-like '
                  + 'effort throughout — almost always an underestimate for training runs.';
        estimates.push(est);
      }
    }
  }

  // Method 3: Firstbeat — linear regression
  if (hasHr && effectiveMaxHr > 0) {
    const SKIP_S: f64 = 180.0;
    const hrLo = effectiveMaxHr * 0.50;
    const hrHi = effectiveMaxHr * 0.97;
    const pairsHr: f64[] = [];
    const pairsVo2: f64[] = [];
    for (let i = 0; i < segs.length; i++) {
      const s = segs[i];
      if (s.elapsedS <= SKIP_S || s.speedMpm <= 80.0 || s.hr < 0) continue;
      const hf = <f64>s.hr;
      if (hf < hrLo || hf > hrHi) continue;
      const vo2 = acsmVo2(s.speedMpm, s.grade);
      if (vo2 > 0) { pairsHr.push(hf); pairsVo2.push(vo2); }
    }
    if (pairsHr.length >= 10) {
      const n = <f64>pairsHr.length;
      let sumX: f64 = 0, sumY: f64 = 0, sumXY: f64 = 0, sumXX: f64 = 0;
      for (let i = 0; i < pairsHr.length; i++) {
        sumX += pairsHr[i]; sumY += pairsVo2[i];
        sumXY += pairsHr[i] * pairsVo2[i];
        sumXX += pairsHr[i] * pairsHr[i];
      }
      const denom = n * sumXX - sumX * sumX;
      if (Math.abs(denom) > 1e-10) {
        const m = (n * sumXY - sumX * sumY) / denom;
        const b = (sumY - m * sumX) / n;
        const vo2Max = m * effectiveMaxHr + b;
        if (m > 0 && vo2Max > 15.0 && vo2Max < 110.0) {
          const yMean = sumY / n;
          let ssRes: f64 = 0, ssTot: f64 = 0;
          for (let i = 0; i < pairsHr.length; i++) {
            const r = pairsVo2[i] - (m * pairsHr[i] + b);
            ssRes += r * r;
            const d = pairsVo2[i] - yMean;
            ssTot += d * d;
          }
          const rSquared: f64 = ssTot > 1e-10
            ? (1.0 - ssRes / ssTot < 0.0 ? 0.0 : 1.0 - ssRes / ssTot)
            : 0.0;
          const confidence = clampI(<i32>Math.round(30.0 + rSquared * 53.0), 20, 83);
          const est = new Vo2Estimate();
          est.method = 'Firstbeat (HR–VO2 Regression)';
          est.value = roundTo(vo2Max, 1);
          est.confidence_pct = confidence;
          est.notes = 'Fits a linear regression across ' + pairsHr.length.toString()
                    + ' steady-state (HR, VO2) pairs and extrapolates to HRmax (R² = '
                    + formatFixed(rSquared, 2)
                    + '). More robust than the point-by-point method as it uses all data together.';
          estimates.push(est);
        }
      }
    }
  }

  let fitnessCat: string | null = null;
  let fitnessDesc: string | null = null;
  if (estimates.length > 0) {
    let bestIdx = 0;
    for (let i = 1; i < estimates.length; i++) {
      if (estimates[i].confidence_pct > estimates[bestIdx].confidence_pct) bestIdx = i;
    }
    const fc = fitnessCategory(estimates[bestIdx].value);
    fitnessCat = fc.name;
    fitnessDesc = fc.description;
  }

  let step = segs.length / 500;
  if (step < 1) step = 1;
  const chart: ChartPoint[] = [];
  for (let i = 0; i < segs.length; i += step) {
    const s = segs[i];
    const cp = new ChartPoint();
    cp.distance_km = roundTo(s.cumDistM / 1000.0, 2);
    cp.pace_min_per_km = s.speedMpm > 10.0 ? roundTo(1000.0 / s.speedMpm, 2) : NaN;
    cp.hr = s.hr;
    cp.elevation_m = s.elevationM;
    chart.push(cp);
  }

  const res = new AnalysisResult();
  res.total_distance_km = roundTo(totalDistKm, 2);
  res.total_duration_min = roundTo(totalDurationMin, 1);
  res.avg_pace_min_per_km = roundTo(avgPace, 2);
  res.elevation_gain_m = Math.round(elevationGain);
  res.avg_hr = avgHrF;
  res.max_hr_recorded = maxHrRecorded;
  res.has_hr_data = hasHr;
  res.has_elevation_data = hasElevation;
  res.has_time_data = hasTime;
  res.point_count = points.length;
  res.estimates = estimates;
  res.fitness_category = fitnessCat;
  res.fitness_description = fitnessDesc;
  res.peak_1km = hasTime ? best1KmVo2(segs, effectiveMaxHr) : null;
  res.chart_points = chart;
  res.cardiac_drift = cardiacDrift;
  res.decoupling_pct = decouplingPct;
  res.descent_points = descentPoints;
  res.error = null;
  return res;
}
