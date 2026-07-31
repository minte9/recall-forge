## DevOps / Git / Fundamentals / Git Configuration

### 1. user.name / user.email

Every commit records an author name and email pulled from config -- Git
never asks at commit time. Setting these once globally (and overriding
per-repo when needed) avoids commits landing under the wrong identity.

~~~bash
git config --global user.name "Ana Pop"
git config --global user.email "ana@example.com"
~~~

### 2. Aliases

Aliases turn long or frequently-typed commands into short custom ones,
defined right in Git config. They're plain string substitutions, so an
alias can also chain flags you always use together.

~~~bash
git config --global alias.st "status -sb"
git config --global alias.lg "log --oneline --graph --all"
~~~

### 3. Core Editor & Line Endings

`core.editor` controls what opens for commit messages and interactive
rebases; `core.autocrlf` normalizes line endings so Windows and
Unix/macOS collaborators don't generate diffs full of CRLF/LF noise.

~~~bash
git config --global core.editor "vim"
git config --global core.autocrlf input   # Unix/macOS setting
~~~
