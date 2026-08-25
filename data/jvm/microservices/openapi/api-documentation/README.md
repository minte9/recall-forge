# JVM / Microservices / OpenAPI / API Documentation

## 1. SpringDoc Dependency & Swagger UI

Adding `springdoc-openapi-starter` to a service gets you a live `/v3/api-docs` spec and a browsable `/swagger-ui/index.html` for free, generated from the controller's existing annotations -- no separate spec file to keep in sync by hand.

```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'
```

## 2. YAML-based Service Configuration

Services tend to move from `.properties` to `.yml` once config grows nested (server port, app name, downstream hosts) -- YAML's indentation makes the grouping visually obvious in a way flat `key.path=value` lines don't.

```yaml
server:
  port: 7000
spring:
  application:
    name: product-composite-service
```
