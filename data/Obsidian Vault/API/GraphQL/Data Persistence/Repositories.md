## 1. Repository

Now we need a way to interact with the `book` table.

```java
package com.example.graphql_demo.repository;

import com.example.graphql.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
```

This tiny interface gives us a large amount of functionality.
We don't need to implement all those method, Spring Data JPA provides implementations automatically. 

```java
findAll()
findById()
save()
delete()
deleteById()
count()
existsById()
```

JpaRepository contains two generic types.

```txt
JpaRepository<Book, Long>
              │     │
              │     └── ID type
              │
              └──────── Entity type
```

Our entity is:

```java
@Entity
public class Book {

    @Id
    private Long id;
}
```

Therefore:

```
JpaRepository<Book, Long>
```

means:

> Create a repository that manages `Book` entities whose IDs are `Long`.

Suppose we execute:

```java
bookRepository.findAll();
```

The flow is approximately:

```txt
BookRepository.findAll()
        ↓
Spring Data JPA
        ↓
Hibernate
        ↓
SQL
        ↓
PostgreSQL
```

Hibernate generates SQL similar to:

```SQL
select
    b.id,
    b.title,
    b.author
from
    book b;
```

PostgreSQL returns rows:

```txt
1 | Effective Java        | Joshua Bloch
2 | Clean Code            | Robert C. Martin
3 | Spring in Action      | Craig Walls
```

Hibernate converts them into:

```java
List<Book>
```

So the repository lets us work primarily with **Java objects** rather than **SQL rows**.

## 2. Service Layer

Technically, our GraphQL controller could call the repository directly:

```
@QueryMapping
public List<Book> books() {
    return bookRepository.findAll();
}
```

However, a more maintainable architecture is:

```
Controller
    ↓
Service
    ↓
Repository
```

Create:

```java
package com.example.graphql_demo.service;

import com.example.graphql_demo.entity.Book;
import com.example.graphql_demo.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }
}
```

## 3. Controller

Why not simply call the repository from the controller? Because later the service can contain **business logic**. 

The controller's job remains focused on GraphQL. The service handles application/business rules. The repository handles persistence. This separation becomes especially useful in larger applications.

```java
package com.example.graphql_demo.controller;

import com.example.graphql_demo.entity.Book;
import com.example.graphql_demo.service.BookService;

import org.springframework.graphql.data.method.annotation.Argument;
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

    @QueryMapping
    public Book bookById(@Argument Long id) {
        return bookService.findById(id)
                .orElse(null);
    }
}
```

Notice something important. The controller doesn't know anything about:

```txt
PostgreSQL
Hibernate
SQL
JDBC
```

It simply asks:

```java
bookService.findAll();
```

That's the benefit of the layers. 