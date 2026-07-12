## JVM / Spring / Boot / Actuator

### 1. Enabling Actuator and /actuator/health

Adding `spring-boot-starter-actuator` exposes production-ready monitoring endpoints -- `/actuator/health` reports whether the app (and its dependencies like the database) is up, without writing any code. It's the standard way load balancers and orchestrators like Kubernetes check if an instance is ready to receive traffic.

~~~java
// build.gradle: implementation("org.springframework.boot:spring-boot-starter-actuator")

// GET /actuator/health -> { "status": "UP" }  -- works out of the box, zero code
~~~


### 2. Exposing and Securing Endpoints

Only `health` and `info` are exposed by default -- others like `/actuator/metrics` or `/actuator/env` must be opted into via `management.endpoints.web.exposure.include`. Actuator endpoints reveal internal details, so exposing more than needed (especially `env`, which can leak secrets) should always be paired with securing them.

~~~properties
# application.properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
~~~


### 3. Custom Health Indicators

A custom `HealthIndicator` bean plugs application-specific checks into `/actuator/health` -- e.g. verifying a downstream API is reachable. Spring Boot merges it with the built-in checks (database, disk space) into one aggregated status, so callers only need to poll a single endpoint.

~~~java
@Component
class DownstreamHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        boolean reachable = checkDownstreamApi(); // your own check
        return reachable
            ? Health.up().build()
            : Health.down().withDetail("api", "unreachable").build();
    }
}
~~~
