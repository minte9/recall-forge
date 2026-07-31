## DevOps / Docker / Container Management & Debugging / Cleaning Up

### 1. docker system prune

Removes stopped containers, unused networks, dangling images, and
build cache in one command -- the standard fix for a host slowly
filling up with leftovers from old builds and runs.

~~~bash
docker system prune          # safe-ish default set
docker system prune -a --volumes  # aggressive: unused images + volumes too
~~~

### 2. Dangling Images & Volumes

A dangling image is an untagged leftover from a rebuild (its old tag
moved to the new image); a dangling volume is one no container
references anymore. Both linger on disk until explicitly cleaned up.

~~~bash
docker image prune     # dangling images only
docker volume prune     # unused volumes only
~~~

### 3. Disk Usage

Shows how much space images, containers, and volumes are consuming
before you decide what to prune, instead of guessing.

~~~bash
docker system df   # summary
docker system df -v  # per-item breakdown
~~~
