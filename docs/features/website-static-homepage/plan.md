# Website Static Homepage — Implementation Plan

**Status:** Complete
**Date:** 2026-04-06
**Branch:** website/static-html-redesign

## Goal

Replace the kmp branch's placeholder `website/index.html` with a static HTML page that matches the visual design of the current Next.js site on `main`, and preserves the privacy policy content at the same URL path (`/`).

## Context

On `main`, the Next.js homepage at `/` **is** the privacy policy — there is no separate marketing page or `/privacy-policy` route. So "update the homepage" and "add the privacy policy at the same path" are the same task: update `index.html` to contain the privacy policy content, styled to match `main`.

The `goat-moon.svg` logo was removed from `website/` on the kmp branch. It will be restored from `main`.

## Phases

### Phase 1: Restore logo asset

- [x] Copy `goat-moon.svg` from `main:website/public/images/goat-moon.svg` into `website/images/goat-moon.svg`

### Phase 2: Update `website/index.html`

- [x] Apply dark theme: background `#020617`, foreground `#f1f5f9` (matching `main`'s `globals.css`)
- [x] Add Inter font via Google Fonts CDN (matching `main`'s layout)
- [x] Add header: `goat-moon.svg` logo + "Way of the Goat" h1 (matching `main`'s `layout.tsx`)
- [x] Port all privacy policy content from `main:website/app/page.tsx` into the `<body>`
- [x] Add a link to the VO2 Max Calculator at `./vo2/` (separate page, not inlined)

**Files to create/modify:**
- `website/images/goat-moon.svg` — restored from main
- `website/index.html` — full rewrite to match main's design + content

## Testing Strategy

- Open `website/index.html` locally in a browser
- Compare typography, colours, and layout against the live Vercel site
- Verify all privacy policy sections (1–7) are present and match exactly

## Dependencies

- `goat-moon.svg` from `main:website/public/images/goat-moon.svg`
- No build step — plain HTML/CSS

## Risks

- **Vercel deployment:** Merging kmp → main will break Vercel because Next.js is gone. A `vercel.json` is needed to tell Vercel to serve static files. This is out of scope for this feature but must be done before the kmp → main merge.
- **Google Fonts CDN:** Adds an external dependency; acceptable for a marketing/legal page.
