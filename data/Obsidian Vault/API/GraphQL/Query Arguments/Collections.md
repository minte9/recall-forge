## 1. List Syntax

So far, our queries have returned a single object:

```graphql
type Query {
    user(id: ID!): User
}
```

But real APIs frequently need to return collections:

```
books
users
products
orders
reviews
```

GraphQL represents collections using list types (that maps to List in java).

```graphql
[Book]
```

If we want a query that returns several books, we use square brackets. 
The brackets mean list of `Book` objects.

```
type Query {
    books: [Book]
}

type Book {
    id: ID!
    title: String!
    author: String!
    pages: Int!
}
```

Let's represent the GraphQL `Book` with a Java record.

```
public record Book(
    String id,
    String title,
    String author,
    Integer pages
) {}
```

Now create some books in the controller.

```
import com.example.graphql_demo.model.Book;
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
            416
        ),
        new Book(
            "BOOK-2",
            "Clean Code",
            "Robert C. Martin",
            464
        ),
        new Book(
            "BOOK-3",
            "Designing Data-Intensive Applications",
            "Martin Kleppmann",
            616
        )
    );

    @QueryMapping
    public List<Book> books() {
        return books;
    }
}
```

The mapping is straightforward:

```
GraphQL

books: [Book]
        │
        ▼
Java

List<Book> books()
```

## 2. Querying collections

Now we can request:

```
query GetBooks {
    books {
        id
        title
        author
        pages
    }
}
```

Notice that we still need a selection set. 
That's because each element inside the list is a Book object. 

```
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query GetBooks { books { id title author pages } }"}'
```

Response:

```
{
  "data": {
    "books": [
      {
        "id": "BOOK-1",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "pages": 416
      },
      {
        "id": "BOOK-2",
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "pages": 464
      },
      {
        "id": "BOOK-3",
        "title": "Designing Data-Intensive Applications",
        "author": "Martin Kleppmann",
        "pages": 616
      }
    ]
  }
}
```

GraphQL's field selection works exactly as it did with a single object. 
If the client only needs book titles: 

```
query GetBooks {
    books {
        title
    }
}
```

Request:

```
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query GetBooks { books { title } }"}' | jq
```

Response:

```
{
  "data": {
    "books": [
      {
        "title": "Effective Java"
      },
      {
        "title": "Clean Code"
      },
      {
        "title": "Designing Data-Intensive Applications"
      }
    ]
  }
}
```

The Java resolver still returns complete Book objects, but GraphQL returns only the fields requested by the client. 

```
public List<Book> books() {
    return books;
}
```

So the same principle applies to both single objects and collections:

```
Java

List<Book>
   │
   ├── id
   ├── title
   ├── author
   └── pages

          │
          │ GraphQL selection
          ▼

books {
    title
}

          │
          ▼

JSON

[
    { "title": "Effective Java" },
    { "title": "Clean Code" },
    ...
]
```

## 3. Nullability of lists

There's an important difference between returning [] or null. 
An empty list means that the query succeeded, but there are no books. 

```json
{
  "data": {
    "books": []
  }
}
```

While null means that  there is no list value. 

```json
{
  "data": {
    "books": null
  }
}
```

In Java List.of() represents an empty list while null represents no list.  
In most collection APIs, an empty collection is often easier for clients to work with than null.

GraphQL list nullability can initially look confusing.  
This is because `!` can apply to either the **list** or its elements.

There are four combinations worth understanding.

```
[Book]
[Book]!
[Book!]
[Book!]!
```

`[Book]` means nullable list containing nullable books. 

``` 
# These are all valid:
null
[]
[Book, Book]
[Book, null, Book]
```

`[Book]!` means that the list itself cannot be null.  

```
# Valid
[]
[Book, Book]
[Book, null, Book]

# Invalid, because ! applies to the list.
null
```

`[Book!]` means that the list itself can be null, but individual books cannot.  

```
# Valid
null
[]
[Book, Book, Book]

# Invalid, because each book must be not-null
[Book, null, Book]
```

`[Book!]!` means that neither the list nor its elements can be null.  

```
# Valid
[]
[Book]
[Book, Book, Book]

# Invalid
null
[Book, null, Book]
```

## 4. List with arguments

Lists become much more useful when combined with arguments. 
For example, suppose we want books by a particular author: 

```graphql
type Query {
    books(author: String): [Book!]!
}
```

The argument is optional because it doesn't have `!`.
We can implement: 

```
@QueryMapping
public List<Book> books(@Argument String author) {

    if (author == null) {
        return books;
    }

    return books.stream()
            .filter(book -> book.author().equals(author))
            .toList();
}
```

Without an argument, we get all books. 

```
query GetBooks {
    books {
        title
    }
}
```

With an argument, we get matching books.

```
query GetBooks {
    books(author: "Joshua Bloch") {
        title
        author
    }
}
```

Test it:

```
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query GetBooks { books(author: \"Joshua Bloch\") { title author } }"}'
```

Response:

```
{
  "data": {
    "books": [
      {
        "title": "Effective Java",
        "author": "Joshua Bloch"
      }
    ]
  }
}
```


## 5. Combining with variables

Instead of hardcoding the author, we can declare a variable:

```graphql
query GetBooks($author: String) {
    books(author: $author) {
        title
        pages
    }
}
```

Complete `curl`:

```
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query GetBooks($author: String) { books(author: $author) { title pages } }",
    "variables": {
      "author": "Joshua Bloch"
    }
  }' | jq
```

Response:

```json
{
  "data": {
    "books": [
      {
        "title": "Effective Java",
        "pages": 416
      }
    ]
  }
}
```


## 6. Complete example

schema.graphqls

```graphql
type Query {
    books(author: String): [Book!]!
}

type Book {
    id: ID!
    title: String!
    author: String!
    pages: Int!
}
```

Book.java

```java
package com.example.graphql_demo.model;

public record Book(
    String id,
    String title,
    String author,
    Integer pages
) {}
```

QueryController.java

```java
package com.example.graphql_demo.controller;

import com.example.graphql_demo.model.Book;
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
            416
        ),
        new Book(
            "BOOK-2",
            "Clean Code",
            "Robert C. Martin",
            464
        ),
        new Book(
            "BOOK-3",
            "Designing Data-Intensive Applications",
            "Martin Kleppmann",
            616
        )
    );

    @QueryMapping
    public List<Book> books(@Argument String author) {

        if (author == null) {
            return books;
        }

        return books.stream()
                .filter(book -> book.author().equals(author))
                .toList();
    }
}
```

Query all books:

```sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ books { id title author pages } }"}'
```

Or filter using a variable:

```sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query GetBooks($author: String) { books(author: $author) { title pages } }",
    "variables": {
      "author": "Joshua Bloch"
    }
  }'
```