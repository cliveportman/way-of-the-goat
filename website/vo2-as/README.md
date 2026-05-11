# VO2 Max Calculator — AssemblyScript build

Fifth implementation of the VO2 calculator, written in AssemblyScript and
compiled to WebAssembly. Same algorithm and JSON output shape as the
Rust / TinyGo / .NET / pure-JS builds, so the frontend is the same.

## Why AssemblyScript?

It's TypeScript syntax that compiles directly to WebAssembly with a
minimal runtime — far smaller than .NET's Mono runtime or Go's runtime,
yet the source reads like the pure-JS port. The interesting comparison is:

- vs **pure JS**: same surface syntax, what does crossing the WASM barrier
  buy us?
- vs **Rust / TinyGo**: how much smaller can a port get without sacrificing
  algorithmic structure?

The current binary is **~46 KB** — the smallest of all five.

## Prerequisites

- Node 18+ and npm

```bash
npm install
```

## Building

```bash
npm run asbuild:release
```

Outputs:
- `build/release.wasm` — the optimised binary (~46 KB)
- `build/release.js` — auto-generated ESM loader with top-level await
- `build/release.d.ts` — TypeScript declarations

## Serving

```bash
python3 -m http.server 8084
```

Then open `http://localhost:8084`.

## Layout

```
vo2-as/
├── assembly/
│   ├── index.ts          # Exported analyze_gpx entry point
│   ├── analyze.ts        # Segments, VO2 methods, drift, descent
│   ├── gpx.ts            # Hand-rolled GPX parser
│   ├── timestamp.ts      # ISO 8601 timestamp parser
│   ├── geo.ts            # Haversine
│   ├── vo2-formulas.ts   # ACSM, Daniels, fitness, rounding, formatFixed
│   ├── types.ts          # AnalysisResult etc.
│   ├── serialize.ts      # Hand-rolled JSON encoder
│   └── json.ts           # JSON encoding primitives
├── test/smoke.mjs        # End-to-end smoke test (node test/smoke.mjs)
├── build/                # asc output
├── asconfig.json
├── package.json
├── index.html
├── main.js
└── style.css
```

## Notes

- AssemblyScript doesn't ship a `JSON.stringify` — `serialize.ts` and
  `json.ts` encode the result object by hand. Cheap given the schema is
  fixed.
- AS doesn't support `try`/`catch` by default (WASM exception handling
  is experimental), so the GPX parser returns a `ParseResult` instead of
  throwing.
- AS strings are UTF-16 like JS, which makes string ops match the
  pure-JS port behaviourally. The asc loader handles the UTF-16 lift
  between WASM linear memory and JS strings automatically.
