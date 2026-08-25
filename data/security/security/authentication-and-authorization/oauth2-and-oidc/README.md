# Security / Security / Authentication & Authorization / OAuth2 and OIDC

## 1. OAuth2 Grant Types

OAuth2 defines different "grant types" for different situations: authorization code (a browser app redirects to a login page, then exchanges a code for a token -- the most common and secure for user-facing apps), and client credentials (a service authenticates as itself, no user involved, used for machine-to-machine calls). Picking the wrong grant type for the situation is a common real-world mistake.

```java
// Client credentials: service-to-service, no user
POST /oauth/token
grant_type=client_credentials&client_id=...&client_secret=...
```

## 2. OpenID Connect vs OAuth2

OAuth2 is an authorization protocol -- it answers "can this app access this resource on the user's behalf." OpenID Connect (OIDC) is a thin identity layer built on top of it that answers "who is this user," adding a standardized ID token, a JWT with the user's identity claims, alongside OAuth2's access token. Using OAuth2 alone for login, without OIDC, is a common source of subtly broken authentication.

```java
// OIDC ID token: standardized identity claims
{ "sub": "user-123", "email": "a@b.com", "iss": "https://idp" }
```
