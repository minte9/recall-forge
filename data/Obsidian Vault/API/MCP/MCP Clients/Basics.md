## 1. Connecting to MCP servers

An MCP client is the component responsible for communicating with an MCP server using the Model Context Protocol. The server exposes capabilities such as tools, resources, and prompts, while the client discovers and consumes them. The client can request tool execution, read resources, and retrieve prompts without needing to know the server's internal implementation.

MCP supports different transports for communication between clients and servers. The two main transports are stdio, where the client launches a local server process and communicates through standard input/output, and Streamable HTTP, where the client connects to an HTTP endpoint. The transport changes how messages travel, but not the core MCP operations.

For example, your Spring Boot MCP server can expose tools over HTTP at `http://localhost:8080/mcp`. A separate Java application can connect to that endpoint, discover tools, and execute them. This is different from calling a traditional REST endpoint such as `/api/prices`, because the MCP client uses standardized protocol methods such as `tools/list` and `tools/call`.

Example conceptual configuration:

```yml
spring:
  ai:
    mcp:
      client:
        type: SYNC
        streamable-http:
          connections:
            pricing-server:
              url: http://localhost:8080
              endpoint: /mcp
```

This assumes the Spring AI MCP client starter is installed and the server exposes Streamable HTTP at `/mcp`.

## 2. Initialization and negotiation

Before a client starts using an MCP server, both sides perform an initialization handshake. The client sends an `initialize` request containing its supported protocol version, client information, and capabilities. The server responds with the protocol version it will use, server information, and its own capabilities. The client then sends a `notifications/initialized` notification to indicate that initialization has completed.

A simplified JSON-RPC initialization request looks like this:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {
    "protocolVersion": "2025-06-18",
    "capabilities": {},
    "clientInfo": {
      "name": "java-mcp-client",
      "version": "1.0.0"
    }
  }
}
```

Example server response:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "protocolVersion": "2025-06-18",
    "capabilities": {
      "tools": {
        "listChanged": true
      },
      "resources": {},
      "prompts": {}
    },
    "serverInfo": {
      "name": "pricing-mcp-server",
      "version": "1.0.0"
    }
  }
}
```

The protocol version above is illustrative; actual client and server versions must be compatible. The response tells the client that the server supports tools, resources, and prompts. The `listChanged` property indicates that the server may notify the client when its tool list changes.

## 3. Discovering server capabilities

After initialization, the client can discover which operations the server supports. For tools, it sends `tools/list`; for resources, `resources/list`; and for prompts, `prompts/list`. These requests allow the client to discover capabilities dynamically instead of hardcoding individual tool names or resource identifiers.

For example, suppose your server exposes the `add` tool. The client can discover its name, description, and input schema through the following request:

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/list"
}
```

Example response:

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "tools": [
      {
        "name": "add",
        "description": "Add two numbers",
        "inputSchema": {
          "type": "object",
          "properties": {
            "a": {
              "type": "number"
            },
            "b": {
              "type": "number"
            }
          },
          "required": ["a", "b"]
        }
      }
    ]
  }
}
```

The client now knows that `add` accepts two numeric arguments. It can construct a valid request without knowing the Java class or method implementing the tool.

Note that declaring support for tools, resources, or prompts during initialization does not automatically retrieve their definitions. The corresponding discovery methods are still necessary, and returned lists may be paginated.

In Spring AI, much of this communication is handled by the MCP SDK and its auto-configuration. You generally do not need to construct JSON-RPC messages manually.

