package vo2

import "math"

func toRadians(deg float64) float64 { return deg * math.Pi / 180.0 }

func haversine(lat1, lon1, lat2, lon2 float64) float64 {
	const r = 6_371_000.0
	dlat := toRadians(lat2 - lat1)
	dlon := toRadians(lon2 - lon1)
	sLat := math.Sin(dlat / 2)
	sLon := math.Sin(dlon / 2)
	a := sLat*sLat + math.Cos(toRadians(lat1))*math.Cos(toRadians(lat2))*sLon*sLon
	c := 2 * math.Atan2(math.Sqrt(a), math.Sqrt(1-a))
	return r * c
}
