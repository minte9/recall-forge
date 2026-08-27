# Security / OAuth2 / OpenID Connect / ID Tokens and Claims

## 1. ID Token vs Access Token

OIDC adds an ID token: a signed JWT that tells the client who the user is and that they just authenticated. It is for the client to read. The access token stays opaque to the client and is only for calling APIs. Using an access token to identify the user is the mistake OIDC exists to prevent.

```
id_token     -> client reads: sub, name, auth_time
access_token -> client forwards to the API, never parses
```

## 2. Standard Claims and Scopes

OIDC standardizes identity claims so every provider looks the same. The `openid` scope is required and returns `sub`, a stable user id. The `profile`, `email`, and `address` scopes add their matching claim sets. `sub` is the only safe key to store -- email addresses get reassigned between people.

```
scope=openid email profile
id_token: sub, email, email_verified, name, picture
```

## 3. The nonce

The client generates a `nonce`, sends it on the authorization request, and checks that the same value appears inside the returned ID token. This binds the token to this specific login attempt and blocks replay of a stolen ID token, the way `state` protects the overall redirect.

```
/authorize ... &nonce=xyz
id_token must contain "nonce": "xyz"   (else reject)
```
