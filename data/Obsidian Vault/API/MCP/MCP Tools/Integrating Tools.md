
## 1. Calling services from tools

We implemented error handling, validation, and logging for MCP tools. Now we'll integrate MCP into a complete Spring Boot application with a service layer, Spring Data JPA, PostgreSQL, and an existing REST API.

MCP tools should generally act as adapters around existing application services rather than contain complex business logic. In Spring Boot, the service layer already encapsulates business rules, validation, transactions, and repository operations. By injecting these services into MCP tools, we can expose the same functionality to AI applications without duplicating code.

For example, consider a product management application with a REST endpoint `GET /api/products/1`. Instead of implementing a separate database query inside the MCP tool, we can inject the existing `ProductService` and call `getProduct(1)`. Both interfaces will execute the same business logic, making maintenance and testing easier.

The architecture looks like this:

![[mcp_integration.png]]

## 2. Spring Boot project

We'll build a small product management application with persistent storage. Our application will expose product lookup and product creation through both REST and MCP.

We'll use Spring Data JPA to handle database operations and PostgreSQL running in Docker. The MCP server will use Streamable HTTP, allowing us to test tools through MCP Inspector.

Project structure:

```txt
mcp-demo/
├── pom.xml
├── compose.yaml
└── src/main/
    ├── java/com/example/mcpdemo/
    │   ├── McpDemoApplication.java
    │   ├── config/
    │   │   └── McpToolConfig.java
    │   ├── controller/
    │   │   └── ProductController.java
    │   ├── model/
    │   │   └── Product.java
    │   ├── repository/
    │   │   └── ProductRepository.java
    │   ├── service/
    │   │   └── ProductService.java
    │   └── tools/
    │       └── ProductTools.java
    └── resources/
        └── application.yml
```

pom.xml

Add these dependencies to your existing Maven project:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    </dependency>
</dependencies>
```

These dependencies provide the REST API, JPA persistence, PostgreSQL driver, and Spring AI MCP server. Keep the Spring Boot parent and Spring AI BOM configuration from previous file (in Basics). 

compose.yaml

```yml
services:
  postgres:
    image: postgres:16
    container_name: mcp-postgres
    environment:
      POSTGRES_DB: mcpdemo
      POSTGRES_USER: mcpuser
      POSTGRES_PASSWORD: mcppassword
    ports:
      - "127.0.0.1:5433:5432"
    volumes:
      - mcp-postgres-data:/var/lib/postgresql/data

volumes:
  mcp-postgres-data:
```

We expose PostgreSQL on host port `5433` to avoid conflicts with another PostgreSQL instance that may already be running on `5432`.

Start the database:

```sh
docker compose up -d
```

application.yml

```yml
server:
  port: 9091

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/mcpdemo
    username: mcpuser
    password: mcppassword

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  ai:
    mcp:
      server:
        name: product-mcp-server
        version: 1.0.0
        protocol: STREAMABLE
        type: SYNC
```

For local learning, `ddl-auto: update` allows Hibernate to create and update tables automatically. In production, use database migrations with Flyway or Liquibase instead. The password is intentionally simple for local development only.

## 3. Entity and Repository

Spring Data JPA maps Java entities to relational database tables. The repository layer provides common CRUD operations without requiring us to write SQL manually.

Our `Product` entity will contain an ID, name, and price. We'll use `BigDecimal` for monetary values because floating-point types such as `double` can introduce precision errors in financial calculations.

Product.java

```java
package com.example.mcp_demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    protected Product() {}

    public Product(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
```

ProductRepository.java

```java
package com.example.mcpdemo.repository;

import com.example.mcpdemo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository
        extends JpaRepository<Product, Long> {
}
```

`JpaRepository<Product, Long>` provides methods such as `findById()`, `findAll()`, `save()`, and `deleteById()`.

We do not need to implement these methods manually. Spring Data JPA generates the repository implementation at runtime.

## 4. Service layer

The service layer contains our business logic and is shared by REST and MCP. This is the most important architectural decision in this lesson.

We'll implement methods to retrieve all products, find a product by ID, and create a new product. Validation will happen here so both interfaces follow the same rules.

ProductService.java

```java
package com.example.mcpdemo.service;

import com.example.mcpdemo.model.Product;
import com.example.mcpdemo.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found: " + id
                        ));
    }

    @Transactional
    public Product create(String name, BigDecimal price) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required"
            );
        }

        if (price == null
                || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Price must be non-negative"
            );
        }

        Product product = new Product(name, price);

        return repository.save(product);
    }
}
```

The `@Transactional` annotation ensures that database operations execute within a transaction. Read-only methods use `@Transactional(readOnly = true)`, while product creation uses a writable transaction.

Notice that there are no MCP or REST dependencies inside `ProductService`. It is a normal Spring service that can be reused by different interfaces.

## 5. Exposing through REST

Let's first expose the service through a traditional REST controller.

The controller will provide three endpoints: list products, retrieve a product by ID, and create a product. These operations are useful for demonstrating that REST and MCP can coexist in the same Spring Boot application.

ProductController.java

```java
package com.example.mcpdemo.controller;

import com.example.mcpdemo.model.Product;
import com.example.mcpdemo.service.ProductService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<Product> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Product findById(@PathVariable Long id) {
        try {
            return service.findById(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage()
            );
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody CreateProductRequest request) {
        try {
            return service.create(request.name(), request.price());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage()
            );
        }
    }

    public record CreateProductRequest(
            String name,
            BigDecimal price
    ) {}
}
```

In a larger REST application, you would typically centralize exception mapping with `@RestControllerAdvice`.

Start the application:

```sh
./mvnw spring-boot:run
```

Create a product:

```sh
curl -X POST http://localhost:9091/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "price": 1200.00
  }'
```

Retrieve all products:

```sh
curl http://localhost:9091/api/products
```

Retrieve a specific product:

```sh
curl http://localhost:9091/api/products/1
```

Expected response:

```json
{
  "id": 1,
  "name": "Laptop",
  "price": 1200.00
}
```

At this point, we have a working REST API backed by PostgreSQL. 

## 6. Exposing through MCP

Now we'll create an MCP tool class that delegates to `ProductService`. Each method will use `@Tool`, allowing Spring AI to generate tool definitions and expose them through the MCP server.

We'll implement three tools: `getProducts`, `getProduct`, and `createProduct`. The first two are read-only operations, while the third creates a database record.

ProductTools.java

```java
package com.example.mcpdemo.tools;

import com.example.mcpdemo.model.Product;
import com.example.mcpdemo.service.ProductService;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductTools {

    private final ProductService service;

    public ProductTools(ProductService service) {
        this.service = service;
    }

    @Tool(description = "Retrieve all available products")
    public List<Product> getProducts() {
        return service.findAll();
    }

    @Tool(description = "Retrieve a product by its numeric ID")
    public Product getProduct(
            @ToolParam(description = "Product ID") Long id) {

        return service.findById(id);
    }

    @Tool(description = "Create a new product with a name and price")
    public Product createProduct(
            @ToolParam(description = "Product name") String name,
            @ToolParam(description = "Product price") BigDecimal price) {

        return service.create(name, price);
    }
}
```

Notice how small the MCP tool implementation is. Each method simply delegates to ProductService.

This separation becomes especially valuable in larger applications with complex business logic, transactions, database access, and external service integrations.

McpToolConfig.java

```java
package com.example.mcpdemo.config;

import com.example.mcpdemo.tools.ProductTools;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider productToolProvider(
            ProductTools productTools) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(productTools)
                .build();
    }
}
```

Start the application and MCP Inspector:

```sh
mvn spring-boot:run

npx @modelcontextprotocol/inspector
```

Select `getProduct` and enter:

```json
{
  "id": 1
}
```

Expected result data:

```json
{
  "id": 1,
  "name": "Laptop",
  "price": 1200.00
}
```

Select `createProduct` and enter:

```json
{
  "name": "Keyboard",
  "price": 80.00
}
```

Select `getProducts` and run:

```json
[
  {
    "name": "Laptop",
    "price": 1200,
    "id": 1
  },
  {
    "name": "Keyboard",
    "price": 80,
    "id": 2
  }
]
```

Verify through REST too:

```sh
curl http://localhost:9091/api/products
```

### REST vs MCP — Same business logic

|Operation|REST|MCP|
|---|---|---|
|List products|`GET /api/products`|`getProducts()`|
|Find product|`GET /api/products/1`|`getProduct(id=1)`|
|Create product|`POST /api/products`|`createProduct(name, price)`|
|Business logic|`ProductService`|`ProductService`|
|Persistence|`ProductRepository`|`ProductRepository`|

Both interfaces operate on the same PostgreSQL database.

## 7. Architectural considerations

The pattern we've implemented is appropriate for integrating MCP into an existing Spring Boot application. However, there are a few important considerations when moving toward production.

Security: MCP tools must enforce the same authorization rules as REST endpoints. An AI client should not gain additional permissions simply because it accesses functionality through MCP. In a secured application, user identity and permissions must be propagated and checked appropriately.

Transactions: Database transactions belong in the service layer. This ensures consistent behavior regardless of whether the operation originates from REST, MCP, or another interface.

Side effects: Tools that create, update, or delete data should be designed carefully. For sensitive operations, consider confirmation requirements, authorization checks, idempotency keys, and audit logging. Tool descriptions alone are not a security boundary.

Deployment: REST and MCP can run in the same Spring Boot process, as demonstrated here. For larger systems, you may deploy a dedicated MCP server that communicates with existing backend services through REST, messaging, or internal service calls. The best choice depends on your application's architecture and security requirements.