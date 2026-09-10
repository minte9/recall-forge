# Architecture / MCP / Why MCP / Why It Helps

## 1. Why a Raw HTTP API Is a Poor Fit for an LLM

An HTTP API assumes a developer read the docs at build time: endpoints are
hardcoded, responses are verbose JSON meant for parsing, auth is
per-request, and each call is stateless. An LLM has none of that context
at runtime -- it needs to discover what is callable, get results small
enough to reason over, and keep state across steps.

```
# What a developer bakes in at build time -- and an LLM lacks at runtime:
POST /v2/orders             # which path + verb
Authorization: Bearer ...   # auth wiring
{ "items": [ ... ] }        # the exact request schema
# ...plus 80-field responses written for code, not a context window
```

## 2. A Server Shapes the Response

An MCP server is a thin adapter in front of the real API. It calls the
API, then trims the payload to the fields that matter, labels them, and
returns a short, model-readable result -- so the model gets an answer, not
200 lines of JSON to wade through.

```
GET /users/42  -> {id, name, email, created, prefs, roles, ...40 more}
tool get_user  -> "User 42: Ada Lovelace <ada@ex.com>, admin, joined 2019"
```

## 3. Secrets Stay Server-Side

API keys and tokens live in the MCP server process. The model sees tool
names, descriptions, and results; it never sees credentials, and a prompt
injection cannot exfiltrate what was never in the context.

```
# In the server process env -- never sent to the model:
GITHUB_TOKEN=ghp_xxxxxxxx
# All the model ever sees:
tool "create_issue"  ->  result: "Opened issue #123"
```

## 4. Swap Models Without Touching Integrations

The server is model-agnostic -- it speaks MCP, not "Claude" or "GPT".
Change the model behind the host and every connected server keeps working
unchanged, because the integration contract is the protocol, not the
model.

```
host.model = "claude"  ->  same tools/list, same tools/call
host.model = "gpt"     ->  same tools/list, same tools/call
# the server implements MCP, not any model vendor's SDK
```
