const std = @import("std");

// ACSM Running Metabolic Equation.
// VO2 (mL/kg/min) = 0.2 × S + 0.9 × S × G + 3.5
pub fn acsmVo2(speed_mpm: f64, grade: f64) f64 {
    return 0.2 * speed_mpm + 0.9 * speed_mpm * grade + 3.5;
}

// Jack Daniels / Gilbert. Returns NaN on invalid input (sentinel for "no value").
pub fn danielsVo2Max(velocity_mpm: f64, time_min: f64) f64 {
    if (velocity_mpm < 60.0 or time_min < 1.0) return std.math.nan(f64);
    const pct = 0.8 +
        0.1894393 * @exp(-0.012778 * time_min) +
        0.2989558 * @exp(-0.1932605 * time_min);
    const vo2 = -4.60 + 0.182258 * velocity_mpm + 0.000104 * velocity_mpm * velocity_mpm;
    if (pct <= 0.0 or vo2 <= 0.0) return std.math.nan(f64);
    return vo2 / pct;
}

pub const Fitness = struct {
    name: []const u8,
    description: []const u8,
};

pub fn fitnessCategory(vo2_max: f64) Fitness {
    if (vo2_max < 30.0) return .{ .name = "Poor", .description = "Below average cardiovascular fitness" };
    if (vo2_max < 40.0) return .{ .name = "Fair", .description = "Average cardiovascular fitness" };
    if (vo2_max < 50.0) return .{ .name = "Good", .description = "Above average cardiovascular fitness" };
    if (vo2_max < 60.0) return .{ .name = "Excellent", .description = "High cardiovascular fitness" };
    if (vo2_max < 75.0) return .{ .name = "Superior", .description = "Very high cardiovascular fitness" };
    return .{ .name = "Elite", .description = "Elite-level cardiovascular fitness" };
}

// Half-away-from-zero rounding to match the other ports.
pub fn roundTo(v: f64, places: i32) f64 {
    const factor = std.math.pow(f64, 10.0, @floatFromInt(places));
    const sign: f64 = if (v < 0.0) -1.0 else 1.0;
    return sign * @round(@abs(v) * factor) / factor;
}

pub inline fn clampF(v: f64, lo: f64, hi: f64) f64 {
    if (v < lo) return lo;
    if (v > hi) return hi;
    return v;
}

pub inline fn clampI(v: i32, lo: i32, hi: i32) i32 {
    if (v < lo) return lo;
    if (v > hi) return hi;
    return v;
}
