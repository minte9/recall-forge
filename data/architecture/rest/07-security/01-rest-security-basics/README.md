# API / REST / Security / REST Security Basics

## 1. Authentication vs Authorization

Authentication answers "who are you?" (credentials, tokens); failure is
401. Authorization answers "may you do this?" (roles, ownership); failure
is 403. Authentication always runs first: you can't authorize an unknown
caller.

```
no/invalid token          -> 401 Unauthorized
valid token, not allowed  -> 403 Forbidden
```

## 2. Stateless APIs

REST APIs usually don't keep a server session: every request carries its
credentials (a bearer token or Basic auth header). This scales
horizontally and avoids CSRF issues tied to cookies, at the cost of
sending and verifying credentials each time.

```
GET /books
Authorization: Bearer eyJhbGciOi...   # sent on every request
# no JSESSIONID, no server-side session
```

## 3. HTTPS

Tokens and passwords travel in headers, so without TLS anyone on the
network can steal them. Every API must be HTTPS-only; redirecting HTTP to
HTTPS is not enough for APIs, since the credentials were already sent in
cleartext.

```
http://api.example.com  -> reject (don't redirect: token already leaked)
https://api.example.com -> ok
Strict-Transport-Security: max-age=31536000
```
