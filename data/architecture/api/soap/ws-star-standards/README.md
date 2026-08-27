# Architecture / API / SOAP / WS-* Standards

## 1. WS-Security

WS-Security protects the message itself, not just the channel: it puts signatures, encryption, and tokens (username, X.509, SAML) in the SOAP header. Because protection lives in the message, it survives intermediaries and non-HTTP hops -- something TLS, which only secures one point-to-point link, cannot do.

```xml
<wsse:Security>
  <wsse:UsernameToken>
    <wsse:Username>svc</wsse:Username>
    <wsse:Password Type="...#PasswordDigest">...</wsse:Password>
  </wsse:UsernameToken>
</wsse:Security>
```

## 2. WS-ReliableMessaging & Transactions

WS-ReliableMessaging guarantees delivery semantics (at-least-once, at-most-once, exactly-once, in-order) by numbering messages and acknowledging them, so a lost message is retransmitted automatically. WS-AtomicTransaction lets a call join a distributed two-phase commit, so work across services either all commits or all rolls back.

```
Sequence: msg 1,2,3  ->  receiver ACKs 1,3  ->  sender resends 2
```

## 3. MTOM Attachments

Base64-encoding a binary blob inside XML inflates it by about a third. MTOM (Message Transmission Optimization Mechanism) instead sends the bytes as a raw MIME part alongside the envelope, referenced by an `xop:Include` link. Large documents and images transfer at full efficiency while the message stays schema-valid.

```xml
<doc:content>
  <xop:Include href="cid:invoice.pdf"/>  <!-- raw bytes, MIME part -->
</doc:content>
```
