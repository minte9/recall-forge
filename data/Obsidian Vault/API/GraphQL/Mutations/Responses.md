
## 1. Mutation Responses

In the previous lessons, our mutations returned simple values:

```
type Mutation {
    createBook(input: CreateBookInput!): Book!
    updateBook(id: ID!, input: UpdateBookInput!): Book
    deleteBook(id: ID!): Boolean!
}
```

This works, but the client gets limited information. 
For example:

```
deleteBook(id: 999)
```

returns:

```json
{
    "data": {
        "deleteBook": false
    }
}
```

What does `false` mean? The book didn't exist? Validation failed? The user didn't have permission?

## 2. Payload

A common GraphQL design is to return a dedicated **payload/result type** containing both the affected object and information about the operation.

Instead of:

```
createBook(input: CreateBookInput!): Book!
```

we can define:

```
createBook(input: CreateBookInput!): BookPayload!
```

Then create:

```
type BookPayload {
    success: Boolean!
    message: String!
    book: Book
}
```

Now a mutation can return three pieces of information:

```
BookPayload
│
├── success    → did the operation succeed?
├── message    → human-readable information
└── book       → affected book
```

For example:

```
{
    "success": true,
    "message": "Book created successfully",
    "book": {
        "id": "3",
        "title": "Spring in Action"
    }
}
```

Let's change all three mutations to use the same payload:

```GraphQL
type Query {
    books: [Book!]!
}

type Mutation {

    createBook(
        input: CreateBookInput!
    ): BookPayload!

    updateBook(
        id: ID!
        input: UpdateBookInput!
    ): BookPayload!

    deleteBook(
        id: ID!
    ): BookPayload!
}

input CreateBookInput {
    title: String!
    author: String!
}

input UpdateBookInput {
    title: String
    author: String
}

type Book {
    id: ID!
    title: String!
    author: String!
}

type BookPayload {
    success: Boolean!
    message: String!
    book: Book
}
```

Notice:

```
book: Book
```

does **not** have `!`.

That's intentional.

For a failed operation, there may be no book to return:

```
{
    "success": false,
    "message": "Book not found",
    "book": null
}
```

## 3. Complete Example

schema.grapqls

```GraphQL
type Query {
    books: [Book!]!
}

type Mutation {

    createBook(
        input: CreateBookInput!
    ): BookPayload!

    updateBook(
        id: ID!
        input: UpdateBookInput!
    ): BookPayload!

    deleteBook(
        id: ID!
    ): BookPayload!
}

input CreateBookInput {
    title: String!
    author: String!
}

input UpdateBookInput {
    title: String
    author: String
}

type Book {
    id: ID!
    title: String!
    author: String!
}

type BookPayload {
    success: Boolean!
    message: String!
    book: Book
}
```

BookPayload.java

```java
package com.example.graphql.model;

public record BookPayload(
    boolean success,
    String message,
    Book book
) {}
```

Our controller mutations are now:

```java
@MutationMapping
public BookPayload createBook(
    @Argument CreateBookInput input
) {

    Book book = bookService.create(input);

    return new BookPayload(
        true,
        "Book created successfully",
        book
    );
}


@MutationMapping
public BookPayload updateBook(
    @Argument Long id,
    @Argument UpdateBookInput input
) {

    Book book = bookService.update(id, input);

    if (book == null) {
        return new BookPayload(
            false,
            "Book not found",
            null
        );
    }

    return new BookPayload(
        true,
        "Book updated successfully",
        book
    );
}


@MutationMapping
public BookPayload deleteBook(
    @Argument Long id
) {

    Book book = bookService.delete(id);

    if (book == null) {
        return new BookPayload(
            false,
            "Book not found",
            null
        );
    }

    return new BookPayload(
        true,
        "Book deleted successfully",
        book
    );
}
```

And change the service delete method from `boolean` to:

```java
public Book delete(Long id) {

    for (int i = 0; i < books.size(); i++) {

        Book book = books.get(i);

        if (book.id().equals(id)) {
            books.remove(i);
            return book;
        }
    }

    return null;
}
```

The client can decide how much of that response it actually wants:

```GraphQL
mutation {
    updateBook(
        id: 2
        input: {
            title: "Effective Java - Third Edition"
        }
    ) {
        success
        message

        book {
            id
            title
        }
    }
}
```

Response:

```json
{
  "data": {
    "updateBook": {
      "success": true,
      "message": "Book updated successfully",  // Look here
      "book": {
        "id": "2",
        "title": "Effective Java - Third Edition"
      }
    }
  }
}
```
