## DevOps / Git / Fundamentals / Ignoring Files

### 1. .gitignore Patterns

`.gitignore` lists patterns for files Git should never offer to stage --
build output, dependency folders, local env files. Patterns support
globs and negation, and are matched relative to the directory the
`.gitignore` file lives in.

~~~gitignore
*.log
node_modules/
!important.log   # negation: un-ignore one specific file
~~~

### 2. Tracked vs Untracked

`.gitignore` only affects files Git has never seen. A file that was
committed before being added to `.gitignore` stays tracked and keeps
showing up in `git status` -- ignoring is not the same as removing.

~~~bash
git status --ignored   # see what's being skipped, and what isn't
~~~

### 3. Untracking Already-Committed Files

To stop tracking a file without deleting it from disk, remove it from the
index only, then add it to `.gitignore` so it doesn't get re-staged by
accident.

~~~bash
git rm --cached .env
echo ".env" >> .gitignore
~~~
