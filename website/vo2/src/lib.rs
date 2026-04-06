use wasm_bindgen::prelude::*;
use serde::Serialize;

// ============================================================
// DATA TYPES
// ============================================================

#[derive(Debug, Clone)]
struct TrackPoint {
    lat: f64,
    lon: f64,
    ele: Option<f64>,
    time_s: Option<f64>, // seconds since 2000-01-01
    hr: Option<u32>,
}

#[derive(Debug, Serialize, Clone)]
pub(crate) struct ChartPoint {
    pub(crate) distance_km: f64,
    pub(crate) pace_min_per_km: Option<f64>,
    pub(crate) hr: Option<u32>,
    pub(crate) elevation_m: Option<f64>,
}

#[derive(Debug, Serialize)]
pub(crate) struct Vo2Estimate {
    pub(crate) method: String,
    pub(crate) value: f64,
    pub(crate) confidence_pct: u32, // 0–100, data-driven
    pub(crate) notes: String,
}

#[derive(Debug, Serialize)]
pub(crate) struct PeakKmResult {
    /// Average VO2 expressed over the km (mL/kg/min) — what your body actually consumed,
    /// grade-corrected via ACSM. Not the same as VO2max unless you genuinely hit your ceiling.
    pub(crate) vo2_expressed: f64,
    /// VO2max implied by extrapolating avg HR to HRmax over this km. Only present when HR
    /// data is available. May be unreliable if the km was very short or HR hadn't settled.
    pub(crate) vo2max_est: Option<f64>,
    pub(crate) pace_min_per_km: f64,
    /// Net gradient as a percentage (positive = uphill, negative = downhill).
    pub(crate) avg_grade_pct: f64,
    pub(crate) avg_hr: Option<u32>,
    /// Cumulative distance at the start of this km.
    pub(crate) start_distance_km: f64,
}

#[derive(Debug, Serialize)]
pub(crate) struct AnalysisResult {
    pub(crate) total_distance_km: f64,
    pub(crate) total_duration_min: f64,
    pub(crate) avg_pace_min_per_km: f64,
    pub(crate) elevation_gain_m: f64,
    pub(crate) avg_hr: Option<f64>,
    pub(crate) max_hr_recorded: Option<u32>,
    pub(crate) has_hr_data: bool,
    pub(crate) has_elevation_data: bool,
    pub(crate) has_time_data: bool,
    pub(crate) point_count: usize,
    pub(crate) estimates: Vec<Vo2Estimate>,
    pub(crate) fitness_category: Option<String>,
    pub(crate) fitness_description: Option<String>,
    pub(crate) peak_1km: Option<PeakKmResult>,
    pub(crate) chart_points: Vec<ChartPoint>,
    pub(crate) error: Option<String>,
}

// ============================================================
// TIMESTAMP PARSING
// ============================================================

fn is_leap(year: i64) -> bool {
    (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
}

fn parse_timestamp(s: &str) -> Option<f64> {
    // Handles: 2024-01-15T10:30:00Z  or  2024-01-15T10:30:00.000Z
    let s = s.trim().trim_end_matches('Z');
    let (date_str, time_str) = s.split_once('T')?;

    let mut dp = date_str.split('-');
    let year: i64 = dp.next()?.parse().ok()?;
    let month: i64 = dp.next()?.parse().ok()?;
    let day: i64 = dp.next()?.parse().ok()?;

    let time_str = time_str.split('.').next().unwrap_or(time_str);
    let mut tp = time_str.split(':');
    let hour: f64 = tp.next()?.parse().ok()?;
    let min: f64 = tp.next()?.parse().ok()?;
    let sec: f64 = tp.next()?.parse().ok()?;

    // Validate ranges to avoid panics on malformed timestamps
    if month < 1 || month > 12 || day < 1 || day > 31 {
        return None;
    }
    if hour < 0.0 || hour > 23.0 || min < 0.0 || min > 59.0 || sec < 0.0 || sec > 59.0 {
        return None;
    }

    // Days since 2000-01-01
    let mut days: i64 = 0;
    for y in 2000..year {
        days += if is_leap(y) { 366 } else { 365 };
    }
    let month_days: [i64; 12] = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
    for m in 0..(month - 1) as usize {
        days += month_days[m];
        if m == 1 && is_leap(year) {
            days += 1;
        }
    }
    days += day - 1;

    Some(days as f64 * 86400.0 + hour * 3600.0 + min * 60.0 + sec)
}

// ============================================================
// GPX PARSING  (hand-rolled to avoid version-specific quick-xml quirks)
// ============================================================

fn parse_attr_f64(tag: &str, attr: &str) -> Option<f64> {
    let pat = format!("{}=\"", attr);
    let start = tag.find(pat.as_str())? + pat.len();
    let rest = &tag[start..];
    let end = rest.find('"')?;
    rest[..end].parse().ok()
}

fn local_tag_name(tag: &str) -> &str {
    if tag.starts_with('/') || tag.starts_with('!') || tag.starts_with('?') {
        return "";
    }
    let name = tag.split_whitespace().next().unwrap_or(tag);
    // strip self-close slash
    let name = name.trim_end_matches('/');
    // strip namespace prefix
    name.split(':').last().unwrap_or(name)
}

fn parse_gpx(content: &str) -> Result<Vec<TrackPoint>, String> {
    let mut points: Vec<TrackPoint> = Vec::new();
    let mut current: Option<TrackPoint> = None;

    // Work entirely on bytes to avoid slicing mid-codepoint.
    // Only slice back into `content` when we know we're at ASCII `<` or `>` boundaries.
    let bytes = content.as_bytes();
    let mut i = 0usize;

    while i < bytes.len() {
        if bytes[i] == b'<' {
            let tag_start = i + 1;
            // Handle CDATA — skip to ]]>
            if bytes.get(tag_start..tag_start + 8) == Some(b"![CDATA[") {
                if let Some(off) = content[i..].find("]]>") {
                    i += off + 3;
                } else {
                    return Err("Unterminated CDATA section".to_string());
                }
                continue;
            }
            // Handle comments — skip to -->
            if bytes.get(tag_start..tag_start + 3) == Some(b"!--") {
                if let Some(off) = content[i..].find("-->") {
                    i += off + 3;
                } else {
                    return Err("Unterminated comment".to_string());
                }
                continue;
            }
            // Find closing '>' by scanning bytes (safe: '>' is single-byte ASCII)
            let mut j = tag_start;
            while j < bytes.len() && bytes[j] != b'>' {
                j += 1;
            }
            if j >= bytes.len() {
                break;
            }
            // Safe to slice: '<' and '>' are single-byte ASCII and cannot appear as
            // continuation bytes in multibyte UTF-8, so byte-scanning for them always
            // lands on valid UTF-8 boundaries.
            let tag = &content[tag_start..j];
            i = j + 1;

            let local = local_tag_name(tag);

            if local == "trkpt" {
                let lat = parse_attr_f64(tag, "lat");
                let lon = parse_attr_f64(tag, "lon");
                if let (Some(lat), Some(lon)) = (lat, lon) {
                    current = Some(TrackPoint { lat, lon, ele: None, time_s: None, hr: None });
                }
            } else if tag.starts_with("/trkpt") {
                if let Some(pt) = current.take() {
                    points.push(pt);
                }
            } else if current.is_some() && !local.is_empty() {
                // Opening a child element — read its text content immediately
                match local {
                    "ele" | "time" | "hr" => {
                        // read text until next '<' (scanning bytes — safe)
                        let text_start = i;
                        while i < bytes.len() && bytes[i] != b'<' {
                            i += 1;
                        }
                        let text = &content[text_start..i];
                        let text = text.trim();
                        if !text.is_empty() {
                            if let Some(ref mut pt) = current {
                                match local {
                                    "ele" => pt.ele = text.parse().ok(),
                                    "time" => pt.time_s = parse_timestamp(text),
                                    "hr" => pt.hr = text.parse().ok(),
                                    _ => {}
                                }
                            }
                        }
                    }
                    _ => {}
                }
            }
        } else {
            i += 1;
        }
    }

    Ok(points)
}

// ============================================================
// GEO CALCULATIONS
// ============================================================

fn haversine(lat1: f64, lon1: f64, lat2: f64, lon2: f64) -> f64 {
    let r = 6_371_000.0_f64;
    let dlat = (lat2 - lat1).to_radians();
    let dlon = (lon2 - lon1).to_radians();
    let a = (dlat / 2.0).sin().powi(2)
        + lat1.to_radians().cos() * lat2.to_radians().cos() * (dlon / 2.0).sin().powi(2);
    let c = 2.0 * a.sqrt().atan2((1.0 - a).sqrt());
    r * c
}

// ============================================================
// VO2 MAX FORMULAS
// ============================================================

/// ACSM Running Metabolic Equation
/// VO2 (mL/kg/min) = 0.2 × S + 0.9 × S × G + 3.5
/// S = speed in m/min, G = fractional grade
///
/// @todo Running economy correction: the 0.2 horizontal cost coefficient assumes
/// population-average running economy. With multiple runs at different paces we
/// could regress the individual's actual oxygen cost per metre and substitute it
/// here, meaningfully improving accuracy for method 1 and the Firstbeat method.
fn acsm_vo2(speed_mpm: f64, grade: f64) -> f64 {
    0.2 * speed_mpm + 0.9 * speed_mpm * grade + 3.5
}

/// Jack Daniels / Gilbert performance formula
/// V = velocity in m/min, t = time in minutes at that effort
/// Returns estimated VO2max in mL/kg/min
fn daniels_vo2max(velocity_mpm: f64, time_min: f64) -> Option<f64> {
    if velocity_mpm < 60.0 || time_min < 1.0 {
        return None;
    }
    let pct_vo2max = 0.8
        + 0.1894393 * (-0.012778 * time_min).exp()
        + 0.2989558 * (-0.1932605 * time_min).exp();
    let vo2 = -4.60 + 0.182258 * velocity_mpm + 0.000104 * velocity_mpm * velocity_mpm;
    if pct_vo2max <= 0.0 || vo2 <= 0.0 {
        return None;
    }
    Some(vo2 / pct_vo2max)
}

fn fitness_category(vo2max: f64) -> (&'static str, &'static str) {
    if vo2max < 30.0 {
        ("Poor", "Below average cardiovascular fitness")
    } else if vo2max < 40.0 {
        ("Fair", "Average cardiovascular fitness")
    } else if vo2max < 50.0 {
        ("Good", "Above average cardiovascular fitness")
    } else if vo2max < 60.0 {
        ("Excellent", "High cardiovascular fitness")
    } else if vo2max < 75.0 {
        ("Superior", "Very high cardiovascular fitness")
    } else {
        ("Elite", "Elite-level cardiovascular fitness")
    }
}

// ============================================================
// ANALYSIS
// ============================================================

struct Segment {
    cum_dist_m: f64,   // cumulative distance from start
    elapsed_s: f64,    // elapsed seconds from start
    speed_mpm: f64,    // speed in m/min for this segment
    grade: f64,        // fractional grade
    hr: Option<u32>,
    elevation_m: Option<f64>,
}

fn build_segments(points: &[TrackPoint]) -> Vec<Segment> {
    let base_time = points.iter().find_map(|p| p.time_s);
    let mut cum_dist = 0.0_f64;
    let mut segments = Vec::with_capacity(points.len());

    for i in 1..points.len() {
        let prev = &points[i - 1];
        let curr = &points[i];

        let dist = haversine(prev.lat, prev.lon, curr.lat, curr.lon);
        let ele_diff = match (curr.ele, prev.ele) {
            (Some(e2), Some(e1)) => e2 - e1,
            _ => 0.0,
        };

        let dt = match (curr.time_s, prev.time_s) {
            (Some(t2), Some(t1)) if t2 > t1 => t2 - t1,
            _ => 1.0,
        };

        cum_dist += dist;

        // Clamp grade to ±50% and ignore noise on very short segments
        let grade = if dist > 0.5 {
            (ele_diff / dist).clamp(-0.50, 0.50)
        } else {
            0.0
        };

        let speed_mpm = (dist / dt) * 60.0;

        let elapsed = match (curr.time_s, base_time) {
            (Some(t), Some(b)) => t - b,
            _ => i as f64,
        };

        segments.push(Segment {
            cum_dist_m: cum_dist,
            elapsed_s: elapsed,
            speed_mpm,
            grade,
            hr: curr.hr,
            elevation_m: curr.ele,
        });
    }
    segments
}

/// Find best average speed (m/min) over any window of `target_min` minutes.
/// Returns (best_speed_mpm, actual_duration_min).
fn best_window(segments: &[Segment], target_min: f64) -> Option<(f64, f64)> {
    let target_s = target_min * 60.0;
    let mut best: Option<(f64, f64)> = None;

    for start_i in 0..segments.len() {
        let start_elapsed = segments[start_i].elapsed_s;
        let start_dist = if start_i == 0 {
            0.0
        } else {
            segments[start_i - 1].cum_dist_m
        };
        let target_elapsed = start_elapsed + target_s;

        // Find the last segment whose elapsed <= target_elapsed
        let end_i = match segments[start_i..]
            .iter()
            .rposition(|s| s.elapsed_s <= target_elapsed)
        {
            Some(off) => start_i + off,
            None => continue,
        };

        if end_i <= start_i {
            continue;
        }

        let actual_s = segments[end_i].elapsed_s - start_elapsed;
        if actual_s < target_s * 0.6 {
            continue; // window too short
        }

        let dist = segments[end_i].cum_dist_m - start_dist;
        if dist < 100.0 {
            continue;
        }

        let speed = dist / actual_s * 60.0;
        let cur = best.unwrap_or((0.0, 0.0));
        if speed > cur.0 {
            best = Some((speed, actual_s / 60.0));
        }
    }
    best
}

/// Find the 1 km window with the highest average expressed VO2 (ACSM, grade-corrected).
/// Uses a distance-based sliding window with a running VO2 sum — O(n).
/// Maximises VO2, not speed, so a slow steep uphill can beat a fast flat km.
fn best_1km_vo2(segments: &[Segment], effective_max_hr: f64) -> Option<PeakKmResult> {
    if segments.len() < 2 {
        return None;
    }

    // Pre-compute instantaneous VO2 at each segment (clamp grade ≥ 0: ACSM doesn't
    // model a metabolic benefit from descending).
    let vo2s: Vec<f64> = segments
        .iter()
        .map(|s| acsm_vo2(s.speed_mpm, s.grade.max(0.0)))
        .collect();

    let mut best: Option<PeakKmResult> = None;
    let mut right = 0usize;
    let mut vo2_sum = 0.0_f64;
    let mut hr_sum = 0u64;
    let mut hr_count = 0usize;

    for left in 0..segments.len() {
        let left_dist = if left == 0 { 0.0 } else { segments[left - 1].cum_dist_m };

        // Extend right until the window covers ≥ 1 000 m
        while right < segments.len() && segments[right].cum_dist_m - left_dist < 1000.0 {
            vo2_sum += vo2s[right];
            if let Some(hr) = segments[right].hr {
                hr_sum += hr as u64;
                hr_count += 1;
            }
            right += 1;
        }

        // How far does the current window actually reach?
        let right_idx = right.min(segments.len() - 1);
        let actual_dist = segments[right_idx].cum_dist_m - left_dist;
        if actual_dist < 800.0 {
            break; // Less than 800 m remaining — stop
        }

        let window_len = right - left;
        if window_len == 0 {
            continue;
        }

        let avg_vo2 = vo2_sum / window_len as f64;
        let is_better = best.as_ref().map_or(true, |b: &PeakKmResult| avg_vo2 > b.vo2_expressed);

        if is_better {
            let left_elapsed = if left == 0 { 0.0 } else { segments[left - 1].elapsed_s };
            let elapsed = segments[right_idx].elapsed_s - left_elapsed;
            let speed_mpm = if elapsed > 0.0 { actual_dist / elapsed * 60.0 } else { 0.0 };

            // Net gradient over the window
            let left_ele = if left == 0 {
                segments[0].elevation_m
            } else {
                segments[left - 1].elevation_m
            };
            let net_ele = match (segments[right_idx].elevation_m, left_ele) {
                (Some(e2), Some(e1)) => e2 - e1,
                _ => 0.0,
            };
            let avg_grade_pct = round_to(net_ele / actual_dist * 100.0, 1);

            let avg_hr = if hr_count > 0 {
                Some((hr_sum as f64 / hr_count as f64).round() as u32)
            } else {
                None
            };

            let vo2max_est = if effective_max_hr > 0.0 {
                avg_hr.and_then(|hr| {
                    if hr == 0 {
                        return None;
                    }
                    let est = avg_vo2 * effective_max_hr / hr as f64;
                    if est > 15.0 && est < 120.0 {
                        Some(round_to(est, 1))
                    } else {
                        None
                    }
                })
            } else {
                None
            };

            best = Some(PeakKmResult {
                vo2_expressed: round_to(avg_vo2, 1),
                vo2max_est,
                pace_min_per_km: if speed_mpm > 0.0 {
                    round_to(1000.0 / speed_mpm, 2)
                } else {
                    0.0
                },
                avg_grade_pct,
                avg_hr,
                start_distance_km: round_to(left_dist / 1000.0, 2),
            });
        }

        // Slide left forward: remove the departing segment from the running totals
        vo2_sum -= vo2s[left];
        if let Some(hr) = segments[left].hr {
            hr_sum = hr_sum.saturating_sub(hr as u64);
            if hr_count > 0 {
                hr_count -= 1;
            }
        }
    }

    best
}

/// `weight_kg` is accepted for a future energy-expenditure calculation but is not
/// used by the current VO2 estimation methods (ACSM VO2 is per-kg, so weight cancels out).
fn analyze(points: &[TrackPoint], _weight_kg: f64, max_hr_input: u32) -> AnalysisResult {
    if points.len() < 10 {
        return AnalysisResult {
            total_distance_km: 0.0,
            total_duration_min: 0.0,
            avg_pace_min_per_km: 0.0,
            elevation_gain_m: 0.0,
            avg_hr: None,
            max_hr_recorded: None,
            has_hr_data: false,
            has_elevation_data: false,
            has_time_data: false,
            point_count: points.len(),
            estimates: vec![],
            fitness_category: None,
            fitness_description: None,
            peak_1km: None,
            chart_points: vec![],
            error: Some(format!(
                "Not enough track points (found {}, need at least 10). Is this a valid GPX file?",
                points.len()
            )),
        };
    }

    let segs = build_segments(points);

    // ---- Activity stats ----
    let total_dist_m = segs.last().map(|s| s.cum_dist_m).unwrap_or(0.0);
    let total_dist_km = total_dist_m / 1000.0;

    let has_time = points.iter().any(|p| p.time_s.is_some());
    let total_duration_s = if has_time {
        match (
            points.last().and_then(|p| p.time_s),
            points.first().and_then(|p| p.time_s),
        ) {
            (Some(end), Some(start)) if end > start => end - start,
            _ => segs.len() as f64,
        }
    } else {
        segs.len() as f64
    };
    let total_duration_min = total_duration_s / 60.0;
    let avg_pace = if total_dist_km > 0.0 {
        total_duration_min / total_dist_km
    } else {
        0.0
    };

    let has_elevation = points.iter().any(|p| p.ele.is_some());
    let elevation_gain: f64 = segs.windows(2).fold(0.0, |acc, w| {
        match (w[1].elevation_m, w[0].elevation_m) {
            (Some(e2), Some(e1)) if e2 > e1 => acc + (e2 - e1),
            _ => acc,
        }
    });

    let hr_vals: Vec<u32> = points.iter().filter_map(|p| p.hr).collect();
    let has_hr = !hr_vals.is_empty();
    let avg_hr = if has_hr {
        Some(hr_vals.iter().map(|&h| h as f64).sum::<f64>() / hr_vals.len() as f64)
    } else {
        None
    };
    let max_hr_recorded = hr_vals.iter().copied().max();

    // Effective max HR: use user-supplied, fall back to recorded + 5%
    let effective_max_hr = if max_hr_input > 0 {
        max_hr_input as f64
    } else if let Some(mhr) = max_hr_recorded {
        mhr as f64 * 1.05
    } else {
        0.0
    };

    // ---- VO2max estimates ----
    let mut estimates: Vec<Vo2Estimate> = Vec::new();

    // Method 1: ACSM + HR (steady-state)
    if has_hr && effective_max_hr > 0.0 {
        let skip_s = 180.0; // skip first 3 min warmup
        let hr_lo = effective_max_hr * 0.65;
        let hr_hi = effective_max_hr * 0.97;

        let mut samples: Vec<f64> = segs
            .iter()
            .filter(|s| {
                s.elapsed_s > skip_s
                    && s.speed_mpm > 80.0 // > ~5 km/h
                    && s.hr
                        .map(|h| {
                            let hf = h as f64;
                            hf >= hr_lo && hf <= hr_hi
                        })
                        .unwrap_or(false)
            })
            .filter_map(|s| {
                let hr = s.hr? as f64;
                let vo2 = acsm_vo2(s.speed_mpm, s.grade);
                let est = vo2 * effective_max_hr / hr;
                if est > 15.0 && est < 110.0 {
                    Some(est)
                } else {
                    None
                }
            })
            .collect();

        if samples.len() >= 5 {
            samples.sort_by(|a, b| a.total_cmp(b));
            let median = samples[samples.len() / 2];

            // Confidence: sample count (saturates at 100) minus a penalty for high variance
            let mean = samples.iter().sum::<f64>() / samples.len() as f64;
            let variance = samples.iter().map(|&x| (x - mean).powi(2)).sum::<f64>()
                / samples.len() as f64;
            let cv = variance.sqrt() / mean; // coefficient of variation
            let count_score = (samples.len() as f64 / 100.0).min(1.0);
            let cv_penalty = (cv / 0.25).min(1.0) * 15.0;
            let confidence_pct =
                ((45.0 + count_score * 40.0 - cv_penalty).round() as i32).clamp(20, 85) as u32;

            estimates.push(Vo2Estimate {
                method: "ACSM + Heart Rate (Steady-State)".to_string(),
                value: round_to(median, 1),
                confidence_pct,
                notes: format!(
                    "Uses ACSM running metabolic equation and HR/HRmax linearity. \
                     Based on {} steady-state data points (HR 65–97% of max, after first 3 min).",
                    samples.len()
                ),
            });
        }
    }

    // Method 2: Jack Daniels performance — best effort windows
    if has_time && total_duration_min > 2.0 {
        // Estimate effort level from HR if available.
        // Daniels assumes race pace: easy runs underestimate VO2max (you were sandbagging),
        // hard efforts give accurate or slightly high results.
        let effort_pct = if has_hr && effective_max_hr > 0.0 {
            avg_hr.map(|h| h / effective_max_hr)
        } else {
            None
        };

        let target_durations: &[f64] = &[20.0, 30.0, 10.0, 60.0, 5.0];
        let mut added = false;

        for &dur in target_durations {
            if total_duration_min < dur * 0.6 {
                continue;
            }
            if let Some((speed, actual_min)) = best_window(&segs, dur) {
                if let Some(vo2max) = daniels_vo2max(speed, actual_min) {
                    if vo2max > 15.0 && vo2max < 110.0 {
                        let pace_km = if speed > 0.0 { 1000.0 / speed } else { 0.0 };
                        let pace_min = pace_km as u32;
                        let pace_sec = ((pace_km - pace_min as f64) * 60.0) as u32;

                        // Confidence and note depend on effort level:
                        // - Easy/Zone 2 (<75% HRmax): will significantly underestimate — low confidence
                        // - Moderate (75–85%): may underestimate — medium confidence
                        // - Hard (>85%): accurate or slightly high — best Daniels confidence
                        let (confidence_pct, effort_note) = match effort_pct {
                            Some(e) if e < 0.75 => (
                                ((20.0 + (dur / 60.0).min(1.0) * 20.0).round() as u32).clamp(15, 35),
                                format!(
                                    "Avg HR was only {:.0}% of max — this was an easy/Zone 2 run. \
                                     Daniels assumes race-like effort, so this result will \
                                     significantly underestimate your actual VO2 max.",
                                    e * 100.0
                                ),
                            ),
                            Some(e) if e < 0.85 => (
                                ((20.0 + (dur / 60.0).min(1.0) * 40.0).round() as u32).clamp(25, 55),
                                format!(
                                    "Avg HR was {:.0}% of max — a moderate effort. Result will \
                                     likely underestimate your VO2 max; most accurate when run \
                                     at race or threshold intensity.",
                                    e * 100.0
                                ),
                            ),
                            Some(e) => (
                                ((20.0 + (dur / 60.0).min(1.0) * 52.0).round() as u32).clamp(35, 72),
                                format!(
                                    "Avg HR was {:.0}% of max — a hard effort. Result is \
                                     reasonably accurate; may slightly overestimate if HR was \
                                     elevated by heat or fatigue rather than pure intensity.",
                                    e * 100.0
                                ),
                            ),
                            // No HR data: can't assess effort level
                            None => (
                                ((20.0 + (dur / 60.0).min(1.0) * 35.0).round() as u32).clamp(20, 55),
                                "No HR data — cannot assess effort level. Result is only accurate \
                                 if this was a race or near-maximal effort."
                                    .to_string(),
                            ),
                        };

                        estimates.push(Vo2Estimate {
                            method: format!("Jack Daniels — Best {:.0}-min Effort", dur),
                            value: round_to(vo2max, 1),
                            confidence_pct,
                            notes: format!(
                                "Based on your fastest {:.0}-minute segment (avg pace {}:{:02} /km). {}",
                                dur, pace_min, pace_sec, effort_note
                            ),
                        });
                        added = true;
                        break;
                    }
                }
            }
        }

        // Also add whole-activity Daniels if we didn't add a windowed one
        if !added && total_dist_km > 0.5 {
            let whole_speed = total_dist_m / total_duration_s * 60.0;
            if let Some(vo2max) = daniels_vo2max(whole_speed, total_duration_min) {
                if vo2max > 15.0 && vo2max < 110.0 {
                    estimates.push(Vo2Estimate {
                        method: "Jack Daniels — Whole Activity".to_string(),
                        value: round_to(vo2max, 1),
                        confidence_pct: 18,
                        notes: "Based on average pace across the entire activity. Assumes race-like \
                                effort throughout — almost always an underestimate for training runs."
                            .to_string(),
                    });
                }
            }
        }
    }

    // Method 3: Firstbeat — linear regression of HR vs VO2, extrapolated to HRmax.
    // More robust than the point-by-point median in method 1: uses all steady-state
    // data together, naturally handles noise, and works across a wider HR range.
    // The regression slope m also encodes individual running economy (see @todo above).
    if has_hr && effective_max_hr > 0.0 {
        let skip_s = 180.0;
        let hr_lo = effective_max_hr * 0.50; // wider range than method 1 to feed the regression
        let hr_hi = effective_max_hr * 0.97;

        let pairs: Vec<(f64, f64)> = segs
            .iter()
            .filter(|s| {
                s.elapsed_s > skip_s
                    && s.speed_mpm > 80.0
                    && s.hr
                        .map(|h| {
                            let hf = h as f64;
                            hf >= hr_lo && hf <= hr_hi
                        })
                        .unwrap_or(false)
            })
            .filter_map(|s| {
                let hr = s.hr? as f64;
                let vo2 = acsm_vo2(s.speed_mpm, s.grade);
                if vo2 > 0.0 { Some((hr, vo2)) } else { None }
            })
            .collect();

        if pairs.len() >= 10 {
            // Ordinary least squares: VO2 = m × HR + b
            let n = pairs.len() as f64;
            let sum_x: f64 = pairs.iter().map(|(hr, _)| hr).sum();
            let sum_y: f64 = pairs.iter().map(|(_, v)| v).sum();
            let sum_xy: f64 = pairs.iter().map(|(hr, v)| hr * v).sum();
            let sum_xx: f64 = pairs.iter().map(|(hr, _)| hr * hr).sum();

            let denom = n * sum_xx - sum_x * sum_x;
            if denom.abs() > 1e-10 {
                let m = (n * sum_xy - sum_x * sum_y) / denom;
                let b = (sum_y - m * sum_x) / n;
                let vo2max = m * effective_max_hr + b;

                // Negative slope would mean HR and VO2 are inversely related — nonsensical
                if m > 0.0 && vo2max > 15.0 && vo2max < 110.0 {
                    // R² measures how well the linear model fits the observed (HR, VO2) data
                    let y_mean = sum_y / n;
                    let ss_res: f64 = pairs
                        .iter()
                        .map(|(hr, vo2)| (vo2 - (m * hr + b)).powi(2))
                        .sum();
                    let ss_tot: f64 =
                        pairs.iter().map(|(_, vo2)| (vo2 - y_mean).powi(2)).sum();
                    let r_squared = if ss_tot > 1e-10 {
                        (1.0 - ss_res / ss_tot).max(0.0)
                    } else {
                        0.0
                    };
                    // Confidence from R²; capped at 83% to reflect extrapolation uncertainty
                    let confidence_pct =
                        ((30.0 + r_squared * 53.0).round() as u32).clamp(20, 83);

                    estimates.push(Vo2Estimate {
                        method: "Firstbeat (HR–VO2 Regression)".to_string(),
                        value: round_to(vo2max, 1),
                        confidence_pct,
                        notes: format!(
                            "Fits a linear regression across {} steady-state (HR, VO2) pairs \
                             and extrapolates to HRmax (R² = {:.2}). More robust than the \
                             point-by-point method as it uses all data together.",
                            pairs.len(),
                            r_squared
                        ),
                    });
                }
            }
        }
    }

    // Fitness category from best available estimate
    let (cat, desc, has_best) = {
        let best = estimates.iter().max_by_key(|e| e.confidence_pct);
        let (c, d) = best.map(|e| fitness_category(e.value)).unwrap_or(("Unknown", ""));
        (c, d, best.is_some())
    };

    // ---- Chart data (sample down to ~500 points) ----
    let step = (segs.len() / 500).max(1);
    let chart_points: Vec<ChartPoint> = segs
        .iter()
        .step_by(step)
        .map(|s| {
            let pace = if s.speed_mpm > 10.0 {
                Some(round_to(1000.0 / s.speed_mpm, 2))
            } else {
                None
            };
            ChartPoint {
                distance_km: round_to(s.cum_dist_m / 1000.0, 2),
                pace_min_per_km: pace,
                hr: s.hr,
                elevation_m: s.elevation_m,
            }
        })
        .collect();

    AnalysisResult {
        total_distance_km: round_to(total_dist_km, 2),
        total_duration_min: round_to(total_duration_min, 1),
        avg_pace_min_per_km: round_to(avg_pace, 2),
        elevation_gain_m: elevation_gain.round(),
        avg_hr,
        max_hr_recorded,
        has_hr_data: has_hr,
        has_elevation_data: has_elevation,
        has_time_data: has_time,
        point_count: points.len(),
        estimates,
        fitness_category: if has_best { Some(cat.to_string()) } else { None },
        fitness_description: if has_best { Some(desc.to_string()) } else { None },
        peak_1km: if has_time { best_1km_vo2(&segs, effective_max_hr) } else { None },
        chart_points,
        error: None,
    }
}

fn round_to(v: f64, places: u32) -> f64 {
    let factor = 10f64.powi(places as i32);
    (v * factor).round() / factor
}

// ============================================================
// WASM EXPORT
// ============================================================

/// Returns JSON as a `String` rather than a `JsValue` via `serde-wasm-bindgen` to keep binary
/// size small — the extra serialise/parse round-trip is negligible for the payload sizes here.
#[wasm_bindgen]
pub fn analyze_gpx(gpx_content: &str, weight_kg: f64, max_hr: u32) -> String {
    let points = match parse_gpx(gpx_content) {
        Ok(p) => p,
        Err(e) => {
            return serde_json::json!({
                "error": format!("Failed to parse GPX: {}", e),
                "estimates": [],
                "chart_points": []
            }).to_string();
        }
    };
    let result = analyze(&points, weight_kg, max_hr);
    serde_json::to_string(&result)
        .unwrap_or_else(|_| r#"{"error":"Serialization failed","estimates":[],"chart_points":[]}"#.to_string())
}
