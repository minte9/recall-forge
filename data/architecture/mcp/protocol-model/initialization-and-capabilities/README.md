# Architecture / MCP / Protocol Model / Initialization and Capabilities

## 1. The initialize Handshake

The first message on every connection is an `initialize` request from the
client. The server replies, then the client sends an `initialized`
notification; only after that may normal requests flow. The handshake
exchanges three things: protocol version, capabilities, and
implementation info (name and version of each side).

```json
{"method":"initialize","params":{
  "protocolVersion":"2025-06-18",
  "capabilities":{"roots":{}, "sampling":{}},
  "clientInfo":{"name":"my-ide","version":"1.4.0"}}}
```

## 2. Version Negotiation

The protocol version is a date string. The client proposes one; if the
server does not support it, the server answers with a version it *does*
support. If neither can agree, the client closes the connection. This lets
new and old implementations interoperate predictably.

```json
// client proposes
{"method":"initialize","params":{"protocolVersion":"2025-06-18"}}
// server only has an older one -> answers with that
{"result":{"protocolVersion":"2025-03-26"}}
// client then accepts and continues, or disconnects
```

## 3. Capabilities Gate Features

Each side declares only what it supports. A server advertises some of
`tools`, `resources`, `prompts`, `logging`, `completions`; a client
advertises `roots`, `sampling`, `elicitation`. Neither side may use a
feature the other did not declare -- capabilities are the contract for the
rest of the session.

```json
{"result":{"capabilities":{
  "tools":{"listChanged":true},   // tools offered, with change notices
  "resources":{}                  // resources offered
  // no "prompts" key -> client must not call prompts/list
}}}
```
