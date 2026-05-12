using System.Runtime.InteropServices.JavaScript;
using System.Text.Json;
using Vo2;

// The wasmbrowser SDK runs Main() once on startup. The JS side then calls
// AnalyzeGpx directly via the [JSExport] binding below.
return 0;

public partial class Vo2Api
{
    [JSExport]
    internal static string AnalyzeGpx(string gpxContent, double weightKg, int maxHr)
    {
        try
        {
            var points = Gpx.Parse(gpxContent);
            var result = Analyze.Run(points, weightKg, (uint)Math.Max(0, maxHr));
            return JsonSerializer.Serialize(result, Vo2JsonContext.Default.AnalysisResult);
        }
        catch (FormatException ex)
        {
            var err = new AnalysisResult { Error = "Failed to parse GPX: " + ex.Message };
            return JsonSerializer.Serialize(err, Vo2JsonContext.Default.AnalysisResult);
        }
        catch (Exception ex)
        {
            var err = new AnalysisResult { Error = "Unexpected error: " + ex.Message };
            return JsonSerializer.Serialize(err, Vo2JsonContext.Default.AnalysisResult);
        }
    }
}
