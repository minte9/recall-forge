# DevOps / Git / Branching & Merging / Branching Strategies

## 1. Feature Branch Workflow

Every change gets its own short-lived branch off `main`, merged back via
pull request once reviewed. It isolates work in progress and gives code
review a clean diff to look at, at the cost of some merge overhead.

```bash
git switch -c feature/add-search
# ...commit work...
git push -u origin feature/add-search   # open a PR from here
```

## 2. Trunk-Based Development

Everyone commits small, frequent changes directly to `main` (or very
short-lived branches merged within a day), relying on feature flags
instead of long-lived branches to hide unfinished work in production.

```bash
if (featureFlags.isEnabled("new-search")) { ... }
```

## 3. Git Flow Overview

A heavier model with dedicated `develop`, `feature/*`, `release/*`, and
`hotfix/*` branches, `main` reserved for released code. Well suited to
projects with scheduled releases; often overkill for continuous deploy.

```bash
git switch -c release/2.4.0 develop
```
