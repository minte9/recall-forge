## 1. Installation

You don't install GraphQL itself as a serve like PostgreSQL or MySQL. In a Java application, GraphQL runs inside your Spring Boot application. Spring Boot starts the web server, Spring for GraphQL handles GraphQL requests, and your Java classes provide the data.

~~~sh
Client (curl)
     |
     | POST /graphql
     v
Spring Boot
     |
     v
GraphQL Query Controller
     |
     v
"Hello, GraphQL!"
~~~

Go to Spring Initializr and select and add dependencies Spring Web and Spring for GraphQL.

~~~sh
Project: Maven
Language:      Java
Spring Boot:   current stable version

Group:         com.example
Artifact:      graphql-demo
Name:          graphql-demo
Packaging:     Jar
Java:          21
~~~
~~~xml
<dependencies>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-graphql</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-webmvc</artifactId>
	</dependency>

	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-graphql-test</artifactId>
		<scope>test</scope>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-webmvc-test</artifactId>
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
~~~

Check to see which process is using port 8080, or display all listening TCP ports.

~~~sh
mvn compile
mvn spring-boot:run
  # Web server failed to start. Port 8080 was already in use.

sudo lsof -i :8080
  # java    1369 jenkins    9u  IPv6  50075      0t0  TCP *:http-alt (LISTEN)

sudo ss -ltnp
  # LISTEN          0               4096                               127.0.0.1:8081                           
  # LISTEN          0               4096                               127.0.0.1:8082       
  # LISTEN          0               4096                                 0.0.0.0:8083    
~~~

Change the port in resource/application.properties

~~~sh
spring.application.name=graphql-demo
server.port=9191
~~~

~~~sh
mvn spring:run
  # http://localhost:9191/
~~~

## 2. Graph API

Our first GraphQL API will expose one query operation and will return data.

~~~sh
query {
    hello
}
~~~

~~~sh
{
  "data": {
    "hello": "Hello, GraphQL!"
  }
}
~~~


There are two important pieces required to make this happen. First, GraphQL needs a **schema** saying that a query called `hello` exists and returns a `String`. Second, Spring needs Java code that actually calculates the value returned by `hello`.

~~~sh
Schema:
    "There is a query named hello returning String"
                       |
                       v
Java:
    "When hello is requested, return Hello, GraphQL!"
~~~

Create the schema file:

~~~sh
src/main/resources/graphql/schema.graphqls
~~~

Spring Boot automatically looks under `src/main/resources/graphql/` for GraphQL schema files. Here we're declaring a single field called `hello`, whose return type is `String`.

~~~GraphQL
type Query {
    hello: String
}
~~~

Notice that this contains no Java code and no implementation, only describes the GraphQL contract. It's conceptually similar to saying `String hello();` without yet providing the method body.

Create a Java Controller class such as:

~~~java
// src/main/java/com/example/graphql_demo/HelloController.java

package com.example.graphqldemo;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class HelloController {

    @QueryMapping
    public String hello() {
        return "Hello, GraphQL!";
    }
}
~~~

`@Controller` makes this class a Spring-managed controller. `@QueryMapping` tells Spring for GraphQL that this method resolves a field of the GraphQL `Query` type. The names connect the schema and Java code (hello). 


Start the application and run your first query.

~~~sh
mvn spring:boot

curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ hello }"}'
  
# {"data":{"hello":"Hello, GraphQL!"}}
~~~

Try requesting something that doesn't exist.

~~~sh
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ goodbye }"}'
  
# Error 403 No valid crumb was included in the request
~~~

GraphQL validates the requested query against the schema before executing it. The client therefore cannot arbitrarily ask for fields that your API hasn't exposed.

This is one of the major roles of the GraphQL schema: it defines exactly what clients are allowed to query.


Add another query by modifying the schema and controller.

~~~GraphQL
type Query {
	hello: String
	version: String
}
~~~

~~~java
@Controller
public class HelloController {

	@QueryMapping
	public String hello() {
		return "Hello, GraphQL!";
	}
	
	@QueryMapping
	public String version() {
		return "1.0";
	}
}
~~~

Restart the application if necessary and query both fields:

~~~sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ hello version }"}' | jq
~~~

~~~json
{
  "data": {
    "hello": "Hello, GraphQL!",
    "version": "1.0"
  }
}
~~~


## 3. GraphQL vs Rest

If we were designing a simplistic REST API, we might have multiple endpoints and java mapping.

~~~sh
GET /api/hello
GET /api/version
~~~

~~~java
@GetMapping("/api/hello")
public String hello() {
    return "Hello";
}

@GetMapping("/api/version")
public String version() {
    return "1.0";
}
~~~

GraphQL takes a different approach by letting the client describe what it wants in the request. 

~~~GraphQL
{
    hello
    version
}
~~~

The endpoint stays the same while the GraphQL operation changes.

~~~sh
POST /graphql
~~~