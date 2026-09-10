# Architecture / MCP / Primitives / Tools

## 1. Model-Controlled Actions

A tool is an operation the *model* chooses to invoke -- a function call
exposed over MCP. The server lists its tools via `tools/list`, and each
tool carries a `name`, a natural-language `description`, and an
`inputSchema` (JSON Schema) describing its arguments. The description is
read by the model, so it is really prompt text.

```json
{"name":"create_issue","description":"Open a GitHub issue in a repo.",
 "inputSchema":{"type":"object",
   "properties":{"repo":{"type":"string"},"title":{"type":"string"}},
   "required":["repo","title"]}}
```

## 2. Calling and Returning

The model triggers `tools/call` with a name and arguments; the server runs
it and returns `content` blocks (text, image, or embedded resource).
Optionally a tool declares an `outputSchema` and returns
`structuredContent` for machine-readable results.

```json
// call
{"method":"tools/call","params":{"name":"get_weather",
 "arguments":{"city":"Oslo"}}}
// result: a list of content blocks
{"result":{"content":[{"type":"text","text":"Oslo: 4C, cloudy"}]}}
```

## 3. Tool Errors Are Data

A tool that fails returns a normal result with `isError: true` and a
message in `content` -- not a JSON-RPC error. This is deliberate: the
model sees the failure and can retry or adjust, instead of the whole call
aborting at the protocol layer.

```json
{"result":{"isError":true,
 "content":[{"type":"text","text":"Repo not found: acme/typo"}]}}
```

## 4. list_changed

If a server's tool set can change at runtime it declares the
`listChanged` capability and sends `notifications/tools/list_changed`. The
client then re-fetches `tools/list`. This is what makes discovery dynamic
rather than fixed at connect time.

```json
// at init:  {"capabilities":{"tools":{"listChanged":true}}}
// later, when the tool set changes:
{"method":"notifications/tools/list_changed"}   // client re-runs tools/list
```
