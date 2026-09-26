# API / REST / Security / JWT

## 1. JWT Structure

A JSON Web Token is `header.payload.signature`, each part Base64URL
encoded. The payload holds claims (`sub`, `exp`, `iat`, roles). The
signature proves the token wasn't altered, but the payload is readable by
anyone, so never put secrets in it.

```
eyJhbGciOiJSUzI1NiJ9 . eyJzdWIiOiJhbGljZSIsImV4cCI6MTcy... . sig
   {"alg":"RS256"}       {"sub":"alice","exp":1727...}
```

## 2. Access Tokens

A JWT access token is self-contained: the API validates the signature and
`exp` locally with no database or auth-server call. Keep lifetimes short
(minutes), since a stolen token is valid until it expires; refresh tokens
issue new ones.

```
validate: signature ok? exp in future? iss/aud expected?
-> yes: trust claims (sub, scope)   -> no: 401
```

## 3. Authenticating REST Requests

The client sends the token as `Authorization: Bearer <jwt>`. In Spring,
the resource-server support validates it and exposes the claims as the
authenticated principal; avoid hand-rolled JWT filters.

```java
http.oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));

@GetMapping("/me")
String me(@AuthenticationPrincipal Jwt jwt) {
    return jwt.getSubject();                 // "alice"
}
```
