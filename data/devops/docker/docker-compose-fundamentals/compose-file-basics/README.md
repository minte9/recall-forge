# DevOps / Docker / Docker Compose Fundamentals / Compose File Basics

## 1. docker-compose.yml Structure

A single YAML file describes every container an app needs -- images to
build, ports, volumes -- so a whole multi-container setup can be started
with one command instead of a separate `docker run` per service.

```yaml
services:
  web:
    image: nginx
    ports:
      - "8080:80"
```

## 2. Services / Volumes / Networks Keys

`services` defines the containers themselves; top-level `volumes` and
`networks` declare named resources services can reference by name,
instead of every service inventing its own.

```yaml
services:
  db:
    image: postgres
    volumes:
      - pgdata:/var/lib/postgresql/data
volumes:
  pgdata:
```

## 3. Compose v2 CLI

Compose is now a Docker CLI plugin (`docker compose`, no hyphen), not a
separate `docker-compose` binary -- functionally the same commands, just
integrated into the main `docker` command.

```bash
docker compose up   # current (v2)
docker-compose up    # legacy standalone binary, still works on many setups
```
