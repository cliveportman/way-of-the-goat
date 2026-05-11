# VO2 Max Calculator — JS build

Pure JavaScript implementation of the VO2 calculator, no WebAssembly at all.
Sits alongside the Rust, TinyGo, and .NET WASM ports as the baseline for
comparison.

## Why?

The interesting questions are:

1. **Is the WASM round-trip even worth it?** WebAssembly buys raw arithmetic
   speed and predictable layout but pays a constant-cost crossing every
   call. For an analysis that runs once per click, the answer is rarely
   obvious until you measure.
2. **How small can the shipped code be?** The JS modules total **~24 KB** raw;
   compared to Rust's ~127 KB WASM, TinyGo's ~448 KB, and .NET's ~8 MB.

## Running locally

No build step. Just serve:

```bash
python3 -m http.server 8083
```

Then open `http://localhost:8083`.

## Layout

```
vo2-js/
├── vo2/
│   ├── index.js          # Public entry: analyzeGpx
│   ├── analyze.js        # Segments, VO2 methods, drift, descent
│   ├── gpx.js            # Hand-rolled GPX parser
│   ├── timestamp.js      # ISO 8601 timestamp parser
│   ├── geo.js            # Haversine
│   └── vo2-formulas.js   # ACSM, Daniels, fitness category, rounding helpers
├── index.html
├── main.js               # UI + chart rendering
└── style.css
```

## Notes

- The analyser returns the result **object directly**, skipping the
  `JSON.stringify` / `JSON.parse` round-trip the WASM builds need. This
  is the most honest measurement of JS's intrinsic compute speed.
- Rounding uses an explicit half-away-from-zero implementation so output
  matches the Rust/Go/.NET ports bit-for-bit on positive and negative
  midpoints — JS's built-in `Math.round` rounds half-toward-positive-infinity
  which would diverge on negative half-integers.
