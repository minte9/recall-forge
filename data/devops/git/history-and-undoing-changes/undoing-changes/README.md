# DevOps / Git / History & Undoing Changes / Undoing Changes

## 1. checkout vs restore

`git restore` was split out of the overloaded `checkout` to make intent
explicit: restoring a file from the index or a commit, versus switching
branches, are now separate commands with separate names.

```bash
git restore file.txt              # discard working-dir changes
git restore --staged file.txt     # unstage, keep the edits
```

## 2. git reset (Soft / Mixed / Hard)

`reset` moves the current branch pointer to another commit. `--soft`
keeps changes staged, `--mixed` (default) unstages them but keeps them
in the working directory, `--hard` discards them entirely.

```bash
git reset --soft HEAD~1   # undo commit, keep changes staged
git reset --hard HEAD~1   # undo commit and discard changes
```

## 3. git revert

Creates a new commit that undoes a previous one, instead of rewriting
history. Safe to use on commits already pushed and shared, unlike
`reset --hard`, which only rewrites local/unshared history safely.

```bash
git revert a1b2c3d   # new commit that undoes a1b2c3d
```
