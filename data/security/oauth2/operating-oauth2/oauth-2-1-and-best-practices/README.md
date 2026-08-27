# Security / OAuth2 / Operating OAuth2 / OAuth 2.1 and Best Practices

## 1. What OAuth 2.1 Consolidates

OAuth 2.1 is not a new protocol -- it folds years of security best-practice documents into one. PKCE becomes mandatory for all clients. The implicit and resource-owner-password grants are removed. Redirect URIs must match exactly. Bearer tokens in query strings are disallowed.

```
2.1 = 2.0 + mandatory PKCE - implicit grant - password grant
```

## 2. Browser App Best Practice

A SPA should use auth code + PKCE as a public client, keep access tokens in memory only, and avoid refresh tokens unless they are rotated and cookie-stored. Better still, put a lightweight backend in front -- the backend-for-frontend pattern -- so tokens never reach JavaScript at all.

```
BFF: browser <-> your backend (holds tokens) <-> auth server
```

## 3. Native App Best Practice

A mobile app uses auth code + PKCE and opens the system browser or an in-app browser tab -- never an embedded webview, which can read the user's credentials directly. The redirect returns via a claimed HTTPS link or a custom scheme registered to the app.

```
open system browser (not WebView)
redirect -> app://cb  or a claimed https:// link
```
