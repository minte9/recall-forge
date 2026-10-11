## 1. @SchemaMapping

Our Java `Book` object might contained the complete `Author`:

```GraphQL
public record Book(
        Long id,
        String title,
        Category category,
        Author author
) {}
```

That makes nested GraphQL fields easy because Spring can simply call:

```
book.author()
```

But real applications often don't work this way.  
A `Book` might store only an author's ID while authors are stored separately.

```java
public record Book(
        Long id,
        String title,
        Category category,
        Long authorId
) {}

Author(101L, "Joshua Bloch")
Author(102L, "Robert C. Martin")
```

Our GraphQL schema can still expose:

```GraphQL
type Book {
    id: ID!
    title: String!
    category: Category!
    author: Author!
}
```

But now there is a problem:

```
GraphQL expects:          Java Book contains:

Book                     Book
 └── author: Author       └── authorId: Long
```

This is what `@SchemaMapping` is for, to resolve that field.
We can create a method responsible specifically for resolving `Book.author`:

```java
@SchemaMapping(typeName = "Book", field = "author")
public Author author(Book book) {

    return authors.stream()
            .filter(author -> author.id().equals(book.authorId()))
            .findFirst()
            .orElse(null);
}
```

The `Book` parameter is the parent object currently being processed.
For example: 

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

Spring first resolves:

```
book(id: 1)
```

and gets:

```GraphQL
Book(
    1L,
    "Effective Java",
    Category.PROGRAMMING,
    101L
)
```

Then GraphQL sees that the client requested:

```
author {
    name
}
```

Spring invokes:

```
author(book)
```

The method uses:

```
book.authorId()
```

to find the corresponding `Author`.

So the resolution process becomes:

```
Query.book
    │
    ▼
Book(id=1, authorId=101)
    │
    │ GraphQL needs "author"
    ▼
@SchemaMapping
    │
    ▼
find Author where id = 101
    │
    ▼
Author(id=101, name="Joshua Bloch")
```

## 2. @QueryMapping

Both annotations resolve GraphQL fields, but at different levels. 
@QueryMapping resolves fields belonging to the root `Query` type:

```GraphQL
type Query {
    book(id: ID!): Book
    books: [Book!]!
}
```

For example:

```java
@QueryMapping
public Book book(@Argument Long id) {
    // ...
}
```

This resolves:

```GraphQL
query {
    book(id: 1) {
        ...
    }
}
```

@SchemaMapping, on the other hand, resolves fields belonging to another GraphQL type:

```GraphQL
type Book {
    id: ID!
    title: String!
    author: Author!
}
```

For example:

```java
@SchemaMapping(typeName = "Book", field = "author")
public Author author(Book book) {
    // ...
}
```

This resolves the nested part:

```
book {
    author {
        ...
    }
}
```

A useful mental model is:

```
@QueryMapping

Query
 └── book
      └── Book


@SchemaMapping

Book
 └── author
      └── Author
```

In fact, conceptually both are GraphQL field resolvers.  
@QueryMapping is essentially a convenient annotation for fields on the GraphQL `Query` type.


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

Notice the distinction. 
GraphQL doesn't need to expose how the relationship is stored internally. 

```
Java internal model:

Book
 └── authorId


GraphQL API model:

Book
 └── author
      ├── id
      └── name
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

Author.java

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

BookController.java

```java
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class BookController {

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

    private final List<Author> authors = List.of(
            new Author(101L, "Joshua Bloch"),
            new Author(102L, "Robert C. Martin"),
            new Author(103L, "Bill Karwin")
    );

    @QueryMapping
    public Book book(@Argument Long id) {

        return books.stream()
                .filter(book -> book.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    @QueryMapping
    public List<Book> books() {
        return books;
    }

    @SchemaMapping(typeName = "Book", field = "author")
    public Author author(Book book) {

        return authors.stream()
                .filter(author ->
                        author.id().equals(book.authorId())
                )
                .findFirst()
                .orElse(null);
    }
}
```

Now query:

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

With `curl`:

```sh
curl http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ books { id title author { id name } } }"
  }'
```

The result should be:

```
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

The important flow is:

```
GraphQL request
      │
      ▼
@QueryMapping
books()
      │
      ▼
List<Book>
      │
      ├── Book(authorId=101)
      │        │
      │        ▼
      │   @SchemaMapping
      │        │
      │        ▼
      │   Author(101)
      │
      ├── Book(authorId=102)
      │        │
      │        ▼
      │   @SchemaMapping
      │        │
      │        ▼
      │   Author(102)
      │
      └── Book(authorId=103)
               │
               ▼
          @SchemaMapping
               │
               ▼
          Author(103)
```