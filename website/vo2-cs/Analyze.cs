using System.Globalization;
using static Vo2.Vo2Formulas;

namespace Vo2;

internal sealed class Segment
{
    public double CumDistM;
    public double ElapsedS;
    public double SpeedMpm;
    public double Grade;
    public uint? Hr;
    public double? ElevationM;
}

internal static class Analyze
{
    public static List<Segment> BuildSegments(List<TrackPoint> points)
    {
        double? baseTime = null;
        foreach (var p in points) { if (p.TimeS.HasValue) { baseTime = p.TimeS; break; } }

        var cum = 0.0;
        var segments = new List<Segment>(points.Count);
        for (int i = 1; i < points.Count; i++)
        {
            var prev = points[i - 1];
            var curr = points[i];

            var dist = Geo.Haversine(prev.Lat, prev.Lon, curr.Lat, curr.Lon);
            var eleDiff = 0.0;
            if (curr.Ele.HasValue && prev.Ele.HasValue) eleDiff = curr.Ele.Value - prev.Ele.Value;

            var dt = 1.0;
            if (curr.TimeS.HasValue && prev.TimeS.HasValue && curr.TimeS.Value > prev.TimeS.Value)
                dt = curr.TimeS.Value - prev.TimeS.Value;

            cum += dist;
            var grade = dist > 0.5 ? Clamp(eleDiff / dist, -0.50, 0.50) : 0.0;
            var speedMpm = (dist / dt) * 60.0;

            var elapsed = curr.TimeS.HasValue && baseTime.HasValue
                ? curr.TimeS.Value - baseTime.Value
                : (double)i;

            segments.Add(new Segment
            {
                CumDistM = cum,
                ElapsedS = elapsed,
                SpeedMpm = speedMpm,
                Grade = grade,
                Hr = curr.Hr,
                ElevationM = curr.Ele,
            });
        }
        return segments;
    }

    public static bool BestWindow(List<Segment> segs, double targetMin, out double speedMpm, out double actualMin)
    {
        speedMpm = 0; actualMin = 0;
        var targetS = targetMin * 60.0;
        var found = false;

        for (int startI = 0; startI < segs.Count; startI++)
        {
            var startElapsed = segs[startI].ElapsedS;
            var startDist = startI == 0 ? 0 : segs[startI - 1].CumDistM;
            var targetElapsed = startElapsed + targetS;

            var endI = -1;
            for (int k = segs.Count - 1; k >= startI; k--)
            {
                if (segs[k].ElapsedS <= targetElapsed) { endI = k; break; }
            }
            if (endI <= startI) continue;

            var actualS = segs[endI].ElapsedS - startElapsed;
            if (actualS < targetS * 0.6) continue;
            var dist = segs[endI].CumDistM - startDist;
            if (dist < 100.0) continue;

            var sp = dist / actualS * 60.0;
            if (sp > speedMpm)
            {
                speedMpm = sp;
                actualMin = actualS / 60.0;
                found = true;
            }
        }
        return found;
    }

    public static PeakKmResult? Best1KmVo2(List<Segment> segs, double effectiveMaxHr)
    {
        if (segs.Count < 2) return null;
        var vo2s = new double[segs.Count];
        for (int i = 0; i < segs.Count; i++)
        {
            var g = segs[i].Grade < 0 ? 0 : segs[i].Grade;
            vo2s[i] = AcsmVo2(segs[i].SpeedMpm, g);
        }

        PeakKmResult? best = null;
        var right = 0;
        var vo2Sum = 0.0;
        ulong hrSum = 0;
        var hrCount = 0;

        for (int left = 0; left < segs.Count; left++)
        {
            var leftDist = left == 0 ? 0 : segs[left - 1].CumDistM;

            while (right < segs.Count && segs[right].CumDistM - leftDist < 1000.0)
            {
                vo2Sum += vo2s[right];
                if (segs[right].Hr.HasValue) { hrSum += segs[right].Hr!.Value; hrCount++; }
                right++;
            }

            var rightIdx = Math.Min(right, segs.Count - 1);
            var actualDist = segs[rightIdx].CumDistM - leftDist;
            if (actualDist < 800.0) break;
            var windowLen = right - left;
            if (windowLen == 0) continue;

            var avgVo2 = vo2Sum / windowLen;
            if (best == null || avgVo2 > best.Vo2Expressed)
            {
                var leftElapsed = left == 0 ? 0 : segs[left - 1].ElapsedS;
                var elapsed = segs[rightIdx].ElapsedS - leftElapsed;
                var sp = elapsed > 0 ? actualDist / elapsed * 60.0 : 0;

                double? leftEle = left == 0 ? segs[0].ElevationM : segs[left - 1].ElevationM;
                var netEle = 0.0;
                if (segs[rightIdx].ElevationM.HasValue && leftEle.HasValue)
                    netEle = segs[rightIdx].ElevationM!.Value - leftEle.Value;
                var avgGradePct = RoundTo(netEle / actualDist * 100.0, 1);

                uint? avgHr = hrCount > 0 ? (uint)Math.Round((double)hrSum / hrCount) : null;

                double? vo2MaxEst = null;
                if (effectiveMaxHr > 0 && avgHr.HasValue && avgHr.Value != 0)
                {
                    var est = avgVo2 * effectiveMaxHr / avgHr.Value;
                    if (est > 15.0 && est < 120.0) vo2MaxEst = RoundTo(est, 1);
                }
                var pace = sp > 0 ? RoundTo(1000.0 / sp, 2) : 0;

                best = new PeakKmResult
                {
                    Vo2Expressed = RoundTo(avgVo2, 1),
                    Vo2MaxEst = vo2MaxEst,
                    PaceMinPerKm = pace,
                    AvgGradePct = avgGradePct,
                    AvgHr = avgHr,
                    StartDistanceKm = RoundTo(leftDist / 1000.0, 2),
                };
            }

            vo2Sum -= vo2s[left];
            if (segs[left].Hr.HasValue)
            {
                var h = segs[left].Hr!.Value;
                hrSum = hrSum >= h ? hrSum - h : 0;
                if (hrCount > 0) hrCount--;
            }
        }
        return best;
    }

    public static (List<DriftPoint> drift, double? decoupling) ComputeDrift(List<Segment> segs)
    {
        const double skipS = 180.0;
        var raw = new List<(double Dist, double Eff)>();
        foreach (var s in segs)
        {
            if (s.ElapsedS <= skipS || s.SpeedMpm <= 50.0) continue;
            if (!s.Hr.HasValue || s.Hr.Value <= 50) continue;
            var eff = s.SpeedMpm / s.Hr.Value;
            if (eff > 0) raw.Add((s.CumDistM, eff));
        }
        if (raw.Count < 20) return (new List<DriftPoint>(), null);

        var span = segs.Count > 0 ? segs[^1].ElapsedS : 1.0;
        if (span < 1) span = 1;
        var samplesPerSec = raw.Count / span;
        var win = (int)Math.Round(samplesPerSec * 600.0);
        win = Clamp(win, 5, 600);
        if (win > raw.Count) win = raw.Count;
        var half = win / 2;

        var smoothed = new (double Dist, double Eff)[raw.Count];
        for (int i = 0; i < raw.Count; i++)
        {
            var s = Math.Max(0, i - half);
            var e = Math.Min(raw.Count, i + half + 1);
            var sum = 0.0;
            for (int k = s; k < e; k++) sum += raw[k].Eff;
            smoothed[i] = (raw[i].Dist, sum / (e - s));
        }

        var baseline = smoothed[0].Eff;
        if (baseline <= 0) return (new List<DriftPoint>(), null);

        var step = Math.Max(1, smoothed.Length / 300);
        var drift = new List<DriftPoint>();
        for (int i = 0; i < smoothed.Length; i += step)
        {
            drift.Add(new DriftPoint
            {
                DistanceKm = RoundTo(smoothed[i].Dist / 1000.0, 2),
                Efficiency = RoundTo(smoothed[i].Eff / baseline, 3),
            });
        }

        var mid = smoothed.Length / 2;
        double m1 = 0, m2 = 0;
        for (int i = 0; i < mid; i++) m1 += smoothed[i].Eff;
        for (int i = mid; i < smoothed.Length; i++) m2 += smoothed[i].Eff;
        m1 /= mid;
        m2 /= smoothed.Length - mid;
        double? decoupling = m1 > 0 ? RoundTo((m1 - m2) / m1 * 100.0, 1) : null;
        return (drift, decoupling);
    }

    public static List<DescentPoint> ComputeDescentPoints(List<Segment> segs)
    {
        var total = segs.Count > 0 ? segs[^1].CumDistM : 1.0;
        if (total < 1) total = 1;

        var pts = new List<DescentPoint>();
        foreach (var s in segs)
        {
            if (s.Grade >= -0.03 || s.SpeedMpm <= 30.0) continue;
            var speedKmh = s.SpeedMpm * 60.0 / 1000.0;
            if (speedKmh < 0.5 || speedKmh > 35.0) continue;
            var gradePct = RoundTo(s.Grade * 100.0, 1);
            if (gradePct < -50.0) continue;
            pts.Add(new DescentPoint
            {
                GradePct = gradePct,
                SpeedKmh = RoundTo(speedKmh, 2),
                Progress = RoundTo(s.CumDistM / total, 3),
            });
        }

        if (pts.Count > 2000)
        {
            var step = pts.Count / 2000;
            var sampled = new List<DescentPoint>(2001);
            for (int i = 0; i < pts.Count; i += step) sampled.Add(pts[i]);
            pts = sampled;
        }
        return pts;
    }

    public static AnalysisResult Run(List<TrackPoint> points, double weightKg, uint maxHrInput)
    {
        _ = weightKg; // currently unused, kept for parity
        if (points.Count < 10)
        {
            return new AnalysisResult
            {
                PointCount = points.Count,
                Error = $"Not enough track points (found {points.Count}, need at least 10). Is this a valid GPX file?",
            };
        }

        var segs = BuildSegments(points);

        var totalDistM = segs.Count > 0 ? segs[^1].CumDistM : 0.0;
        var totalDistKm = totalDistM / 1000.0;

        var hasTime = points.Any(p => p.TimeS.HasValue);
        var totalDurationS = (double)segs.Count;
        if (hasTime)
        {
            double? startT = null, endT = null;
            foreach (var p in points) { if (p.TimeS.HasValue) { startT = p.TimeS; break; } }
            for (int i = points.Count - 1; i >= 0; i--) { if (points[i].TimeS.HasValue) { endT = points[i].TimeS; break; } }
            if (startT.HasValue && endT.HasValue && endT.Value > startT.Value)
                totalDurationS = endT.Value - startT.Value;
        }
        var totalDurationMin = totalDurationS / 60.0;
        var avgPace = totalDistKm > 0 ? totalDurationMin / totalDistKm : 0;

        var hasElevation = points.Any(p => p.Ele.HasValue);
        var elevationGain = 0.0;
        for (int i = 1; i < segs.Count; i++)
        {
            var a = segs[i - 1].ElevationM;
            var b = segs[i].ElevationM;
            if (a.HasValue && b.HasValue && b.Value > a.Value) elevationGain += b.Value - a.Value;
        }

        var hrVals = points.Where(p => p.Hr.HasValue).Select(p => p.Hr!.Value).ToList();
        var hasHr = hrVals.Count > 0;
        double? avgHr = null;
        uint? maxHrRecorded = null;
        if (hasHr)
        {
            double sum = 0;
            uint max = 0;
            foreach (var h in hrVals) { sum += h; if (h > max) max = h; }
            avgHr = sum / hrVals.Count;
            maxHrRecorded = max;
        }

        var effectiveMaxHr = 0.0;
        if (maxHrInput > 0) effectiveMaxHr = maxHrInput;
        else if (maxHrRecorded.HasValue) effectiveMaxHr = maxHrRecorded.Value * 1.05;

        List<DriftPoint> cardiacDrift;
        double? decouplingPct = null;
        if (hasHr && hasTime) (cardiacDrift, decouplingPct) = ComputeDrift(segs);
        else cardiacDrift = new List<DriftPoint>();

        var descentPoints = (hasTime && hasElevation)
            ? ComputeDescentPoints(segs)
            : new List<DescentPoint>();

        var estimates = new List<Vo2Estimate>();

        // Method 1: ACSM + HR (steady-state)
        if (hasHr && effectiveMaxHr > 0)
        {
            const double skipS = 180.0;
            var hrLo = effectiveMaxHr * 0.65;
            var hrHi = effectiveMaxHr * 0.97;

            var samples = new List<double>();
            foreach (var s in segs)
            {
                if (s.ElapsedS <= skipS || s.SpeedMpm <= 80.0 || !s.Hr.HasValue) continue;
                var hf = (double)s.Hr.Value;
                if (hf < hrLo || hf > hrHi) continue;
                var vo2 = AcsmVo2(s.SpeedMpm, s.Grade);
                var est = vo2 * effectiveMaxHr / hf;
                if (est > 15.0 && est < 110.0) samples.Add(est);
            }

            if (samples.Count >= 5)
            {
                samples.Sort();
                var median = samples[samples.Count / 2];

                var mean = samples.Average();
                var variance = samples.Sum(x => (x - mean) * (x - mean)) / samples.Count;
                var cv = Math.Sqrt(variance) / mean;
                var countScore = Math.Min(samples.Count / 100.0, 1.0);
                var cvPenalty = Math.Min(cv / 0.25, 1.0) * 15.0;
                var confidence = Clamp((int)Math.Round(45.0 + countScore * 40.0 - cvPenalty), 20, 85);

                estimates.Add(new Vo2Estimate
                {
                    Method = "ACSM + Heart Rate (Steady-State)",
                    Value = RoundTo(median, 1),
                    ConfidencePct = (uint)confidence,
                    Notes = string.Format(CultureInfo.InvariantCulture,
                        "Uses ACSM running metabolic equation and HR/HRmax linearity. "
                        + "Based on {0} steady-state data points (HR 65–97% of max, after first 3 min).",
                        samples.Count),
                });
            }
        }

        // Method 2: Jack Daniels — best effort windows
        if (hasTime && totalDurationMin > 2.0)
        {
            double? effortPct = (hasHr && effectiveMaxHr > 0 && avgHr.HasValue)
                ? avgHr.Value / effectiveMaxHr
                : null;

            var targets = new[] { 20.0, 30.0, 10.0, 60.0, 5.0 };
            var added = false;
            foreach (var dur in targets)
            {
                if (totalDurationMin < dur * 0.6) continue;
                if (!BestWindow(segs, dur, out var sp, out var actualMin)) continue;
                if (!DanielsVo2Max(sp, actualMin, out var vo2Max)) continue;
                if (vo2Max <= 15.0 || vo2Max >= 110.0) continue;

                var paceKm = sp > 0 ? 1000.0 / sp : 0.0;
                var paceMin = (uint)paceKm;
                var paceSec = (uint)((paceKm - paceMin) * 60.0);

                int confidence;
                string effortNote;
                var durBoost = Math.Min(dur / 60.0, 1.0);
                if (effortPct.HasValue && effortPct.Value < 0.75)
                {
                    confidence = Clamp((int)Math.Round(20.0 + durBoost * 20.0), 15, 35);
                    effortNote = string.Format(CultureInfo.InvariantCulture,
                        "Avg HR was only {0:F0}% of max — this was an easy/Zone 2 run. "
                        + "Daniels assumes race-like effort, so this result will "
                        + "significantly underestimate your actual VO2 max.",
                        effortPct.Value * 100.0);
                }
                else if (effortPct.HasValue && effortPct.Value < 0.85)
                {
                    confidence = Clamp((int)Math.Round(20.0 + durBoost * 40.0), 25, 55);
                    effortNote = string.Format(CultureInfo.InvariantCulture,
                        "Avg HR was {0:F0}% of max — a moderate effort. Result will "
                        + "likely underestimate your VO2 max; most accurate when run "
                        + "at race or threshold intensity.",
                        effortPct.Value * 100.0);
                }
                else if (effortPct.HasValue)
                {
                    confidence = Clamp((int)Math.Round(20.0 + durBoost * 52.0), 35, 72);
                    effortNote = string.Format(CultureInfo.InvariantCulture,
                        "Avg HR was {0:F0}% of max — a hard effort. Result is "
                        + "reasonably accurate; may slightly overestimate if HR was "
                        + "elevated by heat or fatigue rather than pure intensity.",
                        effortPct.Value * 100.0);
                }
                else
                {
                    confidence = Clamp((int)Math.Round(20.0 + durBoost * 35.0), 20, 55);
                    effortNote = "No HR data — cannot assess effort level. Result is only accurate "
                               + "if this was a race or near-maximal effort.";
                }

                estimates.Add(new Vo2Estimate
                {
                    Method = string.Format(CultureInfo.InvariantCulture, "Jack Daniels — Best {0:F0}-min Effort", dur),
                    Value = RoundTo(vo2Max, 1),
                    ConfidencePct = (uint)confidence,
                    Notes = string.Format(CultureInfo.InvariantCulture,
                        "Based on your fastest {0:F0}-minute segment (avg pace {1}:{2:D2} /km). {3}",
                        dur, paceMin, paceSec, effortNote),
                });
                added = true;
                break;
            }

            if (!added && totalDistKm > 0.5)
            {
                var wholeSpeed = totalDistM / totalDurationS * 60.0;
                if (DanielsVo2Max(wholeSpeed, totalDurationMin, out var vo2Max) && vo2Max > 15.0 && vo2Max < 110.0)
                {
                    estimates.Add(new Vo2Estimate
                    {
                        Method = "Jack Daniels — Whole Activity",
                        Value = RoundTo(vo2Max, 1),
                        ConfidencePct = 18,
                        Notes = "Based on average pace across the entire activity. Assumes race-like "
                              + "effort throughout — almost always an underestimate for training runs.",
                    });
                }
            }
        }

        // Method 3: Firstbeat — linear regression of HR vs VO2
        if (hasHr && effectiveMaxHr > 0)
        {
            const double skipS = 180.0;
            var hrLo = effectiveMaxHr * 0.50;
            var hrHi = effectiveMaxHr * 0.97;

            var pairs = new List<(double Hr, double Vo2)>();
            foreach (var s in segs)
            {
                if (s.ElapsedS <= skipS || s.SpeedMpm <= 80.0 || !s.Hr.HasValue) continue;
                var hf = (double)s.Hr.Value;
                if (hf < hrLo || hf > hrHi) continue;
                var vo2 = AcsmVo2(s.SpeedMpm, s.Grade);
                if (vo2 > 0) pairs.Add((hf, vo2));
            }

            if (pairs.Count >= 10)
            {
                var n = (double)pairs.Count;
                double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
                foreach (var p in pairs)
                {
                    sumX += p.Hr; sumY += p.Vo2;
                    sumXY += p.Hr * p.Vo2; sumXX += p.Hr * p.Hr;
                }
                var denom = n * sumXX - sumX * sumX;
                if (Math.Abs(denom) > 1e-10)
                {
                    var m = (n * sumXY - sumX * sumY) / denom;
                    var b = (sumY - m * sumX) / n;
                    var vo2Max = m * effectiveMaxHr + b;
                    if (m > 0 && vo2Max > 15.0 && vo2Max < 110.0)
                    {
                        var yMean = sumY / n;
                        double ssRes = 0, ssTot = 0;
                        foreach (var p in pairs)
                        {
                            var r = p.Vo2 - (m * p.Hr + b);
                            ssRes += r * r;
                            var d = p.Vo2 - yMean;
                            ssTot += d * d;
                        }
                        var rSquared = ssTot > 1e-10 ? Math.Max(0, 1.0 - ssRes / ssTot) : 0;
                        var confidence = Clamp((int)Math.Round(30.0 + rSquared * 53.0), 20, 83);
                        estimates.Add(new Vo2Estimate
                        {
                            Method = "Firstbeat (HR–VO2 Regression)",
                            Value = RoundTo(vo2Max, 1),
                            ConfidencePct = (uint)confidence,
                            Notes = string.Format(CultureInfo.InvariantCulture,
                                "Fits a linear regression across {0} steady-state (HR, VO2) pairs "
                                + "and extrapolates to HRmax (R² = {1:F2}). More robust than the "
                                + "point-by-point method as it uses all data together.",
                                pairs.Count, rSquared),
                        });
                    }
                }
            }
        }

        string? fitnessCat = null, fitnessDesc = null;
        if (estimates.Count > 0)
        {
            var bestIdx = 0;
            for (int i = 1; i < estimates.Count; i++)
                if (estimates[i].ConfidencePct > estimates[bestIdx].ConfidencePct) bestIdx = i;
            (fitnessCat, fitnessDesc) = FitnessCategory(estimates[bestIdx].Value);
        }

        var step = Math.Max(1, segs.Count / 500);
        var chart = new List<ChartPoint>();
        for (int i = 0; i < segs.Count; i += step)
        {
            var s = segs[i];
            double? pace = s.SpeedMpm > 10.0 ? RoundTo(1000.0 / s.SpeedMpm, 2) : null;
            chart.Add(new ChartPoint
            {
                DistanceKm = RoundTo(s.CumDistM / 1000.0, 2),
                PaceMinPerKm = pace,
                Hr = s.Hr,
                ElevationM = s.ElevationM,
            });
        }

        return new AnalysisResult
        {
            TotalDistanceKm = RoundTo(totalDistKm, 2),
            TotalDurationMin = RoundTo(totalDurationMin, 1),
            AvgPaceMinPerKm = RoundTo(avgPace, 2),
            ElevationGainM = Math.Round(elevationGain),
            AvgHr = avgHr,
            MaxHrRecorded = maxHrRecorded,
            HasHrData = hasHr,
            HasElevationData = hasElevation,
            HasTimeData = hasTime,
            PointCount = points.Count,
            Estimates = estimates,
            FitnessCategory = fitnessCat,
            FitnessDescription = fitnessDesc,
            Peak1Km = hasTime ? Best1KmVo2(segs, effectiveMaxHr) : null,
            ChartPoints = chart,
            CardiacDrift = cardiacDrift,
            DecouplingPct = decouplingPct,
            DescentPoints = descentPoints,
        };
    }
}
