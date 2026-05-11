#!/usr/bin/env bash
# Build the VO2 calculator to WebAssembly using TinyGo.
# Run from this directory.
set -euo pipefail

cd "$(dirname "$0")"

if ! command -v tinygo >/dev/null 2>&1; then
  echo "error: tinygo is not installed. Install with: brew install tinygo" >&2
  exit 1
fi

mkdir -p pkg

# -no-debug strips DWARF; -opt=2 favours speed at modest size cost.
tinygo build -o pkg/vo2.wasm -target wasm -no-debug -opt=2 ./cmd/wasm

# Copy the wasm_exec.js shim that pairs with this TinyGo version.
cp "$(tinygo env TINYGOROOT)/targets/wasm_exec.js" pkg/wasm_exec.js

echo "Built pkg/vo2.wasm ($(du -h pkg/vo2.wasm | cut -f1))"
