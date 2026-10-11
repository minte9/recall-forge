
## 1. GraphQL Schema

Our schema can remain focused on the GraphQL API.

```
src/main/resources/graphql/schema.graphqls
```

```
type Book {
    id: ID!
    title: String!
    author: String!
}

type Query {
    books: [Book!]!
    bookById(id: ID!): Book
}
```

Notice that GraphQL doesn't contain anything like:

```
@Entity
@Table
JpaRepository
PostgreSQL
```

GraphQL doesn't care where the data comes from.

The resolver could get books from:

```
PostgreSQL
MongoDB
REST API
another microservice
a file
memory
```

From GraphQL's perspective, it simply receives Java objects that satisfy the schema.

## 2. GraphQL Request

Using a readable GraphQL client, send to: 

```sh
http://localhost:9191/graphiql?path=/graphql
```

Query:

```GraphQL
query {
    books {
        id
        title
        author
    }
}
```

Or with `curl`:

```sh 
curl http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ books { id title author } }"
  }' | jq
```

Response:

```json
{
  "data": {
    "books": []
  }
}
```

We haven't inserted anything yet, so the response is correct.  For now, we can add a couple of rows directly through PostgreSQL so that we can test the query.

```sh
docker exec -it graphql-postgres psql \
    -U graphql_user \
    -d graphql_db
    
\dt
\d book
```

```SQL
INSERT INTO book (title, author)
VALUES ('Effective Java', 'Joshua Bloch');

INSERT INTO book (title, author)
VALUES ('Clean Code', 'Robert C. Martin');

INSERT INTO book (title, author)
VALUES ('Spring in Action', 'Craig Walls');

SELECT * FROM book;
```

Using a readable GraphQL client, send:

```GraphQL
query {
    books {
        id
        title
        author
    }
}
```

Or with `curl`:

```sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ books { id title author } }"}' | jq
```

Response:

```json
{
  "data": {
    "books": [
      {
        "id": "1",
        "title": "Effective Java",
        "author": "Joshua Bloch"
      },
      {
        "id": "2",
        "title": "Clean Code",
        "author": "Robert C. Martin"
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

You can also query one book:

```GraphQL
query {
    bookById(id: 2) {
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
		"bookById": {
			"id": "2",
			"title": "Clean Code",
			"author": "Robert C. Martin"
		}
	}
}
```

## 3. Optional

Notice that our reporitory method returns Optional of Book, because a row with that ID might not exist.  

```java
@Service
public class BookService {
	...
	
	public Optional<Book> findById(Long id) {
		return bookRepository.findById(id);
	}
}
```

Therefore:

```
query {
    bookById(id: 999) {
        id
        title
    }
}
```

Returns:

```
{
  "data": {
    "bookById": null
  }
}
```


## 4. Repository Implementation

There is another important Spring Data concept here. 
We created:

```
public interface BookRepository
        extends JpaRepository<Book, Long> {
}
```

It's only an interface, we never wrote:

```
public class BookRepositoryImpl {
    ...
}
```

Yet this works:

```
bookRepository.findAll();
```

That's because Spring Data creates a repository implementation for us at runtime.

**JPA** vs **Hibernate** vs **Spring Data JPA**

These names can initially be confusing.

A useful mental model is:

```
Spring Data JPA
      ↓
makes repositories convenient

JPA
      ↓
defines persistence API/specification

Hibernate
      ↓
implements JPA

JDBC Driver
      ↓
communicates with PostgreSQL

PostgreSQL
      ↓
stores the data
```

For example:

```
@Entity
```

is imported from:

```
jakarta.persistence.Entity
```

That's part of the Jakarta Persistence/JPA API.

Meanwhile:

```
JpaRepository
```

comes from:

```
org.springframework.data.jpa.repository.JpaRepository
```

That's Spring Data.

You usually use all of these together in a Spring Boot JPA application.

