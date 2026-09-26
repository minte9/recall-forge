# API / REST / Persistence / Spring Data JPA

## 1. Entity, Repository, Service Layers

A REST request flows controller → service → repository → database. The
controller handles HTTP and DTOs, the service holds business rules and
transactions, and the repository only does data access. Each layer can be
tested and changed independently.

```
BookController  (HTTP, DTOs, status codes)
   -> BookService    (rules, @Transactional)
      -> BookRepository (JpaRepository<Book, Long>)
```

## 2. JpaRepository

Extending `JpaRepository<T, ID>` gives CRUD, paging and sorting with no
implementation; Spring generates it at runtime. Add derived query methods
by naming convention for simple lookups (see Spring / Data for depth).

```java
interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByAuthorId(Long authorId);   // derived query
    boolean existsByIsbn(String isbn);
}
```

## 3. Connecting REST to Persistence

The controller never touches the repository directly: it calls the
service, which loads/saves entities and returns something the controller
maps to a response DTO. That keeps HTTP concerns and persistence concerns
apart.

```java
@GetMapping("/{id}")
BookResponse get(@PathVariable Long id) {
    return BookResponse.from(service.get(id));  // service -> repository
}
```
