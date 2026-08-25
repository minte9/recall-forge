# DevOps / Git / Basic Commands / Staging & Committing

## 1. git add Patterns

`git add` supports single files, directories, globs, and `-p` for
staging individual hunks within a file. Staging only part of a change
lets you split unrelated edits into separate, reviewable commits.

```bash
git add src/           # everything under a directory
git add -p file.txt    # interactively choose hunks to stage
```

## 2. git commit Basics

A commit needs a message; `-m` supplies it inline, while omitting `-m`
opens `core.editor` for a longer message. `-a` skips explicit staging for
already-tracked files, but never picks up new untracked files.

```bash
git commit -m "Fix off-by-one in pagination"
git commit -am "Quick fix"   # stage tracked changes + commit
```

## 3. Amending Commits

`--amend` replaces the most recent commit with a new one -- fixing a typo
or adding a forgotten file without creating a separate "oops" commit.
Never amend a commit that's already been pushed and pulled by others.

```bash
git add forgotten-file.txt
git commit --amend --no-edit   # keep the original message
```
