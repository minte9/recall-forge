# DevOps / Git / Remote Collaboration / Forking & Syncing

## 1. Fork vs Clone

A clone is a local copy of a repo you already have write access to. A
fork is a full server-side copy under your own account, used when you
don't have write access to the original -- you push to your fork, not
to the source repo.

```bash
git clone git@host:you/repo-fork.git
git remote add upstream git@host:original/repo.git
```

## 2. Syncing a Fork with Upstream

A fork doesn't automatically receive new commits made to the original
repo -- you fetch from `upstream` and merge (or rebase) them into your
local branch to catch up before starting new work.

```bash
git fetch upstream
git switch main
git merge upstream/main
```

## 3. Contributing via PR from a Fork

Work happens on a branch in your fork, pushed to your own `origin`; the
pull request is then opened against the original repo's `main`, with
your fork's branch as the source.

```bash
git push origin feature/typo-fix
gh pr create --repo original/repo --head you:feature/typo-fix
```
