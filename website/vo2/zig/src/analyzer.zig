const std = @import("std");
const gpx = @import("gpx.zig");
const geo = @import("geo.zig");
const fm = @import("formulas.zig");
const ts = @import("timestamp.zig");
const JsonWriter = @import("json_writer.zig").Writer;

const TrackPoint = gpx.TrackPoint;

const Segment = struct {
    cum_dist_m: f64,
    elapsed_s: f64,
    speed_mpm: f64,
    grade: f64,
    hr: i32,            // -1 = missing
    elevation_m: f64,   // NaN = missing
};

fn buildSegments(allocator: std.mem.Allocator, points: []const TrackPoint) ![]Segment {
    var base_time: f64 = std.math.nan(f64);
    for (points) |p| {
        if (!std.math.isNan(p.time_s)) { base_time = p.time_s; break; }
    }

    var segs: std.ArrayList(Segment) = .empty;
    try segs.ensureTotalCapacity(allocator, points.len);
    errdefer segs.deinit(allocator);
    var cum: f64 = 0;

    var i: usize = 1;
    while (i < points.len) : (i += 1) {
        const prev = points[i - 1];
        const curr = points[i];

        const dist = geo.haversine(prev.lat, prev.lon, curr.lat, curr.lon);
        var ele_diff: f64 = 0;
        if (!std.math.isNan(curr.ele) and !std.math.isNan(prev.ele)) ele_diff = curr.ele - prev.ele;

        var dt: f64 = 1.0;
        if (!std.math.isNan(curr.time_s) and !std.math.isNan(prev.time_s) and curr.time_s > prev.time_s) {
            dt = curr.time_s - prev.time_s;
        }
        cum += dist;
        const grade = if (dist > 0.5) fm.clampF(ele_diff / dist, -0.50, 0.50) else 0.0;
        const speed_mpm = (dist / dt) * 60.0;
        const elapsed = if (!std.math.isNan(curr.time_s) and !std.math.isNan(base_time))
            curr.time_s - base_time
        else
            @as(f64, @floatFromInt(i));

        try segs.append(allocator, .{
            .cum_dist_m = cum,
            .elapsed_s = elapsed,
            .speed_mpm = speed_mpm,
            .grade = grade,
            .hr = curr.hr,
            .elevation_m = curr.ele,
        });
    }
    return segs.toOwnedSlice(allocator);
}

const WindowResult = struct { speed_mpm: f64, actual_min: f64 };

fn bestWindow(segs: []const Segment, target_min: f64) ?WindowResult {
    const target_s = target_min * 60.0;
    var best_speed: f64 = 0;
    var best_min: f64 = 0;
    var found = false;

    var start_i: usize = 0;
    while (start_i < segs.len) : (start_i += 1) {
        const start_elapsed = segs[start_i].elapsed_s;
        const start_dist: f64 = if (start_i == 0) 0 else segs[start_i - 1].cum_dist_m;
        const target_elapsed = start_elapsed + target_s;

        var end_i: ?usize = null;
        var k: usize = segs.len;
        while (k > start_i) {
            k -= 1;
            if (segs[k].elapsed_s <= target_elapsed) { end_i = k; break; }
        }
        const ei = end_i orelse continue;
        if (ei <= start_i) continue;

        const actual_s = segs[ei].elapsed_s - start_elapsed;
        if (actual_s < target_s * 0.6) continue;
        const dist = segs[ei].cum_dist_m - start_dist;
        if (dist < 100.0) continue;

        const sp = dist / actual_s * 60.0;
        if (sp > best_speed) {
            best_speed = sp;
            best_min = actual_s / 60.0;
            found = true;
        }
    }
    return if (found) .{ .speed_mpm = best_speed, .actual_min = best_min } else null;
}

const PeakKm = struct {
    vo2_expressed: f64,
    vo2max_est: f64,    // NaN = none
    pace_min_per_km: f64,
    avg_grade_pct: f64,
    avg_hr: i32,        // -1 = none
    start_distance_km: f64,
};

fn best1KmVo2(allocator: std.mem.Allocator, segs: []const Segment, effective_max_hr: f64) !?PeakKm {
    if (segs.len < 2) return null;
    const vo2s = try allocator.alloc(f64, segs.len);
    defer allocator.free(vo2s);
    for (segs, 0..) |s, i| {
        const g = if (s.grade < 0) 0 else s.grade;
        vo2s[i] = fm.acsmVo2(s.speed_mpm, g);
    }

    var best: ?PeakKm = null;
    var right: usize = 0;
    var vo2_sum: f64 = 0;
    var hr_sum: i64 = 0;
    var hr_count: i32 = 0;

    var left: usize = 0;
    while (left < segs.len) : (left += 1) {
        const left_dist: f64 = if (left == 0) 0 else segs[left - 1].cum_dist_m;

        while (right < segs.len and segs[right].cum_dist_m - left_dist < 1000.0) {
            vo2_sum += vo2s[right];
            if (segs[right].hr >= 0) { hr_sum += segs[right].hr; hr_count += 1; }
            right += 1;
        }

        const right_idx = if (right > segs.len - 1) segs.len - 1 else right;
        const actual_dist = segs[right_idx].cum_dist_m - left_dist;
        if (actual_dist < 800.0) break;
        const window_len = right - left;
        if (window_len == 0) continue;

        const avg_vo2 = vo2_sum / @as(f64, @floatFromInt(window_len));
        const better = if (best) |b| (avg_vo2 > b.vo2_expressed) else true;
        if (better) {
            const left_elapsed: f64 = if (left == 0) 0 else segs[left - 1].elapsed_s;
            const elapsed = segs[right_idx].elapsed_s - left_elapsed;
            const sp: f64 = if (elapsed > 0) actual_dist / elapsed * 60.0 else 0.0;

            const left_ele = if (left == 0) segs[0].elevation_m else segs[left - 1].elevation_m;
            var net_ele: f64 = 0;
            if (!std.math.isNan(segs[right_idx].elevation_m) and !std.math.isNan(left_ele)) {
                net_ele = segs[right_idx].elevation_m - left_ele;
            }
            const avg_grade_pct = fm.roundTo(net_ele / actual_dist * 100.0, 1);

            const avg_hr: i32 = if (hr_count > 0)
                @intFromFloat(@round(@as(f64, @floatFromInt(hr_sum)) / @as(f64, @floatFromInt(hr_count))))
            else -1;

            var vo2max_est: f64 = std.math.nan(f64);
            if (effective_max_hr > 0 and avg_hr > 0) {
                const est = avg_vo2 * effective_max_hr / @as(f64, @floatFromInt(avg_hr));
                if (est > 15.0 and est < 120.0) vo2max_est = fm.roundTo(est, 1);
            }
            const pace = if (sp > 0) fm.roundTo(1000.0 / sp, 2) else 0.0;

            best = .{
                .vo2_expressed = fm.roundTo(avg_vo2, 1),
                .vo2max_est = vo2max_est,
                .pace_min_per_km = pace,
                .avg_grade_pct = avg_grade_pct,
                .avg_hr = avg_hr,
                .start_distance_km = fm.roundTo(left_dist / 1000.0, 2),
            };
        }

        vo2_sum -= vo2s[left];
        if (segs[left].hr >= 0) {
            const h: i64 = segs[left].hr;
            hr_sum = if (hr_sum >= h) hr_sum - h else 0;
            if (hr_count > 0) hr_count -= 1;
        }
    }
    return best;
}

const DriftPoint = struct { distance_km: f64, efficiency: f64 };
const DriftOut = struct { drift: []DriftPoint, decoupling: f64 };

fn computeDrift(allocator: std.mem.Allocator, segs: []const Segment) !DriftOut {
    const SKIP_S: f64 = 180.0;
    var raw_dist: std.ArrayList(f64) = .empty;
    defer raw_dist.deinit(allocator);
    var raw_eff: std.ArrayList(f64) = .empty;
    defer raw_eff.deinit(allocator);

    for (segs) |s| {
        if (s.elapsed_s <= SKIP_S or s.speed_mpm <= 50.0) continue;
        if (s.hr <= 50) continue;
        const eff = s.speed_mpm / @as(f64, @floatFromInt(s.hr));
        if (eff > 0) {
            try raw_dist.append(allocator, s.cum_dist_m);
            try raw_eff.append(allocator, eff);
        }
    }
    if (raw_dist.items.len < 20) return .{ .drift = &[_]DriftPoint{}, .decoupling = std.math.nan(f64) };

    var span: f64 = if (segs.len > 0) segs[segs.len - 1].elapsed_s else 1.0;
    if (span < 1.0) span = 1.0;
    const samples_per_sec = @as(f64, @floatFromInt(raw_dist.items.len)) / span;
    var win: i32 = @intFromFloat(@round(samples_per_sec * 600.0));
    win = fm.clampI(win, 5, 600);
    const raw_len_i32: i32 = @intCast(raw_dist.items.len);
    if (win > raw_len_i32) win = raw_len_i32;
    const half: i32 = @divFloor(win, 2);

    const smooth_dist = try allocator.alloc(f64, raw_dist.items.len);
    defer allocator.free(smooth_dist);
    const smooth_eff = try allocator.alloc(f64, raw_dist.items.len);
    defer allocator.free(smooth_eff);

    var i: i32 = 0;
    while (i < raw_len_i32) : (i += 1) {
        const s: i32 = if (i - half < 0) 0 else i - half;
        const e_end: i32 = if (i + half + 1 > raw_len_i32) raw_len_i32 else i + half + 1;
        var sum: f64 = 0;
        var k: i32 = s;
        while (k < e_end) : (k += 1) sum += raw_eff.items[@intCast(k)];
        smooth_dist[@intCast(i)] = raw_dist.items[@intCast(i)];
        smooth_eff[@intCast(i)] = sum / @as(f64, @floatFromInt(e_end - s));
    }

    const baseline = smooth_eff[0];
    if (baseline <= 0) return .{ .drift = &[_]DriftPoint{}, .decoupling = std.math.nan(f64) };

    var step: usize = smooth_eff.len / 300;
    if (step < 1) step = 1;
    var drift: std.ArrayList(DriftPoint) = .empty;
    errdefer drift.deinit(allocator);

    var di: usize = 0;
    while (di < smooth_eff.len) : (di += step) {
        try drift.append(allocator, .{
            .distance_km = fm.roundTo(smooth_dist[di] / 1000.0, 2),
            .efficiency = fm.roundTo(smooth_eff[di] / baseline, 3),
        });
    }

    const mid = smooth_eff.len / 2;
    var m1: f64 = 0;
    var m2: f64 = 0;
    var j: usize = 0;
    while (j < mid) : (j += 1) m1 += smooth_eff[j];
    j = mid;
    while (j < smooth_eff.len) : (j += 1) m2 += smooth_eff[j];
    m1 /= @as(f64, @floatFromInt(mid));
    m2 /= @as(f64, @floatFromInt(smooth_eff.len - mid));
    const decoupling = if (m1 > 0) fm.roundTo((m1 - m2) / m1 * 100.0, 1) else std.math.nan(f64);

    return .{ .drift = try drift.toOwnedSlice(allocator), .decoupling = decoupling };
}

const DescentPoint = struct { grade_pct: f64, speed_kmh: f64, progress: f64 };

fn computeDescentPoints(allocator: std.mem.Allocator, segs: []const Segment) ![]DescentPoint {
    var total: f64 = if (segs.len > 0) segs[segs.len - 1].cum_dist_m else 1.0;
    if (total < 1.0) total = 1.0;

    var pts: std.ArrayList(DescentPoint) = .empty;
    errdefer pts.deinit(allocator);

    for (segs) |s| {
        if (s.grade >= -0.03 or s.speed_mpm <= 30.0) continue;
        const speed_kmh = s.speed_mpm * 60.0 / 1000.0;
        if (speed_kmh < 0.5 or speed_kmh > 35.0) continue;
        const grade_pct = fm.roundTo(s.grade * 100.0, 1);
        if (grade_pct < -50.0) continue;
        try pts.append(allocator, .{
            .grade_pct = grade_pct,
            .speed_kmh = fm.roundTo(speed_kmh, 2),
            .progress = fm.roundTo(s.cum_dist_m / total, 3),
        });
    }

    if (pts.items.len <= 2000) return pts.toOwnedSlice(allocator);
    const step = pts.items.len / 2000;
    var sampled: std.ArrayList(DescentPoint) = .empty;
    errdefer sampled.deinit(allocator);
    var idx: usize = 0;
    while (idx < pts.items.len) : (idx += step) try sampled.append(allocator, pts.items[idx]);
    pts.deinit(allocator);
    return sampled.toOwnedSlice(allocator);
}

const Estimate = struct {
    method: []const u8,    // owned by `method_buf` arena
    value: f64,
    confidence_pct: i32,
    notes: []const u8,     // owned by `notes_buf` arena
};

const Result = struct {
    total_distance_km: f64,
    total_duration_min: f64,
    avg_pace_min_per_km: f64,
    elevation_gain_m: f64,
    avg_hr: f64,            // NaN = none
    max_hr_recorded: i32,   // -1 = none
    has_hr_data: bool,
    has_elevation_data: bool,
    has_time_data: bool,
    point_count: usize,
    estimates: []Estimate,
    fitness_category: []const u8,    // empty = none
    fitness_description: []const u8,
    peak_1km: ?PeakKm,
    chart_points: []ChartPoint,
    cardiac_drift: []DriftPoint,
    decoupling_pct: f64,
    descent_points: []DescentPoint,
    error_msg: []const u8,           // empty = no error
};

const ChartPoint = struct {
    distance_km: f64,
    pace_min_per_km: f64,   // NaN = none
    hr: i32,
    elevation_m: f64,
};

fn paceFormat(buf: []u8, pace_km: f64) ![]const u8 {
    const pace_min: u32 = @intFromFloat(pace_km);
    const pace_sec: u32 = @intFromFloat((pace_km - @as(f64, @floatFromInt(pace_min))) * 60.0);
    return std.fmt.bufPrint(buf, "{d}:{d:0>2}", .{ pace_min, pace_sec });
}

fn analyze(allocator: std.mem.Allocator, points: []const TrackPoint, _: f64, max_hr_input: i32) !Result {
    if (points.len < 10) {
        var buf: [128]u8 = undefined;
        const msg = try std.fmt.bufPrint(&buf, "Not enough track points (found {d}, need at least 10). Is this a valid GPX file?", .{points.len});
        const owned = try allocator.dupe(u8, msg);
        return Result{
            .total_distance_km = 0, .total_duration_min = 0,
            .avg_pace_min_per_km = 0, .elevation_gain_m = 0,
            .avg_hr = std.math.nan(f64), .max_hr_recorded = -1,
            .has_hr_data = false, .has_elevation_data = false, .has_time_data = false,
            .point_count = points.len,
            .estimates = &[_]Estimate{},
            .fitness_category = "", .fitness_description = "",
            .peak_1km = null,
            .chart_points = &[_]ChartPoint{},
            .cardiac_drift = &[_]DriftPoint{},
            .decoupling_pct = std.math.nan(f64),
            .descent_points = &[_]DescentPoint{},
            .error_msg = owned,
        };
    }

    const segs = try buildSegments(allocator, points);
    defer allocator.free(segs);

    const total_dist_m: f64 = if (segs.len > 0) segs[segs.len - 1].cum_dist_m else 0;
    const total_dist_km = total_dist_m / 1000.0;

    var has_time = false;
    for (points) |p| { if (!std.math.isNan(p.time_s)) { has_time = true; break; } }
    var total_duration_s: f64 = @floatFromInt(segs.len);
    if (has_time) {
        var start_t: f64 = std.math.nan(f64);
        var end_t: f64 = std.math.nan(f64);
        for (points) |p| { if (!std.math.isNan(p.time_s)) { start_t = p.time_s; break; } }
        var i = points.len;
        while (i > 0) {
            i -= 1;
            if (!std.math.isNan(points[i].time_s)) { end_t = points[i].time_s; break; }
        }
        if (!std.math.isNan(start_t) and !std.math.isNan(end_t) and end_t > start_t) total_duration_s = end_t - start_t;
    }
    const total_duration_min = total_duration_s / 60.0;
    const avg_pace = if (total_dist_km > 0) total_duration_min / total_dist_km else 0;

    var has_elevation = false;
    for (points) |p| { if (!std.math.isNan(p.ele)) { has_elevation = true; break; } }
    var elevation_gain: f64 = 0;
    var ei: usize = 1;
    while (ei < segs.len) : (ei += 1) {
        const a = segs[ei - 1].elevation_m;
        const b = segs[ei].elevation_m;
        if (!std.math.isNan(a) and !std.math.isNan(b) and b > a) elevation_gain += b - a;
    }

    var hr_count: usize = 0;
    var hr_sum_f: f64 = 0;
    var max_hr: i32 = 0;
    for (points) |p| {
        if (p.hr >= 0) {
            hr_count += 1;
            hr_sum_f += @floatFromInt(p.hr);
            if (p.hr > max_hr) max_hr = p.hr;
        }
    }
    const has_hr = hr_count > 0;
    const avg_hr_f: f64 = if (has_hr) hr_sum_f / @as(f64, @floatFromInt(hr_count)) else std.math.nan(f64);
    const max_hr_recorded: i32 = if (has_hr) max_hr else -1;

    var effective_max_hr: f64 = 0;
    if (max_hr_input > 0) effective_max_hr = @floatFromInt(max_hr_input)
    else if (max_hr_recorded >= 0) effective_max_hr = @as(f64, @floatFromInt(max_hr_recorded)) * 1.05;

    var cardiac_drift: []DriftPoint = &[_]DriftPoint{};
    var decoupling_pct: f64 = std.math.nan(f64);
    if (has_hr and has_time) {
        const dr = try computeDrift(allocator, segs);
        cardiac_drift = dr.drift;
        decoupling_pct = dr.decoupling;
    }
    const descent_points: []DescentPoint = if (has_time and has_elevation)
        try computeDescentPoints(allocator, segs)
    else
        &[_]DescentPoint{};

    var estimates: std.ArrayList(Estimate) = .empty;
    errdefer estimates.deinit(allocator);

    // Method 1: ACSM + HR
    if (has_hr and effective_max_hr > 0) {
        const SKIP_S: f64 = 180.0;
        const hr_lo = effective_max_hr * 0.65;
        const hr_hi = effective_max_hr * 0.97;
        var samples: std.ArrayList(f64) = .empty;
        defer samples.deinit(allocator);
        for (segs) |s| {
            if (s.elapsed_s <= SKIP_S or s.speed_mpm <= 80.0 or s.hr < 0) continue;
            const hf: f64 = @floatFromInt(s.hr);
            if (hf < hr_lo or hf > hr_hi) continue;
            const est = fm.acsmVo2(s.speed_mpm, s.grade) * effective_max_hr / hf;
            if (est > 15.0 and est < 110.0) try samples.append(allocator, est);
        }
        if (samples.items.len >= 5) {
            std.mem.sort(f64, samples.items, {}, comptime std.sort.asc(f64));
            const median = samples.items[samples.items.len / 2];
            var mean: f64 = 0;
            for (samples.items) |x| mean += x;
            mean /= @floatFromInt(samples.items.len);
            var variance: f64 = 0;
            for (samples.items) |x| { const d = x - mean; variance += d * d; }
            variance /= @floatFromInt(samples.items.len);
            const cv = @sqrt(variance) / mean;
            const count_score = @min(@as(f64, @floatFromInt(samples.items.len)) / 100.0, 1.0);
            const cv_penalty = @min(cv / 0.25, 1.0) * 15.0;
            const conf_raw: i32 = @intFromFloat(@round(45.0 + count_score * 40.0 - cv_penalty));
            const confidence = fm.clampI(conf_raw, 20, 85);
            const notes = try std.fmt.allocPrint(
                allocator,
                "Uses ACSM running metabolic equation and HR/HRmax linearity. Based on {d} steady-state data points (HR 65–97% of max, after first 3 min).",
                .{samples.items.len},
            );
            const method = try allocator.dupe(u8, "ACSM + Heart Rate (Steady-State)");
            try estimates.append(allocator, .{
                .method = method,
                .value = fm.roundTo(median, 1),
                .confidence_pct = confidence,
                .notes = notes,
            });
        }
    }

    // Method 2: Jack Daniels — best effort windows
    if (has_time and total_duration_min > 2.0) {
        const have_effort = has_hr and effective_max_hr > 0 and !std.math.isNan(avg_hr_f);
        const effort_pct: f64 = if (have_effort) avg_hr_f / effective_max_hr else std.math.nan(f64);

        const targets = [_]f64{ 20.0, 30.0, 10.0, 60.0, 5.0 };
        var added = false;
        for (targets) |dur| {
            if (total_duration_min < dur * 0.6) continue;
            const w = bestWindow(segs, dur) orelse continue;
            const vo2_max = fm.danielsVo2Max(w.speed_mpm, w.actual_min);
            if (std.math.isNan(vo2_max) or vo2_max <= 15.0 or vo2_max >= 110.0) continue;

            const pace_km: f64 = if (w.speed_mpm > 0) 1000.0 / w.speed_mpm else 0.0;
            const dur_boost = @min(dur / 60.0, 1.0);

            var confidence: i32 = 0;
            var note_buf: [512]u8 = undefined;
            var effort_note_slice: []const u8 = undefined;

            if (have_effort and effort_pct < 0.75) {
                confidence = fm.clampI(@intFromFloat(@round(20.0 + dur_boost * 20.0)), 15, 35);
                effort_note_slice = try std.fmt.bufPrint(
                    &note_buf,
                    "Avg HR was only {d:.0}% of max — this was an easy/Zone 2 run. Daniels assumes race-like effort, so this result will significantly underestimate your actual VO2 max.",
                    .{effort_pct * 100.0},
                );
            } else if (have_effort and effort_pct < 0.85) {
                confidence = fm.clampI(@intFromFloat(@round(20.0 + dur_boost * 40.0)), 25, 55);
                effort_note_slice = try std.fmt.bufPrint(
                    &note_buf,
                    "Avg HR was {d:.0}% of max — a moderate effort. Result will likely underestimate your VO2 max; most accurate when run at race or threshold intensity.",
                    .{effort_pct * 100.0},
                );
            } else if (have_effort) {
                confidence = fm.clampI(@intFromFloat(@round(20.0 + dur_boost * 52.0)), 35, 72);
                effort_note_slice = try std.fmt.bufPrint(
                    &note_buf,
                    "Avg HR was {d:.0}% of max — a hard effort. Result is reasonably accurate; may slightly overestimate if HR was elevated by heat or fatigue rather than pure intensity.",
                    .{effort_pct * 100.0},
                );
            } else {
                confidence = fm.clampI(@intFromFloat(@round(20.0 + dur_boost * 35.0)), 20, 55);
                effort_note_slice = try std.fmt.bufPrint(
                    &note_buf,
                    "No HR data — cannot assess effort level. Result is only accurate if this was a race or near-maximal effort.",
                    .{},
                );
            }

            var pace_buf: [16]u8 = undefined;
            const pace_str = try paceFormat(&pace_buf, pace_km);
            const dur_int: u32 = @intFromFloat(dur);
            const method = try std.fmt.allocPrint(allocator, "Jack Daniels — Best {d}-min Effort", .{dur_int});
            const notes = try std.fmt.allocPrint(
                allocator,
                "Based on your fastest {d}-minute segment (avg pace {s} /km). {s}",
                .{ dur_int, pace_str, effort_note_slice },
            );
            try estimates.append(allocator, .{
                .method = method,
                .value = fm.roundTo(vo2_max, 1),
                .confidence_pct = confidence,
                .notes = notes,
            });
            added = true;
            break;
        }

        if (!added and total_dist_km > 0.5) {
            const whole_speed = total_dist_m / total_duration_s * 60.0;
            const vo2_max = fm.danielsVo2Max(whole_speed, total_duration_min);
            if (!std.math.isNan(vo2_max) and vo2_max > 15.0 and vo2_max < 110.0) {
                const method = try allocator.dupe(u8, "Jack Daniels — Whole Activity");
                const notes = try allocator.dupe(u8, "Based on average pace across the entire activity. Assumes race-like effort throughout — almost always an underestimate for training runs.");
                try estimates.append(allocator, .{
                    .method = method,
                    .value = fm.roundTo(vo2_max, 1),
                    .confidence_pct = 18,
                    .notes = notes,
                });
            }
        }
    }

    // Method 3: Firstbeat regression
    if (has_hr and effective_max_hr > 0) {
        const SKIP_S: f64 = 180.0;
        const hr_lo = effective_max_hr * 0.50;
        const hr_hi = effective_max_hr * 0.97;
        var pairs_hr: std.ArrayList(f64) = .empty;
        defer pairs_hr.deinit(allocator);
        var pairs_vo2: std.ArrayList(f64) = .empty;
        defer pairs_vo2.deinit(allocator);

        for (segs) |s| {
            if (s.elapsed_s <= SKIP_S or s.speed_mpm <= 80.0 or s.hr < 0) continue;
            const hf: f64 = @floatFromInt(s.hr);
            if (hf < hr_lo or hf > hr_hi) continue;
            const vo2 = fm.acsmVo2(s.speed_mpm, s.grade);
            if (vo2 > 0) {
                try pairs_hr.append(allocator, hf);
                try pairs_vo2.append(allocator, vo2);
            }
        }
        if (pairs_hr.items.len >= 10) {
            const n: f64 = @floatFromInt(pairs_hr.items.len);
            var sum_x: f64 = 0; var sum_y: f64 = 0; var sum_xy: f64 = 0; var sum_xx: f64 = 0;
            for (pairs_hr.items, pairs_vo2.items) |x, y| {
                sum_x += x; sum_y += y; sum_xy += x * y; sum_xx += x * x;
            }
            const denom = n * sum_xx - sum_x * sum_x;
            if (@abs(denom) > 1e-10) {
                const m = (n * sum_xy - sum_x * sum_y) / denom;
                const b = (sum_y - m * sum_x) / n;
                const vo2_max = m * effective_max_hr + b;
                if (m > 0 and vo2_max > 15.0 and vo2_max < 110.0) {
                    const y_mean = sum_y / n;
                    var ss_res: f64 = 0; var ss_tot: f64 = 0;
                    for (pairs_hr.items, pairs_vo2.items) |x, y| {
                        const r = y - (m * x + b);
                        ss_res += r * r;
                        const d = y - y_mean;
                        ss_tot += d * d;
                    }
                    const r_squared: f64 = if (ss_tot > 1e-10) @max(0.0, 1.0 - ss_res / ss_tot) else 0.0;
                    const conf_raw: i32 = @intFromFloat(@round(30.0 + r_squared * 53.0));
                    const confidence = fm.clampI(conf_raw, 20, 83);
                    const method = try allocator.dupe(u8, "Firstbeat (HR–VO2 Regression)");
                    const notes = try std.fmt.allocPrint(
                        allocator,
                        "Fits a linear regression across {d} steady-state (HR, VO2) pairs and extrapolates to HRmax (R² = {d:.2}). More robust than the point-by-point method as it uses all data together.",
                        .{ pairs_hr.items.len, r_squared },
                    );
                    try estimates.append(allocator, .{
                        .method = method,
                        .value = fm.roundTo(vo2_max, 1),
                        .confidence_pct = confidence,
                        .notes = notes,
                    });
                }
            }
        }
    }

    var fitness_cat: []const u8 = "";
    var fitness_desc: []const u8 = "";
    if (estimates.items.len > 0) {
        var best_idx: usize = 0;
        var idx: usize = 1;
        while (idx < estimates.items.len) : (idx += 1) {
            if (estimates.items[idx].confidence_pct > estimates.items[best_idx].confidence_pct) best_idx = idx;
        }
        const fc = fm.fitnessCategory(estimates.items[best_idx].value);
        fitness_cat = fc.name;
        fitness_desc = fc.description;
    }

    var step: usize = segs.len / 500;
    if (step < 1) step = 1;
    var chart: std.ArrayList(ChartPoint) = .empty;
    errdefer chart.deinit(allocator);
    var ci: usize = 0;
    while (ci < segs.len) : (ci += step) {
        const s = segs[ci];
        try chart.append(allocator, .{
            .distance_km = fm.roundTo(s.cum_dist_m / 1000.0, 2),
            .pace_min_per_km = if (s.speed_mpm > 10.0) fm.roundTo(1000.0 / s.speed_mpm, 2) else std.math.nan(f64),
            .hr = s.hr,
            .elevation_m = s.elevation_m,
        });
    }

    const peak = if (has_time) try best1KmVo2(allocator, segs, effective_max_hr) else null;

    return Result{
        .total_distance_km = fm.roundTo(total_dist_km, 2),
        .total_duration_min = fm.roundTo(total_duration_min, 1),
        .avg_pace_min_per_km = fm.roundTo(avg_pace, 2),
        .elevation_gain_m = @round(elevation_gain),
        .avg_hr = avg_hr_f,
        .max_hr_recorded = max_hr_recorded,
        .has_hr_data = has_hr,
        .has_elevation_data = has_elevation,
        .has_time_data = has_time,
        .point_count = points.len,
        .estimates = try estimates.toOwnedSlice(allocator),
        .fitness_category = fitness_cat,
        .fitness_description = fitness_desc,
        .peak_1km = peak,
        .chart_points = try chart.toOwnedSlice(allocator),
        .cardiac_drift = cardiac_drift,
        .decoupling_pct = decoupling_pct,
        .descent_points = descent_points,
        .error_msg = "",
    };
}

fn freeResult(allocator: std.mem.Allocator, r: *Result) void {
    for (r.estimates) |e| {
        allocator.free(e.method);
        allocator.free(e.notes);
    }
    allocator.free(r.estimates);
    allocator.free(r.chart_points);
    allocator.free(r.cardiac_drift);
    allocator.free(r.descent_points);
    if (r.error_msg.len > 0) allocator.free(r.error_msg);
}

fn serializeResult(allocator: std.mem.Allocator, r: Result) ![]u8 {
    var buf: std.ArrayList(u8) = .empty;
    errdefer buf.deinit(allocator);
    try buf.ensureTotalCapacity(allocator, 4096);
    var w = JsonWriter{ .buf = &buf, .allocator = allocator };

    try w.push("{\"total_distance_km\":"); try w.writeNumber(r.total_distance_km);
    try w.push(",\"total_duration_min\":"); try w.writeNumber(r.total_duration_min);
    try w.push(",\"avg_pace_min_per_km\":"); try w.writeNumber(r.avg_pace_min_per_km);
    try w.push(",\"elevation_gain_m\":"); try w.writeNumber(r.elevation_gain_m);
    try w.push(",\"avg_hr\":"); try w.writeNumberOrNull(r.avg_hr);
    try w.push(",\"max_hr_recorded\":"); try w.writeIntOrNull(r.max_hr_recorded);
    try w.push(",\"has_hr_data\":"); try w.push(if (r.has_hr_data) "true" else "false");
    try w.push(",\"has_elevation_data\":"); try w.push(if (r.has_elevation_data) "true" else "false");
    try w.push(",\"has_time_data\":"); try w.push(if (r.has_time_data) "true" else "false");
    try w.push(",\"point_count\":"); try w.writeInt(@intCast(r.point_count));

    try w.push(",\"estimates\":[");
    for (r.estimates, 0..) |e, i| {
        if (i > 0) try w.pushChar(',');
        try w.push("{\"method\":"); try w.writeString(e.method);
        try w.push(",\"value\":"); try w.writeNumber(e.value);
        try w.push(",\"confidence_pct\":"); try w.writeInt(@intCast(e.confidence_pct));
        try w.push(",\"notes\":"); try w.writeString(e.notes);
        try w.pushChar('}');
    }
    try w.pushChar(']');

    try w.push(",\"fitness_category\":");
    if (r.fitness_category.len > 0) try w.writeString(r.fitness_category) else try w.push("null");
    try w.push(",\"fitness_description\":");
    if (r.fitness_description.len > 0) try w.writeString(r.fitness_description) else try w.push("null");

    try w.push(",\"peak_1km\":");
    if (r.peak_1km) |p| {
        try w.push("{\"vo2_expressed\":"); try w.writeNumber(p.vo2_expressed);
        try w.push(",\"vo2max_est\":"); try w.writeNumberOrNull(p.vo2max_est);
        try w.push(",\"pace_min_per_km\":"); try w.writeNumber(p.pace_min_per_km);
        try w.push(",\"avg_grade_pct\":"); try w.writeNumber(p.avg_grade_pct);
        try w.push(",\"avg_hr\":"); try w.writeIntOrNull(p.avg_hr);
        try w.push(",\"start_distance_km\":"); try w.writeNumber(p.start_distance_km);
        try w.pushChar('}');
    } else try w.push("null");

    try w.push(",\"chart_points\":[");
    for (r.chart_points, 0..) |c, i| {
        if (i > 0) try w.pushChar(',');
        try w.push("{\"distance_km\":"); try w.writeNumber(c.distance_km);
        try w.push(",\"pace_min_per_km\":"); try w.writeNumberOrNull(c.pace_min_per_km);
        try w.push(",\"hr\":"); try w.writeIntOrNull(c.hr);
        try w.push(",\"elevation_m\":"); try w.writeNumberOrNull(c.elevation_m);
        try w.pushChar('}');
    }
    try w.pushChar(']');

    try w.push(",\"cardiac_drift\":[");
    for (r.cardiac_drift, 0..) |d, i| {
        if (i > 0) try w.pushChar(',');
        try w.push("{\"distance_km\":"); try w.writeNumber(d.distance_km);
        try w.push(",\"efficiency\":"); try w.writeNumber(d.efficiency);
        try w.pushChar('}');
    }
    try w.pushChar(']');

    try w.push(",\"decoupling_pct\":"); try w.writeNumberOrNull(r.decoupling_pct);

    try w.push(",\"descent_points\":[");
    for (r.descent_points, 0..) |d, i| {
        if (i > 0) try w.pushChar(',');
        try w.push("{\"grade_pct\":"); try w.writeNumber(d.grade_pct);
        try w.push(",\"speed_kmh\":"); try w.writeNumber(d.speed_kmh);
        try w.push(",\"progress\":"); try w.writeNumber(d.progress);
        try w.pushChar('}');
    }
    try w.pushChar(']');

    try w.push(",\"error\":");
    if (r.error_msg.len > 0) try w.writeString(r.error_msg) else try w.push("null");
    try w.pushChar('}');

    return buf.toOwnedSlice(allocator);
}

pub fn run(allocator: std.mem.Allocator, input: []const u8, weight_kg: f64, max_hr: i32) ![]u8 {
    var points = gpx.parseGpx(allocator, input) catch |err| {
        const msg = switch (err) {
            error.UnterminatedCdata => "Failed to parse GPX: Unterminated CDATA section",
            error.UnterminatedComment => "Failed to parse GPX: Unterminated comment",
            error.TooManyPoints => "Failed to parse GPX: file exceeds maximum track points",
            error.OutOfMemory => return err,
        };
        return try std.fmt.allocPrint(
            allocator,
            "{{\"error\":\"{s}\",\"estimates\":[],\"chart_points\":[],\"cardiac_drift\":[],\"descent_points\":[]}}",
            .{msg},
        );
    };
    defer points.deinit(allocator);

    var result = try analyze(allocator, points.items, weight_kg, max_hr);
    defer freeResult(allocator, &result);
    return try serializeResult(allocator, result);
}
