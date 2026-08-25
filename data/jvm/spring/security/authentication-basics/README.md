# JVM / Spring / Security / Authentication Basics

## 1. Authentication vs Authorization

Authentication answers "who is this user?" (logging in, verifying identity); authorization answers "is this user allowed to do this?" (checking permissions after identity is known). Spring Security handles both, but as genuinely separate concerns -- a request can be authenticated yet still rejected by an authorization rule.

```java
// Authentication: verifying who the user is (login)
// Authorization: deciding what they're allowed to do, once known
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN") // authorization rule
);
```


## 2. SecurityContext and the Authentication Object

SecurityContextHolder.getContext().getAuthentication() returns the current request's Authentication object, holding the principal (who), authorities (what they can do), and provider-specific details. It's stored per-thread by default, which is why it's readily available anywhere in a request's call stack without passing it explicitly through every method.

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
boolean isAdmin = auth.getAuthorities().stream()
    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
```


## 3. @AuthenticationPrincipal

@AuthenticationPrincipal injects the current authenticated principal directly as a controller method parameter, sparing a manual SecurityContextHolder lookup. It's the idiomatic way to access "the current user" inside a REST controller, and can even be typed as your own custom principal class instead of the generic one.

```java
@GetMapping("/me")
CurrentUserResponse me(@AuthenticationPrincipal OAuth2User principal) {
    return new CurrentUserResponse(principal.getAttribute("email"));
}
```
