## 1. Host, Client, and Server

Model Context Protocol uses a client-server architecture consisting of three important components: the Host, MCP Client, and MCP Server. 

For example, suppose your AI assistant needs information from a Spring Boot product service and an infrastructure monitoring service. The Host maintains two MCP client connections. One communicates with a product MCP server, while the other communicates with a monitoring MCP server. The language model can use information from both services when generating its response.

![[mcp_architecture.png]]

Important distinction: The MCP Client does not necessarily contain an AI model. It is a protocol component responsible for communication. The Host manages the model interaction and decides how to make discovered tools available to the model.

### Example: responsibilities in Java

A Spring Boot MCP server could expose a method such as:

```java
@Tool(description = "Get product details by ID")
public Product getProduct(Long id) {
    return productService.getProduct(id);
}
```

The Host might receive:

> What is the price of product 42?

The Host can provide the discovered `getProduct` tool to the model. If the model requests that tool, the Host uses its MCP Client to invoke the MCP Server. The returned product data can then be supplied to the model to generate the answer.

## 2. JSON-RPC Communication

MCP uses JSON-RPC 2.0 as its message format. JSON-RPC is a lightweight remote procedure call protocol that represents requests, responses, errors, and notifications using JSON objects. Unlike REST, where an operation is commonly identified by an HTTP method and URL, JSON-RPC identifies operations through a `method` field.

A typical JSON-RPC request contains `jsonrpc`, `id`, `method`, and optionally `params`. The server responds with a matching `id` and either a `result` or an `error`. Notifications are messages without an `id`, and they do not receive a JSON-RPC response. MCP defines protocol methods such as `initialize`, `tools/list`, and `tools/call` on top of this message format.

### Example: listing available tools

Request from MCP Client:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list",
  "params": {}
}
```

Response from MCP Server:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "tools": [
      {
        "name": "get_product",
        "description": "Get product information by ID",
        "inputSchema": {
          "type": "object",
          "properties": {
            "id": {
              "type": "integer"
            }
          },
          "required": ["id"]
        }
      }
    ]
  }
}
```

The client now knows that the server exposes a tool named `get_product` and that the tool requires an integer parameter named `id`.

### Example: executing a tool

Request:

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "get_product",
    "arguments": {
      "id": 42
    }
  }
}
```

Response:

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "{\"id\":42,\"name\":\"Laptop\",\"price\":1200}"
      }
    ],
    "isError": false
  }
}
```

The MCP Server executes the requested tool and returns the result. Notice that the returned data is wrapped in an MCP result structure, rather than simply returning a REST-style JSON response.

### JSON-RPC error example

If the client invokes an unknown protocol method, the server can return:

```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "error": {
    "code": -32601,
    "message": "Method not found"
  }
}
```

An important distinction is that a tool execution failure can instead be represented as a successful JSON-RPC response containing a tool result with `"isError": true`. This allows the AI application to receive and potentially reason about the tool failure.

## 3. MCP Connection Lifecycle

Before an MCP Client can discover tools or execute operations, it must establish a connection and complete the initialization process. MCP uses an initialization handshake to negotiate the protocol version and exchange supported capabilities. The client and server communicate their respective capabilities so that both sides understand which protocol features are available.

After successful initialization, the client can discover tools, resources, and prompts, and invoke supported operations. The connection eventually ends when the client disconnects, the server terminates, or the underlying transport closes. Initialization is a protocol requirement; tool discovery and execution occur after it.

### Lifecycle diagram

1 - Connection established
The client opens a transport to the MCP server

2 - Initialize request
Client sends protocol version and capabilities

3 - Initialize response
Server returns negotiated version and capabilities

4 - Initialized notification
Client confirms that initialization is complete

5 - Normal operations
Discover tools, read resources, invoke tools

6 - Connection ends
Transport closes or session is terminated

### Example: initialization request

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {
    "protocolVersion": "2025-11-25",
    "capabilities": {},
    "clientInfo": {
      "name": "java-mcp-client",
      "version": "1.0.0"
    }
  }
}
```

### Example: initialization response

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "protocolVersion": "2025-11-25",
    "capabilities": {
      "tools": {
        "listChanged": true
      }
    },
    "serverInfo": {
      "name": "product-mcp-server",
      "version": "1.0.0"
    }
  }
}
```

The server declares that it supports tools and can notify clients when the available tool list changes.

The client then sends:

```json
{
  "jsonrpc": "2.0",
  "method": "notifications/initialized"
}
```

This is a notification, so it does not include an `id`.

The exact protocol version must be supported by both implementations. The examples use the published `2025-11-25` protocol version; a real client and server negotiate their mutually supported version.

## 4. Spring Boot Architecture Example

Consider an existing Spring Boot application that exposes product information through REST. You want to make this information accessible to AI assistants without changing the existing REST API.

A practical architecture is to reuse the existing service layer and expose it through both REST and MCP.
### Java example

First, create a shared service:

```java
@Service
public class ProductService {

    public Product getProduct(Long id) {
        return new Product(id, "Laptop", 1200.0);
    }
}
```

Product DTO:

```java
public record Product(
    Long id,
    String name,
    Double price
) {}
```

REST controller:

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return service.getProduct(id);
    }
}
```

MCP tool:

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class ProductMcpTools {

    private final ProductService service;

    public ProductMcpTools(ProductService service) {
        this.service = service;
    }

    @Tool(description = "Retrieve product information by ID")
    public Product getProduct(Long id) {
        return service.getProduct(id);
    }
}
```

Register the annotated tool with Spring AI:

```java
@Configuration
public class McpConfig {

    @Bean
    ToolCallbackProvider productTools(
            ProductMcpTools tools) {

        return MethodToolCallbackProvider.builder()
            .toolObjects(tools)
            .build();
    }
}
```

The REST controller and MCP tool call the same service method. In a complete project, the MCP server starter and transport configuration expose the registered tool to MCP clients. This separation keeps the architecture clean and avoids duplicating business logic.

https://modelcontextprotocol.io/specification/2025-11-25/architecture