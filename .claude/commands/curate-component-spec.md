Curate Component Spec from raw Figma JSON at: $ARGUMENTS

# Task: Curate Component Spec from Figma JSON

## Purpose

Transform raw Figma JSON exports into implementation-ready **component** specs that follow the format defined in `.claude/design-specs-guide.md`.

## Prerequisites

- Raw Figma JSON file exists in `design-specs/raw/`
- `.claude/design-specs-guide.md` exists (defines the spec format)
- `design-specs/tokens.json` exists (or will be created)

## Workflow Steps

### Step 1: Analyze the JSON Structure

Read the raw JSON file and identify:

**Frame Type:**

- Is the top-level frame a component (smaller, reusable element)?
- Or is it a layout section?

**Content Inventory:**

- **Text elements:** Extract all text with fontSize, fontName, colors
    - Classify as: heading, body, label, caption based on size/weight
- **Interactive elements:** Find frames with type "INSTANCE" or "FRAME" that suggest buttons/cards
    - Look for: button-like names, clickable affordances (text + icon)
- **Colors:** Collect unique RGB values from fills and strokes
- **Layout patterns:** Identify layoutMode (HORIZONTAL, VERTICAL, NONE)
- **Spacing:** Extract padding values and itemSpacing

**Key Pattern Recognition:**

- Buttons: Look for circular or rounded frames with text + optional icons
- Cards: Look for frames with fills, strokes, and internal content
- Lists/Grids: Look for layoutMode with itemSpacing
- Navigation elements: Look for dots, arrows, tab bars

### Step 2: Ask Clarifying Questions

**Always ask these questions:**

1. **"What type of design is this?"**
    - Options: Component | Layout

2. **"What should this be named?"**
    - Suggest a name based on frame name or content (e.g., "profile-switch-card", "score-ring")
    - Use kebab-case for filenames

3. **"I found these colors in the design. Please provide semantic token names:"**

```
   #010517 → ?
   #F8FAFC → ?
   #1C2538 → ?
   #9CA3AF → ?
```

- Suggest common token names: surface, onSurface, surfaceVariant, onSurfaceVariant, primary, onPrimary, outline, outlineVariant
- Show the hex values so user can map them

4. **For each interactive element found, ask: "What happens when the user interacts with [element name]?"**
    - Examples: "Navigate to profile selection", "Submit form", "Toggle expanded state"

**Ask these if relevant:**

5. **"Are there any states I should document beyond the default?"** (if interactive elements found)
    - Options: pressed, focused, disabled, selected, error
    - Only ask if the design contains interactive elements

6. **"Are there animations or transitions I should document?"**
    - Only ask for complex components
    - Examples: expand/collapse animations, shared element transitions, fade in/out

### Step 3: Map Design Tokens

**Check existing tokens:**

- Read `design-specs/tokens.json`
- Compare found RGB values against existing tokens
- Identify which colors are new

**For new colors:**

- Suggest semantic token names
- Add them to the tokens mapping

**Token Naming Conventions:**

- Background surfaces: `surface`, `surfaceVariant`, `surfaceContainer`
- Text on surfaces: `onSurface`, `onSurfaceVariant`
- Interactive elements: `primary`, `secondary`, `tertiary`
- Text on interactive: `onPrimary`, `onSecondary`, `onTertiary`
- Borders/dividers: `outline`, `outlineVariant`
- Status colors: `error`, `errorContainer`, `onError`, `onErrorContainer`

### Step 4: Generate the Curated Spec

Using the template from `.claude/design-specs-guide.md`, create a markdown file with:

#### Header Section

```markdown
# [Component Name]

**Type:** [Component | Layout]
**Figma:** [If user provides link, include it; otherwise leave placeholder]
**JSON Export:** `design-specs/raw/[original-json-filename].json`
**Created:** [Current date]
```

#### Layout Section

Extract semantic positioning (don't use absolute x/y coordinates):

- Component dimensions (width × height in dp)
- Content padding (`Modifier.padding(...)` values in dp)
- Element spacing (`Arrangement.spacedBy(...)` values in dp)
- Alignment patterns (SPACE_BETWEEN → `Arrangement.SpaceBetween`, CENTER → `Alignment.CenterVertically`)

Example:

```markdown
## Layout

- **Dimensions:** Full width × 53dp height
- **Content padding:** `Modifier.padding(horizontal = 13.dp, vertical = 9.dp)`
- **Content:** Label stacked above value (vertical), action aligned right
```

#### Typography Section

For each text element found, map to Material3 typography scale:

- Extract fontSize, fontName (family + style), color
- Map to `MaterialTheme.typography.*` style
- Map color to semantic token

Example:

```markdown
## Typography

- **Heading:** `MaterialTheme.typography.headlineMedium` (28sp Bold), `color = MaterialTheme.colorScheme.onSurface`
- **Body:** `MaterialTheme.typography.bodyLarge` (16sp Normal), `color = MaterialTheme.colorScheme.onSurface`
- **Label:** `MaterialTheme.typography.labelSmall` (10sp Medium), `color = MaterialTheme.colorScheme.onSurfaceVariant`
```

**Typography Mapping Guide:**

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

#### Components/Properties Section

List properties and sub-elements.

For each element include:

- **Dimensions:** width × height in dp (use `Modifier.fillMaxWidth()`, `Modifier.wrapContentWidth()`, or specific values)
- **Background:** color token (#hex for reference) → `MaterialTheme.colorScheme.*`
- **Border:** if present, border width in dp + color token → `BorderStroke(Xdp, MaterialTheme.colorScheme.*)`
- **Padding:** dp values as `Modifier.padding(...)`
- **Corner radius:** dp value → `RoundedCornerShape(X.dp)` or `RoundedCornerShape(percent = 50)`
- **Content:** describe what's inside
- **States:** default, pressed, focused, disabled, etc.
- **Action:** what happens on interaction

Example:

```markdown
## Components

### Continue Button

- **Type:** Circular button
- **Dimensions:** 110dp diameter
- **Background:** `MaterialTheme.colorScheme.surfaceVariant` (#0E1729)
- **Border:** `BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)` (#1D293D)
- **Content:** "Continue" text (`MaterialTheme.typography.bodyLarge`, `color = MaterialTheme.colorScheme.onSurface`)
- **Corner radius:** `RoundedCornerShape(percent = 50)` (fully circular)
- **States:**
  - Default: as shown
  - Pressed: surface overlay at 8% opacity (via `interactionSource` + `indication`)
  - Focused: `Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)`
- **Action:** Navigate to next slide in carousel

### Pagination Dots

- **Type:** Progress indicator
- **Count:** 3 dots
- **Size:** 12dp × 12dp each
- **Spacing:** `Arrangement.spacedBy(8.dp)` between dots
- **Active color:** `MaterialTheme.colorScheme.primary`
- **Inactive color:** `MaterialTheme.colorScheme.surfaceVariant`
- **Current position:** Dot 1 (first slide)
```

#### Design Tokens Section

List all tokens used in this design with their hex values:

````markdown
## Design Tokens Used

```json
{
  "surface": "#010517",
  "onSurface": "#F8FAFC",
  "surfaceVariant": "#0E1729",
  "outlineVariant": "#1D293D"
}
```
````

#### Implementation Notes Section

Add helpful notes for implementation:

- Any tricky layout considerations
- Accessibility requirements (`Modifier.semantics { contentDescription = "..." }`, `Role.*`)
- Edge cases to handle
- Performance considerations

Example:

```markdown
## Implementation Notes

- Use `HorizontalPager` (or `PagerState`) for slide navigation; pagination dots derive from `pagerState.currentPage`
- Consider `AnimatedContent` for smooth slide transitions
- **Accessibility:** Apply `Modifier.semantics { contentDescription = "..." }` on the continue button; ensure focus moves correctly between slides using `FocusRequester`
```

### Step 5: Update Design Tokens

If new tokens were identified:

1. Read existing `design-specs/tokens.json`
2. Add new token entries
3. Maintain alphabetical order within categories
4. Follow this structure:

```json
{
  "colors": {
    "surface": "#010517",
    "onSurface": "#F8FAFC",
    "surfaceVariant": "#1C2538",
    "onSurfaceVariant": "#9CA3AF",
    "primary": "#F59E0B",
    "onPrimary": "#FFFFFF",
    "outline": "#374151",
    "outlineVariant": "#4B5563"
  },
  "typography": {
    "heading": { "compose": "MaterialTheme.typography.headlineMedium", "size": "28sp", "weight": "700" },
    "body": { "compose": "MaterialTheme.typography.bodyLarge", "size": "16sp", "weight": "400" },
    "label": { "compose": "MaterialTheme.typography.labelSmall", "size": "10sp", "weight": "500" }
  },
  "spacing": {
    "xs": "4dp",
    "sm": "8dp",
    "md": "16dp",
    "lg": "24dp",
    "xl": "32dp"
  }
}
```

**If tokens.json doesn't exist yet, create it with initial values from this design.**

### Step 6: Save Files and Summarize

**Save the spec:**

- Components go in: `design-specs/components/[name].md`

**Update tokens:**

- Save to: `design-specs/tokens.json`

**Provide summary:**

```markdown
Design spec curation complete!

**Created:**

- design-specs/components/profile-switch-card.md

**Updated:**

- design-specs/tokens.json (added 2 new color tokens)

**What's in the spec:**

- Full component layout
- 3 sub-elements: label stack, value text, action button
- 5 design tokens mapped
- Interaction behavior documented

**Next steps:**

1. Review the generated spec for accuracy
2. Add any missing interaction details
3. Commit to repository
4. Ready for implementation phase!

**Note:** The raw JSON remains at design-specs/raw/profile-switch-card.json for reference.
```

## Special Handling for Common Patterns

### Buttons

Look for these indicators:

- Frame names containing: "button", "btn", "continue", "submit"
- Circular or rounded shapes (borderRadius > 0)
- Text + optional icon inside a frame
- Small, clickable size (typically 40–120dp)

Extract:

- Shape (rectangular/circular)
- Size (width × height or diameter in dp)
- Label text
- Icon (if present)
- Background and border colors

### Cards

Look for these indicators:

- Frame names containing: "card", "item", "container"
- Has both fill and stroke
- Contains multiple child elements
- Medium size (not full screen, not tiny)

Extract:

- Dimensions in dp
- Background/border colors
- Internal layout (horizontal/vertical)
- Content sections

### Navigation Elements

Look for these indicators:

- Pagination dots (multiple small circles)
- Tab bars (horizontal row of items)
- Headers with back buttons
- Bottom navigation bar

Extract:

- Number of items/pages
- Active/inactive states
- Position on screen
- Interaction behavior

### Form Elements

Look for these indicators:

- Frame names containing: "input", "field", "form", "text"
- Rectangular with border
- May contain placeholder text

Extract:

- Input type (text, number, etc.)
- Label and placeholder
- Validation states (error, success)
- Character limits

## Handling Edge Cases

### Missing Information

If JSON doesn't contain enough info for a complete spec:

- Note what's missing in Implementation Notes
- Use placeholders: `[TODO: Check Figma for pressed state]`
- Ask user if they can provide missing details

### Deeply Nested Frames

If JSON has 5+ levels of nesting:

- Focus on semantic structure, not every frame
- Skip pure layout containers (frames with no styling)
- Extract only frames that represent actual UI elements

### Conflicting Information

If JSON has inconsistencies (e.g., two different text sizes for "body"):

- Ask user which is correct
- Document both and ask which to use as standard

### Unknown Color Tokens

If user doesn't know what to name a color:

- Suggest based on usage context
- Use descriptive names as fallback (e.g., "darkNavy", "lightGray")
- Note in summary that these may need better semantic names later

## Quality Checklist

Before finalizing the spec, verify:

- [ ] All interactive elements have documented actions
- [ ] All colors are mapped to semantic tokens
- [ ] Typography uses Material3 theme references (`MaterialTheme.typography.*`)
- [ ] Layout uses dp values and Compose modifier descriptions (not absolute coordinates)
- [ ] States are documented for interactive elements (pressed, focused, disabled)
- [ ] Implementation notes include accessibility guidance (`Modifier.semantics`, `Role.*`)
- [ ] `design-specs/tokens.json` is updated with new values
- [ ] File is saved in `design-specs/components/`
- [ ] Summary clearly states what was created/updated

## Example Invocations

**User says:**

> "Curate the component spec from design-specs/raw/profile-switch-card.json"

**Claude Code does:**

1. Read and analyze the JSON
2. Identify it as a component
3. Ask clarifying questions about colors and interactions
4. Generate `design-specs/components/profile-switch-card.md`
5. Update `design-specs/tokens.json`
6. Provide summary

---

**User says:**

> "/curate-component-spec design-specs/raw/score-ring.json"

**Claude Code does:**

1. Read JSON and identify component pattern
2. Find structural elements, spacing, alignment
3. Ask about token names and interactions
4. Generate `design-specs/components/score-ring.md`
5. Create/update `design-specs/tokens.json`
6. Provide summary with next steps
