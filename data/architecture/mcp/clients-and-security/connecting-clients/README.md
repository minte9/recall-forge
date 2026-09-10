# Architecture / MCP / Clients and Security / Connecting Clients

## 1. One Client per Server

The host spins up a separate client for each configured server, each with
its own connection, capability set, and lifecycle. Servers do not know
about each other; the host is the only place their capabilities are
combined.

```
host
 |- client 1  <->  filesystem server   (own conn, own capabilities)
 |- client 2  <->  github server
 |- client 3  <->  postgres server
# servers never talk to each other
```

## 2. Discovery at Connect Time

After `initialize`, the client calls `tools/list`, `resources/list`, and
`prompts/list` to learn what each server offers, then re-fetches on the
matching `list_changed` notification. The model's available tool set is
the union across all connected servers, assembled by the host.

```
per server, after initialize:
  tools/list  ->  resources/list  ->  prompts/list
on notifications/*/list_changed  ->  re-fetch that list
model's tools = union over all servers (built by the host)
```

## 3. Namespacing and Collisions

Two servers can both expose a tool named `search`. The host must
disambiguate -- typically by prefixing the server name
(`github.search`, `docs.search`) -- before presenting tools to the model,
or one silently shadows the other.

```
server "github" : tool "search"
server "docs"   : tool "search"
host presents   : github.search   docs.search
```

## 4. Aggregation Is the Host's Job

Merging tool lists, routing a `tools/call` to the right client, fanning
`ping` out to all servers, surfacing prompts in the UI -- none of this is
in the protocol. MCP defines one client-server link; orchestrating many
is the host application's responsibility.

```
protocol defines : ONE client  <->  ONE server
host must add     : merge lists, route each call, fan ping,
                    render prompts, apply per-server trust
```
