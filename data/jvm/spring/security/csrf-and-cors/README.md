# JVM / Spring / Security / CSRF and CORS

## 1. CSRF Protection Basics

Cross-Site Request Forgery protection defends against a malicious site tricking a logged-in user's browser into submitting a request to your app using their existing session cookie. Spring Security enables it by default for state-changing requests, requiring a CSRF token that a malicious third-party site has no way to obtain.

```java
// Enabled by default -- state-changing requests (POST/PUT/DELETE) need a valid CSRF token
// alongside the session cookie, which an attacker's site cannot supply.
```


## 2. When to Disable CSRF Safely

CSRF protection is designed around cookie-based sessions; a stateless REST API authenticated with a bearer token (not a cookie) isn't vulnerable to CSRF the same way, since an attacker's page can't attach a token it never had. Disabling CSRF for such an API is common, but it must be paired with a SameSite cookie policy if any cookie-based session remains.

```java
http.csrf(csrf -> csrf.disable()); // reasonable only for token-based, cookie-free APIs

// If a session cookie is still used, rely on SameSite=Lax/Strict instead
// to get equivalent protection without a CSRF token.
```


## 3. CORS Configuration

CORS (Cross-Origin Resource Sharing) is a separate concern from CSRF -- it controls whether a browser running JavaScript on one origin is allowed to call your API on another origin at all. A CorsConfigurationSource bean whitelists allowed origins, methods, and headers explicitly; the default is to allow nothing cross-origin.

```java
@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://myfrontend.example.com"));
    config.setAllowedMethods(List.of("GET", "POST"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```
