# Security / OAuth2 / Operating OAuth2 / Securing APIs

## 1. Audience Restriction

Every token should name the API it is for in its `aud` claim, and every resource server should reject tokens whose `aud` is not itself. Without this, a token phished by a low-value service can be replayed against a high-value one that trusts the same issuer.

```java
if (!jwt.audience().contains("https://api.payments"))
    return status(401);
```

## 2. Token per API

Issue a separate, narrowly scoped access token for each downstream API rather than one broad token forwarded everywhere. If service A must call service B, it exchanges its token for a new one addressed to B (token exchange). A leak in B then cannot be replayed against C.

```
client -> token(aud=A)
A -> token exchange -> token(aud=B)   // A never forwards its own
```

## 3. Sender-Constrained Tokens

Bearer tokens work for anyone who steals them. mTLS-bound and DPoP tokens tie the holder's key into the token, so a stolen token is useless without the matching private key. This is becoming standard for high-value APIs such as open banking and payments.

```
DPoP: client signs each request with its key;
      the token carries the key thumbprint (cnf.jkt)
```
