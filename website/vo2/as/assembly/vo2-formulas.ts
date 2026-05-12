export function acsmVo2(speedMpm: f64, grade: f64): f64 {
  return 0.2 * speedMpm + 0.9 * speedMpm * grade + 3.5;
}

// Returns NaN on invalid input (signals "no value").
export function danielsVo2Max(velocityMpm: f64, timeMin: f64): f64 {
  if (velocityMpm < 60.0 || timeMin < 1.0) return NaN;
  const pct = 0.8
            + 0.1894393 * Math.exp(-0.012778 * timeMin)
            + 0.2989558 * Math.exp(-0.1932605 * timeMin);
  const vo2 = -4.60 + 0.182258 * velocityMpm + 0.000104 * velocityMpm * velocityMpm;
  if (pct <= 0.0 || vo2 <= 0.0) return NaN;
  return vo2 / pct;
}

export class FitnessCategory {
  constructor(public name: string, public description: string) {}
}

export function fitnessCategory(vo2Max: f64): FitnessCategory {
  if (vo2Max < 30.0) return new FitnessCategory('Poor', 'Below average cardiovascular fitness');
  if (vo2Max < 40.0) return new FitnessCategory('Fair', 'Average cardiovascular fitness');
  if (vo2Max < 50.0) return new FitnessCategory('Good', 'Above average cardiovascular fitness');
  if (vo2Max < 60.0) return new FitnessCategory('Excellent', 'High cardiovascular fitness');
  if (vo2Max < 75.0) return new FitnessCategory('Superior', 'Very high cardiovascular fitness');
  return new FitnessCategory('Elite', 'Elite-level cardiovascular fitness');
}

// Half-away-from-zero rounding, matching the Rust/Go/.NET/JS ports.
export function roundTo(v: f64, places: i32): f64 {
  const factor = Math.pow(10.0, <f64>places);
  const sign: f64 = v < 0.0 ? -1.0 : 1.0;
  return sign * Math.round(Math.abs(v) * factor) / factor;
}

@inline export function clampF(v: f64, lo: f64, hi: f64): f64 {
  return v < lo ? lo : v > hi ? hi : v;
}

@inline export function clampI(v: i32, lo: i32, hi: i32): i32 {
  return v < lo ? lo : v > hi ? hi : v;
}

// Equivalent to JS Number.prototype.toFixed — AS doesn't expose it on f64.
// Rounds half-away-from-zero to match the other ports.
export function formatFixed(v: f64, places: i32): string {
  if (isNaN(v)) return 'NaN';
  const factor = Math.pow(10.0, <f64>places);
  const sign: f64 = v < 0.0 ? -1.0 : 1.0;
  const rounded = sign * Math.round(Math.abs(v) * factor) / factor;
  if (places <= 0) return (<i64>rounded).toString();

  const negative = rounded < 0.0;
  const abs = Math.abs(rounded);
  const whole = <i64>Math.floor(abs);
  // Scale the fractional part with the same factor we used to round so we
  // recover the exact integer mantissa of the rounded value.
  const fracInt = <i64>Math.round((abs - <f64>whole) * factor);

  let frac = fracInt.toString();
  while (frac.length < places) frac = '0' + frac;
  const result = whole.toString() + '.' + frac;
  return negative ? '-' + result : result;
}
