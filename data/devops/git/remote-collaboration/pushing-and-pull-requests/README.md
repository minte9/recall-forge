# DevOps / Git / Remote Collaboration / Pushing & Pull Requests

## 1. git push Basics

Uploads local commits on the current branch to the matching branch on a
remote. If the remote branch doesn't exist yet, `-u` both creates it and
sets up tracking for future pushes.

```bash
git push origin feature/login
git push -u origin feature/login   # first push of a new branch
```

## 2. Force-Push Risks

A regular push is rejected if it would drop commits the remote has that
you don't. Force-pushing overrides that check -- necessary after a
rebase, but capable of silently erasing a teammate's work if used on a
shared branch.

```bash
git push --force-with-lease   # fails if remote has commits you haven't seen
git push --force               # skips that safety check entirely
```

## 3. Pull Request Workflow

A pull request proposes merging one branch into another and gives
reviewers a diff to comment on before it lands. The branch keeps
updating with new pushes until it's approved and merged (or rebased) in.

```bash
git push -u origin feature/login
gh pr create --base main --head feature/login
```
