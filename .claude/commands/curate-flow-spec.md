Curate a multi-screen user flow spec for: $ARGUMENTS

# Task: Curate Flow Spec

## Purpose

Capture a multi-screen user flow as an implementation-ready spec. Flows span multiple screens with possible branching logic, shared state, and screen reuse. The output is a **flow spec** (the journey narrative and state machine) plus **screen specs** (one per unique screen layout) that reference existing component specs.

This command is screenshot-first — users typically drop annotated Figma screenshots and Claude extracts the flow structure, then confirms conversationally. Falls back to a fully conversational walkthrough when screenshots aren't available.

## Prerequisites

- `.claude/design-specs-guide.md` exists (defines all spec formats including Flow and Screen)
- `design-specs/` directory exists with a `components/` folder containing curated component specs

## Output Structure

The command produces two layers of specs:

### Layer 1 — Flow Spec (`flows/<flow-name>.md`)

Captures the journey narrative, branching logic, and state machine. References screen specs by relative link.

### Layer 2 — Screen Specs (`screens/<screen-name>.md`)

One per **unique screen layout**. Screens that appear multiple times in the flow with minor differences (e.g. "same screen but with an extra validation message") become **states/variants** within a single screen spec. Screen specs reference component specs in `components/` via relative links.

---

## Workflow Steps

### Step 1: Inventory Existing Specs

**Silently** read all specs:

1. Read all files in `design-specs/components/*.md` — build a list of available component specs
2. Read all files in `design-specs/screens/*.md` — build a list of existing screen specs
3. Read all files in `design-specs/flows/*.md` — check for existing flow specs
4. Read `design-specs/tokens.json` if it exists

**Present a summary to the user:**

```
## Available Component Library

I found {n} component specs:

**Components:** {comma-separated list}

**Existing Screens:** {list or "none yet"}
**Existing Flows:** {list or "none yet"}

These are available to reference in your flow. Components not in this list will be flagged as needing new specs.
```

### Step 2: Gather the Happy Path

**Screenshots are the primary input mode.** Most flows start with Figma screenshots showing the screens, states, and navigation. Prompt for them first:

```
Let's map the happy path. Drop your Figma screenshots of the flow — annotated screens showing the journey from start to finish work best (labelled screens, arrows between steps, notes on branching or validation states).

I'll analyse them to extract:
- Screen names and order
- UI components (matched against your component library)
- Navigation and branching
- Validation states and screen variants

If you don't have screenshots handy, just walk me through the flow verbally instead.
```

#### Screenshot Path (primary)

When the user provides screenshots (typically exported from Figma or similar tools):

1. **Analyse each screenshot in order.** For each, extract:
    - **Screen names** — from labels, frame titles, or headings visible in the screenshot
    - **UI components** — identify buttons, form fields, cards, dialogs, etc. and match against the component library from Step 1
    - **Navigation flow** — arrows, numbered sequences, or visual connections between screens
    - **Branching points** — decision diamonds, labelled arrows showing alternate paths (e.g. "Yes" / "No"), conditional annotations
    - **Validation states** — error messages, warning banners, disabled states, loading indicators
    - **Screen variants** — the same screen layout appearing with different states (e.g. empty vs populated, default vs error)

2. **Present the extracted flow for confirmation:**

```
## Screenshot Analysis

I've extracted the following flow from your screenshots:

**Entry:** {trigger} from {origin}

1. **{Screen name}** — {what user sees based on screenshot}
   - Components spotted: {list matched against library}
   - User action: {primary action} → goes to step 2
2. **{Screen name}** — {what user sees}
   - Components spotted: {list}
   - User action: {action} → goes to step 3
...
n. **{Screen name}** — {what user sees}. Flow complete → {exit destination}

**Branches detected:**
- At step {n}: {branch description} (e.g. "Yes → step 3a, No → step 3b")

**Validation states detected:**
- {screen-name}: `error` state with {description}
- {screen-name}: `loading` state with {description}

**Components not in library:**
- {component description} on {screen-name} — [TODO: Spec needed]

Does this look right? Any screens or paths I've missed or misread?
```

3. **Confirm with the user conversationally.** Screenshots front-load the analysis so the remaining steps (3–6) become confirmations and refinements rather than open-ended questions. After confirmation, proceed to Step 3. Any gaps or ambiguities identified during screenshot analysis feed directly into the conversational follow-up.

#### Conversation Path (fallback)

If the user doesn't have screenshots, fall back to a conversational walkthrough:

```
Let's map the happy path first — the ideal journey from start to finish.

1. **Where does the user come from?** (e.g. dashboard, notification, deep link)
2. **What triggers the flow?** (e.g. taps a button, lands on a route)
3. **Walk me through each screen in order:**
   - What does the user see?
   - What do they do?
   - Where do they go next?
4. **Where does the user end up when the flow is complete?**

Don't worry about error paths or branches yet — we'll cover those next.
```

---

**Regardless of input mode,** after the user responds (or confirms the screenshot analysis), summarise the happy path as numbered steps and **confirm**:

```
## Happy Path Summary

**Entry:** {trigger} from {origin}

1. **{Screen name}** — {what user sees}. User {action} → goes to step 2
2. **{Screen name}** — {what user sees}. User {action} → goes to step 3
...
n. **{Screen name}** — {what user sees}. Flow complete → {exit destination}

Does this look right? Any steps missing or in the wrong order?
```

Wait for confirmation before proceeding.

### Step 3: Identify Screen Reuse and Variants

**If screenshots were provided in Step 2,** present what was already detected and confirm:

```
From the screenshots, I've identified these screen groupings:

1. **{screen-name}** — appears in steps {list}
   - States: `default` (step X), `with-validation` (step Y)
2. **{screen-name}** — appears in step {n} only
   - States: `default`
...

This means I'll create {count} screen specs. Does this look right, or do any screens share a layout that I've missed?
```

**If conversation-only,** ask about screen reuse:

```
Looking at the screens above, do any steps share the same underlying screen layout?

For example:
- Steps 2 and 5 might both be a "form screen" but step 5 has an extra validation summary
- Steps 3 and 7 might be the same "confirmation screen" with different content

Which steps use the same screen layout, and what changes between them?
```

**In either case,** after the user confirms or provides corrections, present the unique screens:

```
## Unique Screens ({count})

1. **{screen-name}** — used in steps {list}
   - States: `default` (step X), `with-validation` (step Y)
2. **{screen-name}** — used in step {n} only
   - States: `default`
...

This means I'll create {count} screen specs. Does this look right?
```

Wait for confirmation.

### Step 4: Map Branching Logic

**If screenshots were provided in Step 2,** present the branches already detected and ask for gaps:

```
From the screenshots, I detected these branches:

- **Step {n}:** {branch description} (e.g. "Yes → step 3a, No → step 3b")
- **Step {n}:** {branch description}

For each step, are there any branches I'm missing?
- **Error paths:** What happens if something fails? (e.g. validation error, API failure, timeout)
- **System conditions:** Does the system route differently based on data? (e.g. user has existing data vs new user)
- **Escape routes:** Can the user cancel, go back, or skip at each step?
```

**If conversation-only,** ask about branching from scratch:

```
Now let's map the branches and alternate paths.

For each step, consider:
- **User choices:** Can the user take different actions? (e.g. "Yes" vs "No", selecting different options)
- **System conditions:** Does the system route differently based on data?
- **Error paths:** What happens if something fails?
- **Escape routes:** Can the user cancel, go back, or skip at each step?

Walk me through any branches, step by step.
```

**In either case,** after the user responds, present the full branching as an **ASCII flow diagram** and confirm:

```
## Flow Diagram

  [Entry: {trigger}]
        │
        ▼
  ┌─────────────┐
  │ 1. {screen}  │
  └──────┬──────┘
         │
    ┌────┴────┐
    ▼         ▼
  [Yes]     [No]
    │         │
    ▼         ▼
  ┌─────┐  ┌─────┐
  │ 2a  │  │ 2b  │
  └──┬──┘  └──┬──┘
     │        │
     └───┬────┘
         ▼
  ┌─────────────┐
  │ 3. {screen}  │
  └──────┬──────┘
         │
         ▼
   [Exit: {destination}]

Does this capture all the paths? Any branches I'm missing?
```

Wait for confirmation.

### Step 5: Identify Shared State

**If screenshots were provided in Step 2,** infer shared state from visible data patterns and confirm:

```
Based on the screens, I can see data that likely flows between steps:

| Key | Type | Description | Set By | Used By |
|-----|------|-------------|--------|---------|
| {key} | {type} | {inferred from visible UI elements} | Step {n} | Steps {list} |
...

A few things I can't tell from the screenshots alone:
- Does any data come from navigation arguments?
- Is anything loaded from an API at the start?
- Does anything need to persist if the user navigates back?

Does this cover all the shared data, or is there anything I've missed?
```

**If conversation-only,** ask about state from scratch:

```
What data is shared across screens in this flow?

For each piece of shared data, I need to know:
- **What is it?** (e.g. "selected profile", "form data", "API response")
- **What type?** (e.g. String, Int, data class)
- **Where is it set?** (which step/screen)
- **Where is it used?** (which steps/screens read it)

Also consider:
- Does any data come from navigation arguments?
- Is anything loaded from an API at the start of the flow?
- Does anything need to persist if the user navigates back?
```

**In either case,** after the user responds, present as a table and confirm:

```
## Shared State

| Key | Type | Description | Set By | Used By |
|-----|------|-------------|--------|---------|
| selectedProfile | String | The profile the user chose | Step 2 | Steps 3, 4, 5 |
| formData | FormData | Accumulated form answers | Steps 2–4 | Step 5 (review) |
...

Does this cover all the shared data?
```

Wait for confirmation.

### Step 6: Map Components per Screen

**If screenshots were provided in Step 2,** the component mapping is already largely done. Present what was extracted and confirm per screen:

```
Let's confirm the component mapping for each screen.

### Screen: {screen-name}

From the screenshots, I spotted these components:

| Order | Component | Spec | Visibility |
|-------|-----------|------|------------|
| 1 | {component} | [{name}](../components/{name}.md) | Always |
| 2 | {component} | [{name}](../components/{name}.md) | `{state}` state only |
| 3 | {component} | **No match** — needs new spec | Always |

Anything I've missed or misidentified?
```

**If conversation-only,** walk through each screen's composition:

```
Let's map the components for each screen.

### Screen: {screen-name}

Looking at this screen, which of these existing components appear?
{list available components from Step 1}

For each component:
1. Where does it sit in the layout? (top, scrollable middle, bottom)
2. Is it always visible, or only in certain states/variants?
3. Does it need any data from shared state?

Are there any UI elements on this screen that don't match an existing component spec?
```

#### Requesting Figma JSON for Detail

If the agent needs more precise layout data (dimensions, padding, exact token values) for a specific screen — particularly for generating accurate screen specs — it can request a Figma JSON export:

```
I'd like more precise layout details for {screen-name}. Could you export the Figma JSON for this screen? I'll use it to extract exact dimensions, spacing, and token values.
```

This is agent-initiated — don't ask for JSON upfront or present it as an input option. Only request it when the screenshots don't provide enough detail for accurate spec generation.

#### Figma JSON Analysis (per screen)

If the user provides a Figma JSON export for a screen:

1. **Read the JSON file** and analyze the top-level frame
2. **Identify the overall layout:** dimensions, padding in dp, spacing, alignment
3. **Find child elements and classify them:**
    - `type: "INSTANCE"` elements — match instance name against existing component specs
    - `type: "FRAME"` elements — pattern-match properties against existing specs
    - Group related elements into logical sections
4. **Extract typography and color tokens** — map against existing `tokens.json`
5. **Present findings:**

```
## Figma JSON Analysis — {screen-name}

**Dimensions:** {width} × {height}
**Layout:** {vertical/horizontal}, padding {values in dp}, gap {value in dp}

| # | Section | Matched Component | Confidence |
|---|---------|-------------------|------------|
| 1 | {description} | [{component}](../components/{name}.md) | High — INSTANCE match |
| 2 | {description} | [{component}](../components/{name}.md) | Medium — pattern match |
| 3 | {description} | **No match** — needs new spec | — |

Does this mapping look right?
```

Use the JSON analysis to confirm or refine the component mapping. Store layout details (dimensions, padding, gap) for use in Step 7 when generating the screen spec.

If no JSON is provided for a screen, continue with conversation-only mapping as normal.

#### Inline Component Speccing

When a section doesn't match any existing component spec, offer to spec it inline:

1. **Extract the component's JSON subtree** from the screen's JSON export (if JSON was provided)
2. **Run through the component spec workflow** (same analysis as `/curate-component-spec`):
    - Analyze dimensions, colors, typography, layout patterns
    - Map to design tokens
    - Identify states and interactions
3. **Ask the user clarifying questions** — token names, interaction behaviour, states, component name
4. **Generate the component spec** and save to `design-specs/components/<name>.md`
5. **Update tokens.json** if new tokens were found
6. **Resume screen curation** with the new component now available in the library

If the user declines to spec a component inline, flag it as `[TODO: Spec needed]` and continue.

#### Component Mapping Summary

Repeat for each unique screen, then present the full summary:

```
## Component Mapping Summary

### {screen-name}
| Order | Component | Spec | Visibility |
|-------|-----------|------|------------|
| 1 | Section Header | [section-header](../components/section-header.md) | Always |
| 2 | Score Ring | [score-ring](../components/score-ring.md) | Always |
| 3 | Validation Error | [TODO: Spec needed] | `error` state only |

### {screen-name-2}
...

**Components needing new specs:** {list, or "None — all specced"}

Does this mapping look right?
```

Wait for confirmation.

### Step 7: Generate Specs

Generate all spec files. **Screen specs first** (because the flow spec references them), then the flow spec.

#### Screen Spec Template

For each unique screen, generate a file at `design-specs/screens/<screen-name>.md`. If Figma JSON was provided for the screen in Step 6, incorporate the JSON-derived layout details (dimensions, padding, gap, typography tokens) into the spec rather than using placeholder values.

```markdown
# {Screen Name}

**Type:** Screen
**Figma:** {link if provided, otherwise [TODO: Add Figma link]}
**Created:** {current date YYYY-MM-DD}

## Overview

{1–2 sentence description of what this screen shows and its role in the flow}

## Used In Flows

- [{flow-name}]({relative path to flow spec}) — Steps {list of step numbers}

## Layout

- **Structure:** {e.g. `Column(modifier = Modifier.fillMaxSize())`, single scrollable column}
- **Content padding:** `Modifier.padding(horizontal = {n}.dp, vertical = {n}.dp)`
- **Section gap:** `Arrangement.spacedBy({n}.dp)`

## Section Layout

| Order | Section | Component Spec | Visibility Condition |
|-------|---------|---------------|---------------------|
| 1 | {section name} | [{component-name}](../components/{component-name}.md) | Always |
| 2 | {section name} | [{component-name}](../components/{component-name}.md) | `{state}` state only |
| 3 | {section name} | [TODO: Spec needed] | Always |

## Screen States

### `default`

{Description of the default appearance — which sections are visible, what data is shown}

### `{variant-name}` (e.g. `with-validation`, `error`, `loading`)

{Description of what changes from default — which sections appear/disappear, what content changes}

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| {paramName} | {type} | {yes/no} | {what it controls} |

## Callbacks

| Name | Signature | Description |
|------|-----------|-------------|
| {callbackName} | `() -> Unit` | {when it fires and what it means} |

## Design Tokens Used

Only tokens for the screen's own layout — not child component internals.

```json
{
  "tokenName": "#hexValue"
}
```

## Implementation Notes

- **Composition:** {how this screen composes its child composables}
- **Data fetching:** {ViewModel StateFlow observations or `LaunchedEffect` API calls}
- **Validation:** {form validation approach if applicable}
- **Accessibility:** {screen-level a11y — `Modifier.semantics`, focus management, `contentDescription`}
- **Route:** {Navigation Compose route, e.g. `Screen.FlowName.StepName`}
```

#### Flow Spec Template

Generate a single file at `design-specs/flows/<flow-name>.md`:

```markdown
# {Flow Name}

**Type:** Flow
**Figma:** {link if provided, otherwise [TODO: Add Figma link]}
**Created:** {current date YYYY-MM-DD}

## Overview

{Narrative description of what the user is trying to accomplish — 2–4 sentences covering the goal, who the user is, and why this flow exists}

## Entry Points

| Origin | Trigger | Initial Step |
|--------|---------|-------------|
| {where user comes from} | {what starts the flow} | Step 1 |

## Shared State

| Key | Type | Description | Set By | Used By |
|-----|------|-------------|--------|---------|
| {key} | {type} | {description} | Step {n} | Steps {list} |

## Flow Steps

### Step 1: {Step Name}

**Screen:** [{screen-name}](../screens/{screen-name}.md)
**Screen State:** `default`
**Purpose:** {what the user does at this step}

| Action | Condition | Next Step | Side Effects |
|--------|-----------|-----------|-------------|
| {user action} | {condition or "always"} | Step 2 | {state changes, API calls, etc.} |
| {alt action} | {condition} | Step 3 | {side effects} |
| Cancel | always | Exit: {destination} | {cleanup} |

### Step 2: {Step Name}

**Screen:** [{screen-name}](../screens/{screen-name}.md)
**Screen State:** `{variant}`
**Purpose:** {what the user does at this step}

| Action | Condition | Next Step | Side Effects |
|--------|-----------|-----------|-------------|
| {action} | {condition} | Step 3 | {side effects} |

{...continue for all steps}

## Exit Points

| Exit | From Step | Condition | Destination |
|------|-----------|-----------|-------------|
| {exit name} | Step {n} | {condition} | {where user goes} |
| Cancel | Any | User cancels | {destination} |

## Error Handling

| Error Condition | Occurs At | Recovery Path |
|----------------|-----------|---------------|
| {error description} | Step {n} | {what happens — retry, show dialog, redirect} |

## Flow Diagram

```
{ASCII diagram from Step 4, refined with final step numbers and screen names}
```

## Implementation Notes

- **Route structure:** {Navigation Compose routes for each screen, e.g. `Screen.FlowName.Step1`, `Screen.FlowName.Step2`}
- **State management:** {ViewModel + StateFlow — how shared state is held and observed across screens}
- **Transitions:** {Compose Navigation transitions — slide, fade, or none}
- **Back button:** {system back button behaviour — `NavController.popBackStack()` to go to previous step, or exit flow}
- **Deep linking:** {whether mid-flow routes support deep link navigation}
- **Guard rails:** {conditional navigation based on ViewModel state to prevent skipping steps or accessing completed flows}
```

### Step 8: Review and Save

Present the generated specs to the user for review:

```
## Generated Specs — Review

I've generated the following files:

### Flow Spec
- `design-specs/flows/{flow-name}.md`

### Screen Specs ({count})
- `design-specs/screens/{screen-1}.md`
- `design-specs/screens/{screen-2}.md`
...

I'll show you each file now. Let me know if anything needs correcting before I save.
```

Show each file's content and wait for the user to approve or request changes. Make corrections as needed. Then save all files.

### Step 9: Summarise

After saving, present a summary:

```
## Flow Spec Curation Complete

**Created:**
- `design-specs/flows/{flow-name}.md`
- `design-specs/screens/{screen-1}.md`
- `design-specs/screens/{screen-2}.md`
...

**Flow Stats:**
- {n} total steps
- {n} unique screens
- {n} exit points
- {n} branching conditions
- {n} shared state keys

**Component Gaps:**
- {list of components flagged as [TODO: Spec needed], or "None — all components have specs"}

**Next Steps:**
1. Review generated specs for accuracy against Figma designs
2. Create missing component specs with `/curate-component-spec`
3. Implement screens referencing the specs
4. Wire up Navigation Compose routes and ViewModel state per the Implementation Notes
5. Commit to repository
```

---

## Quality Checklist

Before finalising, verify:

- [ ] All steps in the happy path are captured
- [ ] Screen reuse is identified — no duplicate screen specs for the same layout
- [ ] All branches and error paths are mapped
- [ ] Shared state is fully documented (set by / used by)
- [ ] Every screen section references a component spec (or is flagged as TODO)
- [ ] Flow spec has correct relative links to screen specs
- [ ] Screen specs have correct relative links to component specs
- [ ] Screen specs use Compose/Material3 patterns (dp values, `MaterialTheme.*`, callbacks not emits)
- [ ] ASCII flow diagram matches the step definitions
- [ ] Exit points cover all ways a user can leave the flow
- [ ] Implementation notes include Navigation Compose route structure, ViewModel state management, and back-button behaviour
- [ ] Files are saved in correct directories (`design-specs/flows/` for flow spec, `design-specs/screens/` for screen specs)

## Handling Edge Cases

### User doesn't know exact branching yet

Document what's known, use `[TODO: Confirm branching logic]` placeholders, and note in the summary that branching needs refinement.

### Screen appears in multiple flows

If a screen spec already exists from a previous flow, don't recreate it. Instead, add the new flow to its "Used In Flows" section.

### Component spec doesn't exist yet

Flag it as `[TODO: Spec needed]` in the screen spec's Section Layout table. List all missing components in the Step 9 summary so the user knows what to curate next.

### Flow is too complex for a single spec

If the flow exceeds ~15 steps, suggest splitting into sub-flows. Each sub-flow gets its own flow spec, and they reference each other in their Entry Points / Exit Points sections.

### User wants to iterate on a specific step

Allow the user to go back to any previous workflow step by saying "let's revisit step {n}". Update only the affected sections and re-confirm.

## Example Invocations

**User says:**

> `/curate-flow-spec onboarding setup wizard`

**Claude Code does:**

1. Inventory existing component specs
2. Walk through: entry trigger → each screen → completion
3. Identify which screens are reused with variants
4. Map branching (e.g. different paths based on goals)
5. Identify shared state (e.g. profile data accumulating across steps)
6. Map components per screen
7. Generate screen specs + flow spec
8. Present for review, save, and summarise

---

**User says:**

> `/curate-flow-spec context-switch`

**Claude Code does:**

1. Inventory specs
2. Gather the context-switch happy path (trigger → profile list → confirm → reload → return)
3. Identify screen reuse (e.g. error state reuses same screen with different content)
4. Map branches (e.g. single profile → skip selection, API error → show dialog)
5. Identify shared state (selected profile ID, originating screen)
6. Map components
7. Generate and save all specs
