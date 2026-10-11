## 1. JPA Entity

We'll replace our in-memory `Book` storage with a proper persistence layer using **JPA entities**, **Spring Data `JpaRepository`**, and a **service layer**.

```txt
src/main/java/com/example/graphql/
│
├── GraphqlApplication.java
│
├── controller/
│   └── BookController.java
│
├── service/
│   └── BookService.java
│
├── repository/
│   └── BookRepository.java
│
└── entity/
    └── Book.java

src/main/resources/
│
├── application.properties
│
└── graphql/
    └── schema.graphqls

docker-compose.yml
pom.xml
```

The complete flow will be:

```
GraphQL request
      ↓
BookController
      ↓
BookService
      ↓
BookRepository
      ↓
Hibernate / JPA
      ↓
PostgreSQL
```

The important idea is that **GraphQL and JPA solve different problems**. GraphQL defines how clients request data; JPA defines how Java objects are persisted in a relational database.

With JPA, we create an **entity**: 

```java
package com.example.graphql_demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;

    public Book() {
    }

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
```

## 2. Annotations

**@Entitly**

The @Entitly annotation tells JPA that bjects of this class should be persisted in the database. Without `@Entity`, `Book` is simply a normal Java class. 

When the application starts, Hibernate examines the entity metadata. Because we configured ddl-auto=update, Hibernate can create/update the corresponding database table. 

**@Id**

Every JPA entity requires an identifier. This tells JPA that `id` is the `primary key`. 

It corresponds roughly to:

```SQL
CREATE TABLE book (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    author VARCHAR(255)
);
```

**@GeneratedValue**

We don't want to manually assign IDs such as:

```
new Book(1L, "Effective Java", "Joshua Bloch");
```

Instead PostgreSQL can generate them.

```
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

**No-argument constructor**

Notice that we have:

```java
public Book() {
}

public Book(String title, String author) {
    this.title = title;
    this.author = author;
}
```

The empty constructor exists primarily for JPA/Hibernate.

Hibernate needs to be able to construct an entity when loading database rows.



