# Security / Security / Web Security / Cookie Security and Clickjacking

## 1. Secure Cookie Flags

`HttpOnly` blocks JavaScript from reading a cookie, closing off the most common way XSS steals session tokens. `Secure` ensures the cookie is only ever sent over HTTPS. `SameSite=Strict` or `Lax` stops the browser from attaching the cookie to cross-site requests, a direct defense against CSRF. All three should be the default, not an afterthought.

```java
ResponseCookie cookie = ResponseCookie.from("session", token)
    .httpOnly(true).secure(true).sameSite("Strict").build();
```

## 2. Clickjacking Defense

Clickjacking loads a legitimate page in an invisible iframe over a decoy page, tricking a user into clicking a real button (like "delete account") while believing they're clicking something else entirely. The `X-Frame-Options` header, or the newer `Content-Security-Policy: frame-ancestors`, tells the browser to refuse to render the page inside a frame at all.

```java
response.setHeader("X-Frame-Options", "DENY");
```
