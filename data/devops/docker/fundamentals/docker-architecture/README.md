# DevOps / Docker / Fundamentals / Docker Architecture

## 1. Docker Engine (Daemon / Client)

The `docker` CLI is just a thin client; the actual work -- building
images, running containers -- happens in `dockerd`, a background daemon
it talks to over a socket or API. The client can even control a daemon
on a different machine.

```bash
docker version   # shows both Client and Server (daemon) versions
```

## 2. Registries Overview

A registry stores and distributes images by name and tag, the way a
package repository does for libraries. Docker Hub is the default public
registry; private registries (ECR, GCR, self-hosted) work the same way
with different auth.

```bash
docker pull nginx:1.27          # from Docker Hub by default
docker pull myregistry.io/nginx  # from a specific registry
```

## 3. Docker Desktop vs Engine

Docker Engine is the daemon + CLI; Docker Desktop is a GUI wrapper for
macOS/Windows that runs Engine inside a lightweight Linux VM, since
containers need a Linux kernel Windows and macOS don't natively provide.

```bash
docker context ls   # shows which engine (local, remote, WSL) is active
```
