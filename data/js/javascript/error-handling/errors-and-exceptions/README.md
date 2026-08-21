## JS / Javascript / Error Handling / Errors & Exceptions

### 1. try/catch/finally

Code that might throw goes in `try`; `catch` handles the error; `finally`
always runs, whether or not an error occurred — useful for cleanup.

~~~javascript
try {
  riskyOperation();
} catch (err) {
  console.error(err.message);
} finally {
  cleanup();   // always runs
}
~~~


### 2. Error Object & Types

`Error` and its built-in subtypes (`TypeError`, `RangeError`,
`SyntaxError`) carry a `message` and `stack`; catching lets you branch on
`err instanceof TypeError` for specific handling.

~~~javascript
try {
  null.foo;
} catch (err) {
  err instanceof TypeError;   // true
  err.message;                 // "Cannot read properties of null..."
}
~~~


### 3. Custom Errors

Extending `Error` creates domain-specific error types; remember to call
`super(message)` so `.message` and `.stack` still work correctly.

~~~javascript
class ValidationError extends Error {
  constructor(field) {
    super(`Invalid field: ${field}`);
    this.name = "ValidationError";
  }
}
~~~
