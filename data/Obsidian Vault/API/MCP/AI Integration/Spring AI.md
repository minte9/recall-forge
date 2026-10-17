## 1. ChatClient

We will implement this integration using Spring AI. Instead of manually calling `calculatePrice()` from Java, we will let an AI model decide when the tool is needed, supply its arguments, and interpret the result.

We will build a small tool-enabled chatbot using Spring Boot, Spring AI `ChatClient`, and an OpenAI model. The chatbot will connect to the MCP server developed in earlier chapters,
discover its available tools, and expose a REST endpoint that accepts natural-language questions.

```txt
Stack: Java 21, Spring Boot, Spring AI, OpenAI, MCP
```

ChatClient is Spring AI's high-level API for interacting with language models. It provides a fluent interface for sending prompts, configuring system instructions, registering tools, and retrieving model responses. It plays a role similar to Spring's `RestClient`, but instead of calling a conventional REST service, it communicates with an AI model through a configured `ChatModel`.

The `ChatClient` itself does not implement an LLM. Spring AI uses model-specific integrations, such as OpenAI, to send requests to the model provider. The application can then register Java tools or MCP-discovered tools with the client, enabling the model to request operations that are executed by the application.

### Basic ChatClient example

`SimpleChatService.java`

```java
package com.example.mcp_ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SimpleChatService {

    private final ChatClient chatClient;

    public SimpleChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
```

Example usage:

```java
String answer = chatService.ask(
    "Explain what MCP is in two sentences"
);

System.out.println(answer);
```

The execution is straightforward:

1. `ChatClient.Builder` is injected by Spring Boot.
2. `.prompt()` creates a new prompt request.
3. `.user(question)` adds the user's message.
4. `.call()` executes the model request synchronously.
5. `.content()` extracts the generated text.
    
This example does not use MCP yet. It simply demonstrates how Spring AI communicates with an LLM.

## 2. Connecting an LLM

To integrate MCP, we need two Spring Boot applications: an MCP server exposing business tools and an AI application containing the MCP client and `ChatClient`. The MCP server does not need to know anything about OpenAI. It only exposes capabilities such as `calculatePrice`, while the AI application is responsible for communicating with the model and executing requested tools.

Spring AI provides MCP client integration that can discover tools from MCP servers and adapt them into Spring AI tool callbacks. We can register these callbacks with `ChatClient` using `defaultToolCallbacks()`. Once registered, Spring AI can make their definitions available to the model and execute them when requested.

![[mcp_chatclient.png]]

The important separation is that the LLM chooses the tool, but the MCP server executes the business logic. This allows us to reuse the same MCP server with different AI applications or models.

## 3. Configuring AI

We will create a new Spring Boot application named `mcp-ai-client` on Ubuntu. This application will run separately from the existing MCP server and use OpenAI as its language model. For this example, we will use `gpt-4.1-mini`, which supports tool calling and is suitable for learning this workflow.

### Project structure

```txt
mcp-ai-client/
├── pom.xml
└── src/
    └── main/
        ├── java/com/example/mcp_ai/
        │   ├── McpAiApplication.java
        │   ├── config/
        │   │   └── ChatConfig.java
        │   ├── controller/
        │   │   └── ChatController.java
        │   └── service/
        │       └── AiChatService.java
        └── resources/
            └── application.yml
```

### Maven dependencies

`pom.xml`

The following example uses Spring Boot 3.5.x and Spring AI 1.1.x. Use a mutually compatible, published patch version of each; the version values below are illustrative rather than a claim about the latest available release.

```xml
<properties>
    <java.version>21</java.version>
    <spring-ai.version>1.1.2</spring-ai.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-client</artifactId>
    </dependency>
</dependencies>
```

The three dependencies have different responsibilities. `spring-boot-starter-web` provides the REST API, `spring-ai-starter-model-openai` configures the OpenAI model integration, and `spring-ai-starter-mcp-client` provides MCP client functionality and auto-configuration.

### Application configuration

`src/main/resources/application.yml`

```yml
server:
  port: 9099

spring:
  application:
    name: mcp-ai-client

  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4.1-mini
          temperature: 0

    mcp:
      client:
        enabled: true
        name: pricing-ai-client
        version: 1.0.0
        type: SYNC

        streamable-http:
          connections:
            pricing-server:
              url: http://localhost:8080
              endpoint: /mcp
```

The OpenAI configuration specifies the API key and model. The MCP configuration defines a synchronous client connection to our existing MCP server, which we assume is listening on `http://localhost:8080/mcp`.

For this configuration, the server must use the Streamable HTTP transport. Its relevant configuration would be:

```yml
spring:
  ai:
    mcp:
      server:
        protocol: STREAMABLE
```

The MCP server also needs the appropriate Spring AI WebMVC MCP server starter and an HTTP port of 9091. If your earlier project uses stdio instead, its transport must be changed or the client configured for stdio.

### OpenAI API key

On Ubuntu:

```sh
export OPENAI_API_KEY="your-api-key"
```

Verify that the variable exists without printing the secret:

```sh
test -n "$OPENAI_API_KEY" && echo "API key configured"
```

Avoid placing the API key directly in `application.yml` or committing it to Git.

## 4. Building a tool-enabled chatbot

Now we will connect `ChatClient` to the tools discovered from our MCP server. The central component is `SyncMcpToolCallbackProvider`, which adapts MCP tools into callbacks understood by Spring AI.

### Configure ChatClient

`ChatConfig.java`

```java
package com.example.mcp_ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Bean
    ChatClient chatClient(
            ChatClient.Builder builder,
            SyncMcpToolCallbackProvider mcpTools) {

        return builder
            .defaultSystem("""
                You are a helpful product pricing assistant.

                Use available tools when the user asks
                for product prices or discounts.

                Do not invent product information.
                Explain results clearly and concisely.
                """)
            .defaultToolCallbacks(mcpTools)
            .build();
    }
}
```

The `SyncMcpToolCallbackProvider` is automatically configured by the Spring AI MCP client starter when synchronous MCP clients are enabled. It exposes the tools discovered from the connected MCP server.

The `defaultSystem()` instruction guides the model's behavior, while `defaultToolCallbacks(mcpTools)` registers the MCP tools with `ChatClient`. We do not explicitly reference `calculatePrice` anywhere in this configuration. The tool is discovered dynamically from the MCP server.

### Create the AI service

`AiChatService.java`

```java
package com.example.mcp_ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
```

This service contains no business logic related to discounts. It sends the user's question to the model and returns the final response. When the model requests an MCP tool, Spring AI handles the intermediate tool-calling workflow through the registered callbacks.

This is a significant difference from the MCP clients we created earlier. Previously, we explicitly selected the tool and supplied its arguments in Java. Here, the model can determine which tool to call based on natural language.

### Expose a REST endpoint

`ChatController.java`

```java
package com.example.mcp_ai.controller;

import com.example.mcp_ai.service.AiChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AiChatService chatService;

    public ChatController(AiChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String answer = chatService.ask(request.message());

        return new ChatResponse(answer);
    }

    public record ChatRequest(String message) {}

    public record ChatResponse(String answer) {}
}
```

The REST controller accepts a JSON request containing a natural-language message. It delegates processing to `AiChatService` and returns the generated response as JSON. The controller does not know whether the model used a tool, which tool was selected, or how many intermediate calls occurred.

For production applications, we would also add request validation, authentication, exception handling, and limits on model/tool execution.

### Main application

`McpAiApplication.java`

```java
package com.example.mcp_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(
            McpAiApplication.class,
            args
        );
    }
}
```

## 5. Running and testing

We will start the MCP server first, then the AI application. This ensures that the MCP client can initialize its connection and discover tools when the chatbot starts.

### Terminal 1 — Start the MCP server

From the existing `mcp-demo` project:

```sh
cd ~/projects/mcp-demo

./mvnw spring-boot:run
```

Confirm that the server is listening on port 9091 and that `calculatePrice` is registered as an MCP tool. You can verify discovery using MCP Inspector.

```sh
npx @modelcontextprotocol/inpector
```

### Terminal 2 — Start the AI application

Start the AI client:

```sh
cd ~/projects/mcp-ai-client

./mvnw spring-boot:run
```

The application should start on port 9099 and initialize its MCP client connection.

### Terminal 3 — Send a chat request

```
curl -X POST http://localhost:9099/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Calculate the final price of a product costing 200 euros with a 15 percent discount."
  }'
```

An expected response is:

```
{
  "answer": "The original price is €200. After applying a 15% discount, the final price is €170."
}
```

The exact wording may vary because the response is generated by an LLM. However, the calculation should come from our MCP tool rather than from a hardcoded formula in the AI application.

### Test without a tool

Now try a general question:

```
curl -X POST http://localhost:9099/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Explain what a percentage discount means."
  }'
```

The model should generally answer directly without invoking `calculatePrice`, because the question asks for an explanation rather than a calculation. This demonstrates that tools are available to the model but do not necessarily need to be executed for every request.

## 6. Debugging tool discovery and execution

When working with MCP and LLMs, it is useful to distinguish between two types of problems: MCP connection failures and model tool-selection problems. If the MCP client cannot connect to the server, the tools may never become available. If the connection succeeds but the model does not call a tool, the problem may be related to tool descriptions, system instructions, or the model's selection behavior.

We can enable additional logging in `application.yml`:

```
logging:
  level:
    org.springframework.ai: DEBUG
    io.modelcontextprotocol: DEBUG
```

We can also inspect the tools discovered by the MCP client at startup.

`McpToolLogger.java`

```java
package com.example.mcp_ai.config;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolLogger {

    @Bean
    ApplicationRunner logMcpTools(
            SyncMcpToolCallbackProvider provider) {

        return args -> {
            for (var tool : provider.getToolCallbacks()) {
                System.out.println(
                    "MCP Tool: " +
                    tool.getToolDefinition().name()
                );
            }
        };
    }
}
```

Expected output:

```
MCP Tool: calculatePrice
```

If your server exposes additional tools, they should also appear in this list.

This is especially useful when troubleshooting errors such as `Unknown tool`, because it allows us to verify the actual registered names rather than assuming that a particular tool is available.