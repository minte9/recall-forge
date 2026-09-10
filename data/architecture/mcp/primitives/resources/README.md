# Architecture / MCP / Primitives / Resources

## 1. Application-Controlled Context

A resource is a piece of data the server can provide -- a file, a database
row, a config blob -- identified by a URI. Resources are
**application-controlled**: the host decides what to pull into context and
when, rather than the model calling them like tools. `resources/list`
enumerates them; `resources/read` returns the contents.

```json
{"uri":"file:///project/README.md","name":"README",
 "mimeType":"text/markdown"}
```

## 2. Templates for Parametrised URIs

When resources are not a fixed set, the server exposes URI templates
(RFC 6570) via `resources/templates/list`, e.g.
`db://customers/{id}`. The client fills in the variables to construct a
concrete URI to read.

```json
{"result":{"resourceTemplates":[
  {"uriTemplate":"db://customers/{id}","name":"Customer record"}]}}
// client fills {id} -> resources/read {"uri":"db://customers/42"}
```

## 3. Subscriptions

A client may `resources/subscribe` to a URI and receive
`notifications/resources/updated` when its contents change, plus
`notifications/resources/list_changed` when resources appear or
disappear. This keeps long-lived context fresh without polling.

```json
--> {"method":"resources/subscribe","params":{"uri":"file:///log.txt"}}
<-- {"method":"notifications/resources/updated",
     "params":{"uri":"file:///log.txt"}}   // re-read to get new contents
```

## 4. Tool vs. Resource

Reach for a **resource** when the host or user picks what to load
(read-only, no side effects); reach for a **tool** when the model should
decide and act. The same underlying data is often exposed both ways.

```
resources/read  db://customers/42    # host/user chose it; read-only
tools/call      refund(order=42)     # model chose it; has a side effect
```
