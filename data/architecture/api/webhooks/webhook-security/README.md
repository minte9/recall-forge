# Architecture / API / Webhooks / Webhook Security

## 1. HMAC Signature Verification

Your endpoint is public, so anyone can POST to it. The provider signs each body with a shared secret (HMAC-SHA256) and sends the digest in a header; you recompute it over the raw bytes and compare with a constant-time check. Reject on mismatch before parsing the body.

```java
var mac = hmacSha256(secret, rawBody);
if (!constantTimeEquals(mac, header("X-Signature"))) return status(401);
```

## 2. Replay Protection & Timestamps

A valid signed request captured off the wire can be resent. Providers include a signed timestamp; reject deliveries older than a few minutes and, for sensitive actions, remember recently seen event ids so a replay inside that window is still caught.

```java
if (now() - signedTimestamp > Duration.ofMinutes(5)) return status(400);
```

## 3. Secret Rotation & IP Allowlists

Support two valid signing secrets at once so you can rotate without downtime: accept either, cut over, then retire the old one. Where the provider publishes source IP ranges, allowlist them as defence in depth -- but never as the only check, since IPs change and can be spoofed upstream.

```java
// accept either secret during a rotation window
verify(body, List.of(secretCurrent, secretPrevious));
```
