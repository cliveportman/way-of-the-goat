# Run Week Row

**Type:** Component
**Figma:** [TODO: Add Figma link]
**JSON Export:** `design-specs/raw/runs-over-time.json` (extracted from screen)
**Created:** 2026-04-04

## Layout

- **Dimensions:** Full width × intrinsic height
- **Direction:** Column (vertical stack)
- **Content:** Header row, then tile row

## Typography

- **Date range:** `MaterialTheme.typography.bodySmall`, `onSurface`
- **Weekly total:** `MaterialTheme.typography.bodySmall`, `onSurface`
- **Distance number:** `MaterialTheme.typography.titleMedium`, `surface` (dark text on bright tile)

## Components

### Header Row

- **Type:** Row, `Arrangement.SpaceBetween`, `Alignment.CenterVertically`
- **Dimensions:** Full width × 20dp
- **Content:**
  - **Date range label** (left) — e.g. "Feb 16-22", bodySmall, `onSurface`
  - **Weekly total** (right) — sum of daily distances with "km" suffix, e.g. "86.6 km", bodySmall, `onSurface`

### Tiles Row

- **Type:** Row
- **Dimensions:** Full width × `GoatSizing.Touch.default` (44dp)
- **Gap:** `GoatSpacing.s4` (4dp) between tiles
- **Spacing from header:** `GoatSpacing.s4` (4dp)
- **Content:** 7 distance tiles, one per day (Mon → Sun)

### Distance Tile

- **Type:** Box with centered text and optional activity dots
- **Dimensions:** `weight(1f)` × `GoatSizing.Touch.default` (44dp)
- **Corner radius:** None (sharp corners)
- **Background:** `MaterialTheme.colorScheme.onSurface` (#F8FAFC) for data tiles
- **Content:**
  - Distance number text — centered horizontally and vertically
  - Activity count dots — bottom-right corner
- **States:**
  - **Has data:** `onSurface` background + distance text + activity dots
  - **No data (blank):** `MaterialTheme.goatColors.surfaceContainerHigh` (#1D293D) background, no text, no dots

#### Activity Count Dots

- **Size:** 4dp diameter per dot
- **Colour:** `MaterialTheme.colorScheme.surface` (#020618)
- **Position:** Bottom-right corner of tile, with ~4dp padding from right and bottom edges
- **Layout:** Horizontal row of dots, ~2dp gap between dots
- **Count:** Matches the number of individual activities recorded that day (1 dot = 1 activity, 2 dots = 2 activities, 3 dots = 3 activities, etc.)
- **Max display:** If more than 3 activities, show 3 dots (cap visual indicator at 3)

## States

- **Default:** Header row + 7 tiles, each with distance or blank
- **All blank:** A week with no activities at all — 7 blank tiles, weekly total shows "0 km" or is absent
- **Partial week:** Some tiles have data, others are blank (e.g. Jan 12-18 in the design)

## Interaction

- None defined — tiles are display-only in this design
- **Accessibility:** Each tile should have a content description: "{day name}: {value} km, {n} activities" or "{day name}: no activity". The row should be grouped with the date range as a semantic label.

## Design Tokens Used

```json
{
  "surface": "#020618",
  "onSurface": "#F8FAFC",
  "surfaceContainerHigh": "#1D293D"
}
```

## Deviations

Record any deliberate differences between this spec and the implementation. This prevents code reviews from re-flagging intentional changes.

| Value | Spec | Implemented | Reason |
|-------|------|-------------|--------|

_(Empty until implementation. Add rows when a value is intentionally changed during or after implementation.)_

## Implementation Notes

- **Tile distribution:** Use `weight(1f)` for each tile so they evenly divide available width, with `GoatSpacing.s4` gap between them. The Figma shows fixed 44dp tiles on a 375dp screen; in implementation, tiles should flex to fill width.
- **Distance number positioning:** Implement as text centered inside each tile Box, matching the `score-week-row` pattern.
- **Activity dots:** Use a small `Row` of `Box(Modifier.size(4.dp).clip(CircleShape).background(...))` positioned with `Modifier.align(Alignment.BottomEnd).padding(4.dp)` inside the tile Box.
- **Data model:** The component receives a list of 7 `DayActivity?` values (nullable for blank days) plus a date range string and weekly total Double. Each `DayActivity` contains `distance: Double` and `activityCount: Int`.
- **Number formatting:** Display distance with one decimal place (e.g. "14.2"), except whole numbers which may omit the decimal (e.g. "8" not "8.0" — confirm from data). The Figma shows both "8" and "8.3" patterns.
- **Weekly total formatting:** "{total} km" with one decimal place where needed (e.g. "86.6 km", "77 km").
