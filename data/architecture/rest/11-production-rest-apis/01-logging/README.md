# API / REST / Production REST APIs / Logging

## 1. Request Logging

Log one line per request with method, path, status, duration and caller
id. This answers "what happened?" during incidents. Use structured
(JSON) logs so fields are searchable, and log at the edge (filter or
access log), not in every controller.

```json
{"method":"POST","path":"/books","status":201,
 "durationMs":38,"user":"alice","requestId":"a1b2c3"}
```

## 2. Correlation / Request IDs

Assign each request an id (or accept the caller's `X-Request-Id`), put it
in the MDC so every log line includes it, return it in the response, and
propagate it to downstream calls. One id then ties together a request's
logs across services.

```java
String id = Optional.ofNullable(req.getHeader("X-Request-Id"))
                    .orElse(UUID.randomUUID().toString());
MDC.put("requestId", id);                 // added to every log line
res.setHeader("X-Request-Id", id);
```

## 3. Avoiding Sensitive-Data Logging

Never log passwords, tokens, `Authorization` headers, full card numbers
or personal data. Log ids instead of payloads, and mask anything
sensitive. Logs are widely accessible and long-lived, making them a
common leak vector.

```java
log.info("login ok user={}", userId);          // good
log.info("login body={}", requestBody);        // bad: has password
log.debug("auth header={}", authHeader);       // bad: token leak
```
