Convert raw JSON Figma Styles at $ARGUMENTS to Design Tokens

# Task: Convert Figma Styles to Design Tokens

## Purpose

Convert raw Figma style exports (text styles, color styles) into platform-agnostic design tokens with Compose/Material3 mappings.

## Prerequisites

- Raw Figma JSON file exists in `design-specs/raw/`
- `design-specs/tokens.json` exists (or will be created)

---

## Workflow

### Step 1: Identify Input Type

Read the raw JSON file and determine the style type:

**Text Styles** — JSON contains `textStyles` array with:
- `name`, `fontFamily`, `fontWeight`, `fontSize`, `letterSpacing`, `textCase`

**Color Styles** — JSON contains `colorStyles` array with:
- `name`, `color` (hex or rgba)

### Step 2: Parse and Transform

#### For Text Styles

Transform each Figma text style to a token object:

```json
{
  "tokenName": {
    "size": "<fontSize>sp",
    "weight": "<numericWeight>",
    "family": "<fontFamily>",
    "letterSpacing": "<letterSpacing>%",
    "compose": "<Material3 typography style>"
  }
}
```

**Token naming:**
- Convert Figma name to camelCase
- Remove "Text: " prefix if present
- Examples: "Text: body copy" → `bodyCopy`, "Bold 18" → `bold18`

**Font weight mapping:**
| Figma Weight | Numeric | Compose |
|--------------|---------|---------|
| Thin | 100 | `FontWeight.Thin` |
| ExtraLight | 200 | `FontWeight.ExtraLight` |
| Light | 300 | `FontWeight.Light` |
| Regular | 400 | `FontWeight.Normal` |
| Medium | 500 | `FontWeight.Medium` |
| SemiBold | 600 | `FontWeight.SemiBold` |
| Bold | 700 | `FontWeight.Bold` |
| ExtraBold | 800 | `FontWeight.ExtraBold` |
| Black | 900 | `FontWeight.Black` |

**Font size mapping to Material3 typography:**

| Size | Compose Style |
|------|--------------|
| 10sp | `MaterialTheme.typography.labelSmall` |
| 12sp | `MaterialTheme.typography.labelMedium` |
| 14sp | `MaterialTheme.typography.bodyMedium` |
| 16sp (Normal/Regular) | `MaterialTheme.typography.bodyLarge` |
| 16sp (Medium) | `MaterialTheme.typography.labelLarge` |
| 22sp | `MaterialTheme.typography.titleLarge` |
| 28sp | `MaterialTheme.typography.headlineLarge` |
| 32sp | `MaterialTheme.typography.displayLarge` |

If a size falls between standard values or the weight changes the intended role, use the closest semantic match and note the deviation.

**Full typography mapping reference:**

| Figma Size/Weight | Compose Implementation |
|-------------------|----------------------|
| 32sp Bold | `MaterialTheme.typography.displayLarge` |
| 28sp Bold | `MaterialTheme.typography.headlineLarge` |
| 22sp SemiBold | `MaterialTheme.typography.titleLarge` |
| 16sp Normal | `MaterialTheme.typography.bodyLarge` |
| 14sp Normal | `MaterialTheme.typography.bodyMedium` |
| 16sp Medium | `MaterialTheme.typography.labelLarge` |
| 12sp Medium | `MaterialTheme.typography.labelMedium` |
| 10sp Medium | `MaterialTheme.typography.labelSmall` |

**Compose field construction:**
```
compose: "MaterialTheme.typography.<styleName>"
```

Example: 16sp Medium → `MaterialTheme.typography.labelLarge`

#### For Color Styles

Transform each Figma color style to a token:

```json
{
  "colors": {
    "tokenName": "#hexvalue"
  }
}
```

**Token naming conventions:**
| Figma Name Pattern | Token Name | Usage |
|-------------------|------------|-------|
| Background | surface | Background surfaces |
| Text on [X] | on[X] | Text color on surfaces |
| Border / Outline | outline | Border colors |
| Error / Warning / Success | error, warning, success | Status colors |
| Primary / Brand | primary | Primary interactive color |
| Text on Primary | onPrimary | Text on primary backgrounds |

**Ask user to confirm semantic names** before finalizing color tokens.

### Step 3: Check for Existing Theme Definitions

Check the Kotlin theme files to identify existing typography and color definitions:

1. **Find `Typography.kt`** — typically at `mobile/shared/src/commonMain/kotlin/.../ui/theme/Typography.kt`
   - Look for existing `TextStyle` definitions in the `Typography { }` block
   - Note any custom font sizes or weights that override Material3 defaults
   - If a Figma style maps to an existing `TextStyle`, confirm the mapping is consistent

2. **Find `Theme.kt`** — typically at `mobile/shared/src/commonMain/kotlin/.../ui/theme/Theme.kt`
   - Look for existing `ColorScheme` definitions (`darkColorScheme { }` / `lightColorScheme { }`)
   - Check if a new color token maps to an existing scheme color

**If a Figma style conflicts with existing theme definitions:**
1. Present the conflict to the user
2. Ask whether to update the theme file or adjust the token mapping
3. Only update theme Kotlin files if the user explicitly approves

**If new typography styles are needed:**
- Note which `TextStyle` would need to be added or updated in `Typography.kt`
- Present this as a suggestion, not an automatic change

### Step 4: Merge with Existing Tokens

1. Read existing `design-specs/tokens.json`
2. Merge new tokens (don't overwrite existing unless user confirms)
3. Maintain alphabetical order within categories
4. Preserve existing tokens that aren't being updated

### Step 5: Present for Review

Show the user:
1. New tokens to be added
2. Any Kotlin theme file updates needed (Typography.kt, Theme.kt)
3. Any naming decisions that need confirmation

### Step 6: Write Updates

After user approval:
1. Update `design-specs/tokens.json` with new tokens
2. **If Typography.kt needs updating** — note the specific changes needed and ask the user to confirm before making them
3. **If Theme.kt needs updating** — note the specific color additions and ask the user to confirm

Token updates to `tokens.json` only require approval of the content; Kotlin source file changes always require explicit user confirmation.

---

## Example: Text Styles

**Input:** `design-specs/raw/textStyles.json`
```json
{
  "textStyles": [
    {
      "name": "Text: body copy",
      "fontFamily": "Roboto",
      "fontWeight": "Medium",
      "fontSize": 16,
      "letterSpacing": { "unit": "PERCENT", "value": 0 }
    }
  ]
}
```

**Output:** Added to `tokens.json`
```json
{
  "typography": {
    "bodyCopy": {
      "size": "16sp",
      "weight": "500",
      "family": "Roboto",
      "letterSpacing": "0%",
      "compose": "MaterialTheme.typography.labelLarge"
    }
  }
}
```

---

## Example: Color Styles

**Input:** `design-specs/raw/colorStyles.json`
```json
{
  "colorStyles": [
    { "name": "Surface", "color": "#010517" },
    { "name": "On Surface", "color": "#F8FAFC" },
    { "name": "Primary", "color": "#F59E0B" }
  ]
}
```

**Proposed tokens (ask user to confirm names):**
```
#010517 "Surface" → surface
#F8FAFC "On Surface" → onSurface
#F59E0B "Primary" → primary? brand?
```

**Output after user confirmation:**
```json
{
  "colors": {
    "onSurface": "#F8FAFC",
    "primary": "#F59E0B",
    "surface": "#010517"
  }
}
```

---

## Invocation Examples

**Convert text styles:**
```
/convert-figma-styles-to-tokens design-specs/raw/textStyles.json
```

**Convert color styles:**
```
/convert-figma-styles-to-tokens design-specs/raw/colorStyles.json
```

---

## Quality Checklist

- [ ] All Figma styles have been converted to tokens
- [ ] Token names follow camelCase convention
- [ ] Font weights are numeric (100–900)
- [ ] Typography tokens use `MaterialTheme.typography.*` Compose style references
- [ ] Color tokens use semantic names matching Material3 conventions
- [ ] Checked `Typography.kt` for existing definitions — no silent conflicts
- [ ] Checked `Theme.kt` for existing color scheme — no silent conflicts
- [ ] User has confirmed semantic names for color tokens
- [ ] User has approved any Kotlin theme file changes before applying them
- [ ] Existing tokens preserved (not accidentally overwritten)
- [ ] `tokens.json` is valid JSON
