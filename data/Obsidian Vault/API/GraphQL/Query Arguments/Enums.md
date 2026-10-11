## 1. GraphQL Enums

Strings work well when values are open-ended, such as names or search text.  

```graphql
books(author: "Joshua Bloch")
```

But sometimes an argument should accept only a fixed set of values.  
Instead of accepting any arbitrary string, GraphQL lets us define an `enum`. 

Suppose every book belongs to one of these categories. 
We can then use it inside our object type. 

```graphql
type Query {
    books(category: BookCategory): [Book!]!
}

type Book {
    id: ID!
    title: String!
    author: String!
    pages: Int!
    category: BookCategory!
}

enum BookCategory {
    PROGRAMMING
    DATABASE
    ALGORITHMS
}
```

The important difference from `String` is that GraphQL knows exactly which values are valid.
These queries are valid: 

```graphql
books(category: PROGRAMMING)
books(category: DATABASE)
books(category: ALGORITHMS)
```

But this isn't.
GraphQL can reject the invalid value before our resolver executes. 

```graphql
books(category: HISTORY)
```

An important syntax detail is that GraphQL enum values are written without quotes. 

```graphql
{
    books(category: PROGRAMMING) {
        title
    }
}
```

Incorrect:

```graphql
{
    books(category: "PROGRAMMING") {
        title
    }
}
```

## 2. Enums as arguments

Our schema says:

```graphql
type Query {
    books(category: BookCategory): [Book!]!
}
```

Therefore Spring can receive the argument directly as our Java enum.

```java
@QueryMapping
public List<Book> books(@Argument BookCategory category) {
    // ...
}
```

```java
public enum BookCategory {
    PROGRAMMING,
    DATABASE,
    ALGORITHMS
}
```

We don't need:

```
@Argument String category
```

followed by:

```
BookCategory.valueOf(category);
```

Instead, Spring GraphQL can perform the conversion for us:

```
GraphQL

category: PROGRAMMING
             │
             ▼
GraphQL enum
BookCategory
             │
             ▼
Spring GraphQL
             │
             ▼
Java

BookCategory.PROGRAMMING
```

The Java method therefore works with the strongly typed enum directly.

## 3. Filtering by enum

Now we can filter our collection. 

```graphql
type Query {
    books(category: BookCategory): [Book!]!
}
```

```java
@QueryMapping
public List<Book> books(@Argument BookCategory category) {

    if (category == null) {
        return books;
    }

    return books.stream()
            .filter(book -> book.category() == category)
            .toList();
}
```

In our schema argument is optional:

```
category: BookCategory
```

and not:

```
category: BookCategory!
```

Therefore, this returns all books.

```
{
    books {
        title
    }
}
```

But this returns only programming books.

```
{
    books(category: PROGRAMMING) {
        title
    }
}
```

Request programming books:

```
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query":"{ books(category: PROGRAMMING) { title author category } }"}' | jq
```


## 4. Complete example

schema.graphqls

```graphql
type Query {
    books(category: BookCategory): [Book!]!
}

type Book {
    id: ID!
    title: String!
    author: String!
    pages: Int!
    category: BookCategory!
}

enum BookCategory {
    PROGRAMMING
    DATABASE
    ALGORITHMS
}
```

BookCategory.java

```java
package com.example.graphql_demo.model;

public enum BookCategory {
    PROGRAMMING,
    DATABASE,
    ALGORITHMS
}
```

Book.java

```java
package com.example.graphql_demo.model;

public record Book(
    String id,
    String title,
    String author,
    Integer pages,
    BookCategory category
) {}
```

QueryController.java

```java
package com.example.graphql_demo.controller;

import com.example.graphql_demo.model.Book;
import com.example.graphql_demo.model.BookCategory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class QueryController {

    private final List<Book> books = List.of(
        new Book(
            "BOOK-1",
            "Effective Java",
            "Joshua Bloch",
            416,
            BookCategory.PROGRAMMING
        ),
        new Book(
            "BOOK-2",
            "Clean Code",
            "Robert C. Martin",
            464,
            BookCategory.PROGRAMMING
        ),
        new Book(
            "BOOK-3",
            "Designing Data-Intensive Applications",
            "Martin Kleppmann",
            616,
            BookCategory.DATABASE
        ),
        new Book(
            "BOOK-4",
            "Introduction to Algorithms",
            "Thomas H. Cormen",
            1312,
            BookCategory.ALGORITHMS
        )
    );

    @QueryMapping
    public List<Book> books(@Argument BookCategory category) {

        if (category == null) {
            return books;
        }

        return books.stream()
                .filter(book -> book.category() == category)
                .toList();
    }
}
```

Test all books:

```sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ books { title category } }"}' | jq
```

```json
{
  "data": {
    "books": [
      {
        "title": "Effective Java",
        "category": "PROGRAMMING"
      },
      {
        "title": "Clean Code",
        "category": "PROGRAMMING"
      },
      {
        "title": "Designing Data-Intensive Applications",
        "category": "DATABASE"
      },
      {
        "title": "Introduction to Algorithms",
        "category": "ALGORITHMS"
      }
    ]
  }
}
```

Filter by category:

```sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ books(category: PROGRAMMING) { title category } }"}' | jq
```

```json
{
  "data": {
    "books": [
      {
        "title": "Effective Java",
        "category": "PROGRAMMING"
      },
      {
        "title": "Clean Code",
        "category": "PROGRAMMING"
      }
    ]
  }
}
```

Or use a variable:

```sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query GetBooks($category: BookCategory) { books(category: $category) { title category } }",
    "variables": {
      "category": "ALGORITHMS"
    }
  }' | jq
```

```json
{
  "data": {
    "books": [
      {
        "title": "Introduction to Algorithms",
        "category": "ALGORITHMS"
      }
    ]
  }
}
```
