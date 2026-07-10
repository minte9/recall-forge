# Frontend Guide

When working on frontend tasks:

- Do not modify production code unless explicitly approved.
- Understand the existing UI/state flow before adding new markup, state, or CSS.
- Keep changes scoped to the panel/component being worked on; don't restyle or refactor unrelated markup.
- Match existing conventions already used nearby (naming, structure, styling approach) rather than introducing new patterns.
- Prefer small, incremental changes over broad rewrites.
- After editing, verify manually in the browser — describe what was checked, since there is no frontend test suite.

Rules:
- Do not add new libraries or a build step without approval.
- Do not silently change backend contracts (API calls, payload shapes) while doing frontend-only work.
- If a change implies a backend change too, stop and confirm scope before touching backend code.
