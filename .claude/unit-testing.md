# RecallForge Claude Instructions

## Unit testing workflow

When adding unit tests:

- Do not modify production code unless explicitly approved.
- Do not create log files or report files.
- Prefer behavior tests over implementation-detail tests.
- Add one focused test file or one focused test group at a time.
- Run only the relevant Gradle test, not the full suite unless asked.
- Use offline Gradle mode when possible:
  `./gradlew test --offline`
- After finishing, report only in the terminal:
  - what was tested
  - files changed
  - command run
  - result
  - next recommended target