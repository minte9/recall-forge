## 1. GraphQL Aliases

Normally, the name of a field in the response matches the name in the query.

```GraphQL
query Query {
    books {
        title
    }
}
```

```sh
curl -X POST http://localhost:9191/graphql \
     -H "Content-Type: application/json" \
     -d '{"query":"{ books {title} }"}' | jq
```

Response:

```json
{
  "data": {
    "books": [
      {"title": "Effective Java"},
      {"title": "Clean Code"},
      {"title": "Designing Data-Intensive Applications"},
      {"title": "Introduction to Algorithms"}
    ]
  }
}
```

An alias lets the client change that response name.

```GraphQL
query GetBooks {
    programmingBooks: books(category: PROGRAMMING) {
        title
    }
}
```

```sh
curl -X POST http://localhost:9191/graphql \
-H "Content-Type: application/json" \
-d '{
"query":"query GetBooks { programmingBooks: books(category: PROGRAMMING) {title} databaseBooks: books(category: DATABASE) {title} }"}' | jq
```

Response:

```
{
  "data": {
    "programmingBooks": [
      {
        "title": "Effective Java"
      },
      {
        "title": "Clean Code"
      }
    ],
    "databaseBooks": [
      {
        "title": "Designing Data-Intensive Applications"
      }
    ]
  }
}
```

```
programmingBooks: books
────────────────  ─────
       │             │
       │             └── actual schema field
       │
       └── alias
```

The alias does not rename anything on the server.  
Yet both fields use the same server resolver: 

```
ONE GraphQL operation
        │
        ├── programmingBooks
        │        │
        │        └── books(PROGRAMMING)
        │                    │
        │                    ▼
        │                books(...)
        │
        └── databaseBooks
                 │
                 └── books(DATABASE)
                             │
                             ▼
                         books(...)
```

Aliases can rename ordinary fields too.

```GraphQL
query GetBooks {
    books {
        bookTitle: title
        writer: author
    }
}
```

```sh
curl -X POST http://localhost:9191/graphql \
-H "Content-Type: application/json" \
-d '{
"query": "query GetBooks {books {bookTitle: title writer: author} }"}' | jq
```

Response:

```json
{
  "data": {
    "books": [
      {
        "bookTitle": "Effective Java",
        "writer": "Joshua Bloch"
      },
      {
        "bookTitle": "Clean Code",
        "writer": "Robert C. Martin"
      },
      {
        "bookTitle": "Designing Data-Intensive Applications",
        "writer": "Martin Kleppmann"
      },
      {
        "bookTitle": "Introduction to Algorithms",
        "writer": "Thomas H. Cormen"
      }
    ]
  }
}
```

## 2. Fragments

A fragment defines a reusable selection set.

```GraphQL
fragment BookFields on Book {
    id
    title
    author
    pages
    category
}
```

Let's break down the syntax:

```
fragment BookFields on Book
         ──────────    ────
              │          │
              │          └── GraphQL type
              │
              └── fragment name
```

Whenever `BookFields` is used on a `Book`, select these fields. 
We use the fragment with: 

```
...BookFields
```

The `...` syntax is called a fragment spread. 
Our complete query becomes: 

```
query GetBooks {
    programmingBooks: books(category: PROGRAMMING) {
        ...BookFields
    }

    databaseBooks: books(category: DATABASE) {
        ...BookFields
    }
}

fragment BookFields on Book {
    id
    title
    author
    pages
    category
}
```

The fragment does not create another API request. 
It doesn't call another resolver.  

It is simply a reusable part of the GraphQL query document: 

```
BookFields
    │
    ├── id
    ├── title
    └── author
        │
        │ reused
        ▼

programmingBooks ──► BookFields
databaseBooks    ──► BookFields
algorithmsBooks  ──► BookFields
```

## 3. GraphiQL

When learning GraphQL, putting the whole query inside `curl -d` quickly becomes unreadable.

```sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query GetBooks { programmingBooks: books(category: PROGRAMMING) { ...BookFields } databaseBooks: books(category: DATABASE) { ...BookFields } } fragment BookFields on Book { id title author category }"
  }' | jq
```

With Spring for GraphQL, you can enable GraphiQL in `application.properties`: 

```
spring.graphql.graphiql.enabled=true
```

and normally open:

```
http://localhost:9191/graphiql
```

You can write the query naturally, then just press Execute. 

```GraphQL
query GetBooks {
  programmingBooks: books(category: PROGRAMMING) {
    ...BookFields
  }

  databaseBooks: books(category: DATABASE) {
    ...BookFields
  }
}

fragment BookFields on Book {
  id
  title
  author
  category
}
```

Response:

```json
{
  "data": {
    "programmingBooks": [
      {
        "id": "BOOK-1",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "category": "PROGRAMMING"
      },
      {
        "id": "BOOK-2",
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "category": "PROGRAMMING"
      }
    ],
    "databaseBooks": [
      {
        "id": "BOOK-3",
        "title": "Designing Data-Intensive Applications",
        "author": "Martin Kleppmann",
        "category": "DATABASE"
      }
    ]
  }
}
```

API clients are another good option (Bruno, Postman, Insomnia).

## 4. Complete example

An important point is that we haven't changed the server.

schema.graphqls

```GraphQL
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

and Java is still: 

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

The interesting part is now entirely in the query:

```GraphQL
query GetBooks(
    $programming: BookCategory
    $database: BookCategory
) {
    programmingBooks: books(category: $programming) {
        ...BookFields
    }

    databaseBooks: books(category: $database) {
        ...BookFields
    }
}

fragment BookFields on Book {
    id
    title
    author
    pages
    category
}
```

Variables:

```json
{
  "programming": "PROGRAMMING",
  "database": "DATABASE"
}
```

And the complete request:

```sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query GetBooks($programming: BookCategory, $database: BookCategory) { programmingBooks: books(category: $programming) { ...BookFields } databaseBooks: books(category: $database) { ...BookFields } } fragment BookFields on Book { id title author pages category }",
    "variables": {
      "programming": "PROGRAMMING",
      "database": "DATABASE"
    }
  }' | jq
```

Response:

```json
{
  "data": {
    "programmingBooks": [
      {
        "id": "BOOK-1",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "pages": 416,
        "category": "PROGRAMMING"
      },
      {
        "id": "BOOK-2",
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "pages": 464,
        "category": "PROGRAMMING"
      }
    ],
    "databaseBooks": [
      {
        "id": "BOOK-3",
        "title": "Designing Data-Intensive Applications",
        "author": "Martin Kleppmann",
        "pages": 616,
        "category": "DATABASE"
      }
    ]
  }
}
```