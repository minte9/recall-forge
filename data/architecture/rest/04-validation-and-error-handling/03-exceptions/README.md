# API / REST / Validation and Error Handling / Exceptions

## 1. Exceptions in Controllers

An uncaught exception from a handler becomes a 500 by default. Spring
already maps its own exceptions (bad JSON, type mismatch, missing param)
to 4xx. Everything else needs an explicit mapping or clients see a generic
server error.

```java
@GetMapping("/books/{id}")
Book get(@PathVariable Long id) {
    return repo.findById(id).orElseThrow();  // NoSuchElement -> 500!
}
```

## 2. Domain Exceptions

Throw meaningful, domain-named exceptions from the service layer
(`BookNotFoundException`, `LoanLimitExceededException`) rather than
generic ones. Controllers stay free of error branching, and each exception
maps to exactly one HTTP outcome.

```java
class BookNotFoundException extends RuntimeException {
    BookNotFoundException(Long id) { super("Book " + id + " not found"); }
}
```

## 3. Mapping Exceptions to HTTP

The quickest mapping is `@ResponseStatus` on the exception class, or
`ResponseStatusException` thrown inline. Both work, but centralized
handlers scale better once you want a consistent error body.

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
class BookNotFoundException extends RuntimeException { ... }

throw new ResponseStatusException(HttpStatus.CONFLICT, "ISBN exists");
```
