# API / REST / Spring Boot REST Basics / POST and Request Body

## 1. @PostMapping

`@PostMapping` handles POST, conventionally used to create a new resource
in a collection. The server assigns the id, so the client posts to the
collection URL, not to an item URL.

```java
@PostMapping("/books")               // POST to the collection
@ResponseStatus(HttpStatus.CREATED)  // 201 instead of 200
Book create(@RequestBody NewBook body) {
    return service.create(body);
}
```

## 2. @RequestBody

`@RequestBody` tells Spring to read the HTTP body and convert it into the
parameter type using the request's `Content-Type`. Without it, Spring
would try to bind form/query parameters instead and your object would be
empty.

```java
@PostMapping("/books")
Book create(@RequestBody NewBook body) { ... }
// missing @RequestBody -> fields silently null
```

## 3. JSON to Java Object

Jackson maps JSON properties to fields/record components by name. Malformed
JSON produces 400; unknown properties are ignored by default in Boot. A
record is a clean fit for incoming payloads.

```java
record NewBook(String title, Long authorId) {}
// {"title":"Dune","authorId":7} -> new NewBook("Dune", 7L)
```
