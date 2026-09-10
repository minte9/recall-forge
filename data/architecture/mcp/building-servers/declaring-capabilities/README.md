# Architecture / MCP / Building Servers / Declaring Capabilities

## 1. Advertise Only What You Implement

In the `initialize` response the server lists the primitive groups it
supports and their sub-features. Declaring `tools.listChanged` commits you
to actually sending that notification; a client may rely on it. Under-
declare rather than over-declare.

```json
{"capabilities":{
  "tools":{"listChanged":true},
  "resources":{"subscribe":true,"listChanged":false}}}
```

## 2. A Tool Definition Is a Contract

Each tool needs a stable `name`, a `description` written for the model,
and a JSON Schema `inputSchema` with `required` fields marked. If the
result should be machine-parsable, add an `outputSchema` and return
`structuredContent` that validates against it.

```json
{"name":"refund_order",
 "description":"Refund a paid order. Use only after the user confirms.",
 "inputSchema":{"type":"object",
   "properties":{"order_id":{"type":"string"}},
   "required":["order_id"]}}
```

## 3. Return Content Blocks

`tools/call` results are a list of typed blocks -- `text`, `image`,
`audio`, or `resource` (embedded resource contents). Most tools return one
short `text` block. Keep it concise: this text goes straight into the
model's context.

```json
{"result":{"content":[
  {"type":"text","text":"Refunded order 42: $19.90"}]}}
```

## 4. Signal Failure Correctly

Bad arguments the schema should have caught -> JSON-RPC `-32602`. The tool
ran and the operation failed (404 from the upstream API, permission
denied) -> result with `isError: true` and a message the model can act
on.

```json
// client sent a schema-invalid call:
{"error":{"code":-32602,"message":"order_id is required"}}
// tool ran, upstream returned 404:
{"result":{"isError":true,
  "content":[{"type":"text","text":"No order with id 42"}]}}
```
