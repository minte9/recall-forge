# API / REST / Security / OAuth2

## 1. Roles and Flows from the API's View

In OAuth 2.0 your REST API is the **resource server**: it doesn't log
users in or issue tokens, it only validates tokens issued by an
authorization server. Flows like Authorization Code + PKCE happen
between client and auth server (see the OAuth2 category).

```
client --(login, consent)--> authorization server --token--> client
client --(Bearer token)--> REST API (resource server) validates it
```

## 2. Bearer Tokens

A bearer token grants access to whoever holds it, so it must only travel
over HTTPS in the `Authorization` header, never in URLs (logs, history).
Scopes in the token limit what the client may do.

```
GET /books
Authorization: Bearer eyJ...       # scope: "books:read"
# never: GET /books?access_token=eyJ...
```

## 3. Spring OAuth2 Resource Server

`spring-boot-starter-oauth2-resource-server` plus the issuer URI is
enough: Spring fetches the signing keys, validates signature, `exp` and
`iss`, and maps scopes to `SCOPE_` authorities.

```yaml
spring.security.oauth2.resourceserver.jwt.issuer-uri:
  https://auth.example.com
```
```java
.requestMatchers(HttpMethod.GET, "/books/**")
    .hasAuthority("SCOPE_books:read")
```
