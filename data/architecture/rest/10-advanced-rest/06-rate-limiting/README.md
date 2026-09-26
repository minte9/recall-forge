# API / REST / Advanced REST / Rate Limiting

## 1. Why Rate Limits Exist

Rate limits cap how many requests a client may make per time window.
They protect the API from abuse and runaway scripts, keep one tenant
from starving others, and slow brute-force attacks on login endpoints.
Limits are usually per API key, user or IP.

```
free plan:  100 requests / minute per API key
/login:     5 attempts / minute per IP
```

## 2. 429 Too Many Requests

When a client exceeds its limit, respond 429 and tell it when to retry
with `Retry-After` (seconds or a date). Well-behaved clients back off
instead of retrying immediately, which would only make things worse.

```
GET /books
-> 429 Too Many Requests
   Retry-After: 30
   {"code":"RATE_LIMITED","message":"Try again in 30s"}
```

## 3. Rate-Limit Headers

Expose the limit state on every response so clients can pace themselves
before hitting 429. The common convention is `RateLimit-Limit`,
`RateLimit-Remaining` and `RateLimit-Reset` (or the older `X-RateLimit-*`
variants).

```
200 OK
RateLimit-Limit: 100
RateLimit-Remaining: 7
RateLimit-Reset: 42        # seconds until the window resets
```
