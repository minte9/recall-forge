# API / REST / Basic / HTTP Methods

## 1. GET, POST, PUT, PATCH, DELETE

The method expresses the intent: GET reads, POST creates (or triggers an
action), PUT replaces a resource, PATCH changes part of it, DELETE removes
it. The URL names the resource; the method says what to do with it.

```
GET    /books        # list
POST   /books        # create
PUT    /books/42     # replace book 42 entirely
PATCH  /books/42     # update some fields
DELETE /books/42     # remove
```

## 2. Safe and Idempotent Methods

**Safe** methods (GET, HEAD, OPTIONS) don't change server state.
**Idempotent** methods (safe ones plus PUT and DELETE) have the same effect
whether sent once or many times, so clients and proxies can retry them.
POST, and usually PATCH, are neither.

```
PUT /books/42 {"title":"Dune"}  x3  -> still one book titled Dune
POST /books   {"title":"Dune"}  x3  -> three new books!
```

## 3. Choosing the Correct Method

Ask: does it change state? If not, GET. Does the client know the target URL
and send the full resource? PUT. Partial change? PATCH. Server assigns the
id or it's a non-idempotent action? POST. Avoid GET with side effects,
since crawlers and prefetchers will trigger them.

```
GET  /books/42/delete   # wrong: GET must not change state
DELETE /books/42        # right
POST /orders/7/cancel   # ok: action that doesn't map to CRUD
```
