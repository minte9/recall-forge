## 1. Updating entities

So far we can create books with:

```
type Mutation {
    createBook(input: CreateBookInput!): Book!
}
```

Now we'll complete the basic CRUD operations by adding:

```
Create  → createBook
Read    → books
Update  → updateBook
Delete  → deleteBook
```

The important new questions are not only _how_ to update and delete data, but also what a mutation should **return** and what should happen when the requested entity doesn't exist.

An update mutation usually needs two pieces of information:

```
1. Which entity should be updated?
2. What should be changed?
```

For example:

```
updateBook(
    id: 2
    input: {
        title: "Effective Java - Third Edition"
        author: "Joshua Bloch"
    }
)
```

We can represent this in our schema as:

```
type Mutation {
    createBook(input: CreateBookInput!): Book!
    updateBook(id: ID!, input: UpdateBookInput!): Book!
}
```

We'll create a separate input type:

```
input UpdateBookInput {
    title: String
    author: String
}
```

Notice something important: the fields aren't marked with `!`.

That means they're optional.

This allows us to update only the title:

```
input: {
    title: "New Title"
}
```

or only the author:

```
input: {
    author: "New Author"
}
```

This is different from creation, where both fields are required:

```
input CreateBookInput {
    title: String!
    author: String!
}
```

## 2. Deleting entities

Deleting is simpler. 
We need to identify which book should be removed: 

```
deleteBook(id: ID!)
```

But we have an API design decision, what should `deleteBook` return?
There are several reasonable possibilities.  

We could return:

```
Boolean!
```

such as:

```
deleteBook(id: ID!): Boolean!
```

or return the deleted object:

```
deleteBook(id: ID!): Book
```

or return a dedicated response object.

We'll use `Boolean` because it's simple:

```
deleteBook(id: ID!): Boolean!
```

## 3. Complete Example (CRUD)

schema.graphqls

```GraphQL
type Query {
    books: [Book!]!
}

type Mutation {

    createBook(
        input: CreateBookInput!
    ): Book!

    updateBook(
        id: ID!
        input: UpdateBookInput!
    ): Book

    deleteBook(
        id: ID!
    ): Boolean!
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
```

Models:

```java
public record Book(
    Long id,
    String title,
    String author
) {}
```

```java
public record CreateBookInput(
    String title,
    String author
) {}
```

```java
public record UpdateBookInput(
    String title,
    String author
) {}
```

BookService.java

```java
package com.example.graphql.service;

import com.example.graphql.model.Book;
import com.example.graphql.model.CreateBookInput;
import com.example.graphql.model.UpdateBookInput;
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

    public Book create(CreateBookInput input) {

        Book book = new Book(
            idGenerator.incrementAndGet(),
            input.title(),
            input.author()
        );

        books.add(book);

        return book;
    }

    public Book update(Long id, UpdateBookInput input) {

        for (int i = 0; i < books.size(); i++) {

            Book existing = books.get(i);

            if (existing.id().equals(id)) {

                Book updated = new Book(
                    existing.id(),
                    input.title() != null 
                        ? input.title()
                        : existing.title(),
                    input.author() != null
                        ? input.author()
                        : existing.author()
                );

                books.set(i, updated);

                return updated;
            }
        }

        return null;
    }

    public boolean delete(Long id) {

        return books.removeIf(
            book -> book.id().equals(id)
        );
    }
}
```

BookController.java

```java
package com.example.graphql.controller;

import com.example.graphql.model.Book;
import com.example.graphql.model.CreateBookInput;
import com.example.graphql.model.UpdateBookInput;
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
        @Argument CreateBookInput input
    ) {
        return bookService.create(input);
    }

    @MutationMapping
    public Book updateBook(
        @Argument Long id,
        @Argument UpdateBookInput input
    ) {
        return bookService.update(id, input);
    }

    @MutationMapping
    public boolean deleteBook(
        @Argument Long id
    ) {
        return bookService.delete(id);
    }
}
```

Test it:

```sh
mvn spring:boot-run
```

Check initial list:

```GraphQL
query {
    books {
        id
        title
        author
    }
}
```

Result:

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
      }
    ]
  }
}
```


--- UPDATE ---

Execute an update:

```GraphQL
mutation {
    updateBook(
        id: 2
        input: {
            title: "Effective Java - Third Edition"
        }
    ) {
        id
        title
        author
    }
}
```

Result of the update:

```json
{
	"data": {
		"updateBook": {
			"id": "2",
			"title": "Effective Java - Third Edition",  // Look here
			"author": "Joshua Bloch"
		}
	}
}
```

Check updated list:

```GraphQL
query {
    books {
        id
        title
        author
    }
}
```

Result after update:

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
        "title": "Effective Java - Third Edition",
        "author": "Joshua Bloch"
      }
    ]
  }
}
```


--- DELETE --- 

Execute the delete mutation:

```GraphQL
mutation {
    deleteBook(id: 2)
}
```

Result:

```json
{
  "data": {
    "deleteBook": true
  }
}
```

The list after delete:

```GraphQL
query {
    books {
        id
        title
    }
}
```

Result after delete:

```json
{
  "data": {
    "books": [
      {
        "id": "1",
        "title": "Clean Code"
      }
    ]
  }
}
```

Book `2` should no longer be present.

If you try:

```
mutation {
    deleteBook(id: 999)
}
```

the response becomes:

```
{
    "data": {
        "deleteBook": false
    }
}
```

because no book with that ID existed.

We've implemented the four basic CRUD operations:

```
                 GraphQL
                    │
       ┌────────────┼─────────────┐
       │            │             │
      READ        CREATE        UPDATE        DELETE
       │            │             │             │
     Query       Mutation      Mutation      Mutation
       │            │             │             │
    books()    createBook()   updateBook()  deleteBook()
```

And there's an important difference between the two input types:

```
input CreateBookInput {
    title: String!
    author: String!
}
```

means:

```
title  → required
author → required
```

whereas:

```
input UpdateBookInput {
    title: String
    author: String
}
```

means:

```
title  → optional, change it if supplied
author → optional, change it if supplied
```

That pattern is common for update operations.