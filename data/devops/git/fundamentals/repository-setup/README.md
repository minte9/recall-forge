## DevOps / Git / Fundamentals / Repository Setup

### 1. git init vs git clone

`git init` creates a brand-new, empty repository in the current directory --
useful for starting a project from scratch. `git clone` copies an existing
repository (full history included) from a remote URL and automatically
wires up `origin` as the tracked remote.

~~~bash
git init                       # new empty repo, no remote
git clone git@host:org/repo.git  # existing repo + history + origin
~~~

### 2. The .git Directory

Everything Git needs -- objects, refs, config, HEAD -- lives inside the
hidden `.git` folder at the repo root. Deleting it removes all history
while leaving your working files untouched; it's the entire "repository"
part of a working copy.

~~~bash
ls .git   # HEAD  config  objects/  refs/  hooks/  ...
~~~

### 3. Global vs Local Config

Config can be set per-repository (`.git/config`) or globally for the user
(`~/.gitconfig`). Local settings override global ones, which is how a work
repo can use a different commit email than personal projects without
touching the global default.

~~~bash
git config --global user.email "me@example.com"   # all repos
git config user.email "me@work.com"                # this repo only
~~~
