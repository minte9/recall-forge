# API / REST / API Design / PUT vs PATCH

## 1. Full vs Partial Update

PUT sends the complete new state and replaces the resource; anything
omitted is lost. PATCH sends only the changes. Sending a partial body with
PUT is a classic bug that silently wipes fields.

```
current: {"title":"Dune","year":1965}
PUT   {"title":"Dune II"}  -> {"title":"Dune II","year":null}
PATCH {"title":"Dune II"}  -> {"title":"Dune II","year":1965}
```

## 2. Idempotency

PUT is idempotent by definition: applying the same full state twice
changes nothing. PATCH may not be: "set title" is idempotent, but
"increment stock" or "append tag" is not, so retrying a failed PATCH can
double-apply.

```
PUT   /books/42 {"stock": 5}      x2 -> stock = 5
PATCH /books/42 {"op":"inc","n":1} x2 -> stock + 2
```

## 3. When to Use Which

Use PUT when clients naturally hold the whole resource (edit forms) or
when the client chooses the URL (upsert). Use PATCH for small, targeted
changes on large resources or for concurrent editors touching different
fields.

```
PUT   /users/7/avatar   # client replaces the whole thing
PATCH /users/7 {"email":"new@x.io"}   # touch one field
```
