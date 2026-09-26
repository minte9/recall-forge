# API / REST / Validation and Error Handling / Error Response Design

## 1. Consistent Error JSON

Every error from every endpoint should share one shape, so clients can
write a single error parser. It should include the status, a
machine-readable code, a human-readable message, and field details for
validation errors.

```json
{"status": 400, "code": "VALIDATION_FAILED",
 "message": "Request has invalid fields",
 "errors": [{"field": "title", "message": "must not be blank"}]}
```

## 2. Error Codes and Messages

Clients branch on stable codes, not on message text, which may change or
be translated. Messages should help the caller fix the request but never
leak internals such as stack traces, SQL or class names.

```
good: {"code":"BOOK_ON_LOAN","message":"Book 42 is currently on loan"}
bad:  {"message":"org.hibernate.exception.ConstraintViolation..."}
```

## 3. ProblemDetail (RFC 9457)

RFC 9457 (successor of 7807) standardizes error bodies as
`application/problem+json` with `type`, `title`, `status`, `detail` and
`instance`, plus custom extension fields. Spring's `ProblemDetail`
implements it; `spring.mvc.problemdetails.enabled=true` applies it to
built-in errors.

```java
ProblemDetail pd = ProblemDetail.forStatusAndDetail(
    HttpStatus.CONFLICT, "Book 42 is on loan");
pd.setType(URI.create("https://api.example.com/errors/on-loan"));
pd.setProperty("code", "BOOK_ON_LOAN");   // extension field
```
