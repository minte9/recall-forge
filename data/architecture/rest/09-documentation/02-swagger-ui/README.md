# API / REST / Documentation / Swagger UI

## 1. Springdoc OpenAPI

Adding `springdoc-openapi-starter-webmvc-ui` scans your controllers,
DTOs and validation annotations and generates the OpenAPI spec at
runtime, with no hand-written YAML. Annotations only refine what it can't
infer (see also Microservices / OpenAPI).

```
dependency: org.springdoc:springdoc-openapi-starter-webmvc-ui
spec:       GET /v3/api-docs        (JSON)
            GET /v3/api-docs.yaml   (YAML)
```

## 2. Swagger UI

Swagger UI renders the spec as an interactive HTML page at
`/swagger-ui.html`, grouping operations by controller and showing
schemas, example payloads and response codes. Secure or disable it in
production if the API isn't public.

```yaml
springdoc:
  swagger-ui.path: /docs
  api-docs.enabled: true       # set false in prod if private
```

## 3. Interactive API Testing

"Try it out" sends real requests from the browser, and the Authorize
button adds tokens when the spec declares a security scheme. It's great
for exploration and demos, but not a replacement for automated tests.

```java
@SecurityScheme(name = "bearer", type = SecuritySchemeType.HTTP,
                scheme = "bearer", bearerFormat = "JWT")
@SecurityRequirement(name = "bearer")   // enables Authorize button
@RestController class BookController { ... }
```
