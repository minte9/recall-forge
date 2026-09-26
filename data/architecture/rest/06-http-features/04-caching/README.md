# API / REST / HTTP Features / Caching

## 1. HTTP Caching

GET responses can be cached by browsers, CDNs and proxies, cutting load
and latency. The server controls caching entirely through response
headers; the client needs no custom logic. Only safe methods are
cacheable in practice.

```
client --GET--> [browser cache] --> [CDN] --> API
               served here if still fresh
```

## 2. Cache-Control

`Cache-Control` sets the policy: `max-age` (seconds fresh), `public`
(shared caches may store), `private` (only the browser), `no-cache`
(store but revalidate each time), `no-store` (never store; for sensitive
data).

```java
return ResponseEntity.ok()
    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10))
                              .cachePublic())
    .body(book);
// -> Cache-Control: max-age=600, public
```

## 3. ETag and Conditional Requests

An `ETag` is a version fingerprint of a representation. The client sends
it back in `If-None-Match`; if unchanged, the server answers 304 Not
Modified with no body. `If-Match` on PUT prevents overwriting a newer
version (412).

```
GET /books/42          -> 200  ETag: "v7"
GET /books/42  If-None-Match: "v7"  -> 304 (no body)
PUT /books/42  If-Match: "v6"       -> 412 Precondition Failed
```
