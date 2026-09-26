# API / REST / Security / Spring Security

## 1. Security Filter Chain

Spring Security is a chain of servlet filters that runs before your
controllers: it authenticates the request, then checks access rules. You
configure it with a `SecurityFilterChain` bean (see Spring / Security for
internals).

```
request -> [CORS] -> [auth filter] -> [authorization] -> controller
                         401 here        403 here
```

## 2. Protecting Endpoints

For a REST API, disable what only suits browser apps (form login, CSRF for
token-based APIs), make the session stateless, and declare access rules.
Rules match top-down, so specific ones come first.

```java
@Bean
SecurityFilterChain api(HttpSecurity http) throws Exception {
    return http
        .csrf(c -> c.disable())            // token auth, no cookies
        .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
        .authorizeHttpRequests(a -> a
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .build();
}
```

## 3. Public vs Authenticated Resources

Explicitly `permitAll()` public endpoints (health check, public catalog,
docs) and require authentication for everything else. Default-deny with
`anyRequest().authenticated()` means a forgotten endpoint is protected,
not exposed.

```java
.authorizeHttpRequests(a -> a
    .requestMatchers(HttpMethod.GET, "/books/**").permitAll()
    .requestMatchers("/actuator/health").permitAll()
    .anyRequest().authenticated())      // default-deny
```
