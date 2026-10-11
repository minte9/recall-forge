## 1. What is Model Context Protocol?

Model Context Protocol (MCP) is an open protocol that standardizes how AI applications connect to external tools, data sources, and services. It was introduced by Anthropic in November 2024. 

A Large Language Model (LLM) generates responses based on its training and the context supplied to it. However, it does not automatically have access to your company's database, local files, internal APIs, or running applications. MCP provides a standardized interface through which an AI application can discover available capabilities and invoke them when needed.

MCP defines three important server capabilities:

a) Tools: Operations the AI can request, such as searching a database, creating an order, or checking server health.
    
b) Resources: Data the application can read, such as configuration files, documentation, or database schemas.
    
c) Prompts: Reusable prompt templates that help users perform predefined workflows.
    

MCP also defines communication mechanisms, including JSON-RPC messages and transports such as standard input/output (stdio) and Streamable HTTP.

### Example: AI accessing a Spring Boot service

Imagine you have a Spring Boot application exposing this REST endpoint:

```
GET http://localhost:8080/api/products/42
```

It returns:

```
{
  "id": 42,
  "name": "Laptop",
  "price": 1200
}
```

Normally, a developer writes code to call this endpoint. With MCP, you can expose a tool named `get_product`, allowing an AI application to request the product information using a standardized interface.

A conceptual MCP tool declaration looks like this:

```
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
```

The AI application can discover the tool, understand that it requires an integer `id`, and request its execution. The MCP server then calls your existing Spring Boot service or repository and returns the result.

## 2. Why MCP was created

Before MCP, integrating AI applications with external systems often required custom implementations. Suppose a company uses three AI assistants and maintains five internal services. Without a shared protocol, developers might need to implement and maintain different integrations for each assistant-service combination.

MCP addresses this problem by establishing a common contract for capability discovery, invocation, and data exchange. An MCP server describes the tools and resources it offers, while an MCP client communicates with that server according to the protocol. This makes integrations more reusable, although client compatibility, authentication, and deployment still require engineering work.

![[mcp_services.png]]

For example, consider an infrastructure monitoring application built with Spring Boot. You already have REST endpoints that expose CPU usage, memory consumption, and container health. Instead of rewriting those endpoints, you could build an MCP server that calls them and exposes tools such as `get_cpu_usage`, `get_memory_usage`, and `list_unhealthy_containers`.

An AI assistant could then answer a request such as "Which containers are unhealthy, and what is the current CPU usage?" by calling those tools. The key benefit is that the assistant can discover what operations are available without needing a separate, manually designed integration for every operation.

## 3. MCP vs REST vs GraphQL

MCP, REST, and GraphQL are related because they all support communication between software components. However, they solve different problems. REST typically exposes resources through HTTP endpoints, while GraphQL provides a typed schema that allows clients to request specific fields and execute queries or mutations. MCP focuses on exposing tools, resources, and prompts to AI applications through a standardized protocol.

These technologies are not mutually exclusive. An MCP server can use REST or GraphQL internally to retrieve information or perform operations. In fact, for an existing Spring Boot application, this is often the most practical approach: keep your current APIs and introduce MCP as an additional integration layer for AI clients.

### Example: same operation using three approaches

Suppose we want to retrieve a product with ID `42`.

REST

```
curl http://localhost:8080/api/products/42
```

The client knows the endpoint and calls it directly.

GraphQL

```
{ product(id: 42) {
    id
    name
    price
  }
}
```

The client selects which product fields it wants.

MCP

```
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "get_product",
    "arguments": {
      "id": 42
    }
  }
}
```

The MCP client requests execution of a discovered tool. This shows the JSON-RPC message body, not a complete transport exchange.

Notice that all three approaches can ultimately execute the same Java service method:

```
public Product getProduct(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() ->
            new IllegalArgumentException(
                "Product not found: " + id
            )
        );
}
```

MCP is therefore better understood as a complementary integration protocol, not a successor to REST or GraphQL.

## 4. Real-world use cases

MCP is useful whenever an AI application needs access to external information or operations. Instead of giving an AI model direct database credentials or unrestricted system access, developers can expose narrowly scoped tools that validate input, enforce authorization, and return structured results.

For a backend developer working with Java and Spring Boot, several practical use cases are particularly relevant.

**Infrastructure monitoring:**

AI request: "Show me the unhealthy Docker containers."  
MCP tool: `list_unhealthy_containers`  
Backend: Spring Boot → monitoring service → MongoDB  

**Database access:**

AI request: "How many orders were created today?"  
MCP tool: `count_orders_by_date`  
Backend: Spring Boot → JPA → PostgreSQL  

**Learning applications:**

AI request: "Which Java topics are due for review?"  
MCP tool: `get_due_topics`  
Backend: Spring Boot → review service → PostgreSQL  

**Enterprise integration:**

AI request: "Check the processing status of transaction 123."  
MCP tool: `get_transaction_status`  
Backend: Spring Boot → internal REST API → transaction system  

### Example: a simple Spring Boot MCP tool

The following example illustrates how an existing Java service can expose a tool using Spring AI's MCP annotations. This is a partial example, not yet a complete runnable MCP server.

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ProductTools {

    private final ProductService productService;

    public ProductTools(ProductService productService) {
        this.productService = productService;
    }

    @Tool(
        name = "get_product",
        description = "Get product information by ID"
    )
    public Product getProduct(Long id) {
        return productService.getProduct(id);
    }
}
```

The `@Tool` annotation provides metadata that Spring AI can use when exposing the method as a tool. The method delegates to an existing `ProductService`, which can retrieve the product from a database or another API.

To make the tool available through MCP, you also need the appropriate Spring AI MCP server dependency, configuration, and tool registration. For example, using Spring AI's `MethodToolCallbackProvider`:

```java
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    ToolCallbackProvider productToolProvider(
            ProductTools productTools) {

        return MethodToolCallbackProvider.builder()
            .toolObjects(productTools)
            .build();
    }
}
```

With the MCP server starter properly configured, a compatible client can discover `get_product` and request its execution. The server remains responsible for access control, validation, and protecting sensitive data.  

https://modelcontextprotocol.io/docs/2026-07-28/getting-started/intro

