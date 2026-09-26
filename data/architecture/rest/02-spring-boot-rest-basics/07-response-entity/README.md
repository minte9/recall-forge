# API / REST / Spring Boot REST Basics / ResponseEntity

## 1. ResponseEntity<T>

`ResponseEntity<T>` wraps the body together with the status code and
headers, giving a handler full control of the response. Returning a plain
object always means 200 (or `@ResponseStatus`); `ResponseEntity` lets you
decide per request.

```java
@GetMapping("/books/{id}")
ResponseEntity<Book> get(@PathVariable Long id) {
    return service.find(id)
        .map(ResponseEntity::ok)                  // 200 + body
        .orElse(ResponseEntity.notFound().build()); // 404, no body
}
```

## 2. Status Codes and Headers

The static builders (`ok()`, `created(uri)`, `noContent()`,
`status(...)`) set the status, and `.header(...)` adds headers.
`created(uri)` sets both 201 and the `Location` header in one call.

```java
URI uri = URI.create("/books/" + book.id());
return ResponseEntity.created(uri)          // 201 + Location
    .header("X-Request-Id", requestId)
    .body(book);
```

## 3. Controlling the Response

Use `ResponseEntity` when the outcome varies (found/not found, created
vs. updated) or headers matter (ETag, caching). For simple fixed
outcomes, returning the object plus `@ResponseStatus` is shorter and
reads better.

```java
@PutMapping("/books/{id}")
ResponseEntity<Book> upsert(@PathVariable Long id,
                            @RequestBody BookDto dto) {
    boolean created = !service.exists(id);
    Book b = service.save(id, dto);
    return created ? ResponseEntity.status(201).body(b)
                   : ResponseEntity.ok(b);
}
```
