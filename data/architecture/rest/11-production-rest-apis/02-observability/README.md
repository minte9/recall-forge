# API / REST / Production REST APIs / Observability

## 1. Spring Boot Actuator

Actuator adds production endpoints under `/actuator`: health, metrics,
info, loggers and more. Expose only what you need over HTTP and secure
the rest, since some endpoints reveal configuration (see Spring / Boot /
Actuator).

```yaml
management.endpoints.web.exposure.include: health,info,prometheus
```

## 2. Health Endpoints

`/actuator/health` aggregates checks (database, disk, custom
indicators) into UP/DOWN. Kubernetes uses the liveness group ("restart
me?") and readiness group ("send me traffic?") separately; don't make
liveness depend on the database.

```
GET /actuator/health/liveness   -> {"status":"UP"}
GET /actuator/health/readiness  -> {"status":"DOWN"}  # DB unreachable
```

## 3. Metrics

Micrometer records `http.server.requests` automatically: count, latency
and errors per URI and status. Scrape them with Prometheus and alert on
error rate and p95/p99 latency, not on averages.

```
http_server_requests_seconds_count{uri="/books/{id}",status="200"} 1523
http_server_requests_seconds_count{uri="/books/{id}",status="500"} 4
# alert: 5xx rate > 1% or p99 > 500ms
```
