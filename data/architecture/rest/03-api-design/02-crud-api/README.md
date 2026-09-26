# API / REST / API Design / CRUD API

## 1. Mapping CRUD to HTTP

Create, read, update and delete map directly onto POST, GET, PUT/PATCH and
DELETE over a collection URL and an item URL. Getting this mapping
consistent across all resources is most of what makes an API feel
"RESTful".

```
Create  POST   /books          201 + Location
Read    GET    /books/{id}     200 | 404
Update  PUT    /books/{id}     200 | 204
Delete  DELETE /books/{id}     204
```

## 2. Create/Read/Update/Delete Flow

A typical client flow: POST to create and receive the new URL in
`Location`, GET it back, modify it with PUT/PATCH, and DELETE it. Each
step's status code tells the client exactly what happened without parsing
the body.

```
POST /books {"title":"Dune"}   -> 201 Location: /books/43
GET  /books/43                 -> 200 {"id":43,...}
PATCH /books/43 {"year":1965}  -> 200
DELETE /books/43               -> 204
GET  /books/43                 -> 404
```

## 3. A Complete /books Controller

One controller per resource keeps the mapping readable: class-level prefix,
one method per operation, and the service layer doing the work.

```java
@RestController @RequestMapping("/books")
class BookController {
    @GetMapping            List<Book> all() { ... }
    @GetMapping("/{id}")   Book one(@PathVariable Long id) { ... }
    @PostMapping           ResponseEntity<Book> create(...) { ... }
    @PutMapping("/{id}")   Book replace(...) { ... }
    @DeleteMapping("/{id}") void delete(@PathVariable Long id) { ... }
}
```
