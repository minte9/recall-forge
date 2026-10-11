## 1. DataFetcher / resolver

When GraphQL receives a query, it needs some Java code that knows how to obtain the requested data. In GraphQL terminology, that code is generally called a resolver or, in GraphQL Java, a `DataFetcher`.

Consider:

```GraphQL
query {
    book(id: 1) {
        title
        author {
            name
        }
    }
}
```

GraphQL itself does not know where books or authors come from. They could come from MySQL, PostgreSQL, another REST API, a cache, or simply a Java `List`.

Something must resolve each field:

```
Query
 │
 └── book(id: 1)          ← resolver
       │
       ├── title           ← automatically from Book
       │
       └── author          ← resolver
             │
             └── name      ← automatically from Author
```

With Spring for GraphQL, annotations such as `@QueryMapping` and `@SchemaMapping` provide a convenient way of defining these resolvers without manually creating GraphQL Java `DataFetcher` objects.

For example:

```java
@QueryMapping
public Book book(@Argument Long id) {
    return bookService.findById(id);
}
```

is a resolver for:

```GraphQL
type Query {
    book(id: ID!): Book
}
```

And:

```java
@SchemaMapping(typeName = "Book", field = "author")
public Author author(Book book) {
    return authorService.findById(book.authorId());
}
```

is a resolver for:

```GraphQL
type Book {
    author: Author!
}
```

## 2. Not every field needs a resolver

An important distinction is that you don't normally write a resolver for every field. 
Spring/GraphQL Java can automatically obtain fields from the corresponding Java object. 

```java
public record Book(
        Long id,
        String title,
        Category category,
        Long authorId
) {}
```

```GraphQL
type Book {
    id: ID!
    title: String!
    category: Category!
    author: Author!
}
```

Conceptually:

```
GraphQL field       Java value

id              →   book.id()
title           →   book.title()
category        →   book.category()
```

But this doesn't work for author because our Java `Book` has:

```
Long authorId
```

not:

```
Author author
```

That's why we provide:

```
@SchemaMapping(typeName = "Book", field = "author")
public Author author(Book book) {
    return authorService.findById(book.authorId());
}
```

So a resolver is especially important when the requested GraphQL field must be computed, fetched, transformed, or otherwise obtained dynamically.  

## 3. Service layers

Our previous example stored everything directly inside `BookController`. 

```java
private final List<Book> books = List.of(...);

private final List<Author> authors = List.of(...);
```

That's useful while learning GraphQL, but it's not a good application structure. 
A better structure is:

```
GraphQL request
      │
      ▼
Controller
      │
      ▼
Service
      │
      ▼
Repository / Database
```

The GraphQL controller should primarily deal with the GraphQL API layer:

```
@QueryMapping
public Book book(@Argument Long id) {
    return bookService.findById(id);
}
```

The service contains application/business logic:

```
public Book findById(Long id) {
    // fetch book
}
```

Later, the service could call a Spring Data repository:

```
bookRepository.findById(id)
```

This separation is no different from what you would normally do with a REST Spring application.

The main difference is the entry point:

```
REST

HTTP
 ↓
@RestController
 ↓
Service
 ↓
Repository


GraphQL

GraphQL operation
 ↓
@Controller
@QueryMapping / @SchemaMapping
 ↓
Service
 ↓
Repository
```


## 4. Complete example

Let's restructure our previous application using services.

```
src/main/java/com/example/graphql/
│
├── GraphqlApplication.java
│
├── controller/
│   └── BookController.java
│
├── service/
│   ├── BookService.java
│   └── AuthorService.java
│
└── model/
    ├── Book.java
    ├── Author.java
    └── Category.java
```

schema.graphqls

```
type Query {
    book(id: ID!): Book
    books: [Book!]!
}

type Book {
    id: ID!
    title: String!
    category: Category!
    author: Author!
}

type Author {
    id: ID!
    name: String!
}

enum Category {
    PROGRAMMING
    DATABASE
}
```

Book.java

```java
public record Book(
        Long id,
        String title,
        Category category,
        Long authorId
) {}
```

Author.java`

```java
public record Author(
        Long id,
        String name
) {}
```

Category.java

```java
public enum Category {
    PROGRAMMING,
    DATABASE
}
```

BookService.java

```java
import com.example.graphql.model.Book;
import com.example.graphql.model.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final List<Book> books = List.of(
            new Book(
                    1L,
                    "Effective Java",
                    Category.PROGRAMMING,
                    101L
            ),
            new Book(
                    2L,
                    "Clean Code",
                    Category.PROGRAMMING,
                    102L
            ),
            new Book(
                    3L,
                    "SQL Antipatterns",
                    Category.DATABASE,
                    103L
            )
    );

    public Book findById(Long id) {

        return books.stream()
                .filter(book -> book.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Book> findAll() {
        return books;
    }
}
```

The controller no longer needs to know about this `List`.
Later we could replace:

```
books.stream()
```

with:

```
bookRepository.findById(id)
```

AuthorService.java

```java
import com.example.graphql.model.Author;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    private final List<Author> authors = List.of(
            new Author(101L, "Joshua Bloch"),
            new Author(102L, "Robert C. Martin"),
            new Author(103L, "Bill Karwin")
    );

    public Author findById(Long id) {

        return authors.stream()
                .filter(author -> author.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}
```

The controller becomes much simpler:

```java
import com.example.graphql.model.Author;
import com.example.graphql.model.Book;
import com.example.graphql.service.AuthorService;
import com.example.graphql.service.BookService;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class BookController {

    private final BookService bookService;
    private final AuthorService authorService;

    public BookController(
            BookService bookService,
            AuthorService authorService
    ) {
        this.bookService = bookService;
        this.authorService = authorService;
    }

    @QueryMapping
    public Book book(@Argument Long id) {
        return bookService.findById(id);
    }

    @QueryMapping
    public List<Book> books() {
        return bookService.findAll();
    }

    @SchemaMapping(typeName = "Book", field = "author")
    public Author author(Book book) {
        return authorService.findById(book.authorId());
    }
}
```

The controller's job is essentially to connect:

```
GraphQL → Java application
```

while the services are responsible for obtaining the data.

```GraphQL
query {
    books {
        id
        title
        author { 
	        id 
	        name 
	    }
    }
}
```

Test it:

```sh
curl http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ books { id title author { id name } } }"
  }' | jq
```

Result:

```json
{
  "data": {
    "books": [
      {
        "id": "1",
        "title": "Effective Java",
        "author": {
          "id": "101",
          "name": "Joshua Bloch"
        }
      },
      {
        "id": "2",
        "title": "Clean Code",
        "author": {
          "id": "102",
          "name": "Robert C. Martin"
        }
      },
      {
        "id": "3",
        "title": "SQL Antipatterns",
        "author": {
          "id": "103",
          "name": "Bill Karwin"
        }
      }
    ]
  }
}
```
