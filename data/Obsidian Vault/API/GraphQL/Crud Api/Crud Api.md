
## 1. GraphQL Schema

Now that `Book` is a JPA entity and `BookRepository` persists data in PostgreSQL, we can implement a complete **CRUD API through GraphQL**.

CRUD means:

```txt
C = Create
R = Read
U = Update
D = Delete
```

Our GraphQL API will provide:

```txt
Read
├── books
└── bookById

Create
└── createBook

Update
└── updateBook

Delete
└── deleteBook
```

Update:

```
src/main/resources/graphql/schema.graphqls
```

```GraphQL
type Book {
    id: ID!
    title: String!
    author: String!
}

input BookInput {
    title: String!
    author: String!
}

type Query {
    books: [Book!]!
    bookById(id: ID!): Book
}

type Mutation {
    createBook(input: BookInput!): Book!
    updateBook(id: ID!, input: BookInput!): Book
    deleteBook(id: ID!): Boolean!
}
```

We now have two operation types:

```GraphQL
type Query {
    ...
}

type Mutation {
    ...
}
```

## 2. BookInput

Instead of defining:

```GraphQL
createBook(
    title: String!
    author: String!
): Book!
```

we're using:

```GraphQL
createBook(input: BookInput!): Book!
```

with:

```GraphQL
input BookInput {
    title: String!
    author: String!
}
```

This gives us a structured request.

The client can send:

```GraphQL
mutation {
    createBook(
        input: {
            title: "Effective Java"
            author: "Joshua Bloch"
        }
    ) {
        id
        title
        author
    }
}
```

This approach becomes particularly useful when an object contains many fields.

We need a corresponding Java type.

Create:

```
src/main/java/com/example/graphql/dto/BookInput.java
```

We can use a Java record:

```java
package com.example.graphql.dto;

public record BookInput(
        String title,
        String author
) {
}
```

Our GraphQL input:

```GraphQL
input BookInput {
    title: String!
    author: String!
}
```

maps naturally to:

```java
public record BookInput(
        String title,
        String author
) {
}
```

Spring GraphQL handles this conversion for us.

## 3. Read all books