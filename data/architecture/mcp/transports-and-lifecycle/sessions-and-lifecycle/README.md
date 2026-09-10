# Architecture / MCP / Transports and Lifecycle / Sessions and Lifecycle

## 1. Three Phases

A connection moves through **initialization** (the `initialize` exchange
and `initialized` notification), **operation** (normal requests and
notifications, bounded by the negotiated capabilities), and
**shutdown** (the transport closes -- for stdio, the client closes stdin
and the server exits).

```
init      : initialize -> (response) -> notifications/initialized
operation : tools/*, resources/*, prompts/*, notifications, ping
shutdown  : transport closes (stdio: close stdin, server exits)
```

## 2. Progress Notifications

For a long-running call the caller includes a `progressToken`, and the
receiver streams `notifications/progress` referencing that token with
`progress` / `total` values. Progress is advisory UI feedback; it does not
change the eventual response.

```json
{"method":"notifications/progress","params":{
  "progressToken":"abc","progress":40,"total":100}}
```

## 3. Cancellation

Either side can abandon an in-flight request by sending
`notifications/cancelled` with its `requestId`. The receiver stops work
and should not send the response. Races are expected -- a response and a
cancellation can cross on the wire.

```json
--> {"id":9,"method":"tools/call","params":{}}
--> {"method":"notifications/cancelled","params":{"requestId":9}}
// receiver drops the work; a response already sent for id 9 may still land
```

## 4. Ping

Either side may send a `ping` request; the peer must reply promptly. It is
a liveness check for idle connections, letting a client drop a server that
has silently died.

```json
--> {"jsonrpc":"2.0","id":"p1","method":"ping"}
<-- {"jsonrpc":"2.0","id":"p1","result":{}}   // no timely reply -> drop
```
