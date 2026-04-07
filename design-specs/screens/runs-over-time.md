# Runs Over Time

**Type:** Screen
**Figma:** [TODO: Add Figma link]
**Created:** 2026-04-04

## Overview

Shows the user's daily endurance activity distances over time as a scrollable weekly grid. Each week displays 7 day tiles showing total distance run, with the date range and weekly total in km. Replaces the existing Progress screen as the third tab in bottom navigation (label: "Runs").

## Used In Flows

None — standalone screen (bottom nav tab).

## Layout

- **Structure:** Column with fixed header (title + day-of-week labels), then scrollable LazyColumn of week rows
- **Content padding:** `GoatSpacing.s12` (12dp) horizontal, `GoatSpacing.s32` (32dp) top
- **Section spacing:** `GoatSpacing.s16` (16dp) between title and day headers; `GoatSpacing.s16` (16dp) between day headers and first week row
- **Row spacing:** `GoatSpacing.s24` (24dp) between week rows in LazyColumn

## Section Layout

| Order | Section | Component Spec | Visibility Condition |
|-------|---------|---------------|---------------------|
| 1 | Screen title — "Your endurance activities" | Plain Text composable | Always |
| 2 | Day-of-week headers (M T W T F S S) | Inline Row of 7 Text | Always (fixed, not scrolled) |
| 3 | Weekly distance rows | [run-week-row](../components/run-week-row.md) × N | Per week with data |

## Section Details

### Screen Title

- **Text:** "Your endurance activities"
- **Style:** `MaterialTheme.typography.headlineLarge`, `onSurface`
- **Position:** Top-left, below status bar safe area
- **Note:** The Figma uses Roboto Bold 28sp; the design system maps this to headlineLarge (Inter SemiBold 28sp)

### Day-of-Week Headers

- **Type:** Row matching the tile grid column positions
- **Labels:** M, T, W, T, F, S, S
- **Style:** `MaterialTheme.typography.titleMedium`, `onSurfaceVariant` (#90A1B9)
- **Alignment:** Each label centered within its column (matching tile width)
- **Sticky:** These headers remain fixed while the week rows scroll beneath them

### Weekly Distance Rows

- **Component:** [run-week-row](../components/run-week-row.md)
- **Ordering:** Most recent week at top, older weeks below
- **Scrolling:** LazyColumn, vertically scrollable to reveal older weeks
- **Item spacing:** `GoatSpacing.s24` (24dp) between rows

## Screen States

### `default`

All sections visible. LazyColumn populated with week rows from available activity data. Most recent week at top.

### `loading`

Title and day-of-week headers visible. Week row area shows a loading indicator or skeleton placeholders.

### `empty`

Title and day-of-week headers visible. Week row area shows empty state message (e.g. "No runs yet. Start logging activities to see your distance here."). [TODO: Confirm empty state design]

## Props / Inputs

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| weeklyActivities | `List<WeekActivityData>` | yes | List of weeks, each containing date range string, 7 day activities (nullable), and weekly total distance |

## Callbacks / Outputs

| Callback | Signature | Description |
|----------|-----------|-------------|
| — | — | No callbacks defined — display-only screen |

## Design Tokens Used

Screen-level layout tokens only (child component tokens in run-week-row spec).

```json
{
  "surface": "#020618",
  "onSurface": "#F8FAFC",
  "onSurfaceVariant": "#90A1B9"
}
```

## Deviations

Record any deliberate differences between this spec and the implementation. This prevents code reviews from re-flagging intentional changes.

| Value | Spec | Implemented | Reason |
|-------|------|-------------|--------|

_(Empty until implementation. Add rows when a value is intentionally changed during or after implementation.)_

## Implementation Notes

- **Composition:** Fixed header (title + day labels) above a scrollable LazyColumn of `RunWeekRow` composables. Use `Column` for the fixed portion and `LazyColumn` for the scrollable week rows.
- **Data source:** ViewModel exposes `StateFlow<RunsOverTimeUiState>` with Loading/Success/Empty sealed states. Reuses the existing activity data currently fetched on the Progress screen — no new data layer work needed.
- **Navigation:** Replaces the Progress screen route. Third item in `bottomNavItems` (label: "Runs"). No route params needed.
- **Day-of-week header alignment:** The header labels must align precisely with the tile columns in the week rows below. Use the same `weight(1f)` distribution and `GoatSpacing.s4` gap as the tiles.
- **Accessibility:** Screen title serves as the screen heading. Each week row should be a semantically grouped element. Day headers provide column context for screen readers.
- **Scroll performance:** Use `LazyColumn` with `key` set to the week's date range for stable item identity during recomposition.
