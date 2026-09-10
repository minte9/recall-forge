# Architecture / MCP / Building Servers / Server Design

## 1. Tool Granularity

Prefer a few task-shaped tools over many thin API-mirroring ones. One
`find_customer(email)` beats `list_customers` + `filter` + `get_page`,
because the model spends fewer turns and less context assembling the
answer. Expose intent, not the raw endpoint surface.

```
# API-mirroring: model burns 3 turns wiring calls together
list_customers() -> filter(field="email") -> get_customer(id)
# task-shaped: 1 turn
find_customer(email="ada@ex.com")
```

## 2. Results Sized for a Context Window

Trim upstream payloads to what the model needs, paginate long lists, and
summarise where you can. A tool that returns a 5,000-line JSON dump costs
tokens on every subsequent turn and buries the signal.

```
# bad: whole upstream body, re-sent in context on every later turn
return raw_json            # 5,000 lines
# good:
return "42 matches. Top 5: ...   (call again with page=2 for more)"
```

## 3. Stateless and Idempotent Where Possible

Do not lean on hidden state between calls -- the model may retry, reorder,
or call in parallel. Make reads idempotent and give writes a natural key
or client-supplied token so a repeat call does not double-create.

```
create_invoice(idempotency_key="ord-42")   # a repeat = the same invoice
get_invoice(id=42)                          # always safe to call again
```

## 4. Descriptions Are Prompt Engineering

The tool `description` and each parameter description are the only
instructions the model gets. Say what the tool is for, when to use it,
what the arguments mean, and any limits. Vague descriptions cause wrong or
skipped calls.

```json
{"name":"send_email",
 "description":"Send ONE email after the user confirms. Not for bulk.",
 "inputSchema":{"type":"object","properties":{
   "to":{"type":"string","description":"A single recipient address"}},
   "required":["to"]}}
```
