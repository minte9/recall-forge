# Security / HTTPS / Deploying HTTPS / Mixed Content and Misconfiguration

## 1. Active vs Passive Mixed Content

A page loaded over HTTPS that pulls sub-resources over HTTP is "mixed content". Browsers block active content (scripts, iframes, fetch/XHR) outright because it can rewrite the page, and increasingly block passive content (images, media) too. `upgrade-insecure-requests` in the CSP rewrites such URLs to HTTPS automatically.

```
Content-Security-Policy: upgrade-insecure-requests
```

## 2. SNI and Virtual Hosts

Server Name Indication carries the target hostname in the `ClientHello` so one IP can serve many certificates. If no virtual host matches, the server falls back to a default certificate and the client shows a name-mismatch error. Every hostname you serve needs a certificate whose `SAN` lists it.

```bash
openssl s_client -connect example.com:443 -servername example.com
```

## 3. Weak Protocol and Cipher Configuration

Leaving SSLv3, TLS 1.0/1.1, RC4, 3DES, or export ciphers enabled is the concrete form of OWASP's "Cryptographic Failures". Disable them, allow TLS 1.2 and 1.3 only, enable OCSP stapling, and re-test after every server upgrade with a scanner such as `testssl.sh` or SSL Labs.

```bash
testssl.sh --severity HIGH https://example.com
```
