## Security / Security / Cryptography Fundamentals / Password Hashing and Salting

### 1. Salting

A salt is random data added to a password before hashing, unique per user and stored alongside the hash. Without it, two users with the same password get identical hashes, and an attacker can precompute hashes for common passwords once (a rainbow table) and match them instantly across every leaked database. Salting forces the attacker to redo that work for every single user.

~~~java
byte[] salt = SecureRandom.getInstanceStrong().generateSeed(16);
// stored alongside the hash, unique per user
~~~

### 2. Password Hashing with BCrypt

BCrypt bakes salting in automatically and, unlike SHA-256, is deliberately slow -- it has a tunable "cost factor" that can be raised over time as hardware gets faster, keeping brute-force attacks expensive even years later. That deliberate slowness is a feature for passwords, the opposite of what you'd want for a checksum.

~~~java
String hash = new BCryptPasswordEncoder().encode(rawPassword);
boolean matches = new BCryptPasswordEncoder().matches(rawPassword, hash);
~~~
