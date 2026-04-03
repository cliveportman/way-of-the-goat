# Score Week Row

**Type:** Component
**Figma:** [TODO: Add Figma link]
**JSON Export:** `design-specs/raw/scores-over-time-screen.json` (extracted from screen)
**Created:** 2026-04-02

## Layout

- **Dimensions:** Full width × intrinsic height
- **Direction:** Column (vertical stack)
- **Content:** Header row, then tile row

## Typography

- **Date range:** `MaterialTheme.typography.bodySmall`, `onSurface`
- **Weekly total:** `MaterialTheme.typography.bodySmall`, `onSurface`
- **Score number:** `MaterialTheme.typography.titleMedium`, `surface` (dark text on bright tile)

## Components

### Header Row

- **Type:** Row, `Arrangement.SpaceBetween`, `Alignment.CenterVertically`
- **Dimensions:** Full width × 20dp
- **Content:**
  - **Date range label** (left) — e.g. "Feb 16-22", bodySmall, `onSurface`
  - **Weekly total** (right) — sum of daily scores, bodySmall, `onSurface`

### Tiles Row

- **Type:** Row
- **Dimensions:** Full width × `GoatSizing.Touch.default` (44dp)
- **Gap:** `GoatSpacing.s4` (4dp) between tiles
- **Spacing from header:** `GoatSpacing.s4` (4dp)
- **Content:** 7 score tiles, one per day (Mon → Sun)

### Score Tile

- **Type:** Box with centered text
- **Dimensions:** `GoatSizing.Touch.default` × `GoatSizing.Touch.default` (48 × 48dp)
- **Corner radius:** None (sharp corners)
- **Background:** Determined by score value (see colour rules below)
- **Content:** Score number text, centered horizontally and vertically
- **States:**
  - **Has score:** Coloured background + score number text
  - **No score (blank):** `MaterialTheme.goatColors.surfaceContainerHigh` background, no text

#### Tile Colour Rules

| Score Range | Token | Hex (dark) |
|-------------|-------|------------|
| ≤ 0 | `MaterialTheme.goatColors.scoreMinus3` | #FB2C36 |
| 1–10 | `MaterialTheme.goatColors.scoreMinus1` | #FFB86A |
| 11–20 | `MaterialTheme.goatColors.score0` | #7BF1A8 |
| 21+ | `MaterialTheme.goatColors.scorePlus2` | #9AE600 |
| No data | `MaterialTheme.goatColors.surfaceContainerHigh` | #1D293D |

Score number text uses `MaterialTheme.goatColors.surface` (#020618 dark) — dark text provides contrast against all four tile colours.

## States

- **Default:** Header row + 7 tiles, each with score or blank
- **All blank:** A week with no scores at all — 7 blank tiles, weekly total shows "0" or is absent
- **Partial week:** Some tiles have scores, others are blank (shown in the design for Jan 12-18)

## Interaction

- None defined — tiles are display-only in this design
- **Accessibility:** Each tile should have a content description: "{day name}: score {value}" or "{day name}: no score". The row should be grouped with the date range as a semantic label.

## Design Tokens Used

```json
{
  "surface": "#020618",
  "onSurface": "#F8FAFC",
  "surfaceContainerHigh": "#1D293D",
  "scorePlus2": "#9AE600",
  "score0": "#7BF1A8",
  "scoreMinus1": "#FFB86A",
  "scoreMinus3": "#FB2C36"
}
```

## Implementation Notes

- **Tile distribution:** Use `weight(1f)` for each tile so they evenly divide available width, with `GoatSpacing.s4` gap between them. The Figma shows fixed 44dp tiles on a 375dp screen; in implementation, tiles should flex to fill width.
- **Score number positioning:** The Figma JSON places score numbers above tiles in Y-coordinates, but uses dark text (`surface` colour) that's only legible on the tile backgrounds. Implement as text centered inside each tile Box.
- **Data model:** The component receives a list of 7 `DayScore?` values (nullable for blank days) plus a date range string and weekly total Int.
- **Edge cases:** Negative scores (e.g. "-4") must display correctly — ensure tile width accommodates minus sign with titleMedium text.
