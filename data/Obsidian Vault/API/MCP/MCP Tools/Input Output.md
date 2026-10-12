
## 1. Structured arguments

We created MCP tools using Spring AI's `@Tool` and `@ToolParam` annotations and registered them through `MethodToolCallbackProvider`. Now we'll explore how MCP tools receive structured arguments, validate inputs, and return results.

MCP uses JSON Schema to describe the arguments accepted by a tool. When an MCP client requests `tools/list`, the server returns the available tools together with their input schemas. These schemas describe parameter names, data types, required fields, and other constraints.

Spring AI automatically generates the input schema from Java method signatures. Simple parameters such as `String`, `int`, and `double` become JSON properties, while Java records and classes can represent structured objects. 

PricingTools.java

```java
package com.example.mcpdemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class PricingTools {

    @Tool(description = "Calculate a discounted product price")
    public double calculateDiscount(
            @ToolParam(description = "Original product price")
            double price,

            @ToolParam(description = "Discount percentage")
            double discount) {

        return price * (1 - discount / 100);
    }
}
```

McpToolConfig.java

```java
package com.example.mcp_demo.config;

import com.example.mcpdemo.tools.CalculatorTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider calculatorToolProvider(
            CalculatorTools calculatorTools,
            PricingTools pricingTools) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(calculatorTools, pricingTools)
                .build();
    }
}
```


Spring AI generates a schema conceptually equivalent to:

```json
{
  "type": "object",
  "properties": {
    "price": {
      "type": "number",
      "description": "Original product price"
    },
    "discount": {
      "type": "number",
      "description": "Discount percentage"
    }
  },
  "required": ["price", "discount"]
}
```

An MCP client can then execute the tool using:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "calculateDiscount",
    "arguments": {
      "price": 1200,
      "discount": 15
    }
  }
}
```

The Java method returns `1020.0`

Notice that the MCP client does not need to know anything about the Java method's implementation. It only needs the tool definition and its schema.

```sh
mvn spring-boot:run

npx @modelcontextprotocol/inspector
```

## 2. Optional parameters

By default, Spring AI treats tool parameters as required unless they are explicitly declared optional. The `@ToolParam` annotation supports `required = false`, allowing a tool to accept parameters that may be omitted from the request.

For optional arguments, use nullable Java types such as `Integer`, `Double`, or `String` instead of primitives. This allows the application to distinguish between a missing value and an actual value such as zero.

Let's extend the pricing tool to support an optional tax percentage.

```java
@Tool(description = "Calculate a product price with discount and optional tax")
public double calculateFinalPrice(
        @ToolParam(description = "Original price")
        double price,

        @ToolParam(description = "Discount percentage")
        double discount,

        @ToolParam(
            description = "Optional tax percentage",
            required = false // Look Here
        )
        Double tax) {

    double discounted = price * (1 - discount / 100);

    double taxRate = tax != null ? tax : 0.0;

    return discounted * (1 + taxRate / 100);
}
```

Both of the following argument objects are valid:

With tax

```json
{
  "price": 100,
  "discount": 10,
  "tax": 19
}
```

Result: 107.1

Without tax

```json
{
  "price": 100,
  "discount": 10
}
```

Result: 90.0

The corresponding JSON Schema lists `price` and `discount` as required properties, while `tax` remains optional.

Important: Making a parameter optional does not automatically assign a Java default value. Your method must handle `null` or another appropriate representation of an absent value.

## 3. Structured results

MCP tools can return more than simple strings or numbers. A Java method can return a record or DTO containing multiple fields, allowing clients to work with structured business data.

PriceRequest.java

```java
package com.example.mcpdemo.model;

public record PriceRequest(
        double price,
        double discount,
        Double tax
) {}
```

PriceResponse.java

```java
package com.example.mcpdemo.model;

public record PriceResponse(
        double originalPrice,
        double discountAmount,
        double taxAmount,
        double finalPrice
) {}
```

PricingService.java

```java
package com.example.mcpdemo.service;

import com.example.mcpdemo.model.PriceRequest;
import com.example.mcpdemo.model.PriceResponse;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    public PriceResponse calculate(PriceRequest request) {

        double taxRate = request.tax() == null
                ? 0.0
                : request.tax();

        double discountAmount =
                request.price() * request.discount() / 100;

        double discountedPrice =
                request.price() - discountAmount;

        double taxAmount =
                discountedPrice * taxRate / 100;

        double finalPrice =
                discountedPrice + taxAmount;

        return new PriceResponse(
                request.price(),
                discountAmount,
                taxAmount,
                finalPrice
        );
    }
}
```

PricingTools.java

```java
package com.example.mcpdemo.tools;

import com.example.mcpdemo.model.PriceRequest;
import com.example.mcpdemo.model.PriceResponse;
import com.example.mcpdemo.service.PricingService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class PricingTools {

    private final PricingService pricingService;

    public PricingTools(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @Tool(description = "Calculate a product price including discount and tax")
    public PriceResponse calculatePrice(
            @ToolParam(description = "Price calculation details")
            PriceRequest request) {

        return pricingService.calculate(request);
    }
}
```

The tool accepts a structured argument named `request`.

McpToolConfig.java

```java
package com.example.mcp_demo.config;

import com.example.mcpdemo.tools.CalculatorTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider calculatorToolProvider(
            PricingTools pricingTools) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(pricingTools)
                .build();
    }
}
```

Example tool arguments:

```json
{
  "request": {
    "price": 1000,
    "discount": 10,
    "tax": 19
  }
}
```

The Java method returns a `PriceResponse`, which Spring AI serializes into JSON-compatible tool content.

Example result data:

```json
{
  "originalPrice": 1000.0,
  "discountAmount": 100.0,
  "taxAmount": 171.0,
  "finalPrice": 1071.0
}
```

Depending on the Spring AI and MCP SDK versions, the result may appear as serialized JSON in a text content block, or structured output may also be exposed. Returning a Java record does not, by itself, guarantee a particular MCP `structuredContent` representation.

## 4. Input validation

JSON Schema provides a contract describing the expected structure of tool arguments, but it should not be treated as a complete replacement for application validation.

For example, a JSON schema may specify that `discount` must be a number, but our business rules also require the discount to be between 0 and 100. Similarly, a price should not be negative, and a tax percentage should remain within an acceptable range.

We'll implement explicit validation in the service layer. This is useful because the same business logic may later be called through REST, GraphQL, or MCP.

Update `PricingService.java`:

```java
package com.example.mcpdemo.service;

import com.example.mcpdemo.model.PriceRequest;
import com.example.mcpdemo.model.PriceResponse;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    public PriceResponse calculate(PriceRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                "Price request is required"
            );
        }

        if (!Double.isFinite(request.price())
                || request.price() < 0) {
                
            throw new IllegalArgumentException(
                "Price must be a finite, non-negative number"
            );
        }

        if (!Double.isFinite(request.discount())
                || request.discount() < 0
                || request.discount() > 100) {
                
            throw new IllegalArgumentException(
                "Discount must be between 0 and 100"
            );
        }

        double taxRate = request.tax() == null
                ? 0.0
                : request.tax();

        if (!Double.isFinite(taxRate)
                || taxRate < 0
                || taxRate > 100) {
                
            throw new IllegalArgumentException(
                "Tax must be between 0 and 100"
            );
        }

        double discountAmount =
                request.price() * request.discount() / 100;

        double discountedPrice =
                request.price() - discountAmount;

        double taxAmount =
                discountedPrice * taxRate / 100;

        return new PriceResponse(
                request.price(),
                discountAmount,
                taxAmount,
                discountedPrice + taxAmount
        );
    }
}
```

Here we use `IllegalArgumentException` to signal invalid business inputs. The MCP tool infrastructure can translate exceptions into error responses, depending on the configured error handling.

```json
{
    "price": 1000,
    "discount": 110,
    "tax": 19
}
```

Response:

```txt
Discount must be between 0 and 100
```
