---
name: design-specs
description: Formats for design spec files (component, screen, flow) and Figma token to Compose mapping. Read before creating or implementing any spec.
---

# Design Specs Skill

Reference for creating and consuming design specs — the markdown files that bridge Figma designs and Compose implementation. Read before generating or implementing any spec.

---

## File Structure

Design specs live at the repository root (shared across all future sub-projects):

```
design-specs/
├── raw/                    # Figma JSON exports (via plugin)
│   └── score-card.json
├── flows/                  # Multi-screen user journey specs
│   ├── onboarding.md
│   └── onboarding-mermaid.md
├── screens/                # Specs for full screen layouts
│   └── scores-screen.md
├── components/             # Specs for reusable UI composables
│   └── score-card.md
└── tokens.json             # Design token values (semantic name → hex)
```

---

## Spec Types and Their Relationships

| Type | Directory | Purpose |
|------|-----------|---------|
| **Component** | `components/` | Reusable `@Composable` function (card, button, field, list item) |
| **Screen** | `screens/` | Full screen layout composed of components |
| **Flow** | `flows/` | Multi-screen user journey with branching and shared state |
| **Flow Diagram** | `flows/` | Mermaid diagram paired with the flow spec (`-mermaid` suffix) |

Specs form a hierarchy with relative links:

```
flows/onboarding.md
  ├── → screens/welcome.md
  ├── → screens/profile-form.md
  └── → screens/confirmation.md
              ├── → components/primary-button.md
              └── → components/text-field.md
```

All specs are created and implemented via `/design-to-code`.

---

## Component Spec Format

````markdown
# {Component Name}

**Type:** Component
**Figma:** {link or [TODO: Add Figma link]}
**JSON Export:** `design-specs/raw/{filename}.json`
**Created:** {YYYY-MM-DD}

## Layout

- **Dimensions:** {width × height in dp, or "Full width × {n}dp"}
- **Content padding:** {dp values, e.g. 13dp horizontal, 9dp vertical}
- **Content:** {brief description of internal layout}

## Typography

- **{Role}:** `MaterialTheme.typography.{slot}`, `{token name}`
- **{Role}:** `MaterialTheme.typography.{slot}`, `{token name}`

## Components

### {Sub-element Name}

- **Type:** {e.g. Surface, Row, Icon}
- **Dimensions:** {dp values}
- **Background:** `MaterialTheme.goatColors.{token}` ({hex for reference})
- **Border:** `BorderStroke({n}.dp, MaterialTheme.goatColors.{token})` or "none"
- **Padding:** `Modifier.padding(...)` values in dp
- **Corner radius:** `GoatRadius.{size}` or `RoundedCornerShape({n}.dp)`
- **Content:** {what's inside}
- **States:**
  - Default: as shown
  - Pressed: {change, e.g. `interactionSource` ripple effect}
  - Disabled: {change}
- **Action:** {what happens on interaction — lambda name}

## Design Tokens Used

```json
{
  "surface": "#010517",
  "onSurface": "#F8FAFC"
}
```

## Deviations

Record any deliberate differences between this spec and the implementation. This prevents code reviews from re-flagging intentional changes.

| Value | Spec | Implemented | Reason |
|-------|------|-------------|--------|

_(Empty until implementation. Add rows when a value is intentionally changed during or after implementation.)_

## Implementation Notes

- **Accessibility:** {contentDescription, semantics, minimum touch target}
- **Edge cases:** {anything to handle}
- **Animations:** {transitions if any}
````

---

## Screen Spec Format

````markdown
# {Screen Name}

**Type:** Screen
**Figma:** {link or [TODO: Add Figma link]}
**Created:** {YYYY-MM-DD}

## Overview

{1–2 sentences: what this screen shows and its purpose in the app}

## Used In Flows

- [{flow-name}](../flows/{flow-name}.md) — Steps {list}

(Or: "None — standalone screen.")

## Layout

- **Structure:** {e.g. single column, full-screen Scaffold}
- **Content padding:** {dp values}
- **Section spacing:** {dp gap between sections}

## Section Layout

| Order | Section | Component Spec | Visibility Condition |
|-------|---------|---------------|---------------------|
| 1 | {section name} | [{component}](../components/{name}.md) | Always |
| 2 | {section name} | [{component}](../components/{name}.md) | `{state}` state only |

## Screen States

### `default`

{Which sections are visible, what data is shown}

### `{variant}` (e.g. `loading`, `empty`, `error`)

{What changes from default — sections appearing/disappearing, content changes}

## Props / Inputs

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| {paramName} | {Kotlin type} | {yes/no} | {what it controls} |

## Callbacks / Outputs

| Callback | Signature | Description |
|----------|-----------|-------------|
| {name} | `() -> Unit` | {when it fires} |
| {name} | `({type}) -> Unit` | {when it fires} |

## Design Tokens Used

Only tokens for the screen's own layout — not child component internals.

```json
{
  "tokenName": "#hexValue"
}
```

## Deviations

Record any deliberate differences between this spec and the implementation. This prevents code reviews from re-flagging intentional changes.

| Value | Spec | Implemented | Reason |
|-------|------|-------------|--------|

_(Empty until implementation. Add rows when a value is intentionally changed during or after implementation.)_

## Implementation Notes

- **Composition:** {how child composables are arranged}
- **Data source:** {ViewModel StateFlow, repository calls}
- **Navigation:** {navController routes or callbacks}
- **Accessibility:** {focus management, screen reader landmarks}
````

---

## Flow Spec Format

````markdown
# {Flow Name}

**Type:** Flow
**Figma:** {link or [TODO: Add Figma link]}
**Created:** {YYYY-MM-DD}

## Overview

{2–4 sentences: what the user is accomplishing, who they are, why this flow exists}

## Entry Points

| Origin | Trigger | Initial Step |
|--------|---------|-------------|
| {where user comes from} | {what starts the flow} | Step 1 |

## Shared State

| Key | Type | Description | Set By | Used By |
|-----|------|-------------|--------|---------|
| {key} | {Kotlin type} | {description} | Step {n} | Steps {list} |

## Flow Steps

### Step 1: {Step Name}

**Screen:** [{screen-name}](../screens/{screen-name}.md)
**Screen State:** `default`
**Purpose:** {what the user does at this step}

| Action | Condition | Next Step | Side Effects |
|--------|-----------|-----------|-------------|
| {user action} | {condition or "always"} | Step 2 | {state changes, saves, etc.} |
| Back/Cancel | always | Exit: {destination} | {cleanup} |

{...continue for all steps}

## Exit Points

| Exit | From Step | Condition | Destination |
|------|-----------|-----------|-------------|
| {exit name} | Step {n} | {condition} | {where user goes} |

## Error Handling

| Error Condition | Occurs At | Recovery Path |
|----------------|-----------|---------------|
| {description} | Step {n} | {retry / show message / navigate} |

## Flow Diagram

```
{ASCII flow diagram — converted to Mermaid file by /design-to-code}
```

## Deviations

Record any deliberate differences between this spec and the implementation. This prevents code reviews from re-flagging intentional changes.

| Value | Spec | Implemented | Reason |
|-------|------|-------------|--------|

_(Empty until implementation. Add rows when a value is intentionally changed during or after implementation.)_

## Implementation Notes

- **Navigation:** {NavController route structure, e.g. `scores/{date}`}
- **State management:** {ViewModel scope, BackStack entry, shared ViewModel}
- **Back behaviour:** {system back action at each step}
- **Deep linking:** {can users navigate directly to a step?}
````

---

## Design Token Mapping

Specs use semantic token names. Never hardcode hex values in implementation.

### Colour tokens → Compose

| Spec token | Compose value |
|---|---|
| `surface` | `MaterialTheme.goatColors.surface` |
| `onSurface` | `MaterialTheme.goatColors.onSurface` |
| `surfaceVariant` | `MaterialTheme.goatColors.surfaceVariant` |
| `onSurfaceVariant` | `MaterialTheme.goatColors.onSurfaceVariant` |
| `primary` | `MaterialTheme.goatColors.primary` |
| `onPrimary` | `MaterialTheme.goatColors.onPrimary` |
| `outline` | `MaterialTheme.goatColors.outline` |
| `outlineVariant` | `MaterialTheme.goatColors.outlineVariant` |
| Custom (e.g. `scorePlus2`) | `MaterialTheme.goatColors.scorePlus2` |

### Typography tokens → Compose

| Figma text style | M3 slot | Compose value |
|---|---|---|
| `display/large` | displayLarge | `MaterialTheme.typography.displayLarge` |
| `headline/medium` | headlineMedium | `MaterialTheme.typography.headlineMedium` |
| `title/large` | titleLarge | `MaterialTheme.typography.titleLarge` |
| `title/medium` | titleMedium | `MaterialTheme.typography.titleMedium` |
| `title/small` | titleSmall | `MaterialTheme.typography.titleSmall` + `.uppercase()` at call site |
| `body/large` | bodyLarge | `MaterialTheme.typography.bodyLarge` |
| `body/medium` | bodyMedium | `MaterialTheme.typography.bodyMedium` |
| `body/small` | bodySmall | `MaterialTheme.typography.bodySmall` |
| `label/large` | labelLarge | `MaterialTheme.typography.labelLarge` |
| `label/medium` | labelMedium | `MaterialTheme.typography.labelMedium` |
| `label/small` | labelSmall | `MaterialTheme.typography.labelSmall` |

### Spacing tokens → Compose

| Figma value | Token | Compose value |
|---|---|---|
| 4dp | `xs` | `GoatSpacing.s4` |
| 8dp | `sm` | `GoatSpacing.s8` |
| 12dp | — | `GoatSpacing.s12` |
| 16dp | `md` | `GoatSpacing.s16` |
| 20dp | — | `GoatSpacing.s20` |
| 24dp | `lg` | `GoatSpacing.s24` |
| 32dp | `xl` | `GoatSpacing.s32` |

Non-standard values: use exact dp if no matching token, with a comment.

### Radius tokens → Compose

| Token | Compose value |
|---|---|
| `GoatRadius.sm` | `RoundedCornerShape(GoatRadius.sm)` |
| `GoatRadius.md` | `RoundedCornerShape(GoatRadius.md)` |
| `GoatRadius.lg` | `RoundedCornerShape(GoatRadius.lg)` |
| `GoatRadius.full` | `CircleShape` |

---

## tokens.json Structure

```json
{
  "colors": {
    "surface": "#010517",
    "onSurface": "#F8FAFC",
    "surfaceVariant": "#1C2538",
    "onSurfaceVariant": "#9CA3AF",
    "primary": "#9AE600",
    "onPrimary": "#000000",
    "outline": "#374151",
    "outlineVariant": "#4B5563",
    "scorePlus2": "#9AE600",
    "scorePlus1": "#D4ED6A"
  }
}
```

Add new tokens as they are discovered during spec curation. Keep keys in alphabetical order within groups.

---

## Key Principles

1. **Semantic over literal** — use token names (`surface`), not hex values (`#1C2538`)
2. **Theme-first** — always use `MaterialTheme.*`, `GoatSpacing.*`, `GoatRadius.*` — never hardcode
3. **Spec hierarchy** — implement bottom-up: components → screens → flow wiring
4. **States matter** — always document default, pressed, disabled states for interactive elements
5. **Raw JSON for precision** — curated spec for meaning; raw JSON export for exact measurements
6. **dp, not px** — all measurements in density-independent pixels
