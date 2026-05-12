import { haversine } from './geo.js';
import {
  acsmVo2, danielsVo2Max, fitnessCategory, roundTo, clamp,
} from './vo2-formulas.js';

function buildSegments(points) {
  let baseTime = null;
  for (const p of points) { if (p.timeS !== null) { baseTime = p.timeS; break; } }

  let cum = 0;
  const segs = new Array(Math.max(0, points.length - 1));
  for (let i = 1; i < points.length; i++) {
    const prev = points[i - 1];
    const curr = points[i];

    const dist = haversine(prev.lat, prev.lon, curr.lat, curr.lon);
    let eleDiff = 0;
    if (curr.ele !== null && prev.ele !== null) eleDiff = curr.ele - prev.ele;

    let dt = 1;
    if (curr.timeS !== null && prev.timeS !== null && curr.timeS > prev.timeS) {
      dt = curr.timeS - prev.timeS;
    }
    cum += dist;

    const grade = dist > 0.5 ? clamp(eleDiff / dist, -0.50, 0.50) : 0;
    const speedMpm = (dist / dt) * 60;
    const elapsed = curr.timeS !== null && baseTime !== null
      ? curr.timeS - baseTime
      : i;

    segs[i - 1] = {
      cumDistM: cum,
      elapsedS: elapsed,
      speedMpm,
      grade,
      hr: curr.hr,
      elevationM: curr.ele,
    };
  }
  return segs;
}

function bestWindow(segs, targetMin) {
  const targetS = targetMin * 60;
  let bestSpeed = 0;
  let bestMin = 0;
  let found = false;
  for (let startI = 0; startI < segs.length; startI++) {
    const startElapsed = segs[startI].elapsedS;
    const startDist = startI === 0 ? 0 : segs[startI - 1].cumDistM;
    const targetElapsed = startElapsed + targetS;

    let endI = -1;
    for (let k = segs.length - 1; k >= startI; k--) {
      if (segs[k].elapsedS <= targetElapsed) { endI = k; break; }
    }
    if (endI <= startI) continue;

    const actualS = segs[endI].elapsedS - startElapsed;
    if (actualS < targetS * 0.6) continue;
    const dist = segs[endI].cumDistM - startDist;
    if (dist < 100) continue;

    const sp = dist / actualS * 60;
    if (sp > bestSpeed) { bestSpeed = sp; bestMin = actualS / 60; found = true; }
  }
  return found ? { speedMpm: bestSpeed, actualMin: bestMin } : null;
}

function best1KmVo2(segs, effectiveMaxHr) {
  if (segs.length < 2) return null;
  const vo2s = new Array(segs.length);
  for (let i = 0; i < segs.length; i++) {
    const g = segs[i].grade < 0 ? 0 : segs[i].grade;
    vo2s[i] = acsmVo2(segs[i].speedMpm, g);
  }

  let best = null;
  let right = 0;
  let vo2Sum = 0;
  let hrSum = 0;
  let hrCount = 0;

  for (let left = 0; left < segs.length; left++) {
    const leftDist = left === 0 ? 0 : segs[left - 1].cumDistM;

    while (right < segs.length && segs[right].cumDistM - leftDist < 1000) {
      vo2Sum += vo2s[right];
      if (segs[right].hr !== null) { hrSum += segs[right].hr; hrCount++; }
      right++;
    }

    const rightIdx = Math.min(right, segs.length - 1);
    const actualDist = segs[rightIdx].cumDistM - leftDist;
    if (actualDist < 800) break;
    const windowLen = right - left;
    if (windowLen === 0) continue;

    const avgVo2 = vo2Sum / windowLen;
    if (best === null || avgVo2 > best.vo2_expressed) {
      const leftElapsed = left === 0 ? 0 : segs[left - 1].elapsedS;
      const elapsed = segs[rightIdx].elapsedS - leftElapsed;
      const sp = elapsed > 0 ? actualDist / elapsed * 60 : 0;

      const leftEle = left === 0 ? segs[0].elevationM : segs[left - 1].elevationM;
      let netEle = 0;
      if (segs[rightIdx].elevationM !== null && leftEle !== null) {
        netEle = segs[rightIdx].elevationM - leftEle;
      }
      const avgGradePct = roundTo(netEle / actualDist * 100, 1);

      const avgHr = hrCount > 0 ? Math.round(hrSum / hrCount) : null;
      let vo2MaxEst = null;
      if (effectiveMaxHr > 0 && avgHr !== null && avgHr !== 0) {
        const est = avgVo2 * effectiveMaxHr / avgHr;
        if (est > 15 && est < 120) vo2MaxEst = roundTo(est, 1);
      }
      const pace = sp > 0 ? roundTo(1000 / sp, 2) : 0;

      best = {
        vo2_expressed: roundTo(avgVo2, 1),
        vo2max_est: vo2MaxEst,
        pace_min_per_km: pace,
        avg_grade_pct: avgGradePct,
        avg_hr: avgHr,
        start_distance_km: roundTo(leftDist / 1000, 2),
      };
    }

    vo2Sum -= vo2s[left];
    if (segs[left].hr !== null) {
      hrSum = Math.max(0, hrSum - segs[left].hr);
      if (hrCount > 0) hrCount--;
    }
  }
  return best;
}

function computeDrift(segs) {
  const SKIP_S = 180;
  const raw = [];
  for (const s of segs) {
    if (s.elapsedS <= SKIP_S || s.speedMpm <= 50) continue;
    if (s.hr === null || s.hr <= 50) continue;
    const eff = s.speedMpm / s.hr;
    if (eff > 0) raw.push({ dist: s.cumDistM, eff });
  }
  if (raw.length < 20) return { drift: [], decoupling: null };

  let span = segs.length > 0 ? segs[segs.length - 1].elapsedS : 1;
  if (span < 1) span = 1;
  const samplesPerSec = raw.length / span;
  let win = Math.round(samplesPerSec * 600);
  win = clamp(win, 5, 600);
  if (win > raw.length) win = raw.length;
  const half = (win / 2) | 0;

  const smoothed = new Array(raw.length);
  for (let i = 0; i < raw.length; i++) {
    const s = Math.max(0, i - half);
    const e = Math.min(raw.length, i + half + 1);
    let sum = 0;
    for (let k = s; k < e; k++) sum += raw[k].eff;
    smoothed[i] = { dist: raw[i].dist, eff: sum / (e - s) };
  }

  const baseline = smoothed[0].eff;
  if (baseline <= 0) return { drift: [], decoupling: null };

  const step = Math.max(1, (smoothed.length / 300) | 0);
  const drift = [];
  for (let i = 0; i < smoothed.length; i += step) {
    drift.push({
      distance_km: roundTo(smoothed[i].dist / 1000, 2),
      efficiency: roundTo(smoothed[i].eff / baseline, 3),
    });
  }

  const mid = (smoothed.length / 2) | 0;
  let m1 = 0, m2 = 0;
  for (let i = 0; i < mid; i++) m1 += smoothed[i].eff;
  for (let i = mid; i < smoothed.length; i++) m2 += smoothed[i].eff;
  m1 /= mid;
  m2 /= smoothed.length - mid;
  const decoupling = m1 > 0 ? roundTo((m1 - m2) / m1 * 100, 1) : null;
  return { drift, decoupling };
}

function computeDescentPoints(segs) {
  let total = segs.length > 0 ? segs[segs.length - 1].cumDistM : 1;
  if (total < 1) total = 1;

  let pts = [];
  for (const s of segs) {
    if (s.grade >= -0.03 || s.speedMpm <= 30) continue;
    const speedKmh = s.speedMpm * 60 / 1000;
    if (speedKmh < 0.5 || speedKmh > 35) continue;
    const gradePct = roundTo(s.grade * 100, 1);
    if (gradePct < -50) continue;
    pts.push({
      grade_pct: gradePct,
      speed_kmh: roundTo(speedKmh, 2),
      progress: roundTo(s.cumDistM / total, 3),
    });
  }

  if (pts.length > 2000) {
    const step = (pts.length / 2000) | 0;
    const sampled = [];
    for (let i = 0; i < pts.length; i += step) sampled.push(pts[i]);
    pts = sampled;
  }
  return pts;
}

function emptyResult(pointCount, error) {
  return {
    total_distance_km: 0,
    total_duration_min: 0,
    avg_pace_min_per_km: 0,
    elevation_gain_m: 0,
    avg_hr: null,
    max_hr_recorded: null,
    has_hr_data: false,
    has_elevation_data: false,
    has_time_data: false,
    point_count: pointCount,
    estimates: [],
    fitness_category: null,
    fitness_description: null,
    peak_1km: null,
    chart_points: [],
    cardiac_drift: [],
    decoupling_pct: null,
    descent_points: [],
    error,
  };
}

export function analyze(points, _weightKg, maxHrInput) {
  if (points.length < 10) {
    return emptyResult(
      points.length,
      `Not enough track points (found ${points.length}, need at least 10). Is this a valid GPX file?`,
    );
  }

  const segs = buildSegments(points);

  const totalDistM = segs.length > 0 ? segs[segs.length - 1].cumDistM : 0;
  const totalDistKm = totalDistM / 1000;

  const hasTime = points.some(p => p.timeS !== null);
  let totalDurationS = segs.length;
  if (hasTime) {
    let startT = null, endT = null;
    for (const p of points) { if (p.timeS !== null) { startT = p.timeS; break; } }
    for (let i = points.length - 1; i >= 0; i--) { if (points[i].timeS !== null) { endT = points[i].timeS; break; } }
    if (startT !== null && endT !== null && endT > startT) totalDurationS = endT - startT;
  }
  const totalDurationMin = totalDurationS / 60;
  const avgPace = totalDistKm > 0 ? totalDurationMin / totalDistKm : 0;

  const hasElevation = points.some(p => p.ele !== null);
  let elevationGain = 0;
  for (let i = 1; i < segs.length; i++) {
    const a = segs[i - 1].elevationM;
    const b = segs[i].elevationM;
    if (a !== null && b !== null && b > a) elevationGain += b - a;
  }

  const hrVals = points.filter(p => p.hr !== null).map(p => p.hr);
  const hasHr = hrVals.length > 0;
  let avgHr = null;
  let maxHrRecorded = null;
  if (hasHr) {
    let sum = 0;
    let mx = 0;
    for (const h of hrVals) { sum += h; if (h > mx) mx = h; }
    avgHr = sum / hrVals.length;
    maxHrRecorded = mx;
  }

  let effectiveMaxHr = 0;
  if (maxHrInput > 0) effectiveMaxHr = maxHrInput;
  else if (maxHrRecorded !== null) effectiveMaxHr = maxHrRecorded * 1.05;

  let cardiacDrift = [];
  let decouplingPct = null;
  if (hasHr && hasTime) {
    const r = computeDrift(segs);
    cardiacDrift = r.drift;
    decouplingPct = r.decoupling;
  }

  const descentPoints = (hasTime && hasElevation) ? computeDescentPoints(segs) : [];

  const estimates = [];

  // Method 1: ACSM + HR (steady-state)
  if (hasHr && effectiveMaxHr > 0) {
    const SKIP_S = 180;
    const hrLo = effectiveMaxHr * 0.65;
    const hrHi = effectiveMaxHr * 0.97;

    const samples = [];
    for (const s of segs) {
      if (s.elapsedS <= SKIP_S || s.speedMpm <= 80 || s.hr === null) continue;
      const hf = s.hr;
      if (hf < hrLo || hf > hrHi) continue;
      const vo2 = acsmVo2(s.speedMpm, s.grade);
      const est = vo2 * effectiveMaxHr / hf;
      if (est > 15 && est < 110) samples.push(est);
    }

    if (samples.length >= 5) {
      samples.sort((a, b) => a - b);
      const median = samples[(samples.length / 2) | 0];

      let mean = 0; for (const x of samples) mean += x; mean /= samples.length;
      let variance = 0;
      for (const x of samples) { const d = x - mean; variance += d * d; }
      variance /= samples.length;
      const cv = Math.sqrt(variance) / mean;
      const countScore = Math.min(samples.length / 100, 1);
      const cvPenalty = Math.min(cv / 0.25, 1) * 15;
      const confidence = clamp(Math.round(45 + countScore * 40 - cvPenalty), 20, 85);

      estimates.push({
        method: 'ACSM + Heart Rate (Steady-State)',
        value: roundTo(median, 1),
        confidence_pct: confidence,
        notes: `Uses ACSM running metabolic equation and HR/HRmax linearity. `
             + `Based on ${samples.length} steady-state data points (HR 65–97% of max, after first 3 min).`,
      });
    }
  }

  // Method 2: Jack Daniels — best effort windows
  if (hasTime && totalDurationMin > 2) {
    const effortPct = (hasHr && effectiveMaxHr > 0 && avgHr !== null)
      ? avgHr / effectiveMaxHr
      : null;

    const targets = [20, 30, 10, 60, 5];
    let added = false;
    for (const dur of targets) {
      if (totalDurationMin < dur * 0.6) continue;
      const w = bestWindow(segs, dur);
      if (w === null) continue;
      const vo2Max = danielsVo2Max(w.speedMpm, w.actualMin);
      if (vo2Max === null || vo2Max <= 15 || vo2Max >= 110) continue;

      const paceKm = w.speedMpm > 0 ? 1000 / w.speedMpm : 0;
      const paceMin = paceKm | 0;
      const paceSec = ((paceKm - paceMin) * 60) | 0;

      let confidence, effortNote;
      const durBoost = Math.min(dur / 60, 1);
      if (effortPct !== null && effortPct < 0.75) {
        confidence = clamp(Math.round(20 + durBoost * 20), 15, 35);
        effortNote = `Avg HR was only ${(effortPct * 100).toFixed(0)}% of max — this was an easy/Zone 2 run. `
                   + `Daniels assumes race-like effort, so this result will `
                   + `significantly underestimate your actual VO2 max.`;
      } else if (effortPct !== null && effortPct < 0.85) {
        confidence = clamp(Math.round(20 + durBoost * 40), 25, 55);
        effortNote = `Avg HR was ${(effortPct * 100).toFixed(0)}% of max — a moderate effort. Result will `
                   + `likely underestimate your VO2 max; most accurate when run `
                   + `at race or threshold intensity.`;
      } else if (effortPct !== null) {
        confidence = clamp(Math.round(20 + durBoost * 52), 35, 72);
        effortNote = `Avg HR was ${(effortPct * 100).toFixed(0)}% of max — a hard effort. Result is `
                   + `reasonably accurate; may slightly overestimate if HR was `
                   + `elevated by heat or fatigue rather than pure intensity.`;
      } else {
        confidence = clamp(Math.round(20 + durBoost * 35), 20, 55);
        effortNote = `No HR data — cannot assess effort level. Result is only accurate `
                   + `if this was a race or near-maximal effort.`;
      }

      estimates.push({
        method: `Jack Daniels — Best ${dur}-min Effort`,
        value: roundTo(vo2Max, 1),
        confidence_pct: confidence,
        notes: `Based on your fastest ${dur}-minute segment (avg pace ${paceMin}:${String(paceSec).padStart(2, '0')} /km). ${effortNote}`,
      });
      added = true;
      break;
    }

    if (!added && totalDistKm > 0.5) {
      const wholeSpeed = totalDistM / totalDurationS * 60;
      const vo2Max = danielsVo2Max(wholeSpeed, totalDurationMin);
      if (vo2Max !== null && vo2Max > 15 && vo2Max < 110) {
        estimates.push({
          method: 'Jack Daniels — Whole Activity',
          value: roundTo(vo2Max, 1),
          confidence_pct: 18,
          notes: 'Based on average pace across the entire activity. Assumes race-like '
               + 'effort throughout — almost always an underestimate for training runs.',
        });
      }
    }
  }

  // Method 3: Firstbeat — linear regression of HR vs VO2
  if (hasHr && effectiveMaxHr > 0) {
    const SKIP_S = 180;
    const hrLo = effectiveMaxHr * 0.50;
    const hrHi = effectiveMaxHr * 0.97;

    const pairs = [];
    for (const s of segs) {
      if (s.elapsedS <= SKIP_S || s.speedMpm <= 80 || s.hr === null) continue;
      const hf = s.hr;
      if (hf < hrLo || hf > hrHi) continue;
      const vo2 = acsmVo2(s.speedMpm, s.grade);
      if (vo2 > 0) pairs.push({ hr: hf, vo2 });
    }

    if (pairs.length >= 10) {
      const n = pairs.length;
      let sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
      for (const p of pairs) {
        sumX += p.hr; sumY += p.vo2;
        sumXY += p.hr * p.vo2; sumXX += p.hr * p.hr;
      }
      const denom = n * sumXX - sumX * sumX;
      if (Math.abs(denom) > 1e-10) {
        const m = (n * sumXY - sumX * sumY) / denom;
        const b = (sumY - m * sumX) / n;
        const vo2Max = m * effectiveMaxHr + b;
        if (m > 0 && vo2Max > 15 && vo2Max < 110) {
          const yMean = sumY / n;
          let ssRes = 0, ssTot = 0;
          for (const p of pairs) {
            const r = p.vo2 - (m * p.hr + b);
            ssRes += r * r;
            const d = p.vo2 - yMean;
            ssTot += d * d;
          }
          const rSquared = ssTot > 1e-10 ? Math.max(0, 1 - ssRes / ssTot) : 0;
          const confidence = clamp(Math.round(30 + rSquared * 53), 20, 83);
          estimates.push({
            method: 'Firstbeat (HR–VO2 Regression)',
            value: roundTo(vo2Max, 1),
            confidence_pct: confidence,
            notes: `Fits a linear regression across ${pairs.length} steady-state (HR, VO2) pairs `
                 + `and extrapolates to HRmax (R² = ${rSquared.toFixed(2)}). More robust than the `
                 + `point-by-point method as it uses all data together.`,
          });
        }
      }
    }
  }

  let fitnessCat = null, fitnessDesc = null;
  if (estimates.length > 0) {
    let bestIdx = 0;
    for (let i = 1; i < estimates.length; i++) {
      if (estimates[i].confidence_pct > estimates[bestIdx].confidence_pct) bestIdx = i;
    }
    [fitnessCat, fitnessDesc] = fitnessCategory(estimates[bestIdx].value);
  }

  const step = Math.max(1, (segs.length / 500) | 0);
  const chart = [];
  for (let i = 0; i < segs.length; i += step) {
    const s = segs[i];
    const pace = s.speedMpm > 10 ? roundTo(1000 / s.speedMpm, 2) : null;
    chart.push({
      distance_km: roundTo(s.cumDistM / 1000, 2),
      pace_min_per_km: pace,
      hr: s.hr,
      elevation_m: s.elevationM,
    });
  }

  return {
    total_distance_km: roundTo(totalDistKm, 2),
    total_duration_min: roundTo(totalDurationMin, 1),
    avg_pace_min_per_km: roundTo(avgPace, 2),
    elevation_gain_m: Math.round(elevationGain),
    avg_hr: avgHr,
    max_hr_recorded: maxHrRecorded,
    has_hr_data: hasHr,
    has_elevation_data: hasElevation,
    has_time_data: hasTime,
    point_count: points.length,
    estimates,
    fitness_category: fitnessCat,
    fitness_description: fitnessDesc,
    peak_1km: hasTime ? best1KmVo2(segs, effectiveMaxHr) : null,
    chart_points: chart,
    cardiac_drift: cardiacDrift,
    decoupling_pct: decouplingPct,
    descent_points: descentPoints,
    error: null,
  };
}
