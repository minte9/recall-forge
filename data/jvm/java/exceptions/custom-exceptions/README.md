# JVM / Java / Exceptions / Custom Exceptions

## 1. Creating a Custom Exception

A custom exception is just a class extending Exception (checked) or RuntimeException (unchecked), typically adding a constructor that accepts a message and/or a cause. Custom exceptions make error handling more expressive -- catching InsufficientFundsException is far clearer at the call site than catching a generic IllegalStateException.

```java
class InsufficientFundsException extends RuntimeException {
    InsufficientFundsException(String message) {
        super(message);
    }
}

if (balance < amount) {
    throw new InsufficientFundsException("Balance too low for withdrawal of " + amount);
}
```


## 2. Exception Chaining

When catching one exception and throwing another (e.g. wrapping a low-level SQLException in a domain-specific one), pass the original as the cause via the constructor so it isn't lost -- getCause() and the printed stack trace both preserve the full chain, essential for actually debugging the root problem.

```java
try {
    // ... throws SQLException
} catch (SQLException e) {
    throw new DataAccessException("Failed to save order", e); // e becomes the cause
}
```


## 3. When to Create One

Create a custom exception when callers genuinely need to catch and handle that specific failure differently from other errors, or when a clearer name meaningfully improves readability at call sites. Don't create one just to wrap every possible failure -- reusing IllegalArgumentException/IllegalStateException is often clearer than a pile of single-use exception classes.

```java
// Worth a custom exception: callers handle this specific case differently
// catch (InsufficientFundsException e) { promptForTopUp(); }

// Not worth it: a generic IllegalArgumentException is already clear enough
if (age < 0) throw new IllegalArgumentException("age cannot be negative");
```
