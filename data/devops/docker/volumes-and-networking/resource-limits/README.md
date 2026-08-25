# DevOps / Docker / Volumes & Networking / Resource Limits

## 1. Memory & CPU Limits (--memory / --cpus)

Without limits, a single container can consume all of the host's memory
or CPU, starving everything else running on the same machine. Docker
enforces limits via cgroups, the same kernel mechanism containers are
built on.

```bash
docker run --memory=512m --cpus=1.5 myapp
```

## 2. OOM Kills in Containers

If a container exceeds its memory limit, the kernel's OOM killer
terminates its main process -- the container exits, often with no
graceful shutdown, which is why memory limits need headroom above
typical usage, not just peak usage.

```bash
docker inspect my-container --format '{{.State.OOMKilled}}'
```
