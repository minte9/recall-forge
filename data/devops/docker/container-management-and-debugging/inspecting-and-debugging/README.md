# DevOps / Docker / Container Management & Debugging / Inspecting & Debugging

## 1. docker inspect

Dumps a container's (or image's) full config as JSON -- IP address,
mounted volumes, env vars, restart policy -- everything `docker run` set
up, for when you need to check what a container was actually started with.

```bash
docker inspect my-container --format '{{.NetworkSettings.IPAddress}}'
```

## 2. docker stats

Streams live CPU, memory, and network I/O usage per container, similar
to `top` but scoped to containers -- useful for spotting which one is
eating resources without shelling into any of them.

```bash
docker stats   # live, updates in place
```

## 3. Attaching to a Running Container

`docker attach` connects your terminal directly to a container's main
process stdin/stdout/stderr, unlike `exec` which starts a separate
process -- detaching (Ctrl-P Ctrl-Q) leaves it running, closing the
terminal on a plain attach can stop it.

```bash
docker attach my-container
```
