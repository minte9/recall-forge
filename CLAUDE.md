# RecallForge Claude Instructions

General rules:
- Never modify production code without approval.
- Explain the plan before changing files.
- Prefer small, focused changes.

Task guides:
- For unit tests, read `.claude/unit-testing.md`.
- For integration tests, read `.claude/integration-testing.md`.
- For debugging, read `.claude/debugging.md`.
- For frontend work, read `.claude/frontend.md`.

Read only the guides relevant to the current task.

If no suitable guide exists,
ask whether a new guide should be created before inventing new conventions.

## Safety

Before editing:
- Explain your plan.
- List the files you intend to modify.
- Wait for approval.

After editing:
- Stop after running the relevant tests.
- Do not continue with additional refactorings unless requested.

## Security

Do not read or display secrets unless explicitly requested.

This includes:
- .env
- .env.*
- *.pem
- *.key
- id_rsa*
- credentials files
- API keys
- passwords
- tokens

If access to one of these files appears necessary, explain why and wait for approval.