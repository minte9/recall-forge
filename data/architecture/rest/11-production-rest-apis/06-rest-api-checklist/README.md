# API / REST / Production REST APIs / REST API Checklist

## 1. URLs and HTTP Methods

Plural nouns, no verbs, consistent casing, one nesting level. The method
matches the intent, GET is never used for side effects, POST creates
with 201 + Location, and DELETE returns 204. The same pattern applies to
every resource.

```
[ ] /books, /books/{id}, /books/{id}/reviews
[ ] GET safe, PUT/DELETE idempotent, POST -> 201 + Location
[ ] specific status codes (no 200 with an error body)
```

## 2. Validation, Security and Errors

Validate all input at the boundary, return one consistent error format
(ProblemDetail), and never leak internals. HTTPS only, default-deny
authorization, least-privilege scopes, and rate limits on
sensitive endpoints.

```
[ ] @Valid on request DTOs; business rules -> 409/422
[ ] global @RestControllerAdvice, RFC 9457 bodies
[ ] HTTPS, 401 vs 403 correct, anyRequest().authenticated()
[ ] CORS restricted, no secrets in logs or URLs
```

## 3. Testing, Documentation and Production Readiness

Slice and integration tests cover success, error and auth paths; an
OpenAPI spec is generated and checked in CI; and the service exposes
health, metrics and request ids, uses paging everywhere, and has a
versioning and deprecation policy.

```
[ ] @WebMvcTest + @SpringBootTest + Testcontainers
[ ] OpenAPI spec in CI, errors documented
[ ] health/readiness, metrics, correlation ids
[ ] pagination, caching headers, versioning plan
```
