namespace Vo2;

internal static class Vo2Formulas
{
    /// <summary>
    /// ACSM Running Metabolic Equation.
    /// VO2 (mL/kg/min) = 0.2 × S + 0.9 × S × G + 3.5
    /// S = speed in m/min, G = fractional grade.
    /// </summary>
    public static double AcsmVo2(double speedMpm, double grade)
        => 0.2 * speedMpm + 0.9 * speedMpm * grade + 3.5;

    /// <summary>
    /// Jack Daniels / Gilbert performance formula.
    /// </summary>
    public static bool DanielsVo2Max(double velocityMpm, double timeMin, out double vo2Max)
    {
        vo2Max = 0;
        if (velocityMpm < 60.0 || timeMin < 1.0) return false;
        var pct = 0.8
                + 0.1894393 * Math.Exp(-0.012778 * timeMin)
                + 0.2989558 * Math.Exp(-0.1932605 * timeMin);
        var vo2 = -4.60 + 0.182258 * velocityMpm + 0.000104 * velocityMpm * velocityMpm;
        if (pct <= 0 || vo2 <= 0) return false;
        vo2Max = vo2 / pct;
        return true;
    }

    public static (string Category, string Description) FitnessCategory(double vo2Max) => vo2Max switch
    {
        < 30.0 => ("Poor", "Below average cardiovascular fitness"),
        < 40.0 => ("Fair", "Average cardiovascular fitness"),
        < 50.0 => ("Good", "Above average cardiovascular fitness"),
        < 60.0 => ("Excellent", "High cardiovascular fitness"),
        < 75.0 => ("Superior", "Very high cardiovascular fitness"),
        _ => ("Elite", "Elite-level cardiovascular fitness"),
    };

    public static double RoundTo(double v, int places)
    {
        var factor = Math.Pow(10, places);
        return Math.Round(v * factor, MidpointRounding.AwayFromZero) / factor;
    }

    public static double Clamp(double v, double lo, double hi)
        => v < lo ? lo : v > hi ? hi : v;

    public static int Clamp(int v, int lo, int hi)
        => v < lo ? lo : v > hi ? hi : v;
}
