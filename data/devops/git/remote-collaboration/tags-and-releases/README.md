# DevOps / Git / Remote Collaboration / Tags & Releases

## 1. Lightweight vs Annotated Tags

A lightweight tag is just a named pointer to a commit, like a branch
that never moves. An annotated tag is a full Git object with its own
message, author, and date -- the recommended kind for marking releases.

```bash
git tag v1.2.0                          # lightweight
git tag -a v1.2.0 -m "Release 1.2.0"    # annotated
```

## 2. Pushing Tags

Tags aren't pushed by a plain `git push` -- they need to be pushed
explicitly, either one at a time or all at once.

```bash
git push origin v1.2.0   # single tag
git push origin --tags    # all local tags
```

## 3. Semantic Versioning Convention

Release tags typically follow `MAJOR.MINOR.PATCH`: major for breaking
changes, minor for backward-compatible features, patch for bug fixes --
letting consumers judge upgrade risk from the version number alone.

```text
v2.1.0 -> v2.1.1  bug fix only, safe to upgrade
v2.1.0 -> v2.2.0  new feature, backward compatible
v2.1.0 -> v3.0.0  breaking change
```
