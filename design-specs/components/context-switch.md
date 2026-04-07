# Context Switch

**Type:** Component
**Figma:** [TODO: Add Figma link]
**JSON Export:** `design-specs/raw/context-switch-raw.json`
**Created:** 2026-02-26

## Layout

- **Dimensions:** Full width (fillMaxWidth) x intrinsic height
- **Padding:** 16dp all sides
- **Direction:** Column (vertical stack)
- **Gap:** 32dp between title and segmented control
- **Alignment:** Start (leading edge)

## Typography

- **Title:** titleMedium (18sp, Inter Bold), onSurface (#F8FAFC)
- **Option label (inactive):** bodyLarge (16sp, Inter Bold), secondary (#90A1B9)
- **Option label (active):** bodyLarge (16sp, Inter Bold), surface (#0F172B)
- **Submit:** bodyLarge (16sp, Inter Bold), onSurface (#F8FAFC)
- **Cancel:** bodyLarge (16sp, Inter Bold), secondary (#90A1B9)

## Components

### Title

- **Content:** "Context switch" (placeholder — replaced by caller)
- **Style:** titleMedium, onSurface

### Segmented Control

- **Type:** Toggle selector with two options
- **Dimensions:** Full width x 52dp
- **Background (track):** outlineVariant (#314158)
- **Corner radius:** [TODO: Check Figma for border radius]
- **Padding:** 4dp all sides
- **Gap:** 4dp between options

#### Option (Inactive State)

- **Dimensions:** Equal share of track width (168dp) x 44dp
- **Background:** surface (#0F172B)
- **Padding:** 8dp vertical, 16dp horizontal
- **Text:** bodyLarge, secondary (#90A1B9)
- **Alignment:** Center (both axes)

#### Option (Active/Selected State)

- **Dimensions:** Equal share of track width (168dp) x 44dp
- **Background:** onSurface (#F8FAFC)
- **Padding:** 8dp vertical, 16dp horizontal
- **Text:** bodyLarge, surface (#0F172B)
- **Alignment:** Center (both axes)

### Submit Action

- **Content:** "Submit" text
- **Style:** bodyLarge, onSurface (#F8FAFC)
- **Action:** Confirms the selection and applies the context switch

### Cancel Action

- **Content:** "Cancel" text
- **Style:** bodyLarge, secondary (#90A1B9)
- **Action:** Dismisses without applying any change

## States

- **Default:** One option selected (shown with light bg), other inactive (dark bg)
- **Pressed option:** [TODO: Define pressed state overlay]
- **Submit disabled:** [TODO: Define style when no change has been made]

## Interaction

- Tapping an option selects it — visual toggle moves the active style to the tapped option
- **Submit:** Confirms the selected option and triggers the context switch callback
- **Cancel:** Dismisses without making any changes
- **Accessibility:** Options should use `selectable` semantics with role description "option". Submit/Cancel should be buttons.

## Design Tokens Used

```json
{
  "background": "#020618",
  "onSurface": "#F8FAFC",
  "outlineVariant": "#314158",
  "surface": "#0F172B",
  "secondary": "#90A1B9"
}
```

## Implementation Notes

- Use `SingleChoiceSegmentedButtonRow` from Material3 if available, or build a custom segmented control with `Row` + `Surface` for each option
- Option labels are dynamic — the component should accept a list of option strings
- The active option gets an inverted color scheme (light bg, dark text) vs inactive (dark bg, muted text)
- Submit and Cancel are text-only actions — consider placing them in a `Row` with `Arrangement.spacedBy` or making Submit a `TextButton` and Cancel a secondary `TextButton`
- The Figma shows Submit and Cancel as separate text elements below the segmented control with no container — confirm whether these should be styled as `TextButton` composables
- Ensure the segmented control evenly divides available width between options (use `weight(1f)`)
- Note: #314158 maps to `outlineVariant` — this may need to be added to the `DarkColorScheme` in Theme.kt if not already present
