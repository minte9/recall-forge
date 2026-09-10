# Architecture / MCP / Primitives / Prompts

## 1. User-Controlled Templates

A prompt is a reusable message template the server offers -- "summarise
this PR", "write a commit message" -- surfaced in the host UI as something
the *user* explicitly picks (a slash command, a menu item). That is the
third control axis: tools for the model, resources for the app, prompts
for the user.

```
# host renders server prompts as explicit user actions:
/review_pr  ->  prompts/get {"name":"review_pr","arguments":{}}
# the model never auto-invokes these
```

## 2. Arguments and prompts/get

`prompts/list` returns each prompt's `name`, `description`, and declared
`arguments`. `prompts/get` takes the argument values and returns a
concrete list of messages ready to send to the model -- the server does
the templating and may inline resource content.

```json
{"name":"review_pr","arguments":[
  {"name":"pr_number","description":"PR to review","required":true}]}
```

## 3. Why Server-Side Templates

Keeping the prompt on the server means the wording, the injected context,
and the argument list are versioned and updated with the integration, not
copy-pasted into each host. The host just renders the choice and forwards
the filled-in messages.

```
prompts/get "review_pr" {pr_number: 42} returns ready-to-send messages:
  system: "You review pull requests for <this project>."
  user:   "<diff of PR 42, inlined from a resource>"
# wording + context live with the server, not in each host
```
