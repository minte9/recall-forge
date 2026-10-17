## 1. Executing tools from a Java client

We created a Java MCP client that connects to our Spring Boot MCP server and discovers available tools using `client.listTools()`. Now we'll extend that client to execute tools, pass arguments, read responses, and handle errors.

Once an MCP client discovers available tools, it can execute them using the `tools/call` protocol method. Unlike a traditional REST client, which sends requests to specific URLs, an MCP client sends a standardized request containing the tool name and its arguments. The MCP server identifies the corresponding registered Java method, executes it, and returns the result.

In Spring AI, `McpSyncClient` exposes the `callTool()` method. We provide a `CallToolRequest` containing the tool name and a map of arguments. The SDK serializes the request into JSON-RPC, sends it over the configured transport, and converts the response into a `CallToolResult`.

MCP tool execution flow:

![[mcp_tool_execution_flow.png]]

Suppose our MCP server exposes the following method:

Server — `CalculatorTools.java`

```java
@Component
public class CalculatorTools {

    @Tool(description = "Add two numbers")
    public double add(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {

        return a + b;
    }
}
```

On the client side, we can execute this tool without importing or referencing `CalculatorTools`.

Client — `CalculatorClientRunner.java`

```java
package com.example.mcpclient.runner;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CalculatorClientRunner implements CommandLineRunner {

    private final McpSyncClient client;

    public CalculatorClientRunner(
            List<McpSyncClient> clients) {

        this.client = clients.get(0);
    }

    @Override
    public void run(String... args) {

        McpSchema.CallToolRequest request =
            new McpSchema.CallToolRequest(
                "add",
                Map.of(
                    "a", 10,
                    "b", 20
                )
            );

        McpSchema.CallToolResult result =
            client.callTool(request);

        System.out.println(result);
    }
}
```

Here, `CallToolRequest` contains two things: the tool name (`add`) and a map of arguments (`a` and `b`). Spring AI converts this into a JSON-RPC request, sends it to the server, and waits for the response.

Conceptually, the request looks like:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "add",
    "arguments": {
      "a": 10,
      "b": 20
    }
  }
}
```

The server executes `add(10, 20)` and returns the result. The client does not need to know whether the server implementation uses Spring Boot, Python, Node.js, or another technology.

With MCP server running, execute:

```sh
mvn clean spring-boot:run
```

Result:

```json
CallToolResult[content=[TextContent[annotations=null, text=30.0, meta=null]], isError=false, structuredContent=null, meta=null]
```

## 2. Passing structured arguments

Simple tools may accept primitive arguments such as integers, doubles, and strings. More complex tools often accept structured objects containing multiple fields. MCP represents these arguments as JSON-compatible data, and the server validates them against the tool's declared input schema.

In Java, we can construct these arguments using `Map<String, Object>`. This is especially useful because MCP clients do not need to share Java DTO classes with their servers. They only need to follow the JSON Schema advertised during tool discovery.

### Example: Pricing tool

Suppose our existing MCP server defines the following request and response records.

Server — `PriceRequest.java`

```java
package com.example.mcp_demo.model;

public record PriceRequest(
    String product,
    double basePrice,
    double discountPercent
) {}
```

Server — `PriceResponse.java`

```java
package com.example.mcp_demo.model;

public record PriceResponse(
    String product,
    double originalPrice,
    double finalPrice
) {}
```

Server `PricingService.java`

```java
package com.example.mcp_demo.service;

import org.springframework.stereotype.Service;
import com.example.mcp_demo.exception.InvalidPriceException;
import com.example.mcp_demo.model.PriceRequest;
import com.example.mcp_demo.model.PriceResponse;

@Service 
public class PricingService {
    
    public PriceResponse calculate(PriceRequest request) {

        if (request == null) {
            throw new InvalidPriceException(
                "Price request is required"
            );
        }

        double discountAmount = 
            request.basePrice() * request.discountPercent() / 100;

        double discountedPrice = 
            request.basePrice() - discountAmount;

        return new PriceResponse(
            request.product(),
            request.basePrice(),
            discountedPrice
        );
    }
}
```

Our pricing tool delegates calculations to a Spring service.

Server — `PricingTools.java`

```java
@Component
public class PricingTools {

    private final PricingService pricingService;

    public PricingTools(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @Tool(description = "Calculate discounted product price")
    public PriceResponse calculatePrice(
            @ToolParam(description = "Pricing request")
            PriceRequest request) {

        return pricingService.calculate(request);
    }
}
```

The tool accepts a structured `PriceRequest`, rather than three separate arguments.
### Client implementation

`PricingClientRunner.java`

```java
package com.example.mcpclient.runner;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PricingClientRunner implements CommandLineRunner {

    private final McpSyncClient client;

    public PricingClientRunner(
            List<McpSyncClient> clients) {

        this.client = clients.get(0);
    }

    @Override
    public void run(String... args) {

        Map<String, Object> priceRequest = Map.of(
            "product", "Laptop",
            "basePrice", 1000.0,
            "discountPercent", 15.0
        );

        McpSchema.CallToolRequest request =
            new McpSchema.CallToolRequest(
                "calculatePrice",
                Map.of("request", priceRequest)
            );

        McpSchema.CallToolResult result =
            client.callTool(request);

        System.out.println(result);
    }
}
```

Notice the nested structure:

```java
Map.of(
    "request",
    Map.of(
        "product", "Laptop",
        "basePrice", 1000.0,
        "discountPercent", 15.0
    )
)
```

The outer `request` key corresponds to the Java parameter name in `calculatePrice(PriceRequest request)`. The inner object corresponds to the fields of `PriceRequest`.

The resulting JSON-RPC parameters look like:

```json
{
  "name": "calculatePrice",
  "arguments": {
    "request": {
      "product": "Laptop",
      "basePrice": 1000.0,
      "discountPercent": 15.0
    }
  }
}
```

The server converts this JSON object into a Java `PriceRequest` and passes it to the pricing service.

Important: The argument structure must match the tool's actual input schema. Inspect `client.listTools()` before constructing requests, especially if parameter names or tool signatures change.

Run the server and client:

```sh
cd ~/projects/mcp-demo
mvn spring-boot:run

cd ~/projects/mcp-client-demo
mvn clean spring-boot:run
```

Result:

```json
CallToolResult[content=[TextContent[annotations=null, text={"product":"Laptop","originalPrice":1000.0,"finalPrice":850.0}, meta=null]], isError=false, structuredContent=null, meta=null]
```

## 3. Complete practical example

We'll combine tool execution, argument handling, response processing, and error handling into one reusable Java service.

McpToolService.java

```java
package com.example.mcpclient.service;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class McpToolService {

    private final McpSyncClient client;

    public McpToolService(List<McpSyncClient> clients) {

        if (clients.size() != 1) {
            throw new IllegalStateException(
                "Expected exactly one MCP client"
            );
        }

        this.client = clients.get(0);
    }

    public McpSchema.CallToolResult execute(
            String toolName,
            Map<String, Object> arguments) {

        McpSchema.CallToolRequest request =
            new McpSchema.CallToolRequest(
                toolName,
                arguments
            );

        McpSchema.CallToolResult result =
            client.callTool(request);

        if (Boolean.TRUE.equals(result.isError())) {
            throw new IllegalStateException(
                "Tool execution failed: " + result.content()
            );
        }

        return result;
    }

    public void printResult(McpSchema.CallToolResult result) {

        if (result.structuredContent() != null) {
            System.out.println(
                "Structured: " + result.structuredContent()
            );
        }

        for (McpSchema.Content content : result.content()) {

            if (content instanceof McpSchema.TextContent text) {
                System.out.println(text.text());
            }
        }
    }
}
```

This service separates MCP communication from the application logic. It can later be reused by REST controllers, GraphQL resolvers, or other Spring services.

ToolExecutionRunner.java

```java
package com.example.mcpclient.runner;

import com.example.mcpclient.service.McpToolService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ToolExecutionRunner implements CommandLineRunner {

    private final McpToolService toolService;

    public ToolExecutionRunner(McpToolService toolService) {
        this.toolService = toolService;
    }

    @Override
    public void run(String... args) {

        System.out.println("=== Calculator ===");

        var calculatorResult = toolService.execute(
            "add",
            Map.of("a", 10, "b", 20)
        );

        toolService.printResult(calculatorResult);

        System.out.println("\n=== Pricing ===");

        var pricingResult = toolService.execute(
            "calculatePrice",
            Map.of(
                "request",
                Map.of(
                    "product", "Laptop",
                    "basePrice", 1000.0,
                    "discountPercent", 15.0
                )
            )
        );

        toolService.printResult(pricingResult);
    }
}
```

Make sure the earlier `ToolDiscoveryRunner`, `CalculatorClientRunner`, and `PricingClientRunner` are disabled or removed if you want only this runner to execute at startup.

Start the MCP server:

```sh
cd ~/projects/mcp-demo
mvn spring-boot:run
```

In another terminal, start the client:

```sh
cd ~/projects/mcp-client-demo
mvn clean spring-boot:run
```

Example output, assuming your server implements the pricing calculation as shown:

```txt
=== Calculator ===
30.0

=== Pricing ===
{"product":"Laptop","originalPrice":1000.0,"finalPrice":850.0}
```