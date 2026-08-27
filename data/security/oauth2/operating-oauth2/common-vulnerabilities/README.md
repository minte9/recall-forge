# Security / OAuth2 / Operating OAuth2 / Common Vulnerabilities

## 1. Redirect URI Validation

The authorization server must match `redirect_uri` against the registered value exactly -- full string, no wildcards, no substring checks. A loose match lets an attacker register a URL that ends up delivering the code to their own domain. This is the single most exploited OAuth2 flaw.

```
registered: https://app.com/cb
request:    https://app.com/cb          OK (exact)
request:    https://app.com.evil.com/cb REJECT
```

## 2. Mix-Up and CSRF

In a mix-up attack the client is tricked into sending a code issued by one provider to a different provider's token endpoint. Defences: always send and verify `state`, use PKCE, and check the `iss` parameter on the callback so the client knows which provider actually responded.

```
callback must carry: state (matches stored) + iss (expected)
```

## 3. Token Leakage

Access tokens leak through `Referer` headers when a page links off-site, through server and proxy logs when placed in query strings, and through browser history. Keep tokens in headers and request bodies only, scrub them from logs, and set `Referrer-Policy` on pages that hold them.

```
BAD:  GET /api/data?access_token=eyJ...
GOOD: Authorization: Bearer eyJ...
```
