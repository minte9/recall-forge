# Architecture / MCP / Transports and Lifecycle / Transports

## 1. stdio

The client launches the server as a subprocess and speaks JSON-RPC over
its stdin and stdout, one JSON message per line. stderr is free for
logging. It is the simplest transport and the default for local servers:
no ports, no auth, process lifetime bounded by the client.

```
client --(stdin: JSON-RPC, newline-delimited)-->  server process
client <--(stdout: JSON-RPC)---------------------  server process
       <--(stderr: free-form logs)---------------
```

## 2. Streamable HTTP

For remote servers, the client POSTs JSON-RPC messages to a single HTTP
endpoint. The server may reply with one JSON response or upgrade to an
SSE stream for multiple messages (progress, server-initiated requests).
This replaced the older two-endpoint HTTP+SSE design.

```
POST /mcp   {"jsonrpc":"2.0","id":1,"method":"tools/call", ...}
# server answers with either:
Content-Type: application/json      -> a single response
Content-Type: text/event-stream    -> SSE: progress... then response
```

## 3. Sessions over HTTP

On connect the server may issue an `Mcp-Session-Id` header that the client
echoes on every later request, tying stateless HTTP calls to one logical
session. SSE streams carry event IDs so a dropped connection can resume
without losing messages.

```
<-- (init response)   Mcp-Session-Id: 7f3c9a...
--> (every later req)  Mcp-Session-Id: 7f3c9a...
# SSE frames include "id: 42" -> reconnect with Last-Event-ID: 42
```

## 4. Choosing One

Local integration, same machine, no network exposure -> stdio. Shared
service, multiple users, remote host -> Streamable HTTP with proper auth.
The primitives and message shapes are identical either way.

```
stdio            : local, same machine, subprocess, no auth
Streamable HTTP  : remote, multi-user, OAuth 2.1
# same tools/resources/prompts and message shapes on both
```
