# Security / HTTPS / TLS Protocol / TLS Handshake

## 1. ClientHello and ServerHello Negotiation

The handshake opens with the client sending a `ClientHello` listing its TLS versions, cipher suites, and extensions (SNI, ALPN, supported groups). The server answers with a `ServerHello` picking one version and one suite, then its certificate. Everything agreed on is chosen from what the client offered, so client capabilities cap the security of the connection.

```
ClientHello  -> versions, cipher suites, key_share, SNI, ALPN
ServerHello  <- chosen version + suite, server key_share
```

## 2. ECDHE Key Exchange and Forward Secrecy

Modern TLS derives the session key with ephemeral Elliptic Curve Diffie-Hellman: each side sends a fresh public value and combines it with its own private one to compute the same shared secret, which never crosses the wire. Because the private values are discarded after the handshake, a stolen server key cannot decrypt past traffic -- that property is forward secrecy.

```
client_secret = ECDH(client_priv, server_pub)
server_secret = ECDH(server_priv, client_pub)   // identical value
```

## 3. Session Resumption

A full handshake costs a round trip and asymmetric crypto. Resumption lets a returning client skip most of it: TLS 1.2 uses session IDs or encrypted session tickets, TLS 1.3 uses a pre-shared key derived from the previous session. Tickets must rotate and expire, or a stolen ticket key weakens forward secrecy.

```
NewSessionTicket  -> opaque blob the client stores
next visit: ClientHello { pre_shared_key: ticket }
```
