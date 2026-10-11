## 1. What is the problem?

The N+1 problem happens when an application first executes one query to retrieve a collection of objects, and then executes an additional query for each object to retrieve related data.

This GraphQL query is a perfect example:

```GraphQL
query {
    books {
        title
        author {
            name
        }
    }
}
```

Suppose `books` contains 100 books. A naive implementation could result in:

```
1 query     → load 100 books

100 queries → load the author for each book

--------------------------------
101 database queries
```

That's why it's called N+1:

```
1 initial query + N additional queries
```

With three books, this doesn't look serious. With hundreds or thousands of objects—and possibly several nested relationships—it can become a major performance problem. 

N+1 is not unique to GraphQL. The same problem can occur with REST, JPA, ORMs, template rendering, and other systems.

But GraphQL makes it particularly easy to encounter because nested fields are naturally resolved independently.

## 2. @BatchMapping

Spring GraphQL's @BatchMapping is designed for this situation. Instead of receiving one parent object, the resolver receives a list of parent objects and resolves their related objects together.

```
1 query → load books
1 query → load all required authors

Total: 2 queries
```

Our old resolver operates on one `Book`:

```java
@SchemaMapping(typeName = "Book", field = "author")
public Author author(Book book) {
    return authorService.findById(book.authorId());
}
```

```
Book #1 → author(Book #1)
Book #2 → author(Book #2)
Book #3 → author(Book #3)
Book #4 → author(Book #4)
```

With `@BatchMapping`, we can instead receive multiple books:

```java
@BatchMapping(typeName = "Book", field = "author")
public Map<Book, Author> author(List<Book> books) {
    ...
}
```

```
Book #1 ─┐
Book #2 ─┤
Book #3 ─┼──→ author(List<Book>)
Book #4 ─┘
```

## 3. Complete Example

schema.graphqls

```GraphQL
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
package com.example.graphql.model;

public record Book(
        Long id,
        String title,
        Category category,
        Long authorId
) {
}
```

Author.java

```java
package com.example.graphql.model;

public record Author(
        Long id,
        String name
) {
}
```

Category.java

```java
package com.example.graphql.model;

public enum Category {
    PROGRAMMING,
    DATABASE
}
```

BookService.java

We'll use four books so that two of them share the same author:

```java
package com.example.graphql.service;

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
                    "Java Concurrency in Practice",
                    Category.PROGRAMMING,
                    101L
            ),
            new Book(
                    3L,
                    "Clean Code",
                    Category.PROGRAMMING,
                    102L
            ),
            new Book(
                    4L,
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

AuthorService.java

Instead of `findById()`, we'll add a method capable of retrieving multiple authors:

```java
package com.example.graphql.service;

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

    public List<Author> findAllByIds(List<Long> ids) {

        System.out.println(
                "Fetching authors: " + ids
        );

        return authors.stream()
                .filter(author -> ids.contains(author.id()))
                .toList();
    }
}
```

BookController.java

Now replace `@SchemaMapping` with `@BatchMapping`:

```java
package com.example.graphql.controller;

import com.example.graphql.model.Author;
import com.example.graphql.model.Book;
import com.example.graphql.service.AuthorService;
import com.example.graphql.service.BookService;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @BatchMapping(typeName = "Book", field = "author")
    public Map<Book, Author> author(List<Book> books) {
		
		// Extract the unique author IDs from all requested books
        List<Long> authorIds = books.stream()
                .map(Book::authorId)
                .distinct()
                .toList();
		
		// Fetch all required authors in a single service/database call
        List<Author> authors =
                authorService.findAllByIds(authorIds);
		
		// Create a lookup map: authorId -> Author 
		// This allows us to quickly find an author for each book
        Map<Long, Author> authorsById = authors.stream()
                .collect(Collectors.toMap(
                        Author::id,
                        author -> author
                ));
		
		// Create the result expected by @BatchMapping: 
		// Book -> corresponding Author
        return books.stream()
                .collect(Collectors.toMap(
                        book -> book,
                        book -> authorsById.get(book.authorId())
                ));
    }
}
```

Test it:

```
query {
    books {
        title
        author {
            name
        }
    }
}
```

```
curl http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ books { title author { name } } }"
  }' | jq
```

```json
{
  "data": {
    "books": [
      {
        "title": "Effective Java",
        "author": {
          "name": "Joshua Bloch"
        }
      },
      {
        "title": "Java Concurrency in Practice",
        "author": {
          "name": "Joshua Bloch"
        }
      },
      {
        "title": "Clean Code",
        "author": {
          "name": "Robert C. Martin"
        }
      },
      {
        "title": "SQL Antipatterns",
        "author": {
          "name": "Bill Karwin"
        }
      }
    ]
  }
}
```

But the interesting part is the server console. 
You should see only:

```
Fetching authors: [101, 102, 103]
```

rather than:

```
Fetching author: 101
Fetching author: 101
Fetching author: 102
Fetching author: 103
```

That's the batching effect.