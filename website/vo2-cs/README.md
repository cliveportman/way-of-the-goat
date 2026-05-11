# VO2 Max Calculator — .NET build

Third implementation of the VO2 calculator, written in C# / .NET 10 and
compiled to WebAssembly using the `wasmbrowser` template with AOT + full
trim. Sits alongside the Rust (`website/vo2/`) and Go (`website/vo2-go/`)
implementations and matches their JSON output shape so the same frontend
drives all three.

## Why three implementations?

The interesting comparison is bundle size and runtime cost — same algorithm,
three toolchains.

| Build  | WASM size (approx) |
|--------|-------------------:|
| Rust   | 127 KB             |
| TinyGo | 448 KB             |
| .NET   | ~1.8–2.5 MB after AOT (runtime overhead is the floor) |

The .NET build is meaningfully heavier because it ships the Mono runtime
plus a trimmed BCL. AOT keeps it from being multi-megabyte; without it
the same project publishes at 5+ MB.

## Prerequisites

- [.NET 10 SDK](https://dotnet.microsoft.com/download)
- `wasm-tools` workload + WebAssembly templates package:

```bash
sudo dotnet workload install wasm-tools
dotnet new install Microsoft.NET.Runtime.WebAssembly.Templates
```

## Building

```bash
./build.sh
```

This publishes to `./dist/wwwroot/`. AOT publish takes a few minutes the
first time; subsequent builds are faster.

## Serving

```bash
cd dist/wwwroot
python3 -m http.server 8082
```

Then open `http://localhost:8082`. The page must be served over HTTP; the
.NET runtime fetches additional files at startup that the browser will
block on the `file://` protocol.

## Layout

```
vo2-cs/
├── Program.cs              # [JSExport] entry — Vo2Api.AnalyzeGpx
├── Analyze.cs              # Segments, VO2 methods, drift, descent
├── Gpx.cs                  # Hand-rolled GPX parser
├── Timestamp.cs            # ISO 8601 timestamp parser
├── Geo.cs                  # Haversine
├── Vo2Formulas.cs          # ACSM, Daniels, fitness category, helpers
├── Types.cs                # DTOs + source-generated JSON context
├── Vo2Cs.csproj            # AOT + trim + InvariantGlobalization
├── wwwroot/                # index.html, main.js, style.css
├── dist/                   # publish output (built by build.sh)
└── build.sh
```

## Notes

- `InvariantGlobalization=true` and `Nullable=enable` are on. ICU is not
  shipped — string parsing uses the invariant culture explicitly.
- JSON serialisation goes through a source-generated `JsonSerializerContext`
  (`Vo2JsonContext`) so the trimmer can prove System.Text.Json doesn't need
  reflection metadata.
- The `[JSExport]` attribute on `Vo2Api.AnalyzeGpx` is how the JS side
  reaches into the C# code. The wasmbrowser SDK generates the binding code
  at build time.
