# API / REST / Documentation / Documenting API Contracts

## 1. Request/Response Contracts

The contract is what clients may rely on: URLs, methods, required fields,
types, formats and status codes. Describe operations and fields with
`@Operation` and `@Schema`, including examples; they show up in the spec
and Swagger UI.

```java
@Operation(summary = "Create a book")
@PostMapping("/books")
ResponseEntity<BookResponse> create(@Valid @RequestBody NewBook b) {...}

record NewBook(
    @Schema(example = "Dune") @NotBlank String title) {}
```

## 2. Error Documentation

Document every non-2xx response an operation can return and its body
shape. Undocumented errors are where client integrations break.
`@ApiResponse` declares them per status.

```java
@ApiResponse(responseCode = "201", description = "Created")
@ApiResponse(responseCode = "400", description = "Validation failed",
    content = @Content(schema = @Schema(implementation =
        ProblemDetail.class)))
@ApiResponse(responseCode = "409", description = "ISBN already exists")
```

## 3. Keeping Documentation Synchronized

Hand-written docs drift. Generate the spec from code (or code from the
spec), commit it, and fail CI when it changes unexpectedly; contract
tests then verify the running API still matches.

```
CI: build -> GET /v3/api-docs > openapi.json
    git diff --exit-code openapi.json   # fail if contract changed
```
