# API / REST / Validation and Error Handling / Bean Validation

## 1. @Valid

Annotating a `@RequestBody` parameter with `@Valid` runs Jakarta Bean
Validation before the handler executes. If any constraint fails, Spring
throws `MethodArgumentNotValidException` and responds 400; your method
never sees invalid input. Requires `spring-boot-starter-validation`.

```java
@PostMapping("/books")
Book create(@Valid @RequestBody NewBook body) {  // validated first
    return service.create(body);
}
```

## 2. @NotNull, @NotBlank, @Size

`@NotNull` rejects null; `@NotBlank` (strings) also rejects "" and
whitespace; `@Size` bounds length or collection size. Others worth
remembering: `@Email`, `@Min`/`@Max`, `@Positive`, `@Pattern`, `@Past`.

```java
record NewBook(
    @NotBlank @Size(max = 200) String title,
    @NotNull @Positive Long authorId,
    @Size(max = 5) List<String> tags) {}
```

## 3. Validating Path and Query Params

Constraints on `@PathVariable`/`@RequestParam` work when the controller is
validated (Boot 3.1+ does this for method parameters with constraints).
Nested objects need `@Valid` on the field to cascade validation.

```java
@GetMapping("/books")
List<Book> page(@RequestParam @Max(100) int size) { ... }

record Order(@Valid @NotNull Address shipTo) {}  // cascade into Address
```
