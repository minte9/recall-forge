# API / REST / Security / Authorization

## 1. Roles and Authorities

An authority is any granted permission string; a role is an authority with
the `ROLE_` prefix. `hasRole("ADMIN")` checks `ROLE_ADMIN`, while
`hasAuthority("books:write")` checks the exact string. Fine-grained
authorities age better than a few coarse roles.

```java
.hasRole("ADMIN")               // checks "ROLE_ADMIN"
.hasAuthority("SCOPE_books:write")  // exact match, e.g. OAuth2 scope
```

## 2. Endpoint Authorization

URL-level rules in the filter chain protect whole areas by path and
method. They're easy to audit in one place, but they can't see the
resource being accessed (e.g. "only the owner may edit").

```java
.authorizeHttpRequests(a -> a
    .requestMatchers(HttpMethod.DELETE, "/books/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.GET, "/books/**").permitAll()
    .anyRequest().authenticated())
```

## 3. @PreAuthorize

With `@EnableMethodSecurity`, `@PreAuthorize` checks an expression before
a service method runs and can reference arguments, which enables
ownership checks. Failures become 403. Use it for rules the URL alone
can't express.

```java
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.name")
public List<Loan> loansOf(String userId) { ... }
```
