## DevOps / Git / History & Undoing Changes / Recovering Work

### 1. git reflog

Records every move HEAD has made locally -- commits, resets, rebases,
checkouts -- even ones no branch points to anymore. It's the safety net
that makes almost any local mistake recoverable, for a limited time.

~~~bash
git reflog   # e.g. shows HEAD@{2}: reset: moving to HEAD~1
~~~

### 2. Recovering Deleted Branches/Commits

Deleting a branch doesn't delete its commits immediately -- they're just
unreferenced. As long as they show up in the reflog (or haven't been
garbage-collected), a new branch can be created pointing right back at
them.

~~~bash
git reflog                       # find the lost commit's hash
git branch recovered a1b2c3d      # point a new branch at it
~~~
