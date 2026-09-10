# Architecture / MCP / Primitives / Sampling and Elicitation

## 1. Sampling: the Server Asks for a Completion

With `sampling/createMessage` a server asks the *client* to run an LLM
call on its behalf. This lets a server be agentic -- summarise, classify,
decide -- without shipping its own model API key or provider account. The
client owns the model and the billing.

```json
{"method":"sampling/createMessage","params":{
  "messages":[{"role":"user","content":{"type":"text",
    "text":"Classify this log line as INFO/WARN/ERROR"}}],
  "maxTokens":50}}
```

## 2. Human in the Loop

Sampling is a capability the client opts into, and the spec expects the
client to show the request for approval and let the user see or edit the
prompt and the response. A server cannot silently drive the model.

```
server -> sampling/createMessage
client :  [show prompt to user]  approve / edit / deny
          -> only then call the model, then let the user vet the reply
```

## 3. Elicitation: the Server Asks the User

`elicitation/create` lets a server request structured input mid-task -- a
missing parameter, a confirmation, a choice -- with a JSON Schema for the
expected shape. The client renders a form and returns the values, so a
server does not have to fail just because one field was unknown.

```json
{"method":"elicitation/create","params":{
  "message":"Which environment should I deploy to?",
  "requestedSchema":{"type":"object","properties":{
    "env":{"type":"string","enum":["dev","prod"]}}}}}
// client shows a form -> returns {"env":"prod"}
```
