# Security / HTTPS / Certificates and PKI / X.509 Certificates

## 1. Certificate Fields

An X.509 certificate binds a public key to an identity. Key fields: `Subject` (who), `Issuer` (which CA signed it), `Not Before` / `Not After` (validity window), `Subject Alternative Name` (the DNS names it covers -- browsers ignore `Common Name` now), `Key Usage`, and the CA's signature over all of it.

```bash
openssl x509 -in cert.pem -noout -text | \
  grep -E "Subject:|Issuer:|Not (Before|After)|DNS:"
```

## 2. Chain of Trust and Intermediates

A leaf certificate is signed by an intermediate CA, which is signed by a root CA whose public key ships in the OS or browser trust store. The server must send the leaf plus every intermediate; the client walks up until it reaches a trusted root. A missing intermediate is the classic "works in Chrome, fails in curl" bug.

```
root CA (in trust store)
  \- intermediate CA (sent by server)
       \- leaf cert for example.com (sent by server)
```

## 3. Self-Signed vs CA-Issued

A self-signed certificate is its own issuer: it encrypts traffic but proves no identity, so clients reject it unless it is trusted by hand. Fine for local development or an internal service with its own private CA; never for a public site. Public trust needs a certificate from a CA in the browser root program.

```bash
# self-signed: quick, but no identity guarantee
openssl req -x509 -newkey rsa:2048 -days 365 -nodes \
  -keyout key.pem -out cert.pem
```
