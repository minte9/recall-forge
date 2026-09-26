# API / REST / Validation and Error Handling / Custom Validation

## 1. Validation Rules

Bean Validation covers input shape (required, length, format). Business
rules that need data ("email already taken", "book is on loan") belong in
the service layer and surface as domain exceptions, usually mapped to 409
or 422.

```java
// shape: annotation on DTO
@Email String email;
// business rule: service
if (users.existsByEmail(email)) throw new EmailTakenException(email);
```

## 2. Custom Validators

For a reusable shape rule, define a constraint annotation plus a
`ConstraintValidator`. It then works like any built-in constraint and
shows up in the standard 400 error response.

```java
@Constraint(validatedBy = IsbnValidator.class)
@Target(FIELD) @Retention(RUNTIME)
@interface Isbn { String message() default "invalid ISBN";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {}; }

class IsbnValidator implements ConstraintValidator<Isbn, String> {
    public boolean isValid(String v, ConstraintValidatorContext c) {
        return v == null || v.matches("\\d{13}");  // null: @NotNull's job
    }
}
```

## 3. Cross-Field Validation

When a rule spans several fields (end after start, password confirmation),
put a class-level constraint on the DTO or, for records, a boolean
`@AssertTrue` method, which is simpler for one-off rules.

```java
record Period(LocalDate from, LocalDate to) {
    @AssertTrue(message = "'to' must be after 'from'")
    boolean isOrdered() {
        return from == null || to == null || to.isAfter(from);
    }
}
```
