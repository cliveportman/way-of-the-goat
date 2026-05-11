# VO2 Max Calculator — Kotlin/Wasm build

Sixth implementation of the VO2 calculator. Same algorithm and JSON shape
as the Rust / TinyGo / .NET / pure-JS / AssemblyScript ports — this one is
written in Kotlin and compiled to WebAssembly via Kotlin/Wasm (Kotlin 2.2).

## Why Kotlin/Wasm?

The mobile app in this repo (`mobile/`) is Kotlin Multiplatform, so the
same business logic can in principle compile to web WASM too. The relevant
questions:

1. **How big is the binary?** Surprisingly small: **~131 KB** WASM with
   `kotlinx.serialization` for JSON encoding. Comparable to Rust (130 KB).
   Kotlin/Wasm's tree-shaker + binaryen optimisation are aggressive.
2. **How readable is the source vs the other ports?** Idiomatic Kotlin —
   data classes, nullable types, `when`, `mapNotNull`, ranges. Closer in
   shape to the pure-JS port than to the Rust port.
3. **What's the runtime cost?** Kotlin/Wasm uses the WasmGC and Exception
   Handling proposals — supported in modern Chrome/Firefox/Safari, but not
   older browsers. Compute time should be in the same ballpark as Rust.

## Prerequisites

- JDK 17 (the repo's mobile project uses Zulu 17)
- The Gradle wrapper handles everything else

## Building

```bash
./build.sh
```

This runs `./gradlew wasmJsBrowserProductionWebpack` and stages the
artefacts in `dist/`. First build downloads NPM, binaryen, etc. and
takes ~1 min; incremental builds are seconds.

## Serving

```bash
python3 -m http.server 8085
```

Then open `http://localhost:8085`.

> The page must be served over HTTP — the `dist/vo2.js` shim instantiates
> the WASM via `WebAssembly.instantiateStreaming(fetch(...))` which is
> blocked on `file://`.

## Layout

```
vo2-kt/
├── src/wasmJsMain/kotlin/
│   ├── Main.kt              # @JsExport analyze_gpx entry point
│   ├── Analyze.kt           # Segments, VO2 methods, drift, descent
│   ├── Gpx.kt               # Hand-rolled GPX parser
│   ├── Timestamp.kt         # ISO 8601 timestamp parser
│   ├── Geo.kt               # Haversine
│   ├── Vo2Formulas.kt       # ACSM, Daniels, fitness, rounding, formatFixed
│   └── Types.kt             # Data classes with kotlinx.serialization annotations
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── build/                   # Gradle output
├── dist/                    # Staged webpack output (built by build.sh)
├── index.html
├── main.js
└── style.css
```

## Notes

- Output JSON shape matches all the other ports via `@SerialName` field
  annotations. `kotlinx.serialization` source-generates the encoder, so
  no reflection is needed at runtime — which is what keeps the WASM small.
- The Kotlin/Wasm target uses the **WasmGC** and **Exception Handling**
  proposals. Older browsers without these will fail to instantiate. As of
  early 2026 every major desktop browser supports them by default.
- Currently using webpack's UMD output. A future improvement would be to
  switch to ESM (`output.library.type = "module"`) so the page can `import`
  the module instead of relying on `globalThis["vo2-kt"]`.
