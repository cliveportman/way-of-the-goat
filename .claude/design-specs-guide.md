# Design Specs Guide

## Overview

Design specs are created during the Research & Design phase in Notion, then committed to the repo as markdown files. They provide the source of truth for implementing UI components and screens.

## File Locations

```
design-specs/
├── raw/                    # JSON exports from Figma (via "Figma to JSON" plugin)
│   ├── profile-switch-card.json
│   └── onboarding-slide-2.json
├── screens/                # Curated specs for full screens
│   └── onboarding-carousel.md
├── components/             # Curated specs for reusable components
│   └── profile-switch-card.md
└── tokens.json            # Design tokens mapping
```

## How to Use Design Specs

When implementing a feature:

1. **Read the curated spec** in `design-specs/screens/` or `design-specs/components/`
   - This has semantic information (what colors mean, interaction behavior)
   - Use this as your primary reference

2. **Check raw JSON** in `design-specs/raw/` if you need precise measurements
   - JSON has exact pixel values but lacks semantic meaning
   - Use for things like "exactly how much padding?"

3. **Map design tokens** to Compose theme values (see section below)
   - Never hardcode hex colors
   - Always use `MaterialTheme.colorScheme.*` or `MaterialTheme.typography.*`

## Design Spec Format

All design specs follow this structure:

````markdown
# [Component/Screen Name]

**Type:** [Screen | Component | Layout]
**Figma:** [Link to Figma frame]
**JSON Export:** `design-specs/raw/[filename].json`

## Layout

- Semantic positioning (e.g., "20px horizontal padding", "centered")
- Key element spacing and alignment
- Screen/component dimensions

## Typography

- **Heading:** MaterialTheme.typography.X, color token
- **Body:** MaterialTheme.typography.X, color token
- Include font size, weight, color for each text element

## Components (for screens) or Properties (for components)

### [Component/Section Name]

- **Dimensions:** width × height (in dp)
- **Background:** color token (#hex for reference)
- **Border:** stroke width, color token
- **Padding/Spacing:** specific values
- **States:** default, pressed, disabled, etc.
- **Action:** what happens on interaction

## Design Tokens Used

```json
{
  "surface": "#010517",
  "onSurface": "#F8FAFC",
  "surfaceVariant": "#0E1729"
}
```

## Implementation Notes

- Technical considerations
- Edge cases to handle
- Accessibility requirements
- Animation/transition details
````

## Design Token Mapping

Design specs use semantic token names. Map these to Compose theme values:

### Color Tokens

```kotlin
// Design spec says: "background: surface"
// Implementation:
backgroundColor = MaterialTheme.colorScheme.surface

// Design spec says: "text: onSurface"
// Implementation:
color = MaterialTheme.colorScheme.onSurface

// Design spec says: "border: outlineVariant"
// Implementation:
border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
```

### Common Token Mappings

| Spec Token         | Compose Theme Value            |
| ------------------ | ------------------------------ |
| `surface`          | `colorScheme.surface`          |
| `onSurface`        | `colorScheme.onSurface`        |
| `surfaceVariant`   | `colorScheme.surfaceVariant`   |
| `onSurfaceVariant` | `colorScheme.onSurfaceVariant` |
| `primary`          | `colorScheme.primary`          |
| `onPrimary`        | `colorScheme.onPrimary`        |
| `outline`          | `colorScheme.outline`          |
| `outlineVariant`   | `colorScheme.outlineVariant`   |

### Typography Tokens

```kotlin
// Design spec says: "Heading: 28sp, Roboto Bold"
// Implementation:
style = MaterialTheme.typography.headlineMedium

// Design spec says: "Body: 16sp, Roboto Regular"
// Implementation:
style = MaterialTheme.typography.bodyLarge

// Design spec says: "Caption: 10sp, Roboto Medium"
// Implementation:
style = MaterialTheme.typography.labelSmall
```

### Spacing

Design specs use pixel values. Convert to `dp` in Compose:

```kotlin
// Design spec says: "padding: 13×9"
// Implementation:
modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
```

## Example: ProfileSwitchCard

Here's a complete example showing how a design spec translates to implementation.

**Design Spec:** `design-specs/components/profile-switch-card.md`

````markdown
# ProfileSwitchCard

**Type:** Component (Interactive Card)
**Figma:** [link]
**JSON Export:** `design-specs/raw/profile-switch-card.json`

## Layout

- **Dimensions:** Full width × 53dp height
- **Padding:** 13dp horizontal, 9dp vertical
- **Content:** Label stacked above value (vertical), action aligned right

## Typography

- **Label:** labelSmall (10sp, Medium), onSurfaceVariant
- **Value:** bodyLarge + SemiBold (16sp), onSurface
- **Action:** labelMedium (12sp), onSurfaceVariant

## Properties

- **Background:** surfaceVariant (#1C2538)
- **Border:** 1px solid outlineVariant @ 50% opacity
- **Corner Radius:** 8dp

## Components

### Label Stack (Vertical)

- "Active Profile" (label style)
- "Body Composition" (value style)

### Action

- "Change" text + chevron icon (→)
- Aligned to trailing edge

## States

- **Default:** as shown
- **Pressed:** surface overlay at 8% opacity

## Interaction

- **Action:** Navigate to profile selection screen
- **Accessibility:** "Change active profile. Current: Body Composition"

## Design Tokens Used

```json
{
  "surfaceVariant": "#1C2538",
  "onSurface": "#FFFFFF",
  "onSurfaceVariant": "#9CA3AF",
  "outlineVariant": "#374151"
}
```
````

**Implementation:**

```kotlin
@Composable
fun ProfileSwitchCard(
    profileName: String,
    onChangeProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onChangeProfile,
        modifier = modifier.fillMaxWidth().height(53.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Active Profile",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = profileName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

## Key Principles

1. **Semantic over literal:** Use token names (`surface`) not hex values (`#1C2538`)
2. **Theme-first:** Always use `MaterialTheme.*` values, never hardcode
3. **Reference both:** Curated spec for meaning, raw JSON for precision
4. **Document tokens:** Each spec includes the tokens it uses
5. **States matter:** Always specify default, pressed, disabled states

## Workflow Integration

This design spec system integrates with the project workflow:

1. **Research & Design phase:** Design specs created in Notion, committed to repo
2. **Implementation phase:** Claude Code reads specs from repo
3. **Retro phase:** Evaluate if specs were complete, update for next time

## Questions or Missing Info?

If a design spec is missing information you need for implementation:

1. Check the raw JSON export for precise measurements
2. Note the gap in implementation and document for retro
3. Update the design spec template to include this information next time
