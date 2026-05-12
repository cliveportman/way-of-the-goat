const EARTH_RADIUS_M = 6_371_000;

function toRadians(deg) { return deg * Math.PI / 180; }

export function haversine(lat1, lon1, lat2, lon2) {
  const dLat = toRadians(lat2 - lat1);
  const dLon = toRadians(lon2 - lon1);
  const sLat = Math.sin(dLat / 2);
  const sLon = Math.sin(dLon / 2);
  const a = sLat * sLat
          + Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) * sLon * sLon;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return EARTH_RADIUS_M * c;
}
