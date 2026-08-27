# Architecture / API / REST / REST Fundamentals

## 1. Resources & URIs

REST models a system as resources -- nouns like an order or a user -- each addressable by a stable URI. A URI names one thing; the same URI keeps pointing at that resource even as its representation changes. Collections (`/orders`) and items (`/orders/42`) nest to express containment, never actions.

```
GET /orders/42          # one resource
GET /orders/42/lines    # sub-collection, still nouns only
```

## 2. HTTP Verbs & Status Codes

The verb carries the intent so the URI stays a pure noun: GET reads, POST creates, PUT replaces, PATCH partially updates, DELETE removes. Status codes tell the client what happened without parsing the body: 2xx success, 3xx redirect, 4xx client mistake, 5xx server fault. Choosing the right code is part of the contract.

```
POST /orders     -> 201 Created + Location: /orders/42
GET  /orders/999 -> 404 Not Found
PUT  /orders/42  -> 200 OK (or 204 No Content)
```

## 3. Statelessness

Each request carries everything the server needs -- credentials, parameters, body -- and the server keeps no client session between calls. Any node can then handle any request, so you scale by adding servers and a crashed node loses nothing. State that must persist goes in a database keyed by the request, not in server memory.

```
# Every call re-sends the token; no server-side session to look up
GET /cart
Authorization: Bearer eyJhbGci...
```
