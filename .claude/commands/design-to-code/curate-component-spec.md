Curate Component Spec from raw Figma JSON at: $ARGUMENTS

# Task: Curate Component Spec from Figma JSON

## Purpose

Transform raw Figma JSON exports into implementation-ready **component** specs for Compose Multiplatform, following the format in `.claude/skills/design-specs/SKILL.md`.

## Prerequisites

- Raw Figma JSON file exists in `design-specs/raw/`
- `.claude/skills/design-specs/SKILL.md` exists (defines the spec format)
- `design-specs/tokens.json` exists (or will be created)

## Specs Root

All design specs live at the repo root: `design-specs/`

---

## Workflow Steps

### Step 1: Analyse the JSON Structure

Read the raw JSON file and identify:

**Frame Type:**
- Is the top-level frame a reusable component (card, button, list item, etc.)?
- Or is it a screen-level section?

**Content Inventory:**
- **Text elements:** Extract all text with fontSize, fontName, colours → classify as heading, body, label, caption
- **Interactive elements:** Frames with `type: "INSTANCE"` or clickable affordances
- **Colours:** Collect unique RGB values from fills and strokes
- **Layout patterns:** Identify `layoutMode` (HORIZONTAL, VERTICAL, NONE)
- **Spacing:** Extract padding values and `itemSpacing`
- **Dimensions:** Width and height in dp

**Pattern Recognition:**
- Cards: frames with fills, strokes, and internal content
- Buttons: circular or rounded frames with text ± icon
- List items: horizontal layouts with leading icon/image, text stack, trailing element
- Input fields: rectangular frames with border and placeholder text
- Navigation elements: tab bars, headers with back arrows

---

### Step 2: Ask Clarifying Questions

**Always ask:**

1. **"What should this component be named?"**
   - Suggest a name based on the frame name or content (e.g. `score-card`, `food-category-item`)
   - Use kebab-case for filenames, PascalCase will be the Kotlin function name

2. **"I found these colours in the design. Please provide semantic token names:"**
   ```
   #010517 → ?
   #9AE600 → ?
   #1C2538 → ?
   ```
   - Suggest from common names: `surface`, `onSurface`, `surfaceVariant`, `primary`, `onPrimary`, `outline`, `outlineVariant`
   - Also check for app-specific tokens like `scorePlus2`, `scorePlus1`, `scoreMinus1`

3. **For each interactive element: "What happens when the user taps [element name]?"**

**Ask if relevant:**

4. **"Are there any states beyond default?"** (for interactive elements)
   - Pressed, disabled, selected, loading, error

5. **"Are there animations or transitions?"**
   - Swipe, expand/collapse, etc.

---

### Step 3: Map Design Tokens

**Check existing tokens:**
- Read `design-specs/tokens.json`
- Compare found RGB values against existing tokens
- Identify which colours are new

**For new colours:** Suggest semantic names and add to tokens mapping.

**Token naming conventions:**
- Background surfaces: `surface`, `surfaceVariant`, `surfaceContainer`
- Text on surfaces: `onSurface`, `onSurfaceVariant`
- Interactive: `primary`, `secondary`
- Text on interactive: `onPrimary`, `onSecondary`
- Borders: `outline`, `outlineVariant`
- Status: `error`, `errorContainer`
- App-specific scores: `scorePlus2`, `scorePlus1`, `scoreNeutral`, `scoreMinus1`, `scoreMinus2`

---

### Step 4: Generate the Curated Spec

Using the template from `.claude/skills/design-specs/SKILL.md`, create a markdown file:

#### Header

```markdown
# {Component Name}

**Type:** Component
**Figma:** [TODO: Add Figma link]
**JSON Export:** `design-specs/raw/{filename}.json`
**Created:** {YYYY-MM-DD}
```

#### Layout

Extract semantic positioning — no absolute x/y coordinates:

```markdown
## Layout

- **Dimensions:** Full width × 53dp height
- **Content padding:** 13dp horizontal, 9dp vertical
- **Content:** Label stacked above value (vertical), action aligned right
```

#### Typography

Map Figma sizes to Material 3 typography slots:

```markdown
## Typography

- **Heading:** `MaterialTheme.typography.titleMedium`, `onSurface`
- **Body:** `MaterialTheme.typography.bodyMedium`, `onSurface`
- **Label:** `MaterialTheme.typography.labelSmall`, `onSurfaceVariant`
```

**Figma → M3 slot mapping:**

| Figma text style | M3 slot |
|---|---|
| `display/large` | `displayLarge` |
| `headline/medium` | `headlineMedium` |
| `title/large` | `titleLarge` |
| `title/medium` | `titleMedium` |
| `title/small` | `titleSmall` (+ `.uppercase()` at call site) |
| `body/large` | `bodyLarge` |
| `body/medium` | `bodyMedium` |
| `body/small` | `bodySmall` |
| `label/large` | `labelLarge` |
| `label/medium` | `labelMedium` |
| `label/small` | `labelSmall` |

If Figma doesn't use named styles, use size/weight to match the closest M3 slot.

#### Components / Properties

```markdown
## Components

### Score Value

- **Type:** Text
- **Content:** Score integer (e.g. "+12")
- **Typography:** `MaterialTheme.typography.headlineMedium`
- **Colour:** `MaterialTheme.goatColors.scorePlus2` (#9AE600)

### Card Container

- **Type:** Surface (clickable)
- **Dimensions:** Full width × 72dp
- **Background:** `MaterialTheme.goatColors.surfaceVariant` (#1C2538)
- **Border:** `BorderStroke(1.dp, MaterialTheme.goatColors.outlineVariant)`
- **Padding:** 16dp horizontal, 12dp vertical
- **Corner radius:** `GoatRadius.md` (8dp)
- **States:**
  - Default: as shown
  - Pressed: ripple via `Surface(onClick = ...)`
  - Disabled: `alpha = 0.38f`
- **Action:** `onClick` lambda — navigates to detail
```

#### Design Tokens Used

```markdown
## Design Tokens Used

```json
{
  "surfaceVariant": "#1C2538",
  "onSurface": "#F8FAFC",
  "onSurfaceVariant": "#9CA3AF",
  "outlineVariant": "#374151",
  "scorePlus2": "#9AE600"
}
```
```

#### Implementation Notes

```markdown
## Implementation Notes

- **Accessibility:** Provide `contentDescription` combining score value and category name
- **Minimum touch target:** `GoatSizing.Touch.default` (48dp)
- **Edge cases:** Score value can be negative — ensure colour token handles this (use `scoreNeutral` for zero)
```

---

### Step 5: Update Design Tokens

If new tokens were identified:
1. Read existing `design-specs/tokens.json`
2. Add new entries in alphabetical order within their category
3. Save the updated file

If `tokens.json` doesn't exist yet, create it:

```json
{
  "colors": {
    "onSurface": "#F8FAFC",
    "onSurfaceVariant": "#9CA3AF",
    "outline": "#374151",
    "outlineVariant": "#4B5563",
    "primary": "#9AE600",
    "surface": "#010517",
    "surfaceVariant": "#1C2538"
  }
}
```

---

### Step 6: Save Files and Summarise

**Save spec:** `design-specs/components/{name}.md`

**Update tokens:** `design-specs/tokens.json`

**Summarise:**

```markdown
## Component Spec Complete

**Created:**
- `design-specs/components/score-card.md`

**Updated:**
- `design-specs/tokens.json` (added 2 new colour tokens: scorePlus2, scoreNeutral)

**What's in the spec:**
- Full component layout and dimensions
- 3 sub-elements: card container, score value, category label
- 6 design tokens mapped
- Pressed and disabled states documented

**Next steps:**
1. Review the spec for accuracy
2. Say "implement" in the main pipeline to generate Kotlin code
```

---

## Handling Edge Cases

### Deeply nested frames
Focus on semantic structure. Skip pure layout containers. Extract only frames that represent visible UI elements.

### Missing information
Use `[TODO: Confirm from Figma]` placeholders and note in Implementation Notes.

### Unknown colour tokens
Suggest based on usage context. Use descriptive names as fallback (`darkNavy`, `brightGreen`) and note they may need better semantic names.

### Conflicting sizes or styles
Ask the user which to use as the standard for this component.

---

## Quality Checklist

- [ ] All interactive elements have documented actions (lambda names)
- [ ] All colours mapped to semantic tokens
- [ ] Typography uses M3 slot references
- [ ] Layout uses dp values (not absolute coordinates)
- [ ] States documented for interactive elements
- [ ] Implementation notes include accessibility guidance
- [ ] `tokens.json` updated with new values
- [ ] File saved in `design-specs/components/`
