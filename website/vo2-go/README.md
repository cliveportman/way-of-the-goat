# VO2 Max Calculator — Go build

A second implementation of the VO2 calculator, written in Go and compiled to
WebAssembly with TinyGo. Behaviourally identical to the Rust build at
`website/vo2/`; the two ship side-by-side so output and bundle size can be
compared.

## Why two implementations?

- **Bundle size**: Rust (~127 KB) vs TinyGo (~450 KB after `-opt=2 -no-debug`).
  Standard Go would be 2–5 MB, hence TinyGo.
- **Future reuse**: the same Go logic can compile to a server-side handler
  once `api/` comes online — without rewriting the analysis pipeline.

## Prerequisites

- [Go](https://go.dev/dl/) 1.22 or newer
- [TinyGo](https://tinygo.org/getting-started/install/macos/) for the WASM build

```bash
brew install tinygo
```

## Building

```bash
./build.sh
```

This compiles `cmd/wasm/main.go` to `pkg/vo2.wasm` and copies the matching
`wasm_exec.js` shim from TinyGo. The `pkg/` output is committed so the page
can be served without a build step.

## Serving

```bash
python3 -m http.server 8081
```

Then open `http://localhost:8081`. The page must be served over HTTP —
`file://` blocks WASM imports.

## Layout

```
vo2-go/
├── cmd/wasm/main.go      # WASM entry point (registers analyze_gpx)
├── api.go                # AnalyzeGPX — public Go entry, JSON in/out
├── analyze.go            # Segments, VO2 methods, drift, descent
├── gpx.go                # Hand-rolled GPX parser
├── timestamp.go          # ISO 8601 timestamp parser
├── geo.go                # Haversine
├── vo2_formulas.go       # ACSM, Daniels, fitness category, helpers
├── types.go              # Output structs with JSON tags
├── pkg/                  # Build output (vo2.wasm + wasm_exec.js)
├── index.html
├── main.js
├── style.css
└── build.sh
```

## Notes

- The GPX parser is hand-rolled rather than using `encoding/xml` because
  TinyGo's stdlib has limited XML support and we wanted a byte-driven parser
  that handles the same edge cases (CDATA, comments, namespace prefixes) as
  the Rust implementation.
- The Go code targets parity with the Rust output JSON shape so the same
  frontend (rendering, charts) works against either WASM binary.
