# API / REST / Spring Boot / PUT PATCH DELETE

## 1. @PutMapping

`@PutMapping` replaces the resource at a known URL with the full
representation in the body. Fields the client omits are overwritten
(typically with null), which is what makes PUT idempotent.

```java
@PutMapping("/books/{id}")
Book replace(@PathVariable Long id, @RequestBody BookDto body) {
    return service.replace(id, body);   // every field overwritten
}
```

## 2. @PatchMapping

`@PatchMapping` applies a partial update: only fields present in the body
change. A common approach is a DTO with nullable fields where `null`
means "leave unchanged".

```java
@PatchMapping("/books/{id}")
Book patch(@PathVariable Long id, @RequestBody BookPatch body) {
    // body.title() == null -> keep current title
    return service.patch(id, body);
}
```

## 3. @DeleteMapping

`@DeleteMapping` removes the resource and usually returns 204 No Content.
Deleting something already gone should still end in the same state, so
many APIs return 204 (or 404) consistently rather than erroring
differently each time.

```java
@DeleteMapping("/books/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
void delete(@PathVariable Long id) {
    service.delete(id);
}
```
