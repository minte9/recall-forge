# DevOps / Git / History & Undoing Changes / Stashing

## 1. git stash Basics

Temporarily shelves uncommitted changes (staged and unstaged) and
restores a clean working directory, without creating a commit -- handy
for switching branches mid-task without losing in-progress work.

```bash
git stash            # shelve current changes
git stash list        # see what's stashed
```

## 2. Applying & Dropping Stashes

`stash pop` reapplies the most recent stash and removes it from the
stash list; `stash apply` reapplies without removing it, useful for
trying the same stash on more than one branch.

```bash
git stash pop           # apply + remove
git stash apply stash@{1}  # apply a specific older stash, keep it
```

## 3. Stashing Untracked Files

By default, `git stash` ignores untracked (new) files. `-u` includes
them, which matters when the in-progress work includes newly created
files, not just edits to existing ones.

```bash
git stash -u   # include untracked files
```
