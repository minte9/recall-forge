# API / REST / Spring Boot / Path Variables

## 1. @PathVariable

`@PathVariable` binds a `{placeholder}` in the URL to a method parameter,
converting the text to the parameter type. Path variables identify a
specific resource, so they are required by default.

```java
@GetMapping("/books/{id}")
Book get(@PathVariable Long id) {   // "/books/42" -> id = 42
    return service.get(id);
}
```

## 2. Conversion and Mismatches

Spring converts the segment to `Long`, `UUID`, enums and so on. If
conversion fails (e.g. `/books/abc` for a `Long`), the request is rejected
with 400 Bad Request before your code runs, which is a free validation layer.

```java
@GetMapping("/books/{id}")
Book get(@PathVariable UUID id) { ... }
// GET /books/not-a-uuid -> 400, handler never called
```

## 3. Multiple Path Variables

Nested resources use several placeholders. Names must match the parameter
names (or be given explicitly), and the hierarchy in the URL mirrors
ownership: the review belongs to the book.

```java
@GetMapping("/books/{bookId}/reviews/{reviewId}")
Review review(@PathVariable Long bookId,
              @PathVariable("reviewId") Long id) {
    return service.review(bookId, id);
}
```
