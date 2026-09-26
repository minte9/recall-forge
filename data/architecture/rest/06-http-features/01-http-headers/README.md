# API / REST / HTTP Features / HTTP Headers

## 1. Request and Response Headers

Headers are metadata about the message: auth, formats, caching,
tracing. Request headers describe the client's needs; response headers
describe what the server sent. In Spring, read them with `@RequestHeader`
and set them via `ResponseEntity`.

```java
@GetMapping("/books")
ResponseEntity<List<Book>> list(
        @RequestHeader("X-Request-Id") String reqId) {
    return ResponseEntity.ok().header("X-Request-Id", reqId)
                         .body(service.all());
}
```

## 2. Content-Type

`Content-Type` declares the format of *this* message's body. A client
sending JSON must set `application/json`, or Spring can't pick a
converter and answers 415 Unsupported Media Type.

```
POST /books
Content-Type: application/json      <- body is JSON
{"title":"Dune"}

missing/wrong Content-Type -> 415 Unsupported Media Type
```

## 3. Accept

`Accept` states which formats the client can handle *in the response*.
The server picks one it supports, or returns 406 Not Acceptable. It's
the input to content negotiation.

```
GET /books/42
Accept: application/json            -> 200, JSON body
Accept: application/pdf             -> 406 if not supported
```
