## DevOps / Git / Basic Commands / Inspecting Changes

### 1. git status

Shows which files are staged, modified-but-unstaged, or untracked,
relative to the last commit. It's the command to run before every add
or commit -- the fastest way to avoid staging the wrong thing.

~~~bash
git status
git status -sb   # short, branch-aware format
~~~

### 2. git diff (Working vs Staged)

Plain `git diff` shows unstaged changes against the index; `git diff
--staged` shows staged changes against the last commit. Together they
cover every place a change can currently live before it's committed.

~~~bash
git diff            # working dir vs staging area
git diff --staged   # staging area vs last commit
~~~

### 3. git show

Displays the full commit message and diff for a single commit, or the
contents of a specific file at a specific revision -- useful for
answering "what did this commit actually change?" without a full log.

~~~bash
git show HEAD           # last commit's diff
git show HEAD~2:file.js # file.js as it was 2 commits ago
~~~
