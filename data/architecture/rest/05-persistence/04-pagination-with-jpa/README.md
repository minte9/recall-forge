# API / REST / Persistence / Pagination with JPA

## 1. Pageable

Declare a `Pageable` parameter in the controller and Spring builds it from
`?page=&size=&sort=`. Pass it straight to the repository; the database
does `LIMIT/OFFSET` and `ORDER BY`. Cap `size` so clients can't request
millions of rows.

```java
@GetMapping("/books")
Page<BookResponse> list(@PageableDefault(size = 20) Pageable p) {
    return repo.findAll(p).map(BookResponse::from);
}
// GET /books?page=1&size=10&sort=title,asc
```

## 2. Page<T>

`Page<T>` carries the content plus total elements and total pages (costing
an extra count query); `Slice<T>` only knows whether there's a next page,
which is cheaper for infinite scrolling.

```java
Page<Book>  page  = repo.findAll(pageable);    // runs COUNT(*) too
Slice<Book> slice = repo.findByAuthorId(7L, pageable); // no count
```

## 3. Stable JSON for Pages

Serializing `PageImpl` directly exposes an unstable internal structure
(Spring warns about it). Return your own page DTO, or enable
`@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`.

```java
record PageResponse<T>(List<T> items, int page, int size, long total) {
    static <T> PageResponse<T> of(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(),
                                  p.getSize(), p.getTotalElements());
    }
}
```
