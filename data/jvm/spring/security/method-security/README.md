# JVM / Spring / Security / Method Security

## 1. @PreAuthorize Basics

@PreAuthorize on a method takes a Spring Expression Language (SpEL) condition evaluated before the method runs -- if it evaluates to false, an AccessDeniedException is thrown and the method body never executes. This puts authorization logic directly on the method it protects, instead of only at the URL/filter level.

```java
@PreAuthorize("hasRole('ADMIN')")
void deleteUser(Long userId) {
    // never reached if the current user doesn't have ROLE_ADMIN
}
```


## 2. Method Security vs URL-Based Security

URL-based rules in SecurityFilterChain protect HTTP endpoints; method security protects the method itself, regardless of how it's called -- directly from another service, a scheduled job, or a message listener, not just an HTTP request. Using both together defends the same operation at two different layers.

```java
// URL-based: only protects this specific HTTP entry point
http.authorizeHttpRequests(auth -> auth.requestMatchers("/admin/**").hasRole("ADMIN"));

// Method-based: protects deleteUser() no matter how/where it's called from
@PreAuthorize("hasRole('ADMIN')")
void deleteUser(Long userId) { }
```


## 3. Enabling Method Security

@PreAuthorize and friends (@PostAuthorize, @Secured) do nothing until method security is explicitly turned on with @EnableMethodSecurity on a configuration class -- without it, the annotations are silently ignored and every method remains unprotected regardless of what's written on them.

```java
@Configuration
@EnableMethodSecurity // required, or @PreAuthorize annotations are silently ignored
class MethodSecurityConfig {
}
```
