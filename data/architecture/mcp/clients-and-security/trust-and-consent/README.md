# Architecture / MCP / Clients and Security / Trust and Consent

## 1. A Third-Party Server Is Untrusted Code

Connecting a server gives it a channel into your model's context and,
through tools, into real systems. Treat an unfamiliar server like an
unaudited dependency: review what it does, run it with least privilege,
and prefer official or self-hosted builds for sensitive data.

```
# a connected server can:
#   - inject text into the model's context (tool + resource output)
#   - act on real systems through its tools
# so: pin the version, sandbox it, prefer official / self-hosted
```

## 2. Consent Before Action

The spec puts the host in charge of consent: the user should approve which
servers connect, see tool calls before they run (especially writes), and
be able to inspect data a server requests. "Always allow" is a user
choice, not a default.

```
connect server        -> user approves
tools/call (a write)   -> user sees the args, approves this call
"always allow X"        -> a user opt-in, never the default
```

## 3. Least Privilege

Scope the server's own credentials down to what its tools need -- a
read-only DB role, a fine-grained token, a sandboxed filesystem root
passed via `roots`. A prompt injection can only reach as far as the
server's permissions allow.

```
db server   : read-only role, not owner
fs server   : roots = ["/home/me/project"]  -- nothing above it
api token   : one repo, no delete scope
```

## 4. Auth on HTTP Transports

Remote servers use OAuth 2.1: tokens must be audience-bound to that
server, and a server must not forward a client's token to an upstream API
(the "confused deputy" and token pass-through problems the spec
explicitly forbids).

```
access token audience == this MCP server (not the upstream API)
server MUST NOT replay the client's token to a downstream service
```
