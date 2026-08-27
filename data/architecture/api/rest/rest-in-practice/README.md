# Architecture / API / REST / REST in Practice

## 1. Idempotency & Safe Methods

A safe method (GET, HEAD) changes nothing; an idempotent one (GET, PUT, DELETE) has the same effect whether called once or ten times. This lets clients and proxies retry after a timeout without fear. POST is neither, so for money-moving calls add an `Idempotency-Key` header the server dedupes on.

```
POST /payments
Idempotency-Key: 9f1c-4b...   # retry with same key -> same result
```

## 2. Pagination, Filtering & ETags

Never return an unbounded collection: page it with `?limit=&cursor=` (cursor beats offset under writes) and expose filters as query params. An `ETag` on responses lets the client send `If-None-Match` and get a cheap 304 when nothing changed, saving bandwidth and rendering work.

```
GET /orders?status=OPEN&limit=50&cursor=eyJpZCI6MTIz
If-None-Match: "a1b2c3"        # -> 304 Not Modified if unchanged
```

## 3. Problem Details Errors

RFC 9457 (`application/problem+json`) standardises error bodies around `type`, `title`, `status`, `detail`, and `instance`. A consistent shape lets clients handle failures generically instead of parsing ad-hoc strings, and `type` is a stable URI clients can branch on.

```json
{
  "type": "https://api.example.com/errors/out-of-stock",
  "title": "Out of stock", "status": 409, "detail": "SKU 12 unavailable"
}
```
