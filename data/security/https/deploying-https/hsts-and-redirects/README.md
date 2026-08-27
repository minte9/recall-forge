# Security / HTTPS / Deploying HTTPS / HSTS and Redirects

## 1. Redirecting HTTP to HTTPS

Listen on port 80 only to issue a 301 to the `https://` URL and serve nothing else there. The first plain request is still interceptable, which is exactly the gap HSTS closes. Use an HTTP-level redirect, never a meta refresh or JavaScript, so it happens before any page content loads.

```
server {
  listen 80;
  return 301 https://$host$request_uri;
}
```

## 2. The Strict-Transport-Security Header

`Strict-Transport-Security` tells the browser to use HTTPS for this host for `max-age` seconds, even if the user types `http://` or follows a plain link. Add `includeSubDomains` to cover every subdomain. Send it only over HTTPS. Start with a small max-age, then raise it to a year once confident.

```
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

## 3. The Preload List

HSTS still trusts the very first visit. The browser-maintained preload list hard-codes your domain as HTTPS-only inside the browser, so even that first request never leaves in plaintext. Submit at hstspreload.org with `preload` in the header; removal takes months, so treat it as one-way.

```
Strict-Transport-Security: max-age=63072000; includeSubDomains; preload
```
