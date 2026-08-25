# DevOps / Git / Branching & Merging / Rebasing

## 1. Rebase vs Merge

Merge combines two histories with a new commit that has two parents.
Rebase instead replays your branch's commits one by one on top of
another branch's tip, producing a straight, linear history with no
merge commit at all.

```bash
git rebase main   # replay current branch's commits onto main
```

## 2. Interactive Rebase

`rebase -i` opens a list of commits you can reorder, squash, reword, or
drop before they're replayed -- the standard way to clean up a messy
feature branch before opening a pull request.

```bash
git rebase -i HEAD~4   # edit the last 4 commits
```

## 3. When Not to Rebase

Rebasing rewrites commit hashes. Doing it on commits already pushed and
pulled by someone else forces everyone downstream to reconcile diverged
history -- only rebase branches that are still private to you.

```bash
git rebase main            # fine, on your own unpushed branch
git push --force-with-lease  # only after rebasing a branch nobody else uses
```
