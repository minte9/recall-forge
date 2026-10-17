## 1. MCP client with Java

We'll create a separate Spring Boot application that connects to the MCP server.  The goal is to discover tools programmatically, without using MCP Inspector or an LLM. 

An MCP client is a Java application that communicates with an MCP server using the MCP protocol. In Spring AI, the MCP Java SDK provides classes such as `McpSyncClient` for synchronous communication and `McpAsyncClient` for asynchronous communication. These clients handle protocol initialization, tool discovery, resource access, and tool execution. 

We'll create a new project named `mcp-client-demo`, separate from your existing `mcp-demo` server. This separation is important because the client and server are independent applications: the server exposes capabilities, while the client discovers and consumes them. Unlike an AI chatbot, this client will directly invoke MCP operations without requiring an OpenAI API key. 
### Project structure

```txt
mcp-client-demo/
├── pom.xml
└── src/
    └── main/
        ├── java/com/example/mcpclient/
        │   ├── McpClientApplication.java
        │   └── runner/
        │       └── ToolDiscoveryRunner.java
        └── resources/
            └── application.yml
```

### Maven dependencies

We need the Spring AI MCP client starter, which provides MCP client auto-configuration and integration with Spring Boot.

`pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="
           http://maven.apache.org/POM/4.0.0
           https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>mcp-client-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.6</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>21</java.version>
        <spring-ai.version>1.1.0</spring-ai.version>
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
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-mcp-client</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

The important dependency is `spring-ai-starter-mcp-client`. It provides client auto-configuration, connection management, and support for both stdio and Streamable HTTP. The Spring AI BOM manages compatible versions of its libraries.

`McpClientApplication.java`

```java
package com.example.mcpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(
            McpClientApplication.class, args
        );
    }
}
```

This is a standard Spring Boot application. Unlike the MCP server, it does not expose tools using `@Tool`. Its responsibility is to consume tools exposed by other applications.

Spring AI can automatically create MCP client instances from `application.yml`. You specify the transport, server address, and client settings, and Spring Boot manages the connection lifecycle.

For this example, we'll use a synchronous client (`SYNC`). A synchronous client waits for a response before continuing, making it appropriate for learning and straightforward service integrations. An asynchronous client (`ASYNC`) uses a reactive programming model and can be useful for non-blocking applications.

`src/main/resources/application.yml`

```yml
spring:
  application:
    name: mcp-client-demo

  main:
    web-application-type: none

  ai:
    mcp:
      client:
        enabled: true
        name: java-learning-client
        version: 1.0.0
        type: SYNC
        initialized: true
        request-timeout: 20s

        streamable-http:
          connections:
            calculator-server:
              url: http://localhost:9091
              endpoint: /mcp
```

Explanation of the configuration:

`enabled`  -  Enables MCP client auto-configuration
`name`  -  Identifies the client during initialization
`type`  -  Selects synchronous or asynchronous operation
`initialized`  -  Automatically initializes the client
`request-timeout`  -  Maximum duration for requests
`connections`  -  Defines named MCP server connections
`url`  -  Base URL of the MCP server
`endpoint`  -  MCP protocol endpoint

The `web-application-type: none` setting prevents Spring Boot from starting an embedded web server. Our application only needs to connect to another server and execute code.

https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html

## 2. Connecting to server

MCP supports both local process communication and network communication. Although they use the same protocol operations, the connection setup is different.

With Streamable HTTP, the MCP server runs independently and listens for requests on an HTTP endpoint. The client connects to it using a URL, much like a REST client, but exchanges standardized MCP messages rather than application-specific REST requests.

First, ensure your existing MCP server is configured to use Streamable HTTP. 

MCP server — `application.yml`

```yml
server:
  port: 9091

spring:
  ai:
    mcp:
      server:
        name: calculator-server
        version: 1.0.0
        type: SYNC
        protocol: STREAMABLE
```

This assumes your server uses the `spring-ai-starter-mcp-server-webmvc` dependency and has already registered its tools. The default Streamable HTTP endpoint is `/mcp`.

Start the server in the first Ubuntu terminal:

```sh
cd ~/projects/mcp-demo

mvn spring-boot:run
```

Then start the client in a second terminal:

```sh
cd ~/projects/mcp-client-demo

mvn spring-boot:run
```

The client will connect to `http://localhost:9091/mcp` during startup.

## 3. Listing available tools

Now we'll implement a Java class that discovers the tools exposed by our MCP server.

Spring AI automatically creates an `McpSyncClient` for each configured connection. We can inject these clients into any Spring-managed component and call methods such as `listTools()`.

ToolDiscoveryRunner.java

```java
package com.example.mcpclient.runner;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolDiscoveryRunner implements CommandLineRunner {

    private final List<McpSyncClient> clients;

    public ToolDiscoveryRunner(
            List<McpSyncClient> clients) {
        this.clients = clients;
    }

    @Override
    public void run(String... args) {

        System.out.println("MCP clients: " + clients.size());

        for (McpSyncClient client : clients) {

            System.out.println("\nConnected server:");

            var serverInfo = client.getServerInfo();

            System.out.println(
                "Name: " + serverInfo.name()
            );

            System.out.println(
                "Version: " + serverInfo.version()
            );

            McpSchema.ListToolsResult result =
                client.listTools();

            for (McpSchema.Tool tool : result.tools()) {

                System.out.println(
                    "\nTool: " + tool.name()
                );

                System.out.println(
                    "Description: " + tool.description()
                );

                System.out.println(
                    "Input schema: " + tool.inputSchema()
                );
            }
        }
    }
}
```

Spring AI automatically creates and initializes MCP clients using the connections configured in `application.yml`. We inject a list because an application can communicate with multiple MCP servers.

```java
private final List<McpSyncClient> clients;
```

During initialization, the server provides its name and version. The client stores this information, allowing us to identify the connected server.

```java
var serverInfo = client.getServerInfo();
```

This sends the MCP `tools/list` request to the server. The server returns its registered tools, including their names, descriptions, and input schemas.

```java
McpSchema.ListToolsResult result =
    client.listTools();
```

Each tool contains metadata describing how it can be called. The input schema defines its accepted arguments using JSON Schema.

```java
for (McpSchema.Tool tool : result.tools()) {
    System.out.println(tool.name());
    System.out.println(tool.inputSchema());
}
```

The code above retrieves one page of tools. For servers with paginated tool lists, later lessons can extend it to follow pagination cursors.
### Run the application

Make sure the MCP server is running, then execute:

```sh
mvn clean spring-boot:run
```

Expected output, assuming your server exposes the calculator and pricing tools:

```
MCP clients: 1

Connected server:
Name: calculator-server
Version: 1.0.0

Tool: add
Description: Add two numbers
Input schema: ...

Tool: calculatePrice
Description: Calculate a product price
Input schema: ...
```

The exact tool names depend on your server registration.

Notice that we never instantiated `CalculatorTools` or `PricingTools` directly. The client knows nothing about their implementation. It only knows the tool definitions returned through MCP.

## 4. Practical experiment — Add another tool

To verify that discovery is dynamic, add a new tool to your existing MCP server.

MCP server — `TextTools.java`

```java
package com.example.mcp_demo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class TextTools {

    @Tool(description = "Convert text to uppercase")
    public String uppercase(String text) {
        return text.toUpperCase();
    }
}
```

Register this tool with your MCP server's tool callback provider, using the same mechanism you used for the calculator and pricing tools. For example:

```java
@Bean
ToolCallbackProvider toolCallbackProvider(
        CalculatorTools calculatorTools,
        PricingTools pricingTools,
        TextTools textTools) {

    return MethodToolCallbackProvider.builder()
        .toolObjects(
            calculatorTools,
            pricingTools,
            textTools
        )
        .build();
}
```

Restart the MCP server, then rerun the client.

You should now see the additional tool:

```txt
Tool: uppercase
Description: Convert text to uppercase
Input schema: ...
```

The MCP client did not require any code changes. It discovered the new tool automatically when it connected and requested the tool list.

This is one of MCP's main advantages: clients can discover server functionality without being tightly coupled to its implementation.

One important detail: `http://localhost:9091/mcp` is not an ordinary REST endpoint. Opening it in a browser is not a reliable test because MCP requests require specific methods, headers, and JSON-RPC messages. Use your Java client or MCP Inspector to verify the connection.

Also, when moving beyond local development, secure the HTTP endpoint. Spring AI's MCP server starter does not automatically enforce authentication or authorization.

https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html

