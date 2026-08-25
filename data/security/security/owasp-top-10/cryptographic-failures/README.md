# Security / Security / OWASP Top 10 / Cryptographic Failures

## 1. Weak Hashing Algorithms

MD5 and SHA-1 were designed for speed and integrity checks, not for protecting secrets -- that speed is exactly what makes them weak for passwords, since it lets an attacker try billions of guesses per second against a leaked hash. A cryptographic failure here isn't using encryption badly, it's using the wrong tool for the job entirely.

```java
// Weak: fast general-purpose hash, crackable at scale
MessageDigest.getInstance("MD5").digest(password.getBytes());

// Better: purpose-built, deliberately slow password hash
new BCryptPasswordEncoder().encode(password);
```

## 2. Storing Secrets in Plaintext

Storing API keys, database passwords, or tokens directly in source code or a plaintext config file means anyone with repo access, a backup, or a log file has them too. Secrets belong in a dedicated store (environment variables backed by a vault, not committed files) so they can be rotated and access-controlled independently of the codebase.

```java
// Bad: secret committed to source control
String apiKey = "sk_live_51H8x...";

// Better: read from environment at runtime
String apiKey = System.getenv("PAYMENT_API_KEY");
```
