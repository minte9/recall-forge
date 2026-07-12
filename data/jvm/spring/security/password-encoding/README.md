## JVM / Spring / Security / Password Encoding

### 1. PasswordEncoder and BCrypt

PasswordEncoder is the abstraction Spring Security uses to hash passwords before storing them and verify them on login; BCryptPasswordEncoder is the standard default, using a slow, salted hashing algorithm specifically designed to resist brute-force attacks. Registering it as a @Bean makes it available for both encoding and matching.

~~~java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // slow and salted by design, resists brute-forcing
}

String hashed = passwordEncoder().encode("myPassword123"); // never store the raw password
~~~


### 2. Why Plain-Text Passwords Are Never OK

Storing passwords as plain text (or with a fast, unsalted hash like plain MD5/SHA-256) means a single database breach exposes every user's real password immediately, and since people reuse passwords across sites, that breach cascades to their other accounts too. BCrypt's deliberate slowness makes large-scale cracking attempts impractical even after a breach.

~~~java
// Never do this:
// user.setPassword(rawPassword); // plain text, catastrophic if the DB leaks

// Always do this:
user.setPassword(passwordEncoder.encode(rawPassword));
~~~


### 3. Verifying Passwords on Login

Login verification never decrypts the stored hash back into the original password (BCrypt hashing isn't reversible) -- instead, matches(rawPassword, encodedPassword) re-hashes the entered password with the same salt and compares the results. This is why a PasswordEncoder needs both an encode() and a matches() method.

~~~java
boolean valid = passwordEncoder.matches(enteredPassword, user.getPasswordHash());
// re-hashes enteredPassword and compares -- the original hash is never reversed
~~~
