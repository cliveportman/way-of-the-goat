const std = @import("std");

const EARTH_RADIUS_M: f64 = 6_371_000.0;

inline fn toRadians(deg: f64) f64 {
    return deg * std.math.pi / 180.0;
}

pub fn haversine(lat1: f64, lon1: f64, lat2: f64, lon2: f64) f64 {
    const d_lat = toRadians(lat2 - lat1);
    const d_lon = toRadians(lon2 - lon1);
    const s_lat = @sin(d_lat / 2.0);
    const s_lon = @sin(d_lon / 2.0);
    const a = s_lat * s_lat + @cos(toRadians(lat1)) * @cos(toRadians(lat2)) * s_lon * s_lon;
    const c = 2.0 * std.math.atan2(@sqrt(a), @sqrt(1.0 - a));
    return EARTH_RADIUS_M * c;
}
