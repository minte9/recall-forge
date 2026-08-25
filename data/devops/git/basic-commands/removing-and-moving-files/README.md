# DevOps / Git / Basic Commands / Removing & Moving Files

## 1. git rm

Deletes a file from both the working directory and the index in one
step, staging the removal for the next commit. Plain `rm` only deletes
from disk -- Git still sees the file as missing-but-tracked until you
stage that too.

```bash
git rm old-script.sh   # deletes + stages the removal
```

## 2. git mv

Renames or moves a file and stages the change as a single operation,
equivalent to `mv` followed by `git add` on both the old and new paths.

```bash
git mv old-name.txt new-name.txt
```

## 3. Rename Detection

Git doesn't store renames explicitly -- it detects them after the fact by
comparing content similarity between deleted and added files in a diff.
A rename plus heavy edits in the same commit can defeat that detection.

```bash
git log --follow new-name.txt   # trace history across a rename
```
