# API / REST / Spring Boot REST Basics / Query Parameters

## 1. @RequestParam

`@RequestParam` binds a query-string value (`?author=Herbert`) to a
parameter. Query parameters refine a request, filtering, sorting or
paging a collection, rather than identifying a resource.

```java
@GetMapping("/books")
List<Book> byAuthor(@RequestParam String author) {
    return service.byAuthor(author);   // GET /books?author=Herbert
}
```

## 2. Required vs Optional

`@RequestParam` is required by default: a missing value gives 400. Set
`required = false` (the value becomes `null`) or use `Optional<T>` for
filters the client may omit.

```java
@GetMapping("/books")
List<Book> search(@RequestParam Optional<String> author,
                  @RequestParam(required = false) Integer year) {
    return service.search(author, year);   // both may be absent
}
```

## 3. Default Values

`defaultValue` makes a parameter optional and supplies a fallback (always
as a String, converted to the target type). It's the usual way to give
paging and sorting sensible defaults.

```java
@GetMapping("/books")
List<Book> page(@RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size) {
    return service.page(page, size);
}
```
