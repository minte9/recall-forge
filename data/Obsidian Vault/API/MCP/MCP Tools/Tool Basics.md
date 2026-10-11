
## 1. Discovery and execution

An MCP tool is a function exposed by an MCP server that an AI application can discover and execute.  

MCP uses JSON-RPC 2.0 messages to communicate between clients and servers. Before executing a tool, the client can request a list of available tools using `tools/list`. The server responds with each tool's name, description, and input schema.

When an AI model decides to use a tool, the MCP client sends a `tools/call` request containing the tool name and its arguments. The server executes the corresponding Java method and returns the result. The AI application can then use that result to generate a response for the user.

Example: Tool discovery

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list"
}
```

Example response (simplified):

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "tools": [
      {
        "name": "get_product",
        "description": "Find a product by ID",
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

Example: Tool execution

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "get_product",
    "arguments": {
      "id": 10
    }
  }
}
```

Example response:

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Product: Laptop, Price: 1200"
      }
    ],
    "isError": false
  }
}
```

The example assumes the MCP session has already been initialized.

## 2. Names, descriptions, and schemas

Every MCP tool has a name, a description, and an input schema. These elements form the contract between the MCP server and its clients. The name identifies the operation, the description explains when it should be used, and the schema defines the arguments accepted by the operation.

Descriptions are especially important because an AI model uses them to determine which tool is appropriate. For example, a tool named `get_product` with the description "Retrieve product details using a numeric product ID" is more useful to an AI model than a vaguely described tool named `execute`.

## 3. Practical example

Consider an existing Spring Boot application with a service that retrieves products. We want an AI assistant to access the same functionality through MCP.

For now, we'll implement the business logic as a normal Java service. We'll expose it using Spring AI's `@McpTool`.

Project structure

```
mcp-demo/
└── src/main/java/com/example/mcpdemo/
    ├── McpDemoApplication.java
    ├── model/
    │   └── Product.java
    └── service/
        └── ProductService.java
```

Product.java

```java
package com.example.mcpdemo.model;

public record Product(
    Long id,
    String name,
    double price
) {}
```

We use a Java record to represent a product. Records are useful for immutable DTOs and reduce boilerplate code.

ProductService.java

```java
package com.example.mcpdemo.service;

import com.example.mcpdemo.model.Product;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductService {

    private final Map<Long, Product> products = Map.of(
        1L, new Product(1L, "Laptop", 1200.0),
        2L, new Product(2L, "Keyboard", 80.0),
        3L, new Product(3L, "Mouse", 40.0)
    );

    public Product getProduct(Long id) {
        Product product = products.get(id);

        if (product == null) {
            throw new IllegalArgumentException(
                "Product not found: " + id
            );
        }

        return product;
    }
}
```

This service is independent of MCP. It could be used by a REST controller, GraphQL resolver, scheduled job, or MCP tool.

McpDemoApplication.java

```java
package com.example.mcpdemo;

import com.example.mcpdemo.service.ProductService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(
            McpDemoApplication.class, args
        );
    }
}
```

Next, we'll introduce a class that exposes `ProductService` through MCP. The MCP layer acts as an adapter between the AI application and your existing backend functionality.