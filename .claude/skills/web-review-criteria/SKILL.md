---
name: web-review-criteria
description: Checklist for reviewing static HTML/CSS/JS and WASM loading code. Used by addy-osmani (reviewer).
user-invocable: false
disable-model-invocation: true
---

# Web Review Criteria

Checklist for reviewing static HTML/CSS/JavaScript and Rust/WASM integration in the Way of the Goat website. Used by `addy-osmani` (reviewer).

---

## 1. HTML Semantics & Structure

- [ ] `<title>` and `<meta name="description">` present on every page
- [ ] Viewport meta tag present: `<meta name="viewport" content="width=device-width, initial-scale=1">`
- [ ] Landmark elements used correctly: `<main>`, `<nav>`, `<header>`, `<footer>`, `<section>`, `<article>`
- [ ] Heading hierarchy is logical — no skipped levels (e.g. `h1` → `h3` without `h2`)
- [ ] Interactive elements use `<button>` (for actions) or `<a href>` (for navigation) — not `<div>`/`<span>` with click handlers
- [ ] `<form>` controls have associated `<label>` elements (via `for`/`id` pairing or wrapping `<label>`)
- [ ] Images have `alt` attributes — descriptive for meaningful images, empty (`alt=""`) for decorative
- [ ] No deprecated elements (`<center>`, `<font>`, `<b>` used for styling rather than semantics)

---

## 2. Accessibility

- [ ] ARIA attributes used only when native HTML semantics are insufficient
- [ ] No ARIA attributes that contradict native semantics (e.g. `role="button"` on a `<button>`)
- [ ] All interactive elements reachable via keyboard (`Tab` order is logical; no keyboard traps)
- [ ] Visible focus styles present — `outline: none` without a replacement style is a failure
- [ ] Colour contrast: text/background combinations pass WCAG AA (4.5:1 for normal text, 3:1 for large)
- [ ] `prefers-reduced-motion` media query wraps any CSS/JS animations
- [ ] Error messages in forms are associated with the relevant input (via `aria-describedby` or proximity)

---

## 3. JavaScript Quality

> **Applies when JS or WASM files are present in the changeset.** Skip this section if no JavaScript is present.

- [ ] All variables declared with `const` or `let` — no bare global assignments
- [ ] WASM module loaded asynchronously (`async`/`await` or `.then()`)
- [ ] WASM load failure handled and surfaced to the user — not silently swallowed
- [ ] No `eval()` or `new Function(string)` usage
- [ ] No `innerHTML` assignment with unsanitised external data (user input, URL parameters, API responses)
- [ ] DOM manipulation uses safe APIs: `textContent`, `createElement`, `setAttribute`
- [ ] Event listeners registered with `addEventListener` — no inline `onclick="..."` handlers
- [ ] Long-running operations do not block the main thread (use `setTimeout` / `requestAnimationFrame` to yield where appropriate)
- [ ] No `console.log` left in production code paths

---

## 4. CSS Quality

- [ ] No `!important` overrides used to work around specificity issues
- [ ] No obviously dead rules (selectors matching no elements in the HTML)
- [ ] Responsive design: layout tested at mobile (360px), tablet (768px), and desktop (1280px) breakpoints
- [ ] No magic `z-index` values (e.g. `z-index: 9999`) without a comment explaining the stacking context
- [ ] CSS custom properties (`--var`) used for repeated values (colours, spacing scales)
- [ ] No vendor-prefixed properties that are now universally supported without a prefix

---

## 5. Performance

> **WASM items apply only when a WASM module is present in the changeset.** The font, image, and script items apply to all pages.

- [ ] WASM fetched and instantiated with `WebAssembly.instantiateStreaming()` — not two-step fetch + `WebAssembly.instantiate(arrayBuffer)`
- [ ] Fonts loaded with `font-display: swap` or `font-display: optional` — not blocking render
- [ ] No synchronous XHR (`XMLHttpRequest` with `async: false`)
- [ ] Images use modern formats (WebP/AVIF) where possible; SVGs used for icons and illustrations
- [ ] No unused CSS or JS bundles loaded on page load
- [ ] `<link rel="preload">` used for critical assets (hero font, WASM binary) where appropriate
- [ ] `<script>` tags use `defer` or `async` unless load order dependency requires otherwise

---

## 6. Security

- [ ] No sensitive data (API keys, credentials, internal URLs) embedded in client-side JS or HTML
- [ ] Content Security Policy (CSP) defined in `vercel.json` or equivalent — flag if absent
- [ ] WASM binary loaded from same origin or an explicitly trusted source
- [ ] No mixed content (HTTP resources on an HTTPS page)
- [ ] Form submissions use HTTPS endpoints only
- [ ] No `target="_blank"` links without `rel="noopener noreferrer"`

---

## 7. UX States

> **Applies when WASM or interactive JS components are present.** Skip this section for static pages with no JavaScript.

- [ ] Loading state displayed while WASM initialises (spinner, skeleton, or "Loading…" text)
- [ ] Error state displayed if WASM fails to load or a computation throws — message is user-friendly, not a raw stack trace
- [ ] Empty/default state meaningful — the page is usable and informative before the user takes any action
- [ ] Submit/calculate button disabled (or shows progress) while a computation is running — prevents double-submit
- [ ] Results area is cleared or reset when the user changes inputs, so stale results are not shown alongside new ones
