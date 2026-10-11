## 1. Building a Hello World MCP Server

An MCP server is an application that exposes capabilities to MCP clients through a standardized protocol. Unlike a conventional REST controller, which defines HTTP endpoints such as `GET /api/products`, an MCP server exposes named tools that clients can discover and invoke through JSON-RPC messages. Spring AI handles the protocol communication, allowing us to concentrate on implementing Java methods.

We'll create a tool called `say_hello` that accepts a person's name and returns a greeting. Although the operation is simple, it demonstrates the complete MCP execution flow: an MCP client connects to the server, discovers the tool, sends its arguments, and receives the execution result. Later, we can replace the greeting logic with database queries, REST API calls, or monitoring operations without changing the basic architecture.

![[mcp_execution_flow.png]]

### Project structure

We'll extend the existing project:

```
mcp-demo/
├── pom.xml
├── mvnw
├── src/
│   ├── main/
│   │   ├── java/com/example/mcpdemo/
│   │   │   ├── McpDemoApplication.java
│   │   │   └── tools/
│   │   │       └── HelloTools.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
└── target/
```

The `tools` package will contain the methods exposed to MCP clients. We keep these methods separate from the main application class so that the project can grow to include additional tools.

## 2. Exposing a Simple MCP Tool

Spring AI provides annotations for describing tools and their arguments. The annotation tells the framework which Java method should be available to MCP clients, while the description explains what the tool does. This metadata is important because AI applications use tool names, descriptions, and parameter schemas to understand when and how a tool should be invoked.

We'll use the MCP-specific `@McpTool` and `@McpToolParam` annotations. The tool receives a `String` argument named `name` and returns a greeting. Spring AI generates the tool's input schema from the Java method signature and exposes it through MCP tool discovery.

### Step 1: Create `HelloTools.java`

`src/main/java/com/example/mcpdemo/tools/HelloTools.java`

```java
package com.example.mcpdemo.tools;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class HelloTools {

    @McpTool(
        name = "say_hello",
        description = "Generate a greeting for a person"
    )
    public String sayHello(
        @McpToolParam(
            description = "Name of the person",
            required = true
        )
        String name
    ) {
        return "Hello, " + name + "!";
    }
}
```

The `@Component` annotation makes `HelloTools` a Spring-managed bean. The `@McpTool` annotation marks the method as an MCP operation, and `@McpToolParam` provides metadata about its input parameter.

The important distinction is that `sayHello()` remains an ordinary Java method. It does not know anything about HTTP, JSON-RPC, or MCP sessions. Spring AI takes care of converting incoming MCP tool calls into Java method invocations and converting the returned value into an MCP tool result.

### Step 2: Register the tool

With Spring AI 2's MCP annotation support, annotated tools can be discovered through the MCP annotation scanner when the relevant annotation infrastructure is enabled. Make sure your project includes the MCP annotations support alongside the MCP server starter.

If your Spring AI version or configuration does not enable annotation scanning automatically, you can use the established `@Tool` plus `MethodToolCallbackProvider` registration approach instead. Do not register the same tool using both approaches, as that may produce duplicate tool names.

For our project, the MCP server should expose one tool:

```
say_hello
```

Its conceptual input schema is:

```json
{
  "type": "object",
  "properties": {
    "name": {
      "type": "string",
      "description": "Name of the person"
    }
  },
  "required": ["name"]
}
```

This schema is what allows an MCP client to discover the method's required arguments.

Spring AI 2 automatically scans Spring beans containing `@McpTool` methods, so no additional registration bean is needed with the standard MCP starter configuration.  

https://docs.spring.io/spring-ai/reference/api/mcp/mcp-annotations-overview.html

## 3. Running the MCP Server Locally

Our server uses Streamable HTTP, so Spring Boot will expose an MCP endpoint on port `9091`. Unlike a stdio server, we start this application as a normal HTTP service. MCP clients can then connect to it using the configured `/mcp` endpoint.

We don't need to start an LLM, connect to OpenAI, or configure a database. The Hello World operation executes entirely within our Java application.

### Step 1: Verify `application.yml`

Keep the configuration from lesson 04:

```yml
server:
  port: 9091
  address: 127.0.0.1

spring:
  application:
    name: mcp-demo

  ai:
    mcp:
      server:
        enabled: true
        name: mcp-demo-server
        version: 1.0.0
        type: SYNC
        protocol: STREAMABLE
        annotation-scanner:
          enabled: true
        capabilities:
          tool: true
          resource: false
          prompt: false
          completion: false
        streamable-http:
          mcp-endpoint: /mcp
```

### Step 2: Compile and run

From the project directory:

```sh
cd mcp-demo

mvn clean package
mvn spring-boot:run
```

In a second terminal:

```sh
ss -ltnp | grep 9091

curl -i http://localhost:9091/mcp
```

The endpoint does not behave like a conventional REST GET endpoint. We need an MCP client to perform the initialization handshake and invoke tools.


## 4. Testing with MCP Inspector

MCP Inspector is an official debugging tool for MCP servers. It provides a browser-based interface that allows developers to connect to a server, discover tools, inspect input schemas, execute operations, and examine responses. It supports both stdio and Streamable HTTP transports.

Inspector is particularly useful during development because it works without an AI model. We can test whether our Java MCP server behaves correctly before connecting it to ChatGPT, Claude, or another AI application.

https://github.com/microsoft/mcp-for-beginners/blob/main/03-GettingStarted/13-mcp-inspector/README.md

### Step 1: Install Node.js if necessary

MCP Inspector runs using Node.js. This does not mean our MCP server requires JavaScript; Node.js is only needed for the testing tool.

Check:

```sh
node --version
npm --version
```

Use a current Node.js LTS version compatible with Inspector. Node.js 22 or newer is a suitable choice.

If you already have Node.js installed, continue to the next step.

### Step 2: Start MCP Inspector

Open a new terminal while the Spring Boot server is running:

```sh
npx @modelcontextprotocol/inspector
```

The Inspector starts a local web interface, normally available at:

```
http://localhost:6274
```

Open the URL displayed in your terminal. Depending on the Inspector version, the terminal may also provide a session-specific proxy token or an authenticated URL. Use the provided URL when required.

### Step 3: Connect to our server

Configure Inspector with these values:

|Setting|Value|
|---|---|
|Transport Type|Streamable HTTP|
|URL|`http://localhost:9091/mcp`|
|Authentication|None, local development|

Click Connect.

Inspector should initialize the MCP connection and display the server's available capabilities.

**Error:** 
Connection Error - Did you add the proxy session token in Configuration?
When you started MCP Inspector on Ubuntu:

```
npx @modelcontextprotocol/inspector
```

Look in the terminal for output similar to:

```
Starting MCP inspector...
Proxy server listening on localhost:6277

Session token: abc123...
```

### Step 4: Discover available tools

Open the Tools section and click List Tools.

You should see:

say_hello

Generate a greeting for a person

Input schema

```
{
  "name": "string"
}
```

The `name` argument is required.

The tool appears because Spring AI discovered the `@McpTool` method and registered it with the MCP server.

### Step 5: Execute the tool

Select `say_hello`, enter:

```
name = John
```

Click Run Tool.

Expected response:

```json
{
  "content": [
    {
      "type": "text",
      "text": "Hello, John!"
    }
  ],
  "isError": false
}
```

The Inspector may display the response as formatted text rather than raw JSON.

![[mcp_inspector.png]]


## 5. Testing from the Command Line

We can also use MCP Inspector's CLI without opening the browser. This is useful for automation, scripting, and testing MCP servers on machines without a graphical interface.

List available tools

```sh
npx @modelcontextprotocol/inspector --cli \
  http://localhost:9091/mcp \
  --transport http \
  --method tools/list
```

Response:

```json
{
  "tools": [
    {
      "name": "say_hello",
      "title": "say_hello",
      "description": "Generate a greeting for a person",
      "inputSchema": {
        "type": "object",
        "properties": {
          "name": {
            "type": "string",
            "description": "Name of the person"
          }
        },
        "required": [
          "name"
        ],
        "$schema": "https://json-schema.org/draft/2020-12/schema"
      },
      "annotations": {
        "title": "",
        "readOnlyHint": false,
        "destructiveHint": true,
        "idempotentHint": false,
        "openWorldHint": true
      }
    }
  ]
}
```

Execute `say_hello`

```sh
npx @modelcontextprotocol/inspector --cli \
  http://localhost:9091/mcp \
  --transport http \
  --method tools/call \
  --tool-name say_hello \
  --tool-arg name=John
```

Response:

```json
{
  "content": [
    {
      "type": "text",
      "text": "Hello, John!"
    }
  ],
  "isError": false
}
```

The CLI manages the MCP connection and initialization for us.  

This is much easier than manually constructing JSON-RPC initialization requests, handling session IDs, and invoking the tool using `curl`.  

https://github.com/modelcontextprotocol/inspector/blob/main/clients/cli/README.md

## 5. Practice

Extend `HelloTools.java` with a second tool:

```java
@McpTool(name = "add_numbers", description = "Add two integer numbers")
public int addNumbers(
    @McpToolParam(description = "First number") int a,
    @McpToolParam(description = "Second number") int b
) {
    return a + b;
}
```

Restart the Spring Boot application and use MCP Inspector to discover and invoke `add_numbers` with `a = 10` and `b = 20`.

Expected result:

```
30
```

This exercise demonstrates that a single MCP server can expose multiple tools, each with its own name, description, parameters, and return type.