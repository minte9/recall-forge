# Architecture / MCP / Why MCP / What MCP Is

## 1. One Standard for Model Context

MCP (Model Context Protocol) is an open standard for connecting an LLM
application to external tools and data through a single, uniform
interface. Instead of writing a bespoke integration per app and per
service, a service exposes one MCP server and every MCP-aware app can use
it. The value is the same as any protocol: N + M integrations instead of
N x M.

```
# Without MCP: each app <-> each service is a custom integration
3 apps x 4 services = 12 integrations to build and maintain
# With MCP: each side speaks the protocol once
3 apps + 4 servers = 7  -- any app now works with any server
```

## 2. Not an Intelligence Layer

MCP does not run the model, improve its reasoning, or decide which tool to
call -- the host application and the model still do that. MCP only
standardises how capabilities are *described* to the model and how calls
and results travel between the app and the service.

```
MCP defines:  discovery, message shapes, transport, lifecycle
MCP ignores:  prompting, planning, model choice, orchestration
```

## 3. Client-Server, Not Request-Response

MCP is a stateful session between a client and a server over a persistent
connection, closer to gRPC or the Language Server Protocol than to REST.
Both sides can send messages at any time, so a server can push a
notification (a tool list changed, a resource updated) without being
polled.

```
REST:  client calls GET /things repeatedly to notice changes (polling)
MCP :  one open connection; server pushes, unprompted:
       {"method":"notifications/tools/list_changed"}
```
