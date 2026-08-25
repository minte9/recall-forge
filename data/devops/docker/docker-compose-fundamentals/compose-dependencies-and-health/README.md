# DevOps / Docker / Docker Compose Fundamentals / Compose Dependencies & Health

## 1. depends_on Basics

Controls the order services start in -- `depends_on: [db]` makes sure
`db`'s container is started before `web`'s. By itself it only waits for
"started," not "actually ready to accept connections."

```yaml
services:
  web:
    depends_on:
      - db
  db:
    image: postgres
```

## 2. Healthchecks in Compose

A `healthcheck` defines how Compose probes whether a service is truly
ready, not just running. Combined with `depends_on: condition:
service_healthy`, a dependent service waits for real readiness instead
of just container start order.

```yaml
services:
  db:
    image: postgres
    healthcheck:
      test: ["CMD", "pg_isready"]
      interval: 5s
  web:
    depends_on:
      db:
        condition: service_healthy
```
