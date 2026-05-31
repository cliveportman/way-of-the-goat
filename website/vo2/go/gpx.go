package vo2

import (
	"fmt"
	"strconv"
	"strings"
)

const maxTrackPoints = 200_000

func parseAttrF64(tag, attr string) (float64, bool) {
	pat := attr + `="`
	idx := strings.Index(tag, pat)
	if idx < 0 {
		return 0, false
	}
	rest := tag[idx+len(pat):]
	end := strings.IndexByte(rest, '"')
	if end < 0 {
		return 0, false
	}
	v, err := strconv.ParseFloat(rest[:end], 64)
	if err != nil {
		return 0, false
	}
	return v, true
}

func localTagName(tag string) string {
	if len(tag) == 0 {
		return ""
	}
	switch tag[0] {
	case '/', '!', '?':
		return ""
	}
	name := tag
	if sp := strings.IndexAny(name, " \t\r\n"); sp >= 0 {
		name = name[:sp]
	}
	name = strings.TrimRight(name, "/")
	if colon := strings.LastIndexByte(name, ':'); colon >= 0 {
		name = name[colon+1:]
	}
	return name
}

// parseGPX is a byte-driven, namespace-tolerant GPX parser. Mirrors the Rust
// implementation: we only extract trkpt elements and their ele/time/hr children.
// Comments and CDATA are skipped. Anything malformed past the first valid trkpts
// is silently dropped — the goal is best-effort extraction, not validation.
func parseGPX(content string) ([]TrackPoint, error) {
	points := make([]TrackPoint, 0, 1024)
	var current *TrackPoint

	bytes := []byte(content)
	i := 0
	for i < len(bytes) {
		if bytes[i] != '<' {
			i++
			continue
		}
		tagStart := i + 1
		// CDATA
		if tagStart+8 <= len(bytes) && string(bytes[tagStart:tagStart+8]) == "![CDATA[" {
			off := strings.Index(content[i:], "]]>")
			if off < 0 {
				return nil, fmt.Errorf("Unterminated CDATA section")
			}
			i += off + 3
			continue
		}
		// Comment
		if tagStart+3 <= len(bytes) && string(bytes[tagStart:tagStart+3]) == "!--" {
			off := strings.Index(content[i:], "-->")
			if off < 0 {
				return nil, fmt.Errorf("Unterminated comment")
			}
			i += off + 3
			continue
		}
		// Find closing '>'
		j := tagStart
		for j < len(bytes) && bytes[j] != '>' {
			j++
		}
		if j >= len(bytes) {
			break
		}
		tag := content[tagStart:j]
		i = j + 1

		local := localTagName(tag)

		if local == "trkpt" {
			lat, latOK := parseAttrF64(tag, "lat")
			lon, lonOK := parseAttrF64(tag, "lon")
			if latOK && lonOK {
				current = &TrackPoint{Lat: lat, Lon: lon}
			}
		} else if strings.HasPrefix(tag, "/trkpt") {
			if current != nil {
				points = append(points, *current)
				current = nil
				if len(points) > maxTrackPoints {
					return nil, fmt.Errorf(
						"GPX file exceeds %d track points — file is too large to analyse",
						maxTrackPoints,
					)
				}
			}
		} else if current != nil && local != "" {
			switch local {
			case "ele", "time", "hr":
				textStart := i
				for i < len(bytes) && bytes[i] != '<' {
					i++
				}
				text := strings.TrimSpace(content[textStart:i])
				if text == "" {
					continue
				}
				switch local {
				case "ele":
					if v, err := strconv.ParseFloat(text, 64); err == nil {
						current.Ele = &v
					}
				case "time":
					if v, ok := parseTimestamp(text); ok {
						current.TimeS = &v
					}
				case "hr":
					if v, err := strconv.ParseUint(text, 10, 32); err == nil {
						u := uint32(v)
						current.HR = &u
					}
				}
			}
		}
	}

	return points, nil
}
