# Architecture / MCP / Protocol Model / JSON-RPC Messaging

## 1. Three Message Shapes

MCP is JSON-RPC 2.0 over a persistent stream. Every message is a
**request** (has an `id` and a `method`, expects a reply), a **response**
(carries the same `id` plus `result` or `error`), or a **notification**
(has a `method` but no `id`, and gets no reply). Notifications are how
either side signals events like "the tool list changed".

```json
{"jsonrpc":"2.0","id":1,"method":"tools/call",
 "params":{"name":"search","arguments":{"q":"mcp"}}}
```

## 2. Correlation by id

Requests and responses are matched only by `id`, so both sides may have
several requests in flight on the same connection at once. An `id` must be
unique within a session and is never reused. This is why MCP feels like
RPC on an open socket, not a sequence of HTTP calls.

```json
--> {"jsonrpc":"2.0","id":1,"method":"tools/list"}
--> {"jsonrpc":"2.0","id":2,"method":"resources/list"}
<-- {"jsonrpc":"2.0","id":2,"result":{}}   // may come back before id 1
<-- {"jsonrpc":"2.0","id":1,"result":{}}   // paired by id, not arrival
```

## 3. Bidirectional by Design

After startup, client and server are peers: a server can call back into
the client (for sampling or elicitation) using the same request shape. The
connection is symmetric at the JSON-RPC layer; only the available
`method`s differ by direction.

```json
// client -> server
{"id":5,"method":"tools/call","params":{"name":"summarize"}}
// server -> client, same shape, while still handling id 5:
{"id":"s1","method":"sampling/createMessage","params":{}}
```
