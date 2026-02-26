# Task: Curate Design Spec from Figma JSON

## Purpose

Transform raw Figma JSON exports into implementation-ready design specs that follow the format defined in `.claude/design-specs-guide.md`.

## Prerequisites

- Raw Figma JSON file exists in `design-specs/raw/`
- `.claude/design-specs-guide.md` exists (defines the spec format)
- `design-specs/tokens.json` exists (or will be created)

## Workflow Steps

### Step 1: Analyze the JSON Structure

Read the raw JSON file and identify:

**Frame Type:**

- Is the top-level frame a full screen (width: 375, height: 812/844)?
- Or is it a component (smaller, reusable element)?
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
   - Options: Screen | Component | Layout
   - Helps determine file location (screens/ vs components/)

2. **"What should this be named?"**
   - Suggest a name based on frame name or content (e.g., "onboarding-carousel", "profile-switch-card")
   - Use kebab-case for filenames

3. **"I found these colors in the design. Please provide semantic token names:"**

```
   #010517 → ?
   #F8FAFC → ?
   #1C2538 → ?
   #9CA3AF → ?
```

- Suggest common Material Design token names: surface, onSurface, surfaceVariant, onSurfaceVariant, primary, onPrimary, outline, outlineVariant
- Show the hex values so user can map them

4. **For each interactive element found, ask: "What happens when the user interacts with [element name]?"**
   - Examples: "Navigate to profile selection", "Submit form", "Toggle expanded state"

**Ask these if relevant:**

5. **"Are there any states I should document beyond the default?"** (if interactive elements found)
   - Options: pressed, hover, disabled, focused, selected, error
   - Only ask if the design contains interactive elements

6. **"Are there animations or transitions I should document?"**
   - Only ask for screens or complex components
   - Examples: slide transitions, fade in/out, button press animations

7. **"Does this component/screen have any responsive or adaptive behavior?"**
   - Only ask for full screens
   - Examples: landscape mode, tablet layout, different breakpoints

### Step 3: Map Design Tokens

**Check existing tokens:**

- Read `design-specs/tokens.json`
- Compare found RGB values against existing tokens
- Identify which colors are new

**For new colors:**

- Suggest semantic token names following Material Design conventions
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
# [Component/Screen Name]

**Type:** [Screen | Component | Layout]
**Figma:** [If user provides link, include it; otherwise leave placeholder]
**JSON Export:** `design-specs/raw/[original-json-filename].json`
**Created:** [Current date]
```

#### Layout Section

Extract semantic positioning (don't use absolute x/y coordinates):

- Screen/component dimensions (width × height in dp)
- Content padding (horizontal × vertical)
- Element spacing (itemSpacing values)
- Alignment patterns (SPACE_BETWEEN → "space between", CENTER → "centered")

Example:

```markdown
## Layout

- **Screen dimensions:** 375 × 812 (iPhone 13 mini)
- **Content padding:** 20dp horizontal, 48dp top, 16dp bottom
- **Heading position:** 230dp from top
- **Element spacing:** 8dp between heading and body
```

#### Typography Section

For each text element found, map to Material Design typography:

- Extract fontSize, fontName (family + style), color
- Suggest equivalent MaterialTheme.typography value
- Map color to semantic token

Example:

```markdown
## Typography

- **Heading:** headlineMedium (28sp, Roboto Bold, onSurface)
- **Body:** bodyLarge (16sp, Roboto Regular, onSurface)
- **Label:** labelSmall (10sp, Roboto Medium, onSurfaceVariant)
```

**Typography Mapping Guide:**
| Figma Size/Weight | Material Typography |
|------------------|-------------------|
| 28sp Bold | headlineMedium |
| 24sp Bold | headlineSmall |
| 22sp Bold | titleLarge |
| 16sp SemiBold | titleMedium |
| 16sp Regular | bodyLarge |
| 14sp Regular | bodyMedium |
| 12sp Regular | bodySmall / labelLarge |
| 10sp Medium | labelSmall |

#### Components/Properties Section

**For screens:** List major sections/components
**For components:** List properties and sub-elements

For each element include:

- **Dimensions:** width × height (use "match_parent", "wrap_content", or specific dp values)
- **Background:** semantic token name (hex for reference)
- **Border:** if present, stroke width + color token
- **Padding:** horizontal × vertical
- **Corner radius:** if rounded
- **Content:** describe what's inside
- **States:** default, pressed, disabled, etc.
- **Action:** what happens on interaction

Example:

```markdown
## Components

### Continue Button

- **Type:** Circular FAB
- **Dimensions:** 110dp diameter
- **Background:** surfaceVariant (#0E1729)
- **Border:** 1dp solid outlineVariant (#1D293D)
- **Content:** "Continue" text (16sp, Roboto Regular, onSurface)
- **Position:** Centered horizontally, 660dp from top
- **States:**
  - Default: as shown
  - Pressed: surface overlay at 8% opacity
- **Action:** Navigate to next slide in carousel

### Pagination Dots

- **Type:** Progress indicator
- **Count:** 3 dots
- **Size:** 12dp diameter each
- **Spacing:** 8dp between dots
- **Active color:** #D9D9D9
- **Inactive color:** #1C293D
- **Current position:** Dot 1 (first slide)
- **Position:** Centered horizontally, 610dp from top
```

#### Design Tokens Section

List all tokens used in this design with their hex values:

````markdown
## Design Tokens Used

```json
{
  "surface": "#010517",
  "onSurface": "#F8FAFC",
  "headingColor": "#F1F5F9",
  "surfaceVariant": "#0E1729",
  "outlineVariant": "#1D293D"
}
```
````

#### Implementation Notes Section

Add helpful notes for implementation:

- Any tricky layout considerations
- Accessibility requirements (content descriptions, semantic roles)
- Edge cases to handle
- Performance considerations
- Platform-specific notes (iOS vs Android)

Example:

```markdown
## Implementation Notes

- Use `HorizontalPager` from accompanist-pager for slide navigation
- Pagination dots update automatically based on pager state
- Continue button advances to next page (or exits onboarding on last page)
- Consider fade animation between slides (300ms duration)
- **Accessibility:** Ensure heading has proper semantic role for screen readers
- **Edge case:** On last slide, Continue button should say "Get Started" and navigate to main app
```

### Step 5: Update Design Tokens

**If new tokens were identified:**

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
    "headlineMedium": "28sp, Roboto Bold",
    "bodyLarge": "16sp, Roboto Regular",
    "labelSmall": "10sp, Roboto Medium"
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

- Screens go in: `design-specs/screens/[name].md`
- Components go in: `design-specs/components/[name].md`

**Update tokens:**

- Save to: `design-specs/tokens.json`

**Provide summary:**

```markdown
✅ Design spec curation complete!

**Created:**

- design-specs/screens/onboarding-carousel.md

**Updated:**

- design-specs/tokens.json (added 2 new color tokens)

**What's in the spec:**

- Full screen layout (375×812)
- 3 major components: heading, body text, continue button
- Pagination dots for carousel navigation
- 5 design tokens mapped
- Interaction behavior documented

**Next steps:**

1. Review the generated spec for accuracy
2. Add any missing interaction details
3. Commit to repository
4. Ready for implementation phase!

**Note:** The raw JSON remains at design-specs/raw/onboarding-slide-2.json for reference.
```

## Special Handling for Common Patterns

### Buttons

Look for these indicators:

- Frame names containing: "button", "btn", "continue", "submit"
- Circular or rounded shapes (borderRadius > 0)
- Text + optional icon inside a frame
- Small, clickable size (typically 40-120dp)

Extract:

- Shape (rectangular/circular)
- Size (width × height or diameter)
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

- Dimensions
- Background/border colors
- Internal layout (horizontal/vertical)
- Content sections

### Navigation Elements

Look for these indicators:

- Pagination dots (multiple small circles)
- Tab bars (horizontal row of items)
- Headers with back buttons
- Bottom navigation

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

- Input type (text, number, email, etc.)
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
- Use descriptive names as fallback (e.g., "darkBlue", "lightGray")
- Note in summary that these may need better semantic names later

## Quality Checklist

Before finalizing the spec, verify:

- [ ] All interactive elements have documented actions
- [ ] All colors are mapped to semantic tokens
- [ ] Typography uses Material Design theme references
- [ ] Layout uses semantic positioning (not absolute coordinates)
- [ ] States are documented for interactive elements
- [ ] Implementation notes include accessibility guidance
- [ ] Tokens.json is updated with new values
- [ ] File is saved in correct directory (screens/ or components/)
- [ ] Summary clearly states what was created/updated

## Example Invocations

**User says:**

> "Curate the design spec from design-specs/raw/profile-switch-card.json"

**Claude Code does:**

1. Read and analyze the JSON
2. Identify it as a component (not full screen)
3. Ask clarifying questions about colors and interactions
4. Generate `design-specs/components/profile-switch-card.md`
5. Update `design-specs/tokens.json`
6. Provide summary

---

**User says:**

> "Follow .claude/tasks/curate-design-spec.md for design-specs/raw/onboarding-slide-2.json"

**Claude Code does:**

1. Read JSON and identify full screen
2. Find heading, body text, button, pagination dots
3. Ask about token names and interactions
4. Generate `design-specs/screens/onboarding-carousel.md`
5. Create/update `design-specs/tokens.json`
6. Provide summary with next steps
