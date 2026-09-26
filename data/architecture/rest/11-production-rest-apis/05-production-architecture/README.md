# API / REST / Production REST APIs / Production Architecture

## 1. Controller → Service → Repository

Controllers translate HTTP (DTOs, status codes, headers); services own
use cases, rules and transactions; repositories own data access. Each
layer calls only the one below it, so HTTP details never leak into
business logic.

```
BookController  -> BookService  -> BookRepository -> DB
  HTTP + DTOs      rules + @Tx     queries only
```

## 2. DTO and Domain Boundaries

DTOs exist only at the edge and map to/from domain objects in the
controller (or a mapper). Entities never appear in controller signatures,
which keeps the database schema free to change without breaking the API
contract.

```java
@PostMapping
ResponseEntity<BookResponse> create(@Valid @RequestBody NewBook dto) {
    Book book = service.create(dto.title(), dto.authorId());
    return ResponseEntity.created(uriOf(book))
                         .body(BookResponse.from(book));
}
```

## 3. Structuring a Real Project

Package by feature, not by layer: everything about books sits together,
and shared concerns (error handling, security, config) get their own
packages. Features stay cohesive and can be split into modules later.

```
com.example.library
├── book/     BookController, BookService, BookRepository, dto/
├── loan/     LoanController, LoanService, ...
├── common/   ApiExceptionHandler, ProblemDetail helpers
└── config/   SecurityConfig, OpenApiConfig
```
