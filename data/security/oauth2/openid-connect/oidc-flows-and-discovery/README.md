# Security / OAuth2 / OpenID Connect / OIDC Flows and Discovery

## 1. Authorization Code Flow for OIDC

OIDC reuses OAuth2's authorization code flow; adding `scope=openid` is what turns an OAuth2 request into an OIDC one. The token response then carries an `id_token` alongside the `access_token`. Implicit and hybrid OIDC flows exist but, as in plain OAuth2, code + PKCE is the current recommendation.

```
GET /authorize?response_type=code&scope=openid%20email&...
POST /token -> { access_token, id_token, refresh_token }
```

## 2. Discovery Document

Every OIDC provider publishes `/.well-known/openid-configuration`, a JSON file listing its endpoints, supported scopes, and signing algorithms. Clients read it at startup instead of hard-coding URLs, so an issuer can rotate keys or move endpoints without breaking integrations.

```
GET https://idp.example.com/.well-known/openid-configuration
-> { authorization_endpoint, token_endpoint, jwks_uri, ... }
```

## 3. The UserInfo Endpoint

When the ID token omits some claims to stay small, the client calls the UserInfo endpoint with the access token to fetch the full profile. The response `sub` must equal the ID token's `sub`; if it does not, the two tokens belong to different logins and the client aborts.

```
GET /userinfo   Authorization: Bearer <access_token>
-> { "sub": "...", "email": "...", "name": "..." }
```
