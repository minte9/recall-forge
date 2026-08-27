# Security / OAuth2 / Tokens / Refresh Tokens

## 1. What Refresh Tokens Do

A refresh token lets the client get a new access token without sending the user back through login. It is long-lived, so it is only ever sent to the token endpoint over the back channel, never to a resource server. Access tokens stay short so a leak has a small window; the refresh token carries continuity.

```
POST /token
grant_type=refresh_token&refresh_token=...
```

## 2. Rotation and Reuse Detection

With rotation, each refresh returns a new refresh token and invalidates the old one. If an already-used refresh token is presented again, the server assumes it was stolen and revokes the whole token family. This turns a silent theft into a detectable, containable event.

```
use RT1 -> get AT2 + RT2   (RT1 now dead)
RT1 seen again -> revoke the whole family (RT1, RT2, ...)
```

## 3. Where to Store Them

A confidential client keeps refresh tokens server-side, tied to the user's session. A browser app ideally has no refresh token at all; if it must, the token belongs in an `HttpOnly`, `Secure`, `SameSite` cookie, never in `localStorage` where any XSS can read it.

```
server app: encrypted in the session store
SPA:        HttpOnly Secure SameSite cookie (or none)
```
