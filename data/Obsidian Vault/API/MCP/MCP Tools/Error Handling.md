## 1. Handling invalid tool arguments

We implemented structured inputs, JSON Schema, and validation for our pricing tool. Now we'll learn how to handle errors correctly when MCP clients execute tools.

An MCP tool can receive invalid arguments even when its input schema is correctly defined. For example, an AI client might omit a required field, provide a string instead of a number, or submit a discount percentage greater than 100.

There are two important validation levels. Schema validation checks whether the supplied arguments match the tool's declared input structure. Business validation checks whether the values make sense for the application's rules. Spring AI can handle argument conversion, but business rules should be validated explicitly in the service layer.

For example, our pricing tool expects:

```json
{
  "request": {
    "price": 1000,
    "discount": 10,
    "tax": 19
  }
}
```

Consider these invalid requests:

|Input|Problem|Validation level|
|---|---|---|
|`"price": "hello"`|Incorrect data type|Schema/conversion|
|Missing `request`|Missing required argument|Schema|
|`"discount": 150`|Invalid percentage|Business|
|`"price": -100`|Negative price|Business|
|`"tax": -10`|Invalid tax|Business|

The distinction matters because the server might reject malformed arguments before executing the Java method, whereas business validation usually happens after the arguments have been converted into Java objects.

## 2. Application errors vs protocol errors

MCP uses JSON-RPC 2.0 for communication, but not every error should be represented as a JSON-RPC protocol error.

A protocol error indicates a problem processing the MCP request itself, such as an unknown method or malformed JSON-RPC message. An application/tool execution error indicates that the requested tool was recognized but could not successfully complete its operation.

For tool execution failures, MCP provides the `isError` property inside a normal `tools/call` result. This allows the AI client to understand that the tool failed and potentially correct its arguments or choose another action.

### Example A — Protocol error

If the client calls a nonexistent JSON-RPC method:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "unknown/method"
}
```

The server can return:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "error": {
    "code": -32601,
    "message": "Method not found"
  }
}
```

### Example B — Tool execution error

If the client invokes a valid tool but provides an invalid discount, the server can return:

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Discount must be between 0 and 100"
      }
    ],
    "isError": true
  }
}
```

This is an illustrative MCP response. The exact error representation depends on the Spring AI and MCP SDK versions and the configured exception handling.

Important: An unknown tool name within `tools/call` is not the same as an unknown JSON-RPC method. Its handling depends on the MCP server implementation and the nature of the failure.

## 3. Custom Exceptions

Let's extend our existing `PricingService` with explicit validation and introduce a custom exception for business errors.

A custom exception helps distinguish expected application failures from unexpected programming or infrastructure problems. For example, an invalid discount is a predictable validation failure, whereas a database connection failure is an unexpected infrastructure problem.

InvalidPriceException.java

```java
package com.example.mcpdemo.exception;

public class InvalidPriceException
        extends RuntimeException {

    public InvalidPriceException(String message) {
        super(message);
    }
}
```

PricingService.java

```java
package com.example.mcpdemo.service;

import com.example.mcpdemo.exception.InvalidPriceException;
import com.example.mcpdemo.model.PriceRequest;
import com.example.mcpdemo.model.PriceResponse;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    public PriceResponse calculate(PriceRequest request) {

        if (request == null) {
            throw new InvalidPriceException(
                    "Price request is required"
            );
        }

        if (!Double.isFinite(request.price())
                || request.price() < 0) {
            throw new InvalidPriceException(
                    "Price must be a finite, non-negative number"
            );
        }

        if (!Double.isFinite(request.discount())
                || request.discount() < 0
                || request.discount() > 100) {
            throw new InvalidPriceException(
                    "Discount must be between 0 and 100"
            );
        }

        double taxRate = request.tax() == null
                ? 0.0
                : request.tax();

        if (!Double.isFinite(taxRate)
                || taxRate < 0
                || taxRate > 100) {
            throw new InvalidPriceException(
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

We now have a dedicated exception type for pricing validation failures.

The service still knows nothing about MCP. This is intentional: the same exception could be handled differently by a REST controller, GraphQL resolver, or MCP tool.

## 4. Handling errors inside Tool

One practical approach is to catch expected business exceptions inside the tool and return a structured result describing success or failure.

This makes error handling predictable and allows the AI client to receive useful feedback without depending entirely on the MCP SDK's exception mapping.

However, there is an important distinction: returning an error DTO normally produces a successful MCP tool call containing an application-level failure. It does not automatically set the MCP `isError` flag to `true`.

ToolResult.java

```java
package com.example.mcpdemo.model;

public record ToolResult<T>(
        boolean success,
        T data,
        String error
) {

    public static <T> ToolResult<T> ok(T data) {
        return new ToolResult<>(true, data, null);
    }

    public static <T> ToolResult<T> failure(String error) {
        return new ToolResult<>(false, null, error);
    }
}
```

PricingTools.java

```java
package com.example.mcpdemo.tools;

import com.example.mcpdemo.exception.InvalidPriceException;
import com.example.mcpdemo.model.PriceRequest;
import com.example.mcpdemo.model.PriceResponse;
import com.example.mcpdemo.model.ToolResult;
import com.example.mcpdemo.service.PricingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class PricingTools {

    private static final Logger log =
            LoggerFactory.getLogger(PricingTools.class);

    private final PricingService pricingService;

    public PricingTools(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @Tool(description = "Calculate product price with discount and tax")
    public ToolResult<PriceResponse> calculatePrice(
            @ToolParam(description = "Price calculation details")
            PriceRequest request) {

        try {
            PriceResponse result =
                    pricingService.calculate(request);

            return ToolResult.ok(result);

        } catch (InvalidPriceException e) {

            log.warn("Invalid pricing request: {}", e.getMessage());

            return ToolResult.failure(e.getMessage());
        }
    }
}
```

Notice that we catch only `InvalidPriceException`. Unexpected exceptions are not silently converted into validation failures.

### Successful result

Request:

```json
{
    "price": 1000,
    "discount": 10,
    "tax": 19
}
```

Response:

```json
{
  "success": true,
  "data": {
    "originalPrice": 1000.0,
    "discountAmount": 100.0,
    "taxAmount": 171.0,
    "finalPrice": 1071.0
  },
  "error": null
}
```

### Application-level failure

Request:

```json
{
    "price": 1000,
    "discount": 110,
    "tax": 19
}
```

Response: 

```json
{
  "success": false,
  "data": null,
  "error": "Discount must be between 0 and 100"
}
```

Both examples show the serialized Java return values, not the complete MCP protocol envelopes.

If you need a genuine MCP execution failure with `isError: true`, use the SDK's tool error handling rather than assuming that `ToolResult.failure()` sets the protocol flag. This distinction is particularly important for clients that interpret `isError` separately from application-specific fields.

## 5. Logging tool execution

Logging is essential when debugging MCP integrations because tool calls may be initiated by AI applications rather than directly by human users. Without useful logs, it can be difficult to determine which tool was invoked, which arguments were supplied, and why execution failed.

A good logging strategy records the tool name, execution duration, and outcome. For production systems, avoid logging sensitive input values, credentials, or entire tool responses. Instead, use identifiers and safe metadata that help correlate requests.

Let's add execution timing to our pricing tool.

PricingTools.java (logging version)

```java
@Tool(description = "Calculate product price with discount and tax")
public ToolResult<PriceResponse> calculatePrice(
        @ToolParam(description = "Price calculation details")
        PriceRequest request) {

    long start = System.nanoTime();

    try {
        log.info("MCP tool calculatePrice started");

        PriceResponse result =
                pricingService.calculate(request);

        log.info("MCP tool calculatePrice succeeded");

        return ToolResult.ok(result);

    } catch (InvalidPriceException e) {

        log.warn("MCP tool calculatePrice validation failed: {}",
                e.getMessage());

        return ToolResult.failure(e.getMessage());

    } catch (RuntimeException e) {

        log.error("MCP tool calculatePrice unexpected failure", e);

        throw e;

    } finally {

        long durationMs =
                (System.nanoTime() - start) / 1_000_000;

        log.info("MCP tool calculatePrice duration={}ms",
                durationMs);
    }
}
```

Example application logs:

```txt
INFO  MCP tool calculatePrice started
INFO  MCP tool calculatePrice succeeded
INFO  MCP tool calculatePrice duration=3ms
```

For an invalid discount:

```txt
INFO  MCP tool calculatePrice started
WARN  MCP tool calculatePrice validation failed:
      Discount must be between 0 and 100
INFO  MCP tool calculatePrice duration=2ms
```

For larger applications, you can centralize tool logging and metrics rather than repeating this code in every tool method.