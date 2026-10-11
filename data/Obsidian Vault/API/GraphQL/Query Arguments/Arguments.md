## 1. Adding arguments

So far, our query always returns the same hard-coded user:

```graphql
type Query {
    user: User!
}
```

```java
@QueryMapping
public User user() {
    return new User(
        "USER-123",
        "John Smith",
        "john@example.com",
        40
    );
}
```

In a real application, we need queries such as:

```graphql
{
    user(id: "USER-123") {
        name
        email
    }
}
```

Here, `id` is a GraphQL argument. 
Spring GraphQL maps that argument to a Java method parameter using `@Argument`.

Arguments are declared inside parentheses after a field name.

```graphql
type Query {
    user(id: ID!): User  # Look here
}

type User {
    id: ID!
    name: String!
    email: String!
    age: Int!
}
```

There are two different type declarations here. 
The `user` field accepts a required `ID` argument called `id` and returns a `User` or `null`.

```
user( id: ID! ): User
      ───────     ────
         │          │
         │          └── return type
         │
         └── argument
```

## 2. @Argument in Spring GraphQL

Java resolver needs to receive the ID sent by the client. 
Spring GraphQL provides `@Argument`:

```java
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;

@QueryMapping
public User user(@Argument String id) {
    // ...
}
```

Spring maps:

```
GraphQL

user(id: "USER-123")
        │
        ▼
Spring GraphQL

@Argument String id
                 │
                 ▼
Java

id = "USER-123"
```

Let's create a simple implementation containing several users. 
We're deliberately using an in-memory list for now.  
Database access comes later.

```java
package com.example.graphql_demo.controller;

import com.example.graphql_demo.model.User;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class QueryController {

    private final List<User> users = List.of(
        new User(
            "USER-123",
            "John Smith",
            "john@example.com",
            40
        ),
        new User(
            "USER-456",
            "Alice Brown",
            "alice@example.com",
            32
        )
    );

    @QueryMapping
    public User user(@Argument String id) {

        return users.stream()
                .filter(user -> user.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}
```

Request the first user:

```sh
curl -X POST http://localhost:9191/graphql \
	 -H "Content-Type: application/json" \
	 -d '{"query":"{ user(id: \"USER-123\") { id name email age } }"}' | jq
```

```json
{
  "data": {
    "user": {
      "id": "USER-123",
      "name": "John Smith",
      "email": "john@example.com",
      "age": 40
    }
  }
}
```

Now change only the argument:

```sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ user(id: \"USER-456\") { id name } }"}'
```

```json
{
  "data": {
    "user": {
      "id": "USER-456",
      "name": "Alice Brown"
    }
  }
}
```

Notice that the resolver hasn't changed. The client controls both which user it wants and which fields it wants. 

These are two independent GraphQL concepts: arguments filter/select the object; selection sets choose the returned fields.

### Optional Argument

We can make `id` is nullable and therefore optional. 
Both are valid:

```graphql
{
    user(id: "USER-123") {
        name
    }
}
```

```graphql
{
    user {
        name
    }
}
```

The Java parameter can consequently be `null`:

```java
@QueryMapping
public User user(@Argument String id) {

    if (id == null) {
        return users.getFirst();
    }

    return users.stream()
            .filter(user -> user.id().equals(id))
            .findFirst()
            .orElse(null);
}
```

### Multiple arguments

A field isn't limited to one argument.

```graphql
type Query {
    user(name: String, age: Int): User
}
```

A client could then send:

```graphql
{
    user(name: "John Smith", age: 40) {
        id
        email
    }
}
```

And Java could receive both:

```java
@QueryMapping
public User user(
        @Argument String name,
        @Argument Integer age
) {
    // search...
}
```

Arguments therefore start becoming especially useful for searches and filters:

```graphq
books(author: "Martin")
products(category: "BOOKS")
orders(status: "PENDING")
users(age: 40)
```


## 3. Arguments vs Fields

Consider: 

```graphql
query GetUser {
    user(id: "USER-123") {
        name
        email
    }
}
```

There are several different GraphQL concepts packed into this small query:

```
query GetUser {
│     │
│     └── operation name
│
└── operation type

    user(id: "USER-123") {
    │    │
    │    └── argument
    │
    └── field on Query

        name
        email
        │
        └── fields on User
    }
}
```

And the server-side schema describes all of it:

```graphql
type Query {
    user(id: ID!): User
}

type User {
    id: ID!
    name: String!
    email: String!
    age: Int!
}
```

## 4. Complete Example

schema.graphqls

```graphql
type Query {
    user(id: ID!): User
}

type User {
    id: ID!
    name: String!
    email: String!
    age: Int!
}
```

User.java

```java
package com.example.graphql_demo.model;

public record User(
    String id,
    String name,
    String email,
    Integer age
) {}
```

QueryController.java

```java
package com.example.graphql_demo.controller;

import com.example.graphql_demo.model.User;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class QueryController {

    private final List<User> users = List.of(
        new User(
            "USER-123",
            "John Smith",
            "john@example.com",
            40
        ),
        new User(
            "USER-456",
            "Alice Brown",
            "alice@example.com",
            32
        )
    );

    @QueryMapping
    public User user(@Argument String id) {
        return users.stream()
                .filter(user -> user.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}
```

Test:

```sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query GetUser { user(id: \"USER-456\") { id name email } }"}'
```

Response:

```json
{
  "data": {
    "user": {
      "id": "USER-456",
      "name": "Alice Brown",
      "email": "alice@example.com"
    }
  }
}
```