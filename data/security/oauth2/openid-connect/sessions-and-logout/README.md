# Security / OAuth2 / OpenID Connect / Sessions and Logout

## 1. RP-Initiated Logout

The client (relying party) sends the user to the provider's `end_session_endpoint` with the ID token as a hint and a post-logout redirect URL. This ends the session at the identity provider, not just locally -- otherwise the next login silently re-authenticates from the provider's still-live session.

```
GET /logout?id_token_hint=...&post_logout_redirect_uri=...
```

## 2. Front-Channel vs Back-Channel Logout

Front-channel logout loads a hidden iframe per client to clear each session -- simple, but broken by third-party cookie restrictions. Back-channel logout has the provider POST a logout token directly to each client's server -- reliable, but every client must expose an endpoint to receive it.

```
front channel: <iframe src="client/logout"> per app
back channel:  provider POSTs logout_token -> client backend
```

## 3. Session Management Pitfalls

A local session that outlives the provider session lets a "logged out" user keep working. A local session shorter than the token causes silent re-login loops. Align local session lifetime with token lifetime, and treat a provider logout event as authoritative.

```
local session TTL  ~=  token TTL
provider logout    ->  drop the local session now
```
