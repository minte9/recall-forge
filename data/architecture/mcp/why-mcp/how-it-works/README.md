# Architecture / MCP / Why MCP / How It Works

## 1. Host, Client, Server

The **host** is the LLM application (an IDE, a desktop assistant); it owns
the model, the UI, and user consent. Inside it, one **client** is created
per connection and maintains a 1:1 link to one **server**. The **server**
wraps a real system (files, a database, an API) and exposes its
capabilities. One host runs many clients, each bound to its own server.

```
        +--------- Host ---------+
        |  client A --- Server A (filesystem)
Model --|  client B --- Server B (GitHub)
        |  client C --- Server C (Postgres)
        +------------------------+
```

## 2. The Six Steps of a Request

(1) The user asks a question. (2) The host sends the question plus the
combined tool list to the model. (3) The model replies that it wants to
call a tool. (4) The client forwards the call to the server. (5) The
server hits the real system and returns a shaped result. (6) The host
gives the result back to the model, which writes the final answer.

```
1  user:   "any open bugs?"
2  host  -> model:   question + tools [list_issues, ...]
3  model -> host:    call list_issues{state:"open"}
4  host  -> server:  tools/call list_issues
5  server-> host:    "3 open: #12 login, #14 export, #19 css"
6  host  -> model -> user:   final answer
```

## 3. Who Builds Servers

Service vendors publish official servers; the community maintains many
open-source ones. You build your own when the system is internal or
proprietary, or when you need to shape responses and combine calls in a
way no generic server does.

```
official   : the service vendor ships and maintains it
community  : open-source repo, third-party maintained
your own   : internal/proprietary system, or custom result shaping
```
