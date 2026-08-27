# Architecture / API / REST / REST Maturity & HATEOAS

## 1. Richardson Maturity Model

The model grades an API in four levels: level 0 tunnels everything through one URI and POST; level 1 introduces separate resource URIs; level 2 uses HTTP verbs and status codes correctly; level 3 adds hypermedia. Most production "REST" APIs sit at level 2, a pragmatic and widely accepted stopping point.

```
# Level 0: RPC over HTTP
POST /api  {"op": "getOrder", "id": 42}

# Level 2: resource + verb
GET /orders/42
```

## 2. Hypermedia & HATEOAS

HATEOAS means responses include links to the actions available next, so the client follows links instead of hardcoding URL templates. The server can then move endpoints or gate transitions -- no "cancel" link once shipped -- without a client release. The cost is heavier payloads and tooling few clients actually exploit.

```json
{
  "id": 42, "status": "PENDING",
  "_links": { "cancel": { "href": "/orders/42/cancel" } }
}
```

## 3. Content Negotiation

One resource can have several representations; the client asks for what it wants with `Accept` and describes what it sends with `Content-Type`. The server picks the best match or returns 406. The same mechanism carries language via `Accept-Language` and, through a versioned media type, the API version.

```
GET /orders/42
Accept: application/json        # or text/csv, application/xml
```
