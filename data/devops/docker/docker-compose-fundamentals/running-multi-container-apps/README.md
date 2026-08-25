# DevOps / Docker / Docker Compose Fundamentals / Running Multi-Container Apps

## 1. up / down / logs

`up` creates and starts every service defined in the file; `down` stops
and removes them (and their network) in one step; `logs` streams output
from all services at once, prefixed by service name.

```bash
docker compose up -d     # start everything, detached
docker compose logs -f    # follow all services' output
docker compose down        # stop + remove containers and network
```

## 2. Scaling Services

A service can run multiple replicas of the same image at once, useful
for load-testing or horizontally scaling a stateless service locally
before it goes anywhere near production orchestration.

```bash
docker compose up -d --scale web=3
```

## 3. .env Files in Compose

Compose automatically loads a `.env` file in the project directory and
substitutes `${VAR}` references in the compose file -- keeping
environment-specific values out of the YAML itself.

```yaml
services:
  web:
    image: myapp:${APP_VERSION}
```
