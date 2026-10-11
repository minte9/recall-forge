## 1. Creating tools with Spring AI

Spring AI provides the `@Tool` annotation to expose Java methods as callable tools. A tool is a regular Java method annotated with metadata describing its functionality. Spring AI uses this metadata to generate the tool definition, including its name, description, and input schema.

Importantly, `@Tool` is not specific to MCP. It belongs to Spring AI's general tool-calling abstraction and can also be used with AI models directly. When we register these methods with an MCP server, Spring AI adapts them into MCP tools that clients can discover and execute.

The following dependencies are assumed to be available in our existing `mcp-demo` project.

pom.xml — relevant dependency

```
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

## 2. Implementing a calculator tool

Let's create a calculator that exposes four operations: addition, subtraction, multiplication, and division. Each operation will be a separate MCP tool, allowing an AI client to discover and invoke it independently.

CalculatorTools.java

```java
package com.example.mcpdemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTools {

    @Tool(description = "Add two numbers")
    public double add(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {

        return a + b;
    }

    @Tool(description = "Subtract the second number from the first")
    public double subtract(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {

        return a - b;
    }

    @Tool(description = "Multiply two numbers")
    public double multiply(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {

        return a * b;
    }

    @Tool(description = "Divide the first number by the second")
    public double divide(
            @ToolParam(description = "Numerator") double a,
            @ToolParam(description = "Denominator") double b) {

        if (b == 0) {
            throw new IllegalArgumentException(
                    "Division by zero is not allowed"
            );
        }

        return a / b;
    }
}
```

The `@Component` annotation registers the class as a Spring bean. The `@Tool` annotation marks each method as a tool and provides a description that AI clients can use when selecting an operation.

The `@ToolParam` annotation describes individual parameters. Because both parameters are primitive `double` values, they are required inputs. Spring AI generates a JSON schema representing these arguments.

Notice that the methods contain only normal Java logic. They do not know anything about JSON-RPC, HTTP, or MCP clients.

## 3. Registering tools with the MCP server

Annotating a method with `@Tool` is not sufficient by itself to expose it through MCP. We must register the methods with Spring AI's tool-calling infrastructure.

For this purpose, we'll use `MethodToolCallbackProvider`, which discovers annotated methods on the supplied objects and creates tool callbacks. Spring AI's MCP server auto-configuration can then expose these callbacks to MCP clients.

McpToolConfig.java

```java
package com.example.mcpdemo.config;

import com.example.mcpdemo.tools.CalculatorTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider calculatorToolProvider(
            CalculatorTools calculatorTools) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(calculatorTools)
                .build();
    }
}
```

Here, `calculatorTools` is injected by Spring because it is annotated with `@Component`. The provider inspects the object and registers its annotated methods as tool callbacks.

## 4. Method parameters and return values

Spring AI uses Java method signatures to derive MCP tool inputs and outputs. Primitive parameters, strings, records, enums, and structured Java objects can all be used to represent tool arguments.

For example, consider the calculator method:

```java
@Tool(description = "Add two numbers")
public double add(
        @ToolParam(description = "First number") double a,
        @ToolParam(description = "Second number") double b) {

    return a + b;
}
```

Spring AI generates an input schema conceptually equivalent to:

```json
{
  "type": "object",
  "properties": {
    "a": {
      "type": "number",
      "description": "First number"
    },
    "b": {
      "type": "number",
      "description": "Second number"
    }
  },
  "required": ["a", "b"]
}
```

The generated schema allows MCP clients to understand which parameters are required and what values are expected.

For the return value, Spring AI converts the Java result into tool response content. In this example, calling `add(10, 20)` returns the numeric result `30.0`, represented in the MCP tool response.

## 5. Configuring the MCP server

We'll use Streamable HTTP transport so MCP Inspector can connect to our Spring Boot application over HTTP.

application.yml

```
server:
  port: 9090

spring:
  ai:
    mcp:
      server:
        name: calculator-mcp-server
        version: 1.0.0
        protocol: STREAMABLE
        type: SYNC
```

The `protocol: STREAMABLE` setting selects MCP's Streamable HTTP transport. The `type: SYNC` setting is appropriate for our synchronous calculator methods.

With the Spring AI WebMVC MCP server starter, the default Streamable HTTP endpoint is:

```
http://localhost:9090/mcp
```

This is the MCP protocol endpoint, not a REST endpoint for each calculator operation. All four tools are discovered and executed through the same MCP connection.

Project structure:

```
mcp-demo/
├── pom.xml
└── src/
    └── main/
        ├── java/com/example/mcpdemo/
        │   ├── McpDemoApplication.java
        │   ├── config/
        │   │   └── McpToolConfig.java
        │   └── tools/
        │       └── CalculatorTools.java
        └── resources/
            └── application.yml
```

## 6. Running the MCP server

Open a terminal on Ubuntu, navigate to the project directory, and start Spring Boot:

```
./mvnw spring-boot:run
```

Or, if Maven is installed globally:

```
mvn spring-boot:run
```

Once the application starts successfully, the MCP endpoint should be available at `http://localhost:9090/mcp`.

### Testing with MCP Inspector

Start MCP Inspector:

```
npx @modelcontextprotocol/inspector
```

Open the Inspector URL displayed in the terminal. 

![[mcp_tools.png]]


## 7. Reusing an existing Spring service

Let's now expose the `ProductService` created in the previous lesson.

Rather than placing business logic directly inside the tool, we'll create a thin adapter that delegates to the service.

ProductTools.java

```java
package com.example.mcpdemo.tools;

import com.example.mcpdemo.model.Product;
import com.example.mcpdemo.service.ProductService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ProductTools {

    private final ProductService productService;

    public ProductTools(ProductService productService) {
        this.productService = productService;
    }

    @Tool(description = "Find a product by its numeric ID")
    public Product getProduct(
            @ToolParam(description = "Product ID") Long id) {

        return productService.getProduct(id);
    }
}
```

Update the tool registration:

```java
@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(
            CalculatorTools calculatorTools,
            ProductTools productTools) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(calculatorTools, productTools)
                .build();
    }
}
```

Now the MCP server exposes five tools, including `getProduct`. 

The MCP client can invokes `getProduct` with `id = 1`.

```sh
mvn spring-boot:run

npx @modelcontextprotocol/inspector
```

![[mcp_inspector2.png]]
