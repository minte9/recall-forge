# API / REST / Basic / HTTP Status Codes

## 1. The Five Classes

The first digit tells the client what happened. 2xx means success, 3xx
means go elsewhere (redirect/not modified), 4xx means the client made a
mistake, and 5xx means the server failed. Clients can branch on the class
even for codes they don't specifically know.

```
2xx  success         200, 201, 204
3xx  redirection     301, 304
4xx  client error    400, 401, 403, 404, 409
5xx  server error    500, 502, 503
```

## 2. Common REST Status Codes

A handful cover most APIs: 200 OK, 201 Created (with Location), 204 No
Content, 400 Bad Request (invalid input), 401 Unauthorized (not
authenticated), 403 Forbidden (not allowed), 404 Not Found, 409 Conflict
and 500 Internal Server Error.

```
401 -> "who are you?"      (missing/invalid credentials)
403 -> "I know you, but no" (authenticated, not permitted)
409 -> state conflict, e.g. duplicate email or stale version
```

## 3. Choosing the Correct Status

Pick the most specific code that matches the outcome, and never return 200
with an error in the body; clients, proxies and monitoring rely on the
status. 4xx says "don't retry the same request", 5xx says "maybe retry
later".

```
POST /books   -> 201 Created   + Location: /books/43
DELETE /books/42 -> 204 No Content
GET /books/999   -> 404 Not Found  (not 200 + {"error":...})
```
