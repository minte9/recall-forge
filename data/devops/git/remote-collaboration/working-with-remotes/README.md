## DevOps / Git / Remote Collaboration / Working with Remotes

### 1. Adding & Viewing Remotes

A remote is just a named URL pointing at another copy of the repo --
`origin` by convention for the one you cloned from. A repo can have
several remotes, e.g. `origin` and `upstream` on a fork.

~~~bash
git remote add upstream git@host:original/repo.git
git remote -v   # list remotes and their URLs
~~~

### 2. Fetch vs Pull

`git fetch` downloads new commits and updates remote-tracking branches
without touching your working directory. `git pull` does a fetch, then
immediately merges (or rebases) into your current branch.

~~~bash
git fetch origin        # see what's new, decide later
git pull origin main     # fetch + merge in one step
~~~

### 3. Tracking Branches

A local branch can track a remote branch, so plain `git push`/`git pull`
know where to send or fetch without naming the remote each time. `-u`
sets that link the first time you push a new branch.

~~~bash
git push -u origin feature/login   # sets up tracking
git push                            # now works with no arguments
~~~
