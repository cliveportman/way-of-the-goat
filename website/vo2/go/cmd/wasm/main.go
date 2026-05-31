//go:build js && wasm

package main

import (
	"syscall/js"

	vo2 "github.com/wayofthegoat/vo2-go"
)

func main() {
	js.Global().Set("analyze_gpx", js.FuncOf(analyzeGPX))
	// Block forever so the Go runtime stays alive; otherwise registered
	// callbacks are torn down when main returns.
	select {}
}

func analyzeGPX(this js.Value, args []js.Value) any {
	if len(args) < 3 {
		return `{"error":"analyze_gpx expects (gpxContent, weightKg, maxHR)"}`
	}
	return vo2.AnalyzeGPX(args[0].String(), args[1].Float(), uint32(args[2].Int()))
}
