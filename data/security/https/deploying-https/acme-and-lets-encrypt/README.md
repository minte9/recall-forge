# Security / HTTPS / Deploying HTTPS / ACME and Let's Encrypt

## 1. ACME Challenge Types

ACME proves domain control before issuing. `HTTP-01` serves a token file under `/.well-known/acme-challenge/` on port 80. `DNS-01` puts the token in a `TXT` record and is the only way to get a wildcard. `TLS-ALPN-01` answers on port 443 through a special ALPN, useful when port 80 is blocked.

```bash
certbot certonly --webroot -w /var/www/html -d example.com
```

## 2. Automated Renewal

Certificates are short-lived by design, so renewal is a cron or systemd job, not a calendar reminder. `certbot renew` does nothing until a cert is within 30 days of expiry, so it is safe to run twice a day. Reload -- not restart -- the web server afterward to pick up the new file.

```bash
# systemd timer or cron, twice a day
certbot renew --quiet --deploy-hook "systemctl reload nginx"
```

## 3. Wildcards and Rate Limits

A wildcard (`*.example.com`) needs `DNS-01` and matches one label deep only. Let's Encrypt enforces rate limits -- notably 50 certificates per registered domain per week and 5 duplicates per week -- so script against the staging environment and avoid re-issuing on every deploy.

```bash
certbot certonly --dns-cloudflare -d example.com -d "*.example.com"
```
