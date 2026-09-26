# API / REST / Spring Boot / GET Endpoints

## 1. @GetMapping

`@GetMapping` binds a handler to HTTP GET for a path. GET handlers should
only read data. The method name doesn't matter to Spring, only the mapping
does, so name it after what it returns.

```java
@GetMapping("/books")
List<Book> findAll() {
    return bookService.findAll();
}
```

## 2. Returning Java Objects

Handlers return plain domain objects, records or collections; Spring picks
an `HttpMessageConverter` to turn them into the response body. Returning a
`null` object gives an empty 200, so signal "not found" explicitly instead.

```java
@GetMapping("/books/{id}")
Book findOne(@PathVariable Long id) {
    return bookService.find(id)
        .orElseThrow(BookNotFoundException::new);  // -> 404 via handler
}
```

## 3. Automatic JSON Serialization

Jackson's `MappingJackson2HttpMessageConverter` serializes return values to
JSON when the client accepts it (the default). Records, getters, nested
objects and lists all map naturally, and `Optional`/`java.time` types work
out of the box in Boot.

```java
record Book(Long id, String title, LocalDate published) {}
// -> {"id":1,"title":"Dune","published":"1965-08-01"}
```
