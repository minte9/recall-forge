## 1. JSON-RPC 2.0

Model Context Protocol uses JSON-RPC 2.0 to structure communication between MCP clients and servers. JSON-RPC is a lightweight Remote Procedure Call protocol that represents operations as JSON messages. Instead of identifying an operation through a URL and HTTP method, as REST commonly does, JSON-RPC uses a `method` field to identify the operation being requested.

JSON-RPC defines three main message types: requests, responses, and notifications. Requests contain an `id` so the client can match the response to the original request. Responses contain the same `id` and either a `result` or an `error`. Notifications do not contain an `id` and therefore do not expect a response. MCP builds on these concepts to define operations such as `initialize`, `tools/list`, and `tools/call`.

### Example: JSON-RPC request

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "say_hello",
    "arguments": {
      "name": "John"
    }
  }
}
```

The MCP server might return:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Hello, John!"
      }
    ],
    "isError": false
  }
}
```

Notice that the response does not contain the original method name. Instead, the client identifies the response using the matching request `id`.

### Example: JSON-RPC notification

```json
{
  "jsonrpc": "2.0",
  "method": "notifications/initialized"
}
```

This message does not have an `id` because it is a notification. The sender does not expect a response.

### Java example: creating a JSON-RPC message

We can represent a JSON-RPC request using a Java record and serialize it with Jackson.

`JsonRpcRequest.java`

```java
import java.util.Map;

public record JsonRpcRequest(
    String jsonrpc,
    int id,
    String method,
    Map<String, Object> params
) {}
```

`Main.java`

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class Main {

    public static void main(String[] args)
            throws Exception {

        JsonRpcRequest request = new JsonRpcRequest(
            "2.0",
            1,
            "tools/call",
            Map.of(
                "name", "say_hello",
                "arguments", Map.of("name", "John")
            )
        );

        ObjectMapper mapper = new ObjectMapper();

        System.out.println(
            mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(request)
        );
    }
}
```

This example only creates a JSON-RPC message. It does not implement an MCP client or server. Later, Spring AI will handle JSON-RPC serialization, request processing, and protocol communication for us.

## 2. Standard Input/Output (stdio)

The stdio transport allows an MCP client to communicate with an MCP server through the operating system's standard input and standard output streams. Typically, the MCP Host starts the server as a child process. The client writes JSON-RPC messages to the server's standard input (`stdin`), and the server writes JSON-RPC responses to standard output (`stdout`).

This transport is particularly useful for local integrations. For example, an AI coding assistant can launch a Java application on your Ubuntu machine and interact with tools that inspect files, query a local database, or analyze a Git repository. Because communication happens through process streams, the MCP server does not need to open a TCP port or expose an HTTP endpoint.

![[mcp_stdio.png]]

### Java example: reading and writing through stdio

The following program demonstrates the basic communication mechanism used by stdio applications.

`StdioExample.java`

```java
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class StdioExample {

    public static void main(String[] args)
            throws Exception {

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in)
        );

        String line;

        while ((line = reader.readLine()) != null) {

            // Diagnostic messages go to stderr
            System.err.println("Received: " + line);

            // Protocol output goes to stdout
            System.out.println(line);
            System.out.flush();
        }
    }
}
```

Compile and run on Ubuntu:

```sh
javac StdioExample.java

java StdioExample
```

Enter:

```json
{"jsonrpc":"2.0","id":1,"method":"ping"}
```

The program echoes the same message.

This is only a demonstration of process communication, not a valid MCP server implementation. A real MCP server must implement the initialization handshake, supported protocol methods, and valid responses.

### Important: stdout vs stderr

When implementing a stdio MCP server, standard output must be reserved for valid MCP protocol messages. Ordinary application logs, debugging messages, and Spring Boot startup banners can corrupt the communication stream.

For example:

```java
// Incorrect for stdio MCP logging
System.out.println("Server started");

// Correct
System.err.println("Server started");
```

In a Spring Boot stdio server, you should also disable the startup banner and configure logging appropriately.

```yml
spring:
  main:
    banner-mode: off
    web-application-type: none

logging:
  pattern:
    console:
```

The exact logging configuration will be covered in the Spring Boot setup lesson. The objective is to ensure that the server never writes non-protocol messages to stdout.

## 3. Streamable HTTP

Streamable HTTP is the network-oriented transport used by modern MCP implementations. Instead of starting a local process and communicating through standard streams, an MCP client sends JSON-RPC messages to an HTTP endpoint exposed by the MCP server. This makes it suitable for Spring Boot applications running locally, in Docker containers, or on remote servers.

Streamable HTTP supports ordinary HTTP responses as well as Server-Sent Events (SSE). A server can return a JSON response directly or use an SSE stream when streaming is appropriate. It can also support sessions, allowing the server to associate multiple requests with a particular MCP connection. The MCP protocol defines the message exchange, while HTTP provides the underlying transport.

![[mcp_host.png]]

### Example: HTTP initialization request

Suppose a Spring Boot MCP server is running at:

```sh
http://localhost:8080/mcp
```

A client begins by sending an initialization request:

```sh
curl -i -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2025-11-25",
      "capabilities": {},
      "clientInfo": {
        "name": "curl-demo",
        "version": "1.0.0"
      }
    }
  }'
```

The server responds with its supported protocol version and capabilities.

If the server uses session management, the response may also include a header such as:

```json
Mcp-Session-Id: example-session-id
```

The client must preserve this session ID and include it in subsequent requests when required.

### Example: listing tools

After successful initialization and the `notifications/initialized` notification, the client can request the tool list:

```sh
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "MCP-Protocol-Version: 2025-11-25" \
  -H "Mcp-Session-Id: example-session-id" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }'
```

The session ID above is illustrative and must be replaced with one returned by the server. If the server operates without sessions, the session header is unnecessary.

For real applications, an MCP SDK or a tool such as MCP Inspector is preferable to manually managing the handshake and headers.

### What about SSE?

Server-Sent Events allow a server to send a stream of events over an HTTP connection. In MCP, SSE is one of the response mechanisms supported by Streamable HTTP.

A simplified SSE message might look like:

```js
event: message
data: {"jsonrpc":"2.0","id":2,"result":{"tools":[]}}
```

Streamable HTTP is different from the older HTTP+SSE transport. New MCP implementations should generally use Streamable HTTP, while legacy HTTP+SSE may still appear in older tutorials and integrations.

## 4. Local vs Remote MCP Servers

An MCP server can run locally on the same machine as the AI application or remotely on another computer. This deployment choice is separate from the MCP protocol itself. Local servers are commonly accessed through stdio, while remote servers typically use Streamable HTTP. However, a Streamable HTTP server can also run locally on `localhost`.

Local MCP servers are convenient for developer tools because they can access local files, Git repositories, and applications without requiring a public network endpoint. Remote MCP servers are more suitable for shared enterprise integrations, cloud applications, and services used by multiple users. Remote deployment introduces additional considerations such as authentication, network security, authorization, monitoring, and session management.

### Comparison

|Feature|Local MCP Server|Remote MCP Server|
|---|---|---|
|Location|User's machine|VPS, cloud, internal server|
|Typical transport|stdio|Streamable HTTP|
|Network access|Not required for stdio|Required|
|Installation|Usually per user/device|Centrally deployed|
|Scaling|Local processes|Shared server infrastructure|
|Security|Local process permissions|TLS, authentication, authorization|
|Example|Local Git analysis|Enterprise monitoring|

### Example A: local Java MCP server

```sh
java -jar target/mcp-server.jar
```

The MCP Host can launch this application and communicate with it through stdio.

A local configuration might conceptually look like:

```json
{
  "mcpServers": {
    "java-tools": {
      "command": "java",
      "args": [
        "-jar",
        "/home/user/mcp-server.jar"
      ]
    }
  }
}
```

This is an illustrative configuration format used by some MCP Hosts. The exact configuration depends on the application.

### Example B: remote Spring Boot MCP server

A Spring Boot server deployed on a VPS could expose:

```txt
https://mcp.example.com/mcp
```

The AI application connects through Streamable HTTP.

For a production deployment, the architecture might be:

```txt
AI Application
      |
      | HTTPS
      v
Traefik Reverse Proxy
      |
      | HTTP (private Docker network)
      v
Spring Boot MCP Server
      |
      v
Existing Business Services
      |
      v
PostgreSQL / MongoDB
```

This architecture fits naturally with Docker and Traefik. The reverse proxy can handle TLS termination, while Spring Boot implements the MCP protocol and exposes selected application capabilities.

**Security note:** Do not expose unrestricted database queries, shell execution, or administrative operations through public MCP tools. Authentication, user-specific authorization, input validation, and audit logging remain necessary. For HTTP servers, validate request origins and protect local endpoints against DNS rebinding attacks.