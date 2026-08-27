# Security / OAuth2 / OAuth2 Core / Authorization Code Flow and PKCE

## 1. The Code Exchange

The client redirects the browser to `/authorize`. The user logs in and consents. The server redirects back with a `code`. The client then POSTs that code to `/token` and receives an access token. The code is single-use and short-lived, so intercepting the redirect alone is not enough to obtain a token.

```
1. -> /authorize?response_type=code&client_id=...&redirect_uri=...
2. <- redirect_uri?code=abc123
3. -> POST /token  code=abc123&client_id=...  => access_token
```

## 2. Why PKCE

On a public client the code passes through the browser and OS, where a malicious app could grab it. PKCE binds the code to the client: it sends a hash (`code_challenge`) up front, then the original `code_verifier` at exchange, and the server rejects the code unless they match. PKCE is now recommended for every client.

```
code_challenge = SHA256(code_verifier)   // sent to /authorize
code_verifier  = original random string  // sent to /token
```

## 3. The state Parameter

`state` is an opaque random value the client sends to `/authorize` and re-checks on the callback. If it does not match what was stored, the response is forged and the client aborts. Without it, an attacker can splice their own authorization code into the victim's session -- login CSRF.

```java
String state = randomToken();
session.put("oauth_state", state);
// on callback: reject unless param "state" equals the stored value
```
