## DevOps / Git / Branching & Merging / Merging

### 1. Fast-Forward vs Three-Way Merge

If the target branch hasn't diverged, Git just moves its pointer forward
(fast-forward, no merge commit). If both branches have new commits, Git
creates a three-way merge commit with two parents instead.

~~~bash
git merge feature/login          # fast-forward if possible
git merge --no-ff feature/login  # always create a merge commit
~~~

### 2. Merge Commits

A merge commit has two parent commits instead of one, recording that two
lines of history were joined. `--no-ff` forces one even when a
fast-forward is possible, preserving the fact that a feature branch
existed.

~~~bash
git log --merges --oneline   # list only merge commits
~~~

### 3. Resolving Merge Conflicts

A conflict happens when both branches changed the same lines. Git marks
the file with conflict markers; you edit to the desired result, remove
the markers, then stage and commit to finish the merge.

~~~text
<<<<<<< HEAD
current branch's version
=======
incoming branch's version
>>>>>>> feature/login
~~~
