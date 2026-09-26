# API / REST / HTTP Features / CORS

## 1. Same-Origin Policy

Browsers block JavaScript from reading responses from a different
origin (scheme + host + port). This protects users' cookies from
malicious sites. It's enforced by the browser only; curl and server-side
clients are unaffected.

```
page:  https://app.example.com
fetch: https://api.example.com/books   # different origin -> blocked
                                        # unless the API allows it
```

## 2. CORS

CORS lets a server opt in to cross-origin access via response headers
(`Access-Control-Allow-Origin`, `-Methods`, `-Headers`). For non-simple
requests (JSON body, custom headers, PUT/DELETE), the browser first sends
an `OPTIONS` preflight.

```
OPTIONS /books   Origin: https://app.example.com
                 Access-Control-Request-Method: PUT
-> 204  Access-Control-Allow-Origin: https://app.example.com
        Access-Control-Allow-Methods: GET, POST, PUT
```

## 3. Spring Boot CORS Configuration

Configure CORS globally with a `WebMvcConfigurer` (or `@CrossOrigin` per
controller). List explicit origins; never combine `*` with credentials.
With Spring Security, also enable `http.cors(withDefaults())` so preflights
aren't rejected before MVC runs.

```java
@Override
public void addCorsMappings(CorsRegistry r) {
    r.addMapping("/api/**")
     .allowedOrigins("https://app.example.com")
     .allowedMethods("GET", "POST", "PUT", "DELETE");
}
```
