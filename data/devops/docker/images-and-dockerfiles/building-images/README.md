# DevOps / Docker / Images & Dockerfiles / Building Images

## 1. docker build Basics

Reads a Dockerfile and a build context (the directory it can `COPY`
from) to produce an image. The context is sent to the daemon in full,
so a large, unfiltered directory slows every build down.

```bash
docker build -t myapp:latest .   # . is both Dockerfile location and context
```

## 2. Tagging Images

A tag is a human-readable label (like `myapp:1.2.0`) pointing at a
specific image. Untagged images default to `latest`, which is just a
convention, not automatically "the newest" -- it has to be tagged that
way explicitly.

```bash
docker build -t myapp:1.2.0 -t myapp:latest .
```

## 3. .dockerignore

Excludes files from the build context the same way `.gitignore` excludes
files from Git -- keeping `node_modules/`, `.git/`, and secrets out of
the image and speeding up context upload to the daemon.

```dockerignore
.git
node_modules
.env
```
