## 1. Spring Boot Setup on Ubuntu

Spring Boot is a good choice for implementing MCP servers because it already provides dependency injection, configuration management, HTTP endpoints, security, and integration with databases and external services. Spring AI adds support for MCP, allowing Java applications to expose tools and other MCP capabilities without manually implementing the underlying JSON-RPC protocol.

We'll use Java 21, Spring Boot 4, Maven, and Spring AI MCP Server. Java 21 is an LTS release and works with Spring Boot 4. Maven will manage dependencies and build the executable JAR. Although Spring AI also supports Gradle, we'll use Maven here to keep the initial setup straightforward.

Verify the development environment:

```
java -version
javac -version
mvn -version
git --version
```

## 2. Spring AI MCP Dependencies

Spring AI is a framework that integrates AI capabilities into Spring applications. It provides support for language models, embeddings, vector databases, tool calling, and MCP clients and servers. For this project, we only need the MCP server functionality. We do not need an OpenAI API key, an LLM, or a database to build our first MCP server.

Spring AI offers different MCP server starters depending on the transport. We'll use `spring-ai-starter-mcp-server-webmvc` with Streamable HTTP. This integrates with Spring MVC and exposes an MCP endpoint through Spring Boot's embedded web server. Spring AI 2.0 supports Spring Boot 4 and provides automatic configuration for this setup.

### Step 1: Choose the MCP dependency

|Dependency|Purpose|
|---|---|
|`spring-ai-starter-mcp-server`|stdio MCP server|
|`spring-ai-starter-mcp-server-webmvc`|HTTP server using Spring MVC|
|`spring-ai-starter-mcp-server-webflux`|Reactive HTTP MCP server|
|`spring-ai-starter-mcp-client`|MCP client implementation|

For our example:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

### Step 2: Create the project

Open [Spring Initializr](https://start.spring.io/) and configure:

|Property|Value|
|---|---|
|Project|Maven|
|Language|Java|
|Spring Boot|4.0.x|
|Group|`com.example`|
|Artifact|`mcp-demo`|
|Packaging|Jar|
|Java|21|

Generate the project with Spring Web MVC, or create a basic Maven project and add the MCP starter manually.

Download and extract it on Ubuntu:

```
unzip mcp-demo.zip
cd mcp-demo
```

### Step 3: Configure `pom.xml`

Replace the generated `pom.xml` with:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="
           http://maven.apache.org/POM/4.0.0
           https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>mcp-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.6</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>21</java.version>
        <spring-ai.version>2.0.1</spring-ai.version>
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
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
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

The Spring Boot parent manages the Spring Boot dependencies and Maven build configuration. The Spring AI BOM manages compatible versions of the Spring AI libraries, so we don't need to specify a version for each Spring AI dependency.

The MCP WebMVC starter supplies the HTTP server integration. We don't need to add a separate REST controller or implement JSON-RPC message handling ourselves.

## 3. Project Structure and Configuration

Our MCP server will follow a conventional Spring Boot project structure. The application entry point initializes Spring Boot, the configuration file controls the MCP transport and server settings, and a dedicated package will contain the tools exposed to MCP clients.

For now, we'll keep the project minimal. We won't implement the actual tool, later we'll create a Hello World MCP server and test it using MCP Inspector.

### Project structure

```
mcp-demo/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/mcpdemo/
    │   │       ├── McpDemoApplication.java
    │   │       └── tools/
    │   │           └── (HelloTools.java - next lesson)
    │   │
    │   └── resources/
    │       └── application.yml
    │
    └── test/
        └── java/
            └── com/example/mcpdemo/
                └── McpDemoApplicationTests.java
```

### Step 1: Application entry point

`McpDemoApplication.java`

```java
package com.example.mcpdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(
            McpDemoApplication.class,
            args
        );
    }
}
```

This is a standard Spring Boot application. The `@SpringBootApplication` annotation enables component scanning and auto-configuration, including the MCP server configuration provided by Spring AI.

### Step 2: Configure Streamable HTTP

`src/main/resources/application.yml`

```yml
server:
  port: 9092
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
        capabilities:
          tool: true
          resource: false
          prompt: false
          completion: false
        streamable-http:
          mcp-endpoint: /mcp
```

The important configuration properties are:

|Property|Explanation|
|---|---|
|`server.port`|Spring Boot HTTP port|
|`server.address`|Bind only to localhost|
|`server.name`|MCP server identification|
|`server.type`|Synchronous or asynchronous execution|
|`server.protocol`|MCP transport protocol|
|`capabilities.tool`|Enable tool support|
|`mcp-endpoint`|HTTP endpoint for MCP|

The MCP-specific properties are documented in the Spring AI server configuration reference.  
https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html

We'll use port `9091` to avoid conflicts with other local Spring Boot applications.

The MCP endpoint will be:

```txt
http://localhost:9091/mcp
```

Unlike a conventional REST endpoint, `/mcp` accepts MCP protocol messages. You should not expect a normal JSON response simply by opening that URL in a browser.

Binding to `127.0.0.1` keeps the server local during development. Spring AI's HTTP MCP starter does not enable authentication automatically, so it should not be exposed publicly without additional security controls.

## 4. Build and Run the MCP Project

We can now compile and start our MCP server. Even though we haven't created any custom tools yet, Spring AI will initialize the MCP infrastructure and expose the configured HTTP transport.

### Step 1: Build

From the project root:

```sh
./mvnw clean package
```

Or, using the system Maven installation:

```sh
mvn clean package
```

Maven will download dependencies, compile Java classes, run tests, and create the executable JAR.

Expected output:

```
[INFO] BUILD SUCCESS
```

### Step 2: Start the application

```sh
./mvnw spring-boot:run
```

Alternatively:

```
java -jar target/mcp-demo-0.0.1-SNAPSHOT.jar
```

The application should start its embedded HTTP server on port `9091`.

### Step 3: Verify the server port

Open another terminal:

```sh
ss -ltnp | grep 9091

	LISTEN   0        100        [::ffff:127.0.0.1]:9091 
```

You can also verify that the endpoint is reachable:

```sh
curl -i http://localhost:9091/mcp

	HTTP/1.1 400 
	Content-Type: text/plain;charset=UTF-8
	Content-Length: 49
	Date: Fri, 09 Oct 2026 13:58:01 GMT
	Connection: close
	
	Invalid Accept header. Expected TEXT_EVENT_STREA
```

A response such as HTTP `405 Method Not Allowed` or another MCP transport-specific response can be normal for a bare GET request. The endpoint expects correctly formatted MCP communication, including initialization.

Important: A successful Spring Boot startup only confirms that the application is running. We will verify the actual MCP protocol and tool execution later.