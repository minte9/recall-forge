# Security / HTTPS / TLS Protocol / Cipher Suites

## 1. Anatomy of a Cipher Suite

A TLS 1.2 suite name spells out every choice: key exchange, authentication, bulk cipher, MAC. `ECDHE_RSA_WITH_AES_128_GCM_SHA256` means ECDHE key exchange, RSA certificate auth, AES-128-GCM for data, SHA-256 for the handshake PRF. TLS 1.3 shortens this to just the bulk cipher and hash, since kex and auth are negotiated separately.

```
TLS 1.2: ECDHE_RSA_WITH_AES_128_GCM_SHA256
TLS 1.3: TLS_AES_128_GCM_SHA256
```

## 2. AEAD Ciphers

Authenticated Encryption with Associated Data combines confidentiality and integrity in one primitive, so there is no separate MAC step to get wrong -- the old encrypt-then-MAC ordering bugs simply cannot occur. AES-GCM is standard where hardware acceleration exists; ChaCha20-Poly1305 is faster on phones and other CPUs without AES instructions.

```
AES-128-GCM              // fast with AES-NI hardware
ChaCha20-Poly1305        // faster on mobile / no AES-NI
```

## 3. Choosing and Ordering Suites

Offer only AEAD suites with forward secrecy (ECDHE), disable anything with RC4, 3DES, CBC, or static RSA, and let the server's preference order win. Mozilla's SSL Configuration Generator emits "modern" and "intermediate" profiles; pick intermediate unless you can drop every pre-1.2 client.

```
ssl_protocols TLSv1.2 TLSv1.3;
ssl_prefer_server_ciphers on;
```
