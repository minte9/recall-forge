# DevOps / Git / Fundamentals / Version Control Basics

## 1. What Git Tracks (Snapshots vs Deltas)

Git stores a full snapshot of every tracked file at each commit, not a diff
against the previous version. Unchanged files are stored once and reused by
reference, which is why branching and checking out old commits is fast --
Git isn't replaying a chain of patches to rebuild state.

```bash
git cat-file -p HEAD^{tree}   # inspect the snapshot a commit points to
```

## 2. The Three Trees (Working Dir / Staging / Repo)

Every Git command moves content between three areas: the working directory
(files you edit), the staging area/index (what will go into the next
commit), and the repository (committed history). `add` moves working ->
staging, `commit` moves staging -> repository.

```bash
git add file.txt      # working dir  -> staging
git commit -m "msg"   # staging      -> repository
```

## 3. Git Object Model (Blob / Tree / Commit)

Under the hood a commit points to a tree (a directory listing), which
points to blobs (file contents) and other trees. Each object is addressed
by the SHA-1 hash of its content, so identical content anywhere in history
is stored only once.

```bash
git log -1 --format="%H %T"   # commit hash and the tree it points to
```
