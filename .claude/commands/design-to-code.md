Design-to-code pipeline for: $ARGUMENTS

# Task: Design to Code

## Purpose

Unified pipeline from Figma design → curated spec(s) → production Compose code. Handles components, screens, and flows in a single session.

Specs are **design documentation as well as implementation blueprints** — always pause for review between curation and implementation.

**Modes:**
- **Create** (default) — new design → new specs → implementation
- **Edit** (`--edit`) — changed design → updated specs → updated implementation

---

## Parsing Arguments

Parse `$ARGUMENTS` before doing anything else:

- If `$ARGUMENTS` starts with `--edit` → **Edit mode**. The remaining text is the path to the existing spec file (or ask if empty).
- Otherwise → **Create mode**. The argument may be a JSON file path, a name/description, or empty.

---

## CREATE MODE

### Step 0: Gather Inputs

Ask the user:

```
What have you got for this design?

- **Figma JSON export** — give me the file path(s) if you've exported JSON via the plugin
- **Screenshots** — drop them now or as we go
- **Just a description** — if you haven't exported anything yet

Also:
Is this a **component**, **screen**, or **flow**?
- **Component** — a reusable UI element (card, button, list item, etc.)
- **Screen** — a full screen (single screen, possibly with multiple states)
- **Flow** — a multi-screen user journey with navigation and branching
```

**Inference rules (apply silently before asking):**

- `$ARGUMENTS` contains a `.json` path → likely component or screen (ask which)
- `$ARGUMENTS` mentions a flow name or process → likely flow
- Screenshots provided upfront → likely flow

Store: `<type>` for all subsequent steps. The specs root is always `design-specs/`.

---

### Step 1: Curation Phase

Route based on `<type>`.

---

#### Route A: Component

Read and follow the workflow in `.claude/commands/design-to-code/curate-component-spec.md` in full.

This covers:
- Analysing the JSON structure
- Asking clarifying questions (token names, interactions, states)
- Generating `design-specs/components/<name>.md`
- Updating `design-specs/tokens.json`

---

#### Route B: Screen

Read and follow the workflow in `.claude/commands/design-to-code/curate-screen-spec.md`, with one addition after the Figma JSON analysis step:

**After analysing the primary JSON**, always ask:

```
Does this screen have significantly different states — ones where the layout or
components change substantially?

- **Minor differences** (a banner appears, a field turns red, an empty placeholder) →
  describe them and I'll document them conversationally
- **Significant differences** (different set of components, major layout restructure,
  e.g. a full-screen error state) → export that state as a separate Figma JSON and
  give me the path

Common states to consider: loading, empty, error, editing, success, with-validation
```

For each additional JSON provided: extract only the *diff* from the base spec (which sections appear/disappear, new components, new tokens) and feed this into the screen spec's `## Screen States` section only.

Continue with the `curate-screen-spec` workflow to completion.

---

#### Route C: Flow

**Part 1 — Flow spec and screen specs**

Read and follow the workflow in `.claude/commands/design-to-code/curate-flow-spec.md` in full.

When gathering inputs, prompt:

```
Drop your Figma screenshots of the flow — an annotated overview showing all screens
and navigation, or individual screen images in order, works great.

I'll extract the screen sequence, branching, and component mapping from them.
If you don't have screenshots yet, describe the flow and we'll build it conversationally.
```

**Part 2 — Mermaid flow diagram**

Immediately after the flow spec is confirmed (before the review gate), read and follow the workflow in `.claude/commands/design-to-code/curate-flow-diagram.md`.

Use **Path B (existing diagram)**: the ASCII flow diagram captured in the flow spec is the source material. Convert it to a Mermaid diagram following conventions in `.claude/skills/mermaid/SKILL.md`.

Generate `design-specs/flows/<name>-mermaid.md` and iterate until confirmed. This is part of the curation phase — complete it before moving to the review gate.

---

### Step 2: Spec Review Gate

Present all generated files:

```
## Specs Ready — Review Before Implementation

These files are your design documentation. Review them before I write any code.

**Created:**
{list every generated spec file with its full path}

{if flow: also list the Mermaid diagram file}

Take a look and let me know if anything needs changing. When you're ready,
say **"implement"** and I'll proceed to code generation.
```

**Do not proceed to the next step until the user explicitly confirms.**

If the user requests changes: apply them, update the affected files, and re-present the summary. Repeat until confirmed.

---

### Step 2.5: Planning Gate (Screens and Flows)

**Skip this step for components** — they rarely need a plan.

After specs are confirmed, ask:

```
This is a {screen/flow} with {brief scope summary — e.g. "a new ViewModel, data layer
helper, and navigation changes"}.

Would you like to plan the implementation before coding?

- **"plan"** — I'll create a `docs/features/{name}/plan.md` covering architecture,
  data flow, phases, and testing strategy
- **"skip"** — proceed straight to implementation
```

**Deriving the feature name:** use the spec filename without extension (e.g. `scores-over-time.md` → `scores-over-time`). If the name is ambiguous or derived from a raw JSON path, ask the user to confirm.

**If the user says "plan":**

1. Use the Agent tool to invoke the **`rubber-duck`** agent, passing:
   - The confirmed spec file path(s)
   - The instruction to read the specs and create an implementation plan at `docs/features/{name}/plan.md` — write only this file, no session log. Override the default session log behaviour — do not create a session log or run the `rm -f` cleanup for this invocation.
   - A note to follow the `plan.md` format in `.claude/skills/docs-conventions/SKILL.md`
   - The scope: architecture decisions, ViewModel design, data layer changes, navigation wiring, testing strategy, and phased tasks
2. Present the plan to the user for review. Iterate until confirmed.
3. Pass the plan path to the `jake-wharton` agent in Step 3 alongside the spec paths.

**If the user says "skip":** proceed directly to Step 3.

---

### Step 3: Implementation

Use the Agent tool to invoke the **`jake-wharton`** agent, passing:
- The confirmed spec file path(s)
- The instruction to implement following the workflow in `.claude/commands/design-to-code/implement-spec.md`
- The dependency order below

| Spec Type | Implementation Order |
|-----------|---------------------|
| Component | Single component spec |
| Screen | Referenced component specs (check they're implemented first) → screen spec |
| Flow | New component specs → each screen spec → navigation wiring |

The agent runs in its own context and returns the results. For flows, ask the agent to note any navigation wiring that requires manual connection.

---

### Step 4: Summary

```
## Design to Code Complete

**Specs created:**
{list}

**Code generated:**
{list}

{if flow:}
**Navigation wiring needed:**
{any remaining manual steps for NavHost route registration, ViewModel sharing, etc.}
```

---

## EDIT MODE

Used when an existing design has changed and specs plus code need updating.

---

### Step E0: Identify the Spec

If a spec path was provided after `--edit` in `$ARGUMENTS`, read that file.
Otherwise ask:

```
Which spec file needs updating? Provide the path
(e.g. design-specs/components/score-card.md)
```

Read the existing spec. Note:
- **Type** (Component / Screen / Flow) from the spec header
- **Current values** — layout, tokens, states, components (for diffing in E2)

For a **Flow spec**, also read all screen specs it references.

---

### Step E1: Gather What Changed

```
What changed in the design? You can:

1. **Updated Figma JSON** — I'll compare it against the current spec and identify differences
2. **Screenshots** — show me the updated screens (good for flows)
3. **Description** — if the changes are straightforward, just tell me
```

---

### Step E2: Analyse Differences

**If updated JSON was provided:** Re-analyse and present a diff:

```
## What Changed

**Layout:** {changes, or "no changes"}
**Typography:** {changes, or "no changes"}
**Tokens:** {new tokens, changed values, removed tokens — or "no changes"}
**Components:** {added / removed / changed — or "no changes"}
**States:** {new states, removed states, changed states — or "no changes"}

Does this capture all the changes? Anything I've missed?
```

Wait for confirmation.

**If screenshots provided (flow):** Identify which steps and screens changed — new screens, removed screens, changed branching, new components.

**If verbal description:** Extract the described changes, confirm before applying.

---

### Step E3: Update Specs

Apply confirmed changes to existing files.

**Component edit:** Update `design-specs/components/<name>.md` in place. Update `design-specs/tokens.json` if values changed.

**Screen edit:** Update `design-specs/screens/<name>.md`. If sections now reference different components, update the Section Layout table. Create new component specs inline if needed.

**Flow edit:** Update `design-specs/flows/<name>.md` and all affected screen specs. If screens were added, create new screen specs. Regenerate `design-specs/flows/<name>-mermaid.md` following the `curate-flow-diagram` workflow, iterate until confirmed.

---

### Step E4: Spec Review Gate

```
## Updated Specs — Review Before Re-implementing

**Modified:**
{list every updated spec file}

**Created:**
{list any new files}

Review the changes. When you're ready, tell me whether to:
- **"Update the code"** — re-run implementation for all changed specs
- **"Just the specs"** — stop here; you'll handle the code separately
```

---

### Step E5: Update Implementation (if requested)

Use the Agent tool to invoke the **`jake-wharton`** agent, passing:
- The changed spec file path(s)
- The instruction to update the implementation following `.claude/commands/design-to-code/implement-spec.md`
- A note that this is an edit — existing files should be updated in place, not recreated

---

## Quality Checklist

Before completing either mode, verify:

- [ ] All specs reviewed and confirmed by the user before implementation
- [ ] `design-specs/tokens.json` is up to date with all colour values
- [ ] Spec cross-references (flow → screens → components) use correct relative links
- [ ] For flows: Mermaid diagram confirmed before the review gate
- [ ] Implementation files are in the correct directories under `mobile/`
- [ ] Summary lists every created and updated file clearly

---

## Example Invocations

**Create — component from JSON:**
```
/design-to-code design-specs/raw/score-card.json
```

**Create — screen (states emerge during conversation):**
```
/design-to-code
```

**Create — flow from screenshots:**
```
/design-to-code food logging flow
```
*(drop Figma screenshots when asked)*

**Edit — component:**
```
/design-to-code --edit design-specs/components/score-card.md
```

**Edit — flow:**
```
/design-to-code --edit design-specs/flows/onboarding.md
```
