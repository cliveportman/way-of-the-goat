const std = @import("std");

// Returns NaN on parse failure to match the floating-point sentinel pattern
// used throughout the analyser.

fn isLeap(year: i32) bool {
    return (@mod(year, 4) == 0 and @mod(year, 100) != 0) or @mod(year, 400) == 0;
}

const MONTH_DAYS = [_]i32{ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

pub fn parseTimestamp(input: []const u8) f64 {
    var s = std.mem.trim(u8, input, " \t\r\n");
    if (s.len > 0 and s[s.len - 1] == 'Z') s = s[0 .. s.len - 1];
    const t_idx = std.mem.indexOfScalar(u8, s, 'T') orelse return std.math.nan(f64);
    const date_str = s[0..t_idx];
    var time_str = s[t_idx + 1 ..];

    var date_parts = std.mem.splitScalar(u8, date_str, '-');
    const year_str = date_parts.next() orelse return std.math.nan(f64);
    const month_str = date_parts.next() orelse return std.math.nan(f64);
    const day_str = date_parts.next() orelse return std.math.nan(f64);
    if (date_parts.next() != null) return std.math.nan(f64);

    const year = std.fmt.parseInt(i32, year_str, 10) catch return std.math.nan(f64);
    const month = std.fmt.parseInt(i32, month_str, 10) catch return std.math.nan(f64);
    const day = std.fmt.parseInt(i32, day_str, 10) catch return std.math.nan(f64);

    if (std.mem.indexOfScalar(u8, time_str, '.')) |dot| time_str = time_str[0..dot];
    var time_parts = std.mem.splitScalar(u8, time_str, ':');
    const hour_str = time_parts.next() orelse return std.math.nan(f64);
    const min_str = time_parts.next() orelse return std.math.nan(f64);
    const sec_str = time_parts.next() orelse return std.math.nan(f64);
    if (time_parts.next() != null) return std.math.nan(f64);

    const hour = std.fmt.parseFloat(f64, hour_str) catch return std.math.nan(f64);
    const min = std.fmt.parseFloat(f64, min_str) catch return std.math.nan(f64);
    const sec = std.fmt.parseFloat(f64, sec_str) catch return std.math.nan(f64);

    if (year < 2000 or year > 2100) return std.math.nan(f64);
    if (month < 1 or month > 12) return std.math.nan(f64);
    const max_day: i32 = switch (month) {
        2 => if (isLeap(year)) @as(i32, 29) else 28,
        4, 6, 9, 11 => 30,
        else => 31,
    };
    if (day < 1 or day > max_day) return std.math.nan(f64);
    if (hour < 0 or hour > 23 or min < 0 or min > 59 or sec < 0 or sec > 59) return std.math.nan(f64);

    var days: i32 = 0;
    var y: i32 = 2000;
    while (y < year) : (y += 1) days += if (isLeap(y)) @as(i32, 366) else 365;
    var m: i32 = 0;
    while (m < month - 1) : (m += 1) {
        days += MONTH_DAYS[@intCast(m)];
        if (m == 1 and isLeap(year)) days += 1;
    }
    days += day - 1;

    return @as(f64, @floatFromInt(days)) * 86400.0 + hour * 3600.0 + min * 60.0 + sec;
}
