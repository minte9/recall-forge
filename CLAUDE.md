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
- For creating/editing markdown topic content, read `.claude/content-authoring.md`. For direct DB import/edits or Docker mechanics, also read `.claude/content-authoring.local.md` (gitignored, operational).

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

## Git safety

- Before editing, run `git branch --show-current`.
- If the current branch is `master`, automatically create and switch to a new
  task branch (`task/<short-slug-of-the-request>`) before making any changes — don't ask first, 
  just do it and state the branch name. Never modify files directly on `master`.
- Implement the task entirely on the task branch.
- When the task is complete, do not commit, merge, push, or create a pull
  request automatically.
- Report the changed files, tests run, and current diff, then ask the user to
  choose one of these completion options:
  1. Create a pull request.
  2. Merge the task branch into `master` locally.
- If the user chooses a pull request:
  - commit the changes;
  - push only the task branch to `origin`;
  - create a pull request targeting `master`;
  - switch back to `master`;
  - report the pull request URL and stop;
  - do not merge the pull request or delete the task branch.
- If the user chooses a local merge:
  - commit the changes;
  - switch to `master`;
  - merge the task branch into `master` using `--no-ff`;
  - delete the local task branch;
  - do not push unless explicitly requested.
- If changes are requested during pull-request review, switch back to the same
  task branch, implement the changes, commit them, push them to the existing
  pull request, then switch back to `master`.
- Do not amend, rebase, cherry-pick, tag, force-push, pull, fetch, or modify
  remotes.
- Never push directly to `master`.
- Never run commands that print the Git remote URL, such as:
  `git remote -v`, `git config --get remote.origin.url`, or
  `git remote show origin`. The remote embeds a GitHub token in the URL, and
  printing it leaks the token into the conversation.
- Never call the GitHub API directly using `curl` or a token obtained from the
  remote URL, environment variables, or any other source. Use the `gh` CLI for
  GitHub operations. If `gh` is unavailable, stop and ask the user to perform
  the GitHub action manually.
- At completion, report changed files, tests run, the current diff, and, when
  applicable, the pull request URL.
- Don't offer any other Git suggestions after the report.

### Local merge (opt-in, must be explicitly requested)

If the user explicitly asks for a local merge instead of a PR for the
current task (e.g. "merge this locally", "just merge it into master and
drop the branch") — this does NOT apply by default, only when asked:
- Still wait for explicit approval before committing.
- Commit the changes on the task branch as usual.
- Switch to `master`, merge the task branch in (`--no-ff`), then delete
  the task branch.
- Do not push anything to `origin` and do not open a PR for this change.
- Report the files changed, tests run, and confirmation that `master`
  now has the merge — then stop.

Never choose this path on your own initiative — the default for every
task remains: task branch, PR, no local merge, no push to master.


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

- Never run commands that print the git remote URL (e.g. `git remote -v`,
  `git config --get remote.origin.url`, `git remote show origin`) — this
  remote embeds a GitHub token in the URL, and printing it leaks the token
  into the conversation.
- Never call the GitHub API directly (e.g. via `curl`) using a token pulled
  from the remote URL, environment variables, or any other source. Use the
  `gh` CLI for GitHub operations (PRs, issues, etc.), which manages its own
  auth separately. If `gh` is unavailable, stop and ask the user to perform
  the action themselves rather than falling back to a raw token.