# API / REST / Security / Basic Auth

## 1. HTTP Basic Authentication

The client sends `Authorization: Basic base64(user:password)` on every
request. Base64 is encoding, not encryption, so HTTPS is mandatory. It's
simple and fine for internal or machine-to-machine APIs, but passwords are
sent constantly and there's no expiry.

```
Authorization: Basic YWxpY2U6czNjcmV0    # "alice:s3cret" base64
missing/wrong -> 401 + WWW-Authenticate: Basic realm="api"
```

## 2. Spring Security Configuration

Enable it with `httpBasic()` and provide users through a
`UserDetailsService` with hashed passwords. In-memory users are only for
demos; real apps load them from a database.

```java
http.httpBasic(Customizer.withDefaults())
    .authorizeHttpRequests(a -> a.anyRequest().authenticated());

@Bean UserDetailsService users(PasswordEncoder enc) {
    return new InMemoryUserDetailsManager(User.withUsername("alice")
        .password(enc.encode("s3cret")).roles("USER").build());
}
```

## 3. Testing with curl

`curl -u user:pass` builds the Basic header for you. Use `-i` to see the
status and `WWW-Authenticate` header when checking the 401 path.

```bash
curl -i https://localhost:8080/books                # -> 401
curl -i -u alice:s3cret https://localhost:8080/books # -> 200
```
