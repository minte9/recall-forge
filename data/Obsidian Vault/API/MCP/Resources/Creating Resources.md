## 1. Exposing static resources

A static resource contains predefined information that does not depend on request parameters. Typical examples include application documentation, API specifications, instructions, and Markdown files.

In Spring AI, resources can be registered through `McpServerFeatures.SyncResourceSpecification`. Each resource has a URI, a name, a MIME type, and a callback responsible for returning its content.

**Example:** Application documentation

Create:

`src/main/java/com/example/mcp_demo/resources/DocumentationResources.java`

```java
package com.example.mcp_demo.resources;

import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationResources {

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification>
            staticResources() {

        var resource = McpSchema.Resource.builder()
				.uri("docs://application/overview")
				.name("Application Overview")
				.description("General information about the application")
				.mimeType("text/markdown")
				.build();

        var specification =
            new McpServerFeatures().SyncResourceSpecification(
                resource,
                (exchange, request) -> {

                    String content = """
                        # MCP Demo Application

                        This application demonstrates MCP integration
                        with Java and Spring Boot.

                        ## Features

                        - MCP Tools
                        - MCP Resources
                        - MCP Prompts

                        ## Technology

                        - Java 21
                        - Spring Boot
                        - Spring AI
                        """;

                    return new McpSchema.ReadResourceResult(
                        List.of(
                            new McpSchema.TextResourceContents(
                                request.uri(),
                                "text/markdown",
                                content
                            )
                        )
                    );
                }
            );

        return List.of(specification);
    }
}
```

The `McpSchema.Resource` object describes the resource to MCP clients. The URI `docs://application/overview` identifies it, while `text/markdown` tells clients how to interpret the returned content.

The callback is executed when a client requests the resource using `resources/read`. It receives the requested URI and returns a `ReadResourceResult` containing one or more resource contents. Although the Markdown is hardcoded here, the same callback can retrieve information from files, services, or databases.

Verify your `application.yml`:

```yml
spring:
  ai:
    mcp:
      server:
        name: mcp-demo
        version: 1.0.0
        type: SYNC
        protocol: STREAMABLE
        capabilities:
          tool: true
          resource: true
          prompt: true
```

This configuration enables MCP resource capability and keeps the server synchronous, matching the `SyncResourceSpecification` implementation.

Important: The examples assume a compatible Spring AI MCP starter and MCP Java SDK version. If your project uses a different version, the resource specification constructors or configuration properties may differ.

Start your application and launch MCP Inspector:

```sh
./mvnw spring-boot:run

npx @modelcontextprotocol/inspector
```

In the Resources tab, list available resources and select:

```
Application Overview
```

You should receive the Markdown content defined in the callback.

```json
{
  "contents": [
    {
      "uri": "docs://application/overview",
      "mimeType": "text/markdown",
      "text": "#
	       MCP Demo Application
      
		   This application demonstrates MCP integration
		   with Java and Spring Boot.     
		   ..."
    }
  ]
}
```

## 2. Exposing dynamic resources

A dynamic resource generates its content when a client requests it. This is useful for application status, system metrics, current configuration, or information obtained from Spring services.

For example, instead of returning hardcoded documentation, we can expose a resource containing the application's current runtime information.

**Example:** Runtime information

Create:

`src/main/java/com/example/mcp_demo/resources/RuntimeResources.java`

```java
package com.example.mcp_demo.resources;

import java.time.Instant;
import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeResources {

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification>
            runtimeResource() {

        var resource = McpSchema.Resource.builder()
				.uri("system://runtime")
				.name("Runtime Information")
				.description("Current Java runtime information")
				.mimeType("application/json")
				.build();

        var specification =
            new McpServerFeatures.SyncResourceSpecification(
                resource,
                (exchange, request) -> {

                    String json = """
                        {
                          "javaVersion": "%s",
                          "timestamp": "%s",
                          "availableProcessors": %d
                        }
                        """.formatted(
                            System.getProperty("java.version"),
                            Instant.now(),
                            Runtime.getRuntime()
                                .availableProcessors()
                        );

                    return new McpSchema.ReadResourceResult(
                        List.of(
                            new McpSchema.TextResourceContents(
                                request.uri(),
                                "application/json",
                                json
                            )
                        )
                    );
                }
            );

        return List.of(specification);
    }
}
```

The URI remains constant:

```
system://runtime
```

However, the content is generated each time the resource is read. The timestamp changes between requests, and the Java version and processor count reflect the running environment.

```sh
./mvnw spring-boot:run

npx @modelcontextprotocol/inspector
```

Response:

```json
{
  "contents": [
    {
      "uri": "system://runtime",
      "mimeType": "application/json",
      "text": "{
	      "javaVersion": "21.0.7",
	      "timestamp": "2026-10-12T15:01:18.588367486Z",
	      "availableProcessors": 8}"
    }
  ]
}
```

This demonstrates an important distinction: a resource does not need to be static simply because it is read-only. A resource can expose live information without modifying application state.

For production applications, prefer serializing a Java record with Jackson rather than manually constructing JSON strings.

## 3. Resource templates

So far, every resource has had a fixed URI. But imagine exposing hundreds of learning notes:

```txt
notes://topics/1
notes://topics/2
notes://topics/3
```

Registering every possible URI individually would be inefficient. MCP solves this with resource templates, which define parameterized URI patterns.

For example:

```
notes://topics/{id}
```

The `{id}` placeholder identifies the requested topic. Clients can discover resource templates using `resources/templates/list` and read a matching URI through `resources/read`.

**Example:** Topic resource template

Create:

`src/main/java/com/example/mcp_demo/resources/TopicTemplateResources.java`

```java
package com.example.mcp_demo.resources;

import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicTemplateResources {

    @Bean
    public List<McpServerFeatures.SyncResourceTemplateSpecification>
            topicTemplates() {

        var template = McpSchema.ResourceTemplate
		        .builder("notes://topics/{id}", "Learning Topic")
				.description("Read a learning topic by ID")
				.mimeType("text/markdown")
				.build();

        var specification =
            new McpServerFeatures.SyncResourceTemplateSpecification(
                template,
                (exchange, request) -> {

                    String uri = request.uri();

                    String id = uri.substring(
                        "notes://topics/".length()
                    );

                    String content = """
                        # Topic %s

                        This is dynamically generated content
                        for learning topic %s.
                        """.formatted(id, id);

                    return new McpSchema.ReadResourceResult(
                        List.of(
                            new McpSchema.TextResourceContents(
                                uri,
                                "text/markdown",
                                content
                            )
                        )
                    );
                }
            );

        return List.of(specification);
    }
}
```

The template declares that the server supports URIs matching `notes://topics/{id}`. When a client requests `notes://topics/15`, the callback extracts `15` and generates content for that topic.

```sh
./mvnw spring-boot:run

npx @modelcontextprotocol/inspector
```

Open Resource Templates

Response:

```json
{
  "contents": [
    {
      "uri": "notes://topics/1",
      "mimeType": "text/markdown",
      "text": "
	      # Topic 1
	      
	      This is dynamically generated content
	      for learning topic 1."
    }
  ]
}
```

In this introductory example, the ID is extracted manually. In a production implementation, validate the URI structure and parameter value before using them. Also, clients may differ in their support for resource templates.

## 4. Database Content

Now let's replace the generated content with actual database records. This is where MCP resources become particularly useful for Spring Boot applications.

### JPA entity

`model/LearningTopic.java`

```java
package com.example.mcp_demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class LearningTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    protected LearningTopic() {
    }

    public LearningTopic(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
```

This entity represents a learning topic stored in PostgreSQL. The `content` field contains Markdown text, which can be returned directly to MCP clients.

### Repository

`repository/LearningTopicRepository.java`

```java
package com.example.mcp_demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mcp_demo.model.LearningTopic;

public interface LearningTopicRepository
        extends JpaRepository<LearningTopic, Long> {
}
```

Spring Data JPA automatically provides methods such as `findById()`, `findAll()`, and `save()`, so no SQL is required for basic operations.

### Service

`service/LearningTopicService.java`

```java
package com.example.mcp_demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mcp_demo.model.LearningTopic;
import com.example.mcp_demo.repository.LearningTopicRepository;

@Service
public class LearningTopicService {

    private final LearningTopicRepository repository;

    public LearningTopicService(
            LearningTopicRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public LearningTopic findById(Long id) {

        return repository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Topic not found: " + id
                )
            );
    }
}
```

The service encapsulates the database lookup. Keeping persistence logic outside the MCP callback makes the implementation reusable from REST controllers, GraphQL resolvers, or MCP tools.

### Resource

`resources/DatabaseTopicResources.java`

```java
package com.example.mcp_demo.resources;

import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.mcp_demo.model.LearningTopic;
import com.example.mcp_demo.service.LearningTopicService;

@Configuration
public class DatabaseTopicResources {

    @Bean
    public List<McpServerFeatures.SyncResourceTemplateSpecification>
            databaseTopicTemplates(
                LearningTopicService topicService) {

        var templateDb = McpSchema.ResourceTemplate
				.builder("db://topics/{id}", "Database Learning Topic")
				.description("Read a learning topic from PostgreSQ")
				.mimeType("text/markdown")
				.build();

        var specification =
            new McpServerFeatures.SyncResourceTemplateSpecification(
                templateDb,
                (exchange, request) -> {

                    String uri = request.uri();

                    String prefix = "db://topics/";

                    if (!uri.startsWith(prefix)) {
                        throw new IllegalArgumentException(
                            "Invalid resource URI"
                        );
                    }

                    Long id = Long.parseLong(
                        uri.substring(prefix.length())
                    );

                    LearningTopic topic =
                        topicService.findById(id);

                    String markdown = """
                        # %s

                        %s
                        """.formatted(
                            topic.getTitle(),
                            topic.getContent()
                        );

                    return new McpSchema.ReadResourceResult(
                        List.of(
                            new McpSchema.TextResourceContents(
                                uri,
                                "text/markdown",
                                markdown
                            )
                        )
                    );
                }
            );

        return List.of(specification);
    }
}
```

When a client requests:

```
db://topics/1
```

The MCP server extracts the ID, calls `LearningTopicService`, retrieves the corresponding database record, and returns its content as Markdown.

This architecture is especially useful when adding MCP support to an existing Spring Boot application. You do not need to duplicate your business logic: the MCP resource callback becomes another interface to the same service layer.

In a multi-user application, the service must also verify that the requesting user is authorized to read the topic. A resource URI alone must never grant access to private database records.

```sh
./mvnw spring-boot:run

npx @modelcontextprotocol/inspector
```

Open Resource Templates:

Database Learning Topic

Request:

```json
{
  "method": "completion/complete",
  "params": {
    "argument": {
      "name": "id",
      "value": "1"
    },
    "ref": {
      "type": "ref/resource",
      "uri": "db://topics/{id}"
    }
  }
}
```

Response:

```json
{
  "error": "MCP error -32602: AsyncCompletionSpecification not found: ResourceReference[uri=db://topics/{id}]"
}
```