namespace Vo2;

internal static class Geo
{
    private const double EarthRadiusM = 6_371_000.0;

    public static double ToRadians(double deg) => deg * Math.PI / 180.0;

    public static double Haversine(double lat1, double lon1, double lat2, double lon2)
    {
        var dLat = ToRadians(lat2 - lat1);
        var dLon = ToRadians(lon2 - lon1);
        var sLat = Math.Sin(dLat / 2);
        var sLon = Math.Sin(dLon / 2);
        var a = sLat * sLat
              + Math.Cos(ToRadians(lat1)) * Math.Cos(ToRadians(lat2)) * sLon * sLon;
        var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
        return EarthRadiusM * c;
    }
}
