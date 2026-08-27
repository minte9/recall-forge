# Security / OAuth2 / OAuth2 Core / Roles and Endpoints

## 1. The Four Roles

OAuth2 splits delegated access into four roles. The resource owner is the user who owns the data. The client is the app that wants access. The authorization server authenticates the owner and issues tokens. The resource server hosts the API and accepts those tokens. Keeping these separate is what lets a user grant access without sharing a password.

```
resource owner        --grants-->    client
authorization server  --issues-->    access token
client  --presents token-->  resource server (the API)
```

## 2. Authorization and Token Endpoints

The authorization server exposes two endpoints. The authorization endpoint is reached through a browser redirect; it authenticates the user and returns a short-lived code. The token endpoint is a back-channel POST where the client swaps that code, plus its credentials, for tokens. Returning tokens straight from the authorization endpoint is the discarded implicit flow.

```
GET  /authorize   -> browser, returns ?code=...
POST /token       -> back channel, returns access_token
```

## 3. Confidential vs Public Clients

A confidential client -- a server-side app -- can keep a secret and authenticates to the token endpoint with it. A public client -- a SPA or mobile app -- cannot hold a secret safely, so it proves itself with PKCE instead and gets shorter token lifetimes. Registering the wrong type weakens the whole flow.

```
confidential: client_id + client_secret
public:       client_id + PKCE (no secret)
```
