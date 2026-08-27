# Security / OAuth2 / Tokens / Access Tokens and Scopes

## 1. Bearer Tokens

An access token is a bearer credential: whoever holds it can use it, like cash. The client sends it in the `Authorization: Bearer` header, over TLS only. Because there is no proof of possession, tokens are kept short-lived and must never be logged, placed in URLs, or stored where scripts can read them.

```
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
```

## 2. Opaque vs JWT Access Tokens

An opaque token is a random string the resource server must look up at the authorization server (introspection) -- easy to revoke, but a network call per request. A JWT access token is self-contained and verified locally by signature -- fast, but hard to revoke before it expires. Short-lived JWTs are a common middle ground.

```
opaque:  "a1b2c3"   -> POST /introspect
jwt:     "eyJ..."   -> verify signature locally
```

## 3. Scopes and Least Privilege

Scopes narrow what a token can do (`invoices:read`, not blanket access). The client requests scopes at `/authorize`, the user sees them on the consent screen, and the resource server enforces them per endpoint. Request the minimum a feature needs; broad scopes turn one leaked token into a full account compromise.

```java
if (!token.scopes().contains("invoices:read"))
    return status(403);
```
