# Debugging Guide

When debugging a bug or suspicious behavior:

1. Understand the symptom first.
   - Ask for reproduction steps if unclear.
   - Identify expected behavior vs actual behavior.
   - Do not edit files yet.

2. Inspect relevant code paths.
   - Start from the user-facing symptom.
   - Trace controller/service/repository/helper flow.
   - Look for recent tests, logs, TODOs, edge cases, and null handling.

3. Reproduce before fixing.
   - Prefer adding or running an existing failing test.
   - If a UI/manual issue, identify the smallest reproducible scenario.
   - Do not guess.

4. Report findings before changing code.
   Include:
   - likely root cause
   - files inspected
   - confidence level
   - proposed fix
   - suggested test coverage

5. Only after approval:
   - make the smallest production change possible
   - add or update a regression test
   - run the relevant Gradle test
   - summarize the result in the terminal

Rules:
- Do not rewrite unrelated code.
- Do not add dependencies without approval.
- Do not silence errors unless explicitly approved.
- Do not remove tests to make the build pass.
- If the root cause is uncertain, say so.