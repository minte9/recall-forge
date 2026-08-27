# Security / OAuth2 / Tokens / Token Validation

## 1. Verifying a JWT Signature

The resource server fetches the authorization server's public keys from its JWKS URL (named in the discovery document), matched to the token by its `kid` header. It verifies the signature with that key and rejects `alg: none` or any algorithm it did not expect. Keys are cached and refreshed on rotation.

```
GET /.well-known/jwks.json  -> { keys: [ { kid, kty, n, e } ] }
```

## 2. Checking the Claims

A valid signature is not enough. The server must check `exp` (not expired), `iss` (from the expected issuer), and `aud` (this API is the intended audience). Skipping the `aud` check lets a token minted for one service be replayed against another.

```java
require(jwt.issuer().equals(EXPECTED_ISS));
require(jwt.audience().contains(MY_API_ID));
require(jwt.expiresAt().isAfter(now()));
```

## 3. Introspection and Revocation

For opaque tokens, or when instant revocation matters, the resource server calls the authorization server's introspection endpoint, which returns `active: true/false` plus scopes. The revocation endpoint lets a client proactively kill a token on logout. Both are back-channel and authenticated.

```
POST /introspect  token=...  -> { "active": true, "scope": "..." }
POST /revoke      token=...   -> 200
```
