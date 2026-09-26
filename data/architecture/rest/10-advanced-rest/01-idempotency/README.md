# API / REST / Advanced REST / Idempotency

## 1. Idempotent Operations

An operation is idempotent if doing it twice leaves the same state as
doing it once. GET, PUT and DELETE are idempotent by HTTP semantics; POST
is not. Idempotency is what makes an operation safe to retry.

```
DELETE /books/42  x3  -> book gone (same end state)
PUT /books/42 {...} x3 -> same book
POST /payments {...} x3 -> three payments!
```

## 2. Retry Problems

Networks fail after the server acted but before the client got the
response. The client can't tell "not done" from "done, reply lost", so it
retries, and a non-idempotent POST charges or orders twice.

```
client --POST /payments--> server charges card
client <--  (timeout)  --  response lost
client --POST /payments--> charged again
```

## 3. Idempotency Keys

The client generates a unique key per logical operation and sends it in a
header. The server stores key → result; a repeat with the same key
returns the stored result instead of re-executing. Keys expire after a
retention window.

```
POST /payments
Idempotency-Key: 5f1c9e2a-...     # same key on every retry
-> 1st: execute, store result under key, 201
-> retry: key seen, return stored 201 (no second charge)
```
