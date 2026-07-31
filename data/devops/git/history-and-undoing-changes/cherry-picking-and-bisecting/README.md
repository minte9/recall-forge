## DevOps / Git / History & Undoing Changes / Cherry-Picking & Bisecting

### 1. git cherry-pick

Applies the changes from a single commit on one branch onto another,
without merging the whole branch -- typical use case is porting one bug
fix from `main` onto a release branch.

~~~bash
git switch release/2.4
git cherry-pick a1b2c3d   # apply just that one commit here
~~~

### 2. git bisect for Finding Bugs

Binary-searches history between a known-good and known-bad commit,
asking you to test each candidate, to pinpoint the exact commit that
introduced a regression in O(log n) steps instead of a linear search.

~~~bash
git bisect start
git bisect bad                 # current commit is broken
git bisect good v1.0.0          # this old commit was fine
# Git checks out a midpoint; test it, then:
git bisect good   # or: git bisect bad
~~~
