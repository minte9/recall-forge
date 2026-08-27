# Security / HTTPS / TLS Protocol / TLS 1.2 vs TLS 1.3

## 1. What TLS 1.3 Removed

TLS 1.3 deleted every option that had become a foot-gun: RSA key exchange (no forward secrecy), static Diffie-Hellman, CBC-mode ciphers, RC4, MD5/SHA-1 signatures, compression, and renegotiation. Only AEAD suites and ephemeral key exchange remain, so a badly configured 1.3 server is much harder to produce than a 1.2 one.

```
TLS 1.2: 300+ cipher suites, many unsafe
TLS 1.3: 5 suites, all AEAD + forward secrecy
```

## 2. One-RTT and Zero-RTT Handshakes

TLS 1.2 needs two round trips before application data flows. TLS 1.3 folds key exchange into the `ClientHello` -- the client guesses the group -- so data can be sent after one round trip. With a prior session, 0-RTT sends data in the first flight: fast, but replayable, so it must carry only idempotent requests.

```
1.2: 2-RTT   1.3: 1-RTT   1.3 resumed: 0-RTT (replay risk)
```

## 3. Downgrade Protection

An attacker who tampers with the `ClientHello` could force both sides to a weaker protocol. A TLS 1.3 server that answers with 1.2 writes a known sentinel into the `ServerHello` random; a genuine 1.3 client that sees it aborts. The Finished-message MAC also covers the entire handshake transcript.

```
ServerHello.random tail == "DOWNGRD\x01"  -> client aborts
```
