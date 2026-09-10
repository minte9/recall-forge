# Architecture / MCP / Transports and Lifecycle / Errors and Timeouts

## 1. Protocol Errors vs. Tool Errors

A JSON-RPC `error` response means the *request itself* was bad or
unhandleable -- wrong method, malformed params, server fault. A tool that
ran but failed does **not** use this; it returns a normal result with
`isError: true` so the model can see and react. Keep the two layers
separate.

```json
{"jsonrpc":"2.0","id":7,
 "error":{"code":-32602,"message":"Invalid params: 'repo' required"}}
```

## 2. Standard Codes

The JSON-RPC set applies: `-32700` parse error, `-32600` invalid request,
`-32601` method not found, `-32602` invalid params, `-32603` internal
error. Servers may add application codes outside the reserved range.

```
-32700 parse error        -32601 method not found
-32600 invalid request    -32602 invalid params
-32603 internal error
# -32000..-32099 are reserved; app-specific codes go outside them
```

## 3. Timeouts

The caller sets its own per-request timeout and issues
`notifications/cancelled` when it fires. A `progress` notification may be
allowed to reset the timer, but a maximum timeout should still be enforced
so a stuck server cannot hang the session forever.

```
send request            -> start timer (e.g. 30s)
  progress notification  -> optionally reset the timer
  timer expires          -> send notifications/cancelled, give up
# keep a hard ceiling even while progress keeps arriving
```

## 4. Reconnection

stdio: a crashed server is re-spawned and re-initialised from scratch --
no session to restore. Streamable HTTP: the client reconnects with its
`Mcp-Session-Id` and resumes the SSE stream from the last event ID;
in-flight requests may need replaying.

```
stdio : server exits -> re-spawn -> full initialize again (no state)
HTTP  : reconnect with Mcp-Session-Id + Last-Event-ID -> resume stream
```
