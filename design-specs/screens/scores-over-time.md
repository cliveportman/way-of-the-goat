# Scores Over Time

**Type:** Screen
**Figma:** [TODO: Add Figma link]
**Created:** 2026-04-02

## Overview

Shows the user's daily diet quality scores over time as a scrollable weekly heatmap grid. Each week displays 7 day tiles coloured by score tier, with the date range and weekly total. Replaces the existing Activity screen as the second tab in bottom navigation.

## Used In Flows

None — standalone screen (bottom nav tab).

## Layout

- **Structure:** Column with fixed header (title + day-of-week labels), then scrollable LazyColumn of week rows
- **Content padding:** `GoatSpacing.s12` (12dp) horizontal, `GoatSpacing.s32` (32dp) top
- **Section spacing:** `GoatSpacing.s16` (16dp) between title and day headers; `GoatSpacing.s16` (16dp) between day headers and first week row
- **Row spacing:** `GoatSpacing.s12` (12dp) between week rows in LazyColumn

## Section Layout

| Order | Section | Component Spec | Visibility Condition |
|-------|---------|---------------|---------------------|
| 1 | Screen title — "Your scores" | Plain Text composable | Always |
| 2 | Day-of-week headers (M T W T F S S) | Inline Row of 7 Text | Always (fixed, not scrolled) |
| 3 | Weekly score rows | [score-week-row](../components/score-week-row.md) × N | Per week with data |

## Section Details

### Screen Title

- **Text:** "Your scores"
- **Style:** `MaterialTheme.typography.headlineLarge`, `onSurface`
- **Position:** Top-left, below status bar safe area
- **Note:** The Figma uses Roboto Bold 28sp; the design system maps this to headlineLarge (Inter SemiBold 28sp)

### Day-of-Week Headers

- **Type:** Row matching the tile grid column positions
- **Labels:** M, T, W, T, F, S, S
- **Style:** `MaterialTheme.typography.titleMedium`, `onSurfaceVariant` (#90A1B9)
- **Alignment:** Each label centered within its column (matching tile width)
- **Sticky:** These headers remain fixed while the week rows scroll beneath them

### Weekly Score Rows

- **Component:** [score-week-row](../components/score-week-row.md)
- **Ordering:** Most recent week at top, older weeks below
- **Scrolling:** LazyColumn, vertically scrollable to reveal older weeks
- **Item spacing:** `GoatSpacing.s4` (4dp) between rows

## Screen States

### `default`

All sections visible. LazyColumn populated with week rows from available score data. Most recent week at top.

### `loading`

Title and day-of-week headers visible. Week row area shows a loading indicator or skeleton placeholders.

### `empty`

Title and day-of-week headers visible. Week row area shows empty state message (e.g. "No scores yet. Start logging food to see your scores here."). [TODO: Confirm empty state design]

## Props / Inputs

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| weeklyScores | `List<WeekScoreData>` | yes | List of weeks, each containing date range, 7 day scores (nullable), and weekly total |

## Callbacks / Outputs

| Callback | Signature | Description |
|----------|-----------|-------------|
| — | — | No callbacks defined — display-only screen |

## Design Tokens Used

Screen-level layout tokens only (child component tokens in score-week-row spec).

```json
{
  "surface": "#020618",
  "onSurface": "#F8FAFC",
  "onSurfaceVariant": "#90A1B9"
}
```

## Implementation Notes

- **Composition:** Fixed header (title + day labels) above a scrollable LazyColumn of `ScoreWeekRow` composables. Use `Column` for the fixed portion and `LazyColumn` for the scrollable week rows.
- **Data source:** ViewModel exposes `StateFlow<ScoresOverTimeUiState>` with Loading/Success/Error sealed states. The ViewModel queries daily scores from the repository, groups them by week (Mon-Sun), calculates weekly totals, and sorts most recent first.
- **Navigation:** Route `"scores_over_time"` in the existing NavHost. Second item in `bottomNavItems` (label: "History"). No route params needed.
- **Day-of-week header alignment:** The header labels must align precisely with the tile columns in the week rows below. Use the same `weight(1f)` distribution and `GoatSpacing.s4` gap as the tiles.
- **Accessibility:** Screen title serves as the screen heading. Each week row should be a semantically grouped element. Day headers provide column context for screen readers.
- **Scroll performance:** Use `LazyColumn` with `key` set to the week's date range for stable item identity during recomposition.
