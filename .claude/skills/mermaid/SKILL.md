---
name: mermaid-diagrams
description: Generate consistent, well-structured Mermaid diagrams for user flows, architecture, and technical documentation. Use when asked to create flowcharts, user flows, sequence diagrams, state diagrams, or any visual documentation. Produces diagrams that follow team conventions for node shapes, naming, layout, and annotation style.
---

# Mermaid Diagrams

Generate Mermaid diagrams that follow team conventions for consistency across the codebase. All diagrams should be readable by both humans and AI agents, living alongside code in version control.

Before generating any diagram, read the relevant reference files:
```
Read syntax-cheatsheet.md   # Node shapes, edge syntax, styling
Read examples.md            # Team-style example diagrams
```

## Diagram Type Selection

Choose the right diagram type for the content:

| Content | Diagram Type | Mermaid Keyword |
|---------|-------------|-----------------|
| User flows, registration, onboarding | Flowchart | `flowchart LR` or `flowchart TD` |
| API request/response sequences | Sequence diagram | `sequenceDiagram` |
| Component state (e.g. form states) | State diagram | `stateDiagram-v2` |
| Feature timelines, sprint planning | Gantt chart | `gantt` |
| Data models, entity relationships | ER diagram | `erDiagram` |
| User experience mapping | Journey | `journey` |

## Conventions

### Flow Direction

- **User flows**: Use `flowchart LR` (left-to-right) as the default. Users read flows like sentences.
- **Hierarchical/tree structures**: Use `flowchart TD` (top-down).
- **Never mix directions** within a single diagram. If a flow has loops, use edge labels and back-references rather than reversing direction.

### Node Shapes

Use shapes consistently to convey meaning at a glance:

| Shape | Syntax | Use For |
|-------|--------|---------|
| Stadium (rounded) | `([text])` | Start/end points, entry screens, terminals |
| Rectangle | `[text]` | User actions, process steps |
| Diamond | `{text}` | Decision points (Yes/No, conditionals) |
| Parallelogram | `[/text/]` | UI output: dialogs, toasts, error messages |
| Hexagon | `{{text}}` | API calls, system operations |
| Subroutine | `[[text]]` | Reusable sub-flows, shared processes |

### Node IDs and Labels

- Use short, descriptive camelCase IDs: `userSubmit`, `checkAuth`, `showError`
- Never use single letters (`A`, `B`, `C`) — they make diagrams unreadable when revisited
- Prefix with the flow area if the diagram is large: `reg_submit`, `reg_checkEmail`, `auth_validate`
- **Do not put step numbers in node labels** — describe what the node does instead. Step numbers belong in flow spec documents as reference anchors, not in diagrams.

### Edge Labels

- Decision edges **must** have labels: `-->|Yes|` and `-->|No|`
- Use short, clear labels: `-->|Valid|`, `-->|Invalid|`, `-->|Authenticated|`
- Non-decision edges should only have labels when the transition isn't obvious

### Annotations and Notes

Mermaid flowcharts don't have native annotation support. For implementation notes, conditional context, or developer-facing commentary, use one of these approaches:

**Option 1 — Styled note nodes (preferred):**
```mermaid
noteCondition[/"If user has no profiles, skip selection step"/]
style noteCondition fill:#f9f9f9,stroke:#999,stroke-dasharray: 5 5,font-size:12px
```

**Option 2 — Subgraph as context block:**
```mermaid
subgraph note_context ["Note"]
    noteText["Similar flow to onboarding\nbut with different messaging"]
end
style note_context fill:#f9f9f9,stroke:#999,stroke-dasharray: 5 5
```

Use Option 1 for short inline notes. Use Option 2 for longer contextual blocks.

**Do not use emojis or icons** (e.g. `ℹ️`) in note text — keep annotations plain text.

### Subgraphs

- Use subgraphs to group related steps (e.g. "Profile Setup", "Score Calculation", "Sync")
- Label subgraphs with clear phase names
- Keep subgraphs to a maximum of 2 levels deep — deeper nesting becomes unreadable

### Styling

Apply a consistent colour scheme using `classDef`:

```mermaid
classDef startEnd fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px,color:#1b5e20
classDef action fill:#e3f2fd,stroke:#1565c0,stroke-width:1px,color:#0d47a1
classDef decision fill:#fff3e0,stroke:#e65100,stroke-width:2px,color:#bf360c
classDef uiOutput fill:#f3e5f5,stroke:#6a1b9a,stroke-width:1px,color:#4a148c
classDef apiCall fill:#fce4ec,stroke:#b71c1c,stroke-width:1px,color:#880e4f
classDef note fill:#f9f9f9,stroke:#999,stroke-dasharray: 5 5,color:#666
```

Always define class styles at the end of the diagram. Apply them with `:::className` syntax on the node or with `class nodeId className` statements.

Only apply styling when the diagram has enough complexity to benefit from it (roughly 8+ nodes). Small diagrams are clearer without colour.

## File Location and Naming

- Store diagrams in `design-specs/flows/`
- Name files to match the associated spec with a `-mermaid` suffix: `context-switch-mermaid.md`, `onboarding-mermaid.md`
- Each file should contain:
  1. A heading with the diagram title
  2. A brief description of what the flow covers
  3. The mermaid code block
  4. An optional "Notes" section for context that doesn't fit in the diagram

Example file (`context-switch-mermaid.md`):

````markdown
# Context Switch Flow

Covers the flow for switching between user profiles including
confirmation, data reload, and navigation back to the triggering screen.

```mermaid
flowchart LR
    ...
```

## Notes
- Profile switch triggers a full data reload for the new profile
- If only one profile exists, the selection screen is skipped
````

## Working with claude-mermaid MCP

When the `mermaid_preview` tool is available, use it to render diagrams live in the browser during iteration. This lets the user see layout and readability issues in real time.

Workflow:
1. Generate the initial diagram based on the user's description
2. Render with `mermaid_preview` for visual feedback
3. Iterate based on user feedback — the preview auto-refreshes via WebSocket
4. When finalised, save the source markdown to the repository. Do not commit rendered SVG/PNG files — GitHub renders mermaid code blocks natively

If `mermaid_preview` is not available, write the diagram to a `.md` file. The user can preview it with the VS Code Mermaid extension or at https://mermaid.live.

## Quality Checklist

Before presenting a diagram as complete:

- [ ] Every decision node has labelled edges for each outcome
- [ ] Node IDs are descriptive, not single letters
- [ ] Start and end points use rounded/stadium shapes
- [ ] UI outputs (dialogs, errors, toasts) use parallelogram shapes
- [ ] The flow can be read without referring to external documentation
- [ ] Annotations/notes are visually distinct from flow nodes
- [ ] The diagram renders without syntax errors
