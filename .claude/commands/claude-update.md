# Update Claude Review Knowledge

Refresh the `claude-review-criteria` skill with new knowledge — Anthropic feature releases, Claude Code documentation changes, and relevant updates from competitor tools. Proposes specific changes for your approval before writing anything.

This command does **not** review `.claude/` changes. For that, use `/review --audit`. This command maintains the knowledge base that powers those reviews.

## Instructions

### 1. Read the current skill

Read `.claude/skills/claude-review-criteria/SKILL.md` in full. Note the date-sensitive sections, particularly:

- Section 7 (Anthropic Documentation Alignment) — platform capabilities and best practices
- Section 8 (Competitor Tool Awareness) — what each tool does, opportunities and differentiators

### 2. Fetch new knowledge

Run the following searches in parallel. Prioritise sources from the last 6 months. Discard anything older than 12 months unless it corrects something currently in the skill.

**Anthropic / Claude Code:**
- Search: `Claude Code new features release notes 2025`
- Search: `Anthropic Claude Code hooks agents skills documentation`
- Fetch: `https://docs.anthropic.com/en/docs/claude-code/overview`
- Fetch: `https://docs.anthropic.com/en/docs/claude-code/hooks`
- Fetch: `https://docs.anthropic.com/en/docs/claude-code/sub-agents`

**Competitor tools:**
- Search: `Codex CLI OpenAI updates 2025 features`
- Search: `Opencode AI coding tool 2025 features agents`
- Search: `Cursor IDE rules AI features 2025`
- Search: `Gemini CLI Google AI coding 2025`
- Search: `Windsurf Codeium AI IDE features 2025`

You do not need to exhaustively read every result. Skim for:
- New capabilities or conventions that don't appear in the current skill
- Changes to existing patterns (e.g. a tool has dropped a convention we reference)
- Anything that would strengthen the "opportunities" or "differentiators" analysis

### 3. Compare and identify changes

Cross-reference what you found against the current skill content. Categorise each finding:

**New** — something genuinely new that should be added
**Stale** — something in the skill that is now outdated or incorrect
**Confirm** — something in the skill that new sources actively validate (no change needed, but worth noting)

Ignore minor phrasing updates. Focus on substantive changes to capability, convention, or competitive positioning.

### 4. Present proposed changes

Do not write anything yet. Present findings in this format:

---

## Knowledge Update Proposals

### New additions
| # | Section | Proposed addition | Source |
|---|---------|------------------|--------|
| N1 | Section 8 — Codex | [what to add and why] | [url or search result] |

### Stale content to update or remove
| # | Section | Current text (excerpt) | Proposed replacement | Reason |
|---|---------|----------------------|---------------------|--------|
| S1 | Section 7 | "..." | [replacement or "remove"] | [why it's stale] |

### Confirmed (no change needed)
[Brief list of things checked and found accurate — builds confidence in what isn't changing]

---

If there is nothing to update, say so clearly and stop.

### 5. Stop and wait for instructions

After presenting proposals, **stop**. Do not edit any files. Wait for the user to respond:

- "Apply all" — apply everything proposed
- "Apply N1 and S1" — apply specific items
- "Skip N2, apply the rest" — selective application
- "Tell me more about N1" — expand on the reasoning before deciding
- "Nothing to apply" — close out

### 6. Apply approved changes

For each approved item, edit `.claude/skills/claude-review-criteria/SKILL.md` surgically — change only the specific content being updated. Preserve all structure, headings, and surrounding content.

After applying, show a brief summary of what changed (section and nature of change — no need to reproduce the full diff).

### 7. Run a self-check

After writing, re-read the updated SKILL.md and confirm:

- All nine sections are still present and coherent
- No internal contradictions introduced (e.g. a new "opportunity" that contradicts an existing "differentiator")
- File is still under 500 lines

If the self-check finds a problem, report it and ask whether to fix it.

### 8. Commit (if requested)

If the user wants to commit the update, suggest they run `/commit`.
