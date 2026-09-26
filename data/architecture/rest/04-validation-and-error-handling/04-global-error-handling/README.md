# API / REST / Validation and Error Handling / Global Error Handling

## 1. @ControllerAdvice

`@RestControllerAdvice` (`@ControllerAdvice` + `@ResponseBody`) declares a
class whose exception handlers apply to all controllers. It's the single
place where exceptions become HTTP responses.

```java
@RestControllerAdvice
class ApiExceptionHandler { /* handlers for every controller */ }
```

## 2. @ExceptionHandler

An `@ExceptionHandler` method catches the listed exception types (and
subclasses) and returns the response. The most specific matching handler
wins, so a catch-all `Exception` handler can safely sit alongside
specific ones.

```java
@ExceptionHandler(BookNotFoundException.class)
ResponseEntity<ProblemDetail> notFound(BookNotFoundException e) {
    var pd = ProblemDetail.forStatusAndDetail(
        HttpStatus.NOT_FOUND, e.getMessage());
    return ResponseEntity.status(404).body(pd);
}
```

## 3. Centralized Error Handling

Centralizing gives every endpoint the same error format, keeps controllers
clean, and lets you log unexpected errors once. Handle validation errors
there too, turning field errors into a readable list.

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
ProblemDetail invalid(MethodArgumentNotValidException e) {
    var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setProperty("errors", e.getFieldErrors().stream()
        .map(f -> f.getField() + ": " + f.getDefaultMessage()).toList());
    return pd;
}
```
