
## 1. Custom GraphQL object type

Real applications usually return objects: users, products, orders, reviews, accounts, and so on. 
GraphQL's most important features: the client chooses which fields of the object it wants returned. 

```
type Query {
    user: User!
}

type User {
    id: ID!
    name: String!
    email: String!
    age: Int!
}
```

We now need something in Java that represents the user. 
GraphQL does not require you to use Java records, a normal Java class works too. 

```java
public record User(
    String id,
    String name,
    String email,
    Integer age
) {}
```

Now modify the controller:

```java
import com.example.graphql_demo.model.User;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class QueryController {

    @QueryMapping
    public User user() {
        return new User(
            "USER-123",
            "John Smith",
            "john@example.com",
            40
        );
    }
}
```

The complete flow is:

```
GraphQL query
     │
     ▼
user
     │
     ▼
@QueryMapping
User user()
     │
     ▼
Java User object
     │
     ├── id
     ├── name
     ├── email
     └── age
```

## 2. Selecting fields from an object

Now we encounter a major difference from our previous queries. 

This query is not valid:

```
{
    user
}
```

Because `user` is not a scalar. It is an object:

```
user: User!
```

GraphQL needs us to specify which fields of `User` we want. 
This nested block is called a selection set. 

```
{
    user {
        id
        name
    }
}
```

We are effectively saying: 
Give me the `user`, but from that user I only want `id` and `name`.

```sh
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ user { id name } }"}'
```

```json
{
  "data": {
    "user": {
      "id": "USER-123",
      "name": "John Smith"
    }
  }
}
```

## 3. Nested selection sets

Selection sets can be nested. 
Let's extend our model with another object. 

```java
public record User(
    String id,
    String name,
    String email,
    Integer age,
    Address address
) {}

public record Address(
    String city,
    String country
) {}
```

Our GraphQL schema becomes: 

```
type Query {
    user: User!
}

type User {
    id: ID!
    name: String!
    email: String!
    age: Int!
    address: Address!
}

type Address {
    city: String!
    country: String!
}
```

And the resolver:

```java
@QueryMapping
public User user() {

    Address address = new Address(
        "Bucharest",
        "Romania"
    );

    return new User(
        "USER-123",
        "John Smith",
        "john@example.com",
        40,
        address
    );
}
```

Now we can rewrite the query. 
This ability to traverse related objects is where GraphQL becomes especially useful.

```graphql
query GetUser {
    user {
        name
        email
        address {
            city
            country
        }
    }
}
```

```sh
curl -X POST http://localhost:8080/graphql \
-H "Content-Type: application/json" \
-d '{"query":"query GetUser { user { name email address { city country } } }"}'
```

```json
{
  "data": {
    "user": {
      "name": "John Smith",
      "email": "john@example.com",
      "address": {
        "city": "Bucharest",
        "country": "Romania"
      }
    }
  }
}
```

## 4. Complete Example

Our project now looks roughly like:

```
src/main/
├── java/
│   └── com/example/graphql_demo/
│       ├── GraphqlDemoApplication.java
│       │
│       ├── controller/
│       │   └── QueryController.java
│       │
│       └── model/
│           ├── User.java
│           └── Address.java
│
└── resources/
    └── graphql/
        └── schema.graphqls
```

```graphql
# resources/graphql/schema.graphqls

type Query {
    user: User!
}

type User {
    id: ID!
    name: String!
    email: String!
    age: Int!
    address: Address!
}

type Address {
    city: String!
    country: String!
}
```

```java
package com.example.graphql_demo.model;

public record Address(
    String city,
    String country
) {}
```

```java
package com.example.graphql_demo.model;

public record User(
    String id,
    String name,
    String email,
    Integer age,
    Address address
) {}
```

```java
package com.example.graphql_demo.controller;

import com.example.graphql_demo.model.Address;
import com.example.graphql_demo.model.User;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class QueryController {

    @QueryMapping
    public User user() {

        Address address = new Address(
            "Bucharest",
            "Romania"
        );

        return new User(
            "USER-123",
            "John Smith",
            "john@example.com",
            40,
            address
        );
    }
}
```

```sh
# run
mvn spring-boot:run

# request
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query GetUser { user { id name email age address { city country } } }"}' | jq
  
# response
{
  "data": {
    "user": {
      "id": "USER-123",
      "name": "John Smith",
      "email": "john@example.com",
      "age": 40,
      "address": {
        "city": "Bucharest",
        "country": "Romania"
      }
    }
  }
}
```
