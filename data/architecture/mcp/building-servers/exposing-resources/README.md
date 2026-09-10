# Architecture / MCP / Building Servers / Exposing Resources

## 1. Design the URI Scheme

Every resource is addressed by a URI, and the scheme is yours to design:
`file:///...`, `db://table/{id}`, `repo://org/name/issues/{n}`. Make it
stable and meaningful -- clients may store URIs and read them again later.

```
file:///abs/path/to/file.md
repo://acme/api/issues/144
db://customers/42
# stable + meaningful: a client may save a URI and re-read it days later
```

## 2. Static List vs. Templates

If the set is small and fixed, return it all from `resources/list`. If it
is open-ended (every row in a table), expose a template via
`resources/templates/list` and let the client build concrete URIs.

```json
{"uriTemplate":"db://customers/{id}","name":"Customer record",
 "mimeType":"application/json"}
```

## 3. Text or Binary

`resources/read` returns contents as either `text` or base64 `blob`, each
with a `mimeType`. Pick the type honestly -- a client decides how to
render or feed the data based on it.

```json
{"result":{"contents":[
  {"uri":"...","mimeType":"text/plain","text":"line 1\nline 2"}]}}
// binary instead:
//  {"uri":"...","mimeType":"image/png","blob":"<base64>"}
```

## 4. Notify on Change

If you declared `subscribe`, track subscribed URIs and emit
`notifications/resources/updated` when their contents change. If the set
of resources itself changes, emit `notifications/resources/list_changed`.
Skipping these silently gives clients stale context.

```json
// a subscribed URI's contents changed:
{"method":"notifications/resources/updated","params":{"uri":"..."}}
// a resource was added or removed from the list:
{"method":"notifications/resources/list_changed"}
```
