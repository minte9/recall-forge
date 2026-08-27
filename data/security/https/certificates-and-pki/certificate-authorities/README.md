# Security / HTTPS / Certificates and PKI / Certificate Authorities

## 1. Validation Levels

Domain Validation (DV) proves only control of the domain -- an ACME challenge -- and issues in seconds. Organisation Validation (OV) and Extended Validation (EV) add vetting of the legal entity. Browsers no longer show a distinct EV indicator, so for almost everyone DV is enough; OV and EV mainly satisfy internal policy.

```
DV  -> control of the domain (automated)
OV  -> + organisation identity checked
EV  -> + extended legal vetting (no UI benefit now)
```

## 2. CAA Records

A `CAA` DNS record names which CAs may issue for your domain. A compliant CA must check it before issuing and refuse if it is not listed, which limits the damage if an attacker tricks some other CA. Add an `iodef` entry to be emailed about violation attempts.

```
example.com.  CAA  0 issue "letsencrypt.org"
example.com.  CAA  0 iodef "mailto:security@example.com"
```

## 3. Building and Verifying the Chain

The client checks each signature from leaf to root, checks validity dates, confirms the requested hostname matches a `SAN`, and checks revocation. It also enforces name constraints and basic constraints -- an intermediate must be marked as a CA. Any failure aborts the connection before application data flows.

```bash
openssl verify -CAfile roots.pem -untrusted chain.pem cert.pem
```
