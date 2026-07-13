# Claude Code Guide

## Structure

The project uses two kinds of AI instructions.

### .claude/

Contains reusable project knowledge and workflows.

Examples:

- unit testing
- frontend work
- debugging
- content authoring

These documents are the source of truth.

---

### .agents/

Contains specialized Claude Code subagents.

A subagent should contain:

- role
- responsibilities
- decision-making
- quality standards

A subagent should NOT duplicate workflow instructions.

Instead, it should read the relevant guide from `.claude/`.

---

## Rule of thumb

Workflow → `.claude/`

Judgement → `.agents/`