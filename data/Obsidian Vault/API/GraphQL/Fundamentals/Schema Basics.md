
## 1. Schema Definition Language

GraphQL uses **SDL** to describe the API.  
A very small schema might look like this and does not contain the implementation.

```
# src/main/resources/graphql/schema.graphqls

type Query {
    hello: String
    age: Int
    price: Float
    active: Boolean
    userId: ID
}
```

You can think of the schema somewhat like a Java interface. The analogy isn't exact, but it is useful. The GraphQL schema describes the contract, while your Java code fulfills that contract. 

```
interface Api {
    String hello();
    Integer age();
    Double price();
    Boolean active();
    String userId();
}
```

## 2. Query type

The special GraphQL type defines the entry points for reading data from your API. In REST, you might expose several endpoints. In GraphQL, these can instead be fields under the root `Query` type.

```
type Query {
    hello: String
    age: Int
    price: Float
    userId: ID
}
```

## 3. Scalar Types

GraphQL provides several built-in scalar types. The five fundamental ones are:

```
GraphQL | Java maping       | Example
--------|-------------------|--------
String  | String            | "John"`
Int     | Integer / int     | 42
Float   | Double / double   | 19.99
Boolean | Boolean / boolean | true
ID      | String            | "123"
```

ID represents an identifier. It is serialized similarly to a string, but its meaning is different: GraphQL is being told that this value represents an identifier, rather than ordinary textual data.

Let's implement all five.

```
# resources/graphql/schema.graphqls

type Query {
    hello: String
    age: Int
    price: Float
    active: Boolean
    userId: ID
}
```

```java
package com.example.graphql_demo.controller;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class QueryController {

    @QueryMapping
    public String hello() {
        return "Hello GraphQL!";
    }

    @QueryMapping
    public Integer age() {
        return 40;
    }

    @QueryMapping
    public Double price() {
        return 19.99;
    }

    @QueryMapping
    public Boolean active() {
        return true;
    }

    @QueryMapping
    public String userId() {
        return "USER-123";
    }
}
```

Spring connects the method names to fields in `type Query`.

```
GraphQL query

    age
     │
     ▼
schema.graphqls

    age: Int
     │
     ▼
Spring resolver

    @QueryMapping
    public Integer age()
     │
     ▼
    return 40
```

Assuming Spring Boot is running on port `9191`:

```bash
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ hello age price active userId }"}' | jq
```

```json
{
  "data": {
    "hello": "Hello, GraphQL!",
    "age": 40,
    "price": 19.99,
    "active": true,
    "userId": "USER-123"
  }
}
```

We made one HTTP request but requested five different fields. We can also request only some fields. GraphQL returns only the fields requested by the client. 

```bash
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ hello price }"}' | jq
```

```json
{
  "data": {
    "hello": "Hello, GraphQL!",
    "price": 19.99
  }
}
```

## 4. Nullable

By default, GraphQL fields are nullable.  Here, hello returns a String, but it is also allowed to return null. The controller method is valid.

```
type Query {
    hello: String
}
```

```java
@QueryMapping
public String hello() {
    return null;
}
```

If the value must exist, add `!` which means `String OR null`.

```
type Query {
    hello: String!
}
```
