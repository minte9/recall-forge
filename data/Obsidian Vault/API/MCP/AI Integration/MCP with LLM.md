
## 1. LLM tool integration

A Large Language Model (LLM) generates responses based on its training and the context provided in a conversation. However, it cannot independently access your database, execute Java methods, or retrieve live application data. To perform these operations, it needs an external application that exposes tools and executes them on its behalf.

MCP provides a standardized way for applications to discover and invoke external tools. For example, an MCP server might expose `calculatePrice`, `getProduct`, and `checkInventory`. An AI application can discover these tools and make their descriptions and parameter schemas available to an LLM. The model can then select a suitable tool and generate its arguments. The actual execution happens in the MCP client and server, not inside the LLM.

Consider this user request:

> What is the final price of a product costing €200 with a 15% discount?

Without tools, the LLM might calculate the result using its own reasoning. With MCP tools available, it can request execution of `calculatePrice`, receive the calculated result, and formulate its answer using that result.

![[mcp_llm_too_integration.png]]

The LLM does not need to understand the Java implementation of `calculatePrice`. It only needs the tool's name, description, and input schema.

## 2. Tool selection

Tool selection is the process by which an LLM determines whether a tool is needed and which available tool best matches the user's request. The model receives tool definitions containing names, descriptions, and JSON schemas for their arguments. These definitions help it distinguish between tools such as `calculatePrice`, `findProduct`, and `getWeather`.

For example, if the user asks "What is the discounted price of a laptop?", the LLM may select `calculatePrice`. If the user asks "Explain how discounts work", it may answer directly without executing any tool. This behavior depends on the model, instructions, tool definitions, and tool-choice configuration. Tool selection is not guaranteed to be correct, so the application must validate inputs and enforce authorization independently.

### Example: MCP tool definition in Java

`PricingTools.java`

```java
package com.example.mcp_demo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class PricingTools {

    @Tool(description = """
        Calculate the final product price
        after applying a percentage discount.
        Use this tool for discount calculations.
        """)
    public double calculatePrice(
            @ToolParam(description = "Original price")
            double price,

            @ToolParam(description = "Discount percentage")
            double discount) {

        if (price < 0 || discount < 0 || discount > 100) {
            throw new IllegalArgumentException(
                "Invalid price or discount"
            );
        }

        return price * (1 - discount / 100);
    }
}
```

When this method is registered with an MCP server, the server exposes a tool definition describing its name, parameters, and expected types.

A simplified representation of the discovered tool looks like this:

```json
{
  "name": "calculatePrice",
  "description": "Calculate the final product price after applying a percentage discount.",
  "inputSchema": {
    "type": "object",
    "properties": {
      "price": {
        "type": "number"
      },
      "discount": {
        "type": "number"
      }
    },
    "required": ["price", "discount"]
  }
}
```

The LLM might generate a tool-call request with the following arguments:

```json
{
  "price": 200,
  "discount": 15
}
```

The MCP client forwards the request to the MCP server, which executes the Java method and returns `170.0`.

Important: The JSON above represents the logical tool definition and arguments. The actual MCP communication uses JSON-RPC messages, and the tool result is wrapped in an MCP response structure.

## 3. Client server workflow

The integration involves three main components: the LLM, the MCP client, and the MCP server. The LLM interprets natural language and requests tool calls. The MCP client manages communication with the MCP server, while the server executes business operations and returns results. In a Spring Boot application, Spring AI can coordinate the LLM tool-calling loop and bridge MCP tools into the model's tool-calling interface.

The process usually begins when the Spring Boot application starts. Its MCP client connects to one or more MCP servers, negotiates capabilities, and discovers available tools. During a conversation, the application supplies relevant tool definitions to the model. When the model requests a tool call, Spring AI invokes the corresponding tool callback, which communicates with the MCP server. The result is then sent back to the model, allowing it to produce a natural-language response.

### Example: the messages exchanged

A typical interaction looks like this:

Step 1 — User sends a request

```json
User: Calculate the price of a €200 product
      with a 15% discount.
```

Step 2 — Model requests a tool call

```json
{
  "tool": "calculatePrice",
  "arguments": {
    "price": 200,
    "discount": 15
  }
}
```

Step 3 — MCP client calls the server

A simplified MCP JSON-RPC request:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "calculatePrice",
    "arguments": {
      "price": 200,
      "discount": 15
    }
  }
}
```

Step 4 — MCP server returns the result

For a tool returning a Java `double`, a simplified MCP response could look like:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "170.0"
      }
    ],
    "isError": false
  }
}
```

Step 5 — Model generates the final answer

```txt
The original price is €200.
After applying a 15% discount,
the final price is €170.
```

Notice that the LLM does not communicate directly with the MCP server. The Spring Boot application coordinates the process.

## 4. MCP vs direct LLM function calling

Function calling is a capability offered by many LLM APIs. It allows an application to describe functions to the model, receive structured function-call requests, execute the functions, and return their results. MCP builds on a related idea but addresses a different problem: standardizing how applications discover and access external tools, resources, and prompts.

With direct function calling, a Spring Boot application typically defines and registers its own tool callbacks. With MCP, the tools can live in independent servers and be discovered dynamically by MCP clients. Importantly, these approaches are not mutually exclusive: Spring AI can expose MCP-discovered tools to an LLM through its normal function/tool-calling mechanism.

|Feature|Direct function calling|MCP|
|---|---|---|
|Tool definitions|Registered with the AI application|Discovered from MCP servers|
|Communication|Application-specific|Standard MCP protocol|
|Tool location|Often local to the application|Local or remote|
|Reusability|Requires integration work|Shared across MCP clients|
|Resources and prompts|Not standardized by function calling|Supported by MCP|
|LLM required|Usually yes|No, MCP works without an LLM|
### Example: Direct Spring AI tool calling

Without MCP, we can register a local Java tool directly with `ChatClient`.

```java
@Service
public class PricingAssistant {

    private final ChatClient chatClient;

    public PricingAssistant(
            ChatClient.Builder builder,
            PricingTools pricingTools) {

        this.chatClient = builder
            .defaultTools(pricingTools)
            .build();
    }

    public String ask(String question) {
        return chatClient.prompt()
            .user(question)
            .call()
            .content();
    }
}
```

Here, `PricingTools` runs inside the same Spring Boot application as the AI assistant. No MCP server is required.

With MCP, the assistant can use tools from a separate application:

```txt
Spring Boot AI Assistant
        |
        | MCP Client
        v
Spring Boot MCP Server
        |
        v
PricingTools
        |
        v
PricingService
```

This separation becomes especially useful when multiple AI applications need access to the same business capabilities.

## 5. Practical example: choosing the correct tool

Imagine our MCP server exposes three tools:

```json
@Tool(description = "Calculate discounted product price")
public double calculatePrice(
        double price,
        double discount) {
    return price * (1 - discount / 100);
}

@Tool(description = "Find a product by its ID")
public Product getProduct(long productId) {
    return productService.findById(productId);
}

@Tool(description = "Check available inventory for a product")
public int checkInventory(long productId) {
    return inventoryService.getQuantity(productId);
}
```

Now consider these user requests:

|User request|Expected behavior|
|---|---|
|"Calculate 20% off €100"|`calculatePrice`|
|"Find product 42"|`getProduct`|
|"How many units of product 42 are available?"|`checkInventory`|
|"Explain what an inventory system does"|No tool needed|
|"Find product 42 and calculate a 10% discount"|`getProduct`, then `calculatePrice`|

The final example illustrates an important capability: the LLM may perform multiple tool calls to complete a single request. It can retrieve the product price using `getProduct`, then use that price as an argument for `calculatePrice`.

## 6. Key takeaways

- LLM:   Understands user requests and decides which available tools to request.
- Spring AI:   Coordinates model interaction, tool execution, and returning results.
- MCP Client:   Discovers tools and communicates with MCP servers.
- MCP Server:   Exposes capabilities and executes business operations.
- `@Tool`:   Defines Java methods that can be exposed through Spring AI and MCP.
- MCP vs function calling:   Function calling is how the model requests actions; MCP standardizes access to external capabilities.

One important architectural principle is that the LLM should not be treated as a trusted authorization or validation layer. MCP servers must still validate arguments, enforce permissions, and protect sensitive operations, even when the model selects the correct tool.
