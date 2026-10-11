## 1. Writing queries

A basic GraphQL query looks like this:

```
{ hello }
```

It means: from the root `Query` type, give me the field `hello`.  
GraphQL looks at the schema and finds that `hello` is a valid field. 

```
type Query {
    hello: String!
}
```

Spring then finds the corresponding resolver:

```java
@QueryMapping
public String hello() {
    return "Hello, GraphQL!";
}
```

The execution flow is therefore:

```
Client

{ hello }
    │
    ▼
GraphQL Schema

hello: String!
    │
    ▼
Spring Resolver

@QueryMapping
public String hello()
    │
    ▼
"Hello, GraphQL!"
```

We send the GraphQL query inside a JSON request:

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ hello }"}'
```

The response is:

```json
{
  "data": {
    "hello": "Hello, GraphQL!"
  }
}
```

## 2. Selecting fields

One of GraphQL's central ideas is that the client explicitly selects the fields it wants.

```
type Query {
    hello: String!
    age: Int!
    price: Float!
    active: Boolean!
    userId: ID!
}
```

We can request only `age`. Even though `hello`, `price`, `active`, and `userId` exist in the schema, GraphQL doesn't return them.

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ age }"}'
```

```json
{
  "data": {
    "age": 40
  }
}
```

This becomes much more important when we start working with objects.

```
type User {
    id: ID!
    name: String!
    email: String!
    age: Int!
}
```

A client might request:

```
{
    user {
        name
        email
    }
}
```

GraphQL can return only:

```
{
  "data": {
    "user": {
      "name": "John",
      "email": "john@example.com"
    }
  }
}
```

## 3. Names

This is called the query shorthand syntax.

```
{
    hello
}
```

The more explicit version is:

```
query {
    hello
}
```

Both mean essentially the same thing.

Here, `query` is the operation type.  
GraphQL has three operation types:

```
query          read data
mutation       modify data
subscription   receive updates
```

We can also write an operation name chosen by the client. 
It does not need to match the Java method.

```
query GetHello {
    hello
}
```

It still executes hello, because it is the schema field being selected.

```java
@QueryMapping
public String hello() {
    return "Hello, GraphQL!";
}
```

So there are three different names here that are easy to confuse:

```
query GetHello {
      │
      │ operation name
      │
      ▼
    hello
      │
      │ schema field
      ▼
@QueryMapping
public String hello()
              │
              │ Java method
              ▼
"Hello, GraphQL!"
```

In this example, the schema field and Java method happen to both be named `hello`. 
But `GetHello` belongs entirely to the client's GraphQL operation.

```sh
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query GetHello { hello }"}'
```

```json
{
  "data": {
    "hello": "Hello, GraphQL!"
  }
}
```

The operation name doesn't normally appear in the response. 

Names become very useful for debugging, logging, tracing, monitoring, and when a GraphQL document contains multiple operations. 

For real applications, named operations are generally preferable.

## 4. Multiple fields

GraphQL allows us to request several root fields in a single operation.

```
query GetInfo {
    hello
    age
    price
}
```

```sh
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query GetInfo { hello age price }"}'
```

```json
{
  "data": {
    "hello": "Hello, GraphQL!",
    "age": 40,
    "price": 19.99
  }
}
```

This is an important difference from the typical REST approach.

```
GET /hello
GET /age
GET /price
```

Could require three HTTP requests:

```
Client ──GET /hello──► Server

Client ──GET /age────► Server

Client ──GET /price──► Server
```

With our GraphQL schema, the client can instead query one endpoint and receive everything in one response.

```
Client
   │
   │ POST /graphql
   │
   │ query GetInfo {
   │     hello
   │     age
   │     price
   │ }
   ▼
GraphQL Server
```

This ability becomes much more useful once our fields represent real application data and relationships rather than simple scalar examples.