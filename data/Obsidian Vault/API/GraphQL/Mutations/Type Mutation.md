## 1. What is a mutation

So far, we have used GraphQL queries to retrieve data. A mutation is a GraphQL operation used when we want to change data on the server.

```
Query     → read data
Mutation  → change data
```

This query retrieves books:

```graphql
query {
    books {
        id
        title
    }
}
```

While this creates a book:

```GraphQL
mutation {
    createBook(title: "Effective Java") {
        id
        title
    }
}
```

## 2. Type Mutation

Just as our GraphQL schema has a `Query` type, we can define a `Mutation` type:

```GraphQL
type Query {
    books: [Book!]!
}

type Mutation {
    createBook(title: String!, author: String!): Book!
}

type Book {
    id: ID!
    title: String!
    author: String!
}
```

This tells GraphQL that these fields represent operations that modify server-side data. Notice that `createBook` looks structurally similar to a query field: 

```
createBook(
    title: String!
    author: String!
): Book!
```

It accepts arguments and returns a GraphQL type.

## 3. Query vs mutation

GraphQL queries and mutations are structurally very similar.

A query might be:

```
query {
    book(id: 1) {
        id
        title
    }
}
```

while a mutation might be:

```
mutation {
    createBook(
        title: "Effective Java"
        author: "Joshua Bloch"
    ) {
        id
        title
        author
    }
}
```

The important conceptual difference is their purpose:

```
              GraphQL Operation
                     │
          ┌──────────┴──────────┐
          │                     │
        Query                Mutation
          │                     │
       READ DATA             CHANGE DATA
          │                     │
       book()              createBook()
       books()             updateBook()
                           deleteBook()
```

A mutation can still return data. In fact, returning the newly created or modified object is extremely common.

For example:

```
mutation {
    createBook(
        title: "Effective Java"
        author: "Joshua Bloch"
    ) {
        id
        title
    }
}
```

Here we're saying: create this book, and after creating it, return its `id` and `title`.  Just like queries, the client chooses which fields it wants back.  

## 4. Code Example

schema.graphqls

```GraphQL
type Query {
    books: [Book!]!
}

type Mutation {
    createBook(title: String!, author: String!): Book!
}

type Book {
    id: ID!
    title: String!
    author: String!
}
```

Book.java

```java
package com.example.graphql.model;

public record Book(
    Long id,
    String title,
    String author
) {}
```

BookService.java

```java
package com.example.graphql.service;

import com.example.graphql.model.Book;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BookService {

    private final List<Book> books = new ArrayList<>();

    private final AtomicLong idGenerator = new AtomicLong();

    public BookService() {
        books.add(
            new Book(
                idGenerator.incrementAndGet(),
                "Clean Code",
                "Robert C. Martin"
            )
        );

        books.add(
            new Book(
                idGenerator.incrementAndGet(),
                "Effective Java",
                "Joshua Bloch"
            )
        );
    }

    public List<Book> findAll() {
        return books;
    }

    public Book create(String title, String author) {

        Book book = new Book(
            idGenerator.incrementAndGet(),
            title,
            author
        );

        books.add(book);

        return book;
    }
}
```

BookController.java

```java
package com.example.graphql.controller;

import com.example.graphql.model.Book;
import com.example.graphql.service.BookService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @QueryMapping
    public List<Book> books() {
        return bookService.findAll();
    }

    @MutationMapping
    public Book createBook(
        @Argument String title,
        @Argument String author
    ) {
        return bookService.create(title, author);
    }
}
```

Test it:

```
mvn spring-boot:run
```

```
mutation {
    createBook(
        title: "Spring in Action"
        author: "Craig Walls"
    ) {
        id
    }
}
```

Response:

```json
{
	"data": {
		"createBook": {
			"id": "3"
		}
	}
}
```

After creating the book, execute:

```
query {
    books {
        id
        title
        author
    }
}
```

Response:

```json
{
    "data": {
        "books": [
            {
                "id": "1",
                "title": "Clean Code",
                "author": "Robert C. Martin"
            },
            {
                "id": "2",
                "title": "Effective Java",
                "author": "Joshua Bloch"
            },
            {
                "id": "3",
                "title": "Spring in Action",
                "author": "Craig Walls"
            }
        ]
    }
}
```

So the complete flow is:

```
Client
  │
  │ mutation createBook(...)
  ▼
GraphQL
  │
  │ finds Mutation.createBook
  ▼
@MutationMapping
  │
  ▼
BookController.createBook()
  │
  ▼
BookService.create()
  │
  ▼
Book created
  │
  ▼
Book returned
  │
  ▼
GraphQL selects requested fields
  │
  ▼
JSON response
```