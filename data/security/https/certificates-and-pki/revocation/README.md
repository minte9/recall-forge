# Security / HTTPS / Certificates and PKI / Revocation

## 1. Certificate Revocation Lists

A CRL is a signed list of serial numbers the CA has revoked -- key compromise, superseded, CA error. Clients fetch it from the URL named in the certificate. CRLs grow large and are cached, so a revocation can take hours to propagate, which is why browsers stopped relying on them alone.

```bash
openssl x509 -in cert.pem -noout -text | grep -A2 "CRL Distribution"
```

## 2. OCSP and OCSP Stapling

The Online Certificate Status Protocol asks the CA about one certificate in real time, but that leaks browsing history to the CA and adds latency. OCSP stapling fixes both: the server fetches a short-lived signed "good" response itself and attaches it to the handshake, so the client never contacts the CA.

```
ssl_stapling on;
ssl_stapling_verify on;
```

## 3. Short-Lived Certificates

If a certificate lives only 7 to 90 days, a compromised key is exposed for a bounded window and revocation matters far less. This is the direction the ecosystem is taking -- Let's Encrypt is 90 days and piloting shorter. It only works with fully automated issuance and renewal.

```
90-day cert + auto-renew at day 60  ->  revocation rarely needed
```
