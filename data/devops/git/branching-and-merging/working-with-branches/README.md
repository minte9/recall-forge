## DevOps / Git / Branching & Merging / Working with Branches

### 1. Creating & Switching Branches

A branch is just a movable pointer to a commit -- creating one is nearly
instant since no files are copied. `switch` (or the older `checkout`)
moves HEAD to point at a different branch, updating the working
directory to match.

~~~bash
git branch feature/login       # create, stay on current branch
git switch feature/login       # move HEAD to it
git switch -c feature/signup   # create + switch in one step
~~~

### 2. Detached HEAD

Checking out a specific commit instead of a branch puts you in "detached
HEAD" -- any new commits aren't attached to a branch name and can be lost
once you switch away, unless you create a branch to hold them first.

~~~bash
git checkout a1b2c3d          # detached HEAD at that commit
git switch -c rescue-branch   # save work made while detached
~~~

### 3. Deleting Branches

`-d` deletes a branch only if it's fully merged; `-D` force-deletes
regardless. The safe flag exists specifically to stop you from losing
commits that only exist on that branch.

~~~bash
git branch -d feature/login    # safe: refuses if unmerged
git branch -D feature/login    # force delete
~~~
