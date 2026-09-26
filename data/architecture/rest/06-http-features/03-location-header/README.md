# API / REST / HTTP Features / Location Header

## 1. 201 Created

When POST creates a resource, respond 201 Created, not 200. It tells the
client (and any tooling) that something new now exists, and pairs with
the `Location` header pointing at it.

```
POST /books {"title":"Dune"}
-> HTTP/1.1 201 Created
   Location: /books/43
```

## 2. The Location Header

`Location` holds the URI of the newly created resource, so the client
doesn't have to construct URLs itself. Build it from the current request
to keep host, port and context path correct behind proxies.

```java
URI location = ServletUriComponentsBuilder.fromCurrentRequest()
    .path("/{id}").buildAndExpand(book.getId()).toUri();
return ResponseEntity.created(location).body(BookResponse.from(book));
```

## 3. Returning the New Resource

Returning the created representation in the body saves the client an
extra GET and exposes server-generated fields (id, timestamps). Returning
only `Location` with an empty body is also valid, so pick one and be
consistent.

```
201 Created
Location: /books/43
{"id":43,"title":"Dune","createdAt":"2026-09-26T10:00:00Z"}
```
