# Architecture / API / SOAP / SOAP Message Structure

## 1. Envelope, Header & Body

Every SOAP message is an XML `Envelope` with an optional `Header` and a mandatory `Body`. The header carries cross-cutting metadata -- security tokens, routing, transactions -- often flagged `mustUnderstand`; the body carries the actual call or its response. The structure is transport-neutral: the same envelope rides HTTP, JMS, or SMTP.

```xml
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
  <soap:Header><wsse:Security>...</wsse:Security></soap:Header>
  <soap:Body><getOrder><id>42</id></getOrder></soap:Body>
</soap:Envelope>
```

## 2. SOAP Faults

Errors travel as a structured `Fault` element inside the body, not via HTTP status codes. It has a `Code` (sender vs receiver fault), a human-readable `Reason`, and an optional `Detail` for application data. Because faults are part of the contract, clients parse them uniformly regardless of transport.

```xml
<soap:Fault>
  <soap:Code><soap:Value>soap:Sender</soap:Value></soap:Code>
  <soap:Reason><soap:Text>Unknown order id</soap:Text></soap:Reason>
</soap:Fault>
```

## 3. SOAP vs REST Trade-offs

SOAP gives a strict, tooling-generated contract plus built-in standards for security, reliable delivery, and transactions -- valuable in enterprise and B2B integration. REST is lighter, cache-friendly, and browser-native. Choose SOAP when a formal WSDL contract or WS-* guarantees are required; choose REST for public web and mobile APIs.

```
SOAP: WS-Security, WS-AtomicTransaction, WSDL codegen, verbose XML
REST: HTTP caching, simple JSON, no built-in transaction story
```
