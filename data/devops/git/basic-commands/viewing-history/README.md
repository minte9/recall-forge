# DevOps / Git / Basic Commands / Viewing History

## 1. git log Basics

`git log` walks commit history from HEAD backwards, newest first, showing
author, date, and message by default. It's the starting point for
answering "what happened and when" on any branch.

```bash
git log            # full detail
git log -5         # last 5 commits only
```

## 2. Useful log Flags (--oneline, --graph)

`--oneline` compresses each commit to a single line; `--graph` draws the
branch/merge topology in ASCII. Combined, they turn a tangled merge
history into something scannable at a glance.

```bash
git log --oneline --graph --all --decorate
```

## 3. git blame

Shows, line by line, which commit and author last touched each line of a
file -- the fastest way to find out why a suspicious line exists before
assuming it's a bug.

```bash
git blame src/PricingService.java
git blame -L 40,60 file.js   # only lines 40-60
```
