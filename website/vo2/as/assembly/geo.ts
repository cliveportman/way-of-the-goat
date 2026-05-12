const EARTH_RADIUS_M: f64 = 6_371_000.0;

@inline
function toRadians(deg: f64): f64 { return deg * Math.PI / 180.0; }

export function haversine(lat1: f64, lon1: f64, lat2: f64, lon2: f64): f64 {
  const dLat = toRadians(lat2 - lat1);
  const dLon = toRadians(lon2 - lon1);
  const sLat = Math.sin(dLat / 2.0);
  const sLon = Math.sin(dLon / 2.0);
  const a = sLat * sLat
          + Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) * sLon * sLon;
  const c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
  return EARTH_RADIUS_M * c;
}
