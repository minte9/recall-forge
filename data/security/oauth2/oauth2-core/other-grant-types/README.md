# Security / OAuth2 / OAuth2 Core / Other Grant Types

## 1. Client Credentials

When there is no user -- a backend job calling another service -- the client authenticates as itself and receives a token scoped to its own permissions. There is no redirect and no refresh token; the client simply requests a fresh token when the old one expires.

```
POST /token
grant_type=client_credentials
client_id=svc-a&client_secret=...
```

## 2. Device Authorization Grant

For input-constrained devices -- a TV, a CLI -- the device shows a short code and a URL. The user opens that URL on a phone, enters the code, and approves. Meanwhile the device polls the token endpoint until approval completes. No browser or keyboard is needed on the device itself.

```
device shows: visit example.com/activate, code WDJB-MJHT
device polls: POST /token grant_type=device_code  (every 5s)
```

## 3. Deprecated Grants

The implicit grant returned tokens straight from the authorization endpoint, exposing them in the URL and browser history -- replaced by auth code + PKCE. The resource owner password grant had the client collect the user's password directly, defeating the entire point of OAuth2. OAuth 2.1 removes both.

```
implicit:  #access_token=... in the URL fragment   (don't use)
password:  client sends username + password         (don't use)
```
