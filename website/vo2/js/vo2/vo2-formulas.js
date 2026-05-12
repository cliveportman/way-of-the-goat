// ACSM Running Metabolic Equation.
// VO2 (mL/kg/min) = 0.2 × S + 0.9 × S × G + 3.5
// S = speed in m/min, G = fractional grade.
export function acsmVo2(speedMpm, grade) {
  return 0.2 * speedMpm + 0.9 * speedMpm * grade + 3.5;
}

// Jack Daniels / Gilbert performance formula. Returns null on invalid input.
export function danielsVo2Max(velocityMpm, timeMin) {
  if (velocityMpm < 60 || timeMin < 1) return null;
  const pct = 0.8
            + 0.1894393 * Math.exp(-0.012778 * timeMin)
            + 0.2989558 * Math.exp(-0.1932605 * timeMin);
  const vo2 = -4.60 + 0.182258 * velocityMpm + 0.000104 * velocityMpm * velocityMpm;
  if (pct <= 0 || vo2 <= 0) return null;
  return vo2 / pct;
}

export function fitnessCategory(vo2Max) {
  if (vo2Max < 30) return ['Poor', 'Below average cardiovascular fitness'];
  if (vo2Max < 40) return ['Fair', 'Average cardiovascular fitness'];
  if (vo2Max < 50) return ['Good', 'Above average cardiovascular fitness'];
  if (vo2Max < 60) return ['Excellent', 'High cardiovascular fitness'];
  if (vo2Max < 75) return ['Superior', 'Very high cardiovascular fitness'];
  return ['Elite', 'Elite-level cardiovascular fitness'];
}

// Half-away-from-zero rounding, matching the Rust / Go / .NET ports.
export function roundTo(v, places) {
  const factor = Math.pow(10, places);
  return Math.sign(v) * Math.round(Math.abs(v) * factor) / factor;
}

export function clamp(v, lo, hi) {
  return v < lo ? lo : v > hi ? hi : v;
}
