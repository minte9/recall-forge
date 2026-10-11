
## 1. Docker Compose

Until now, our GraphQL examples have mostly stored `Book` objects in Java collections. We will now introduce a real PostgreSQL database and Spring Data JPA.  The architecture will gradually become: 

```txt
GraphQL request
       ↓
GraphQL Controller
       ↓
Service
       ↓
Spring Data JPA Repository
       ↓
Hibernate
       ↓
PostgreSQL
```

Create a `docker-compose.yml` in the project root. If another PostgreSQL Docker container exposes 543, expose yours on 5433.

```yaml
services:

  postgres:
    image: postgres:16
    container_name: graphql-postgres
    environment:
      POSTGRES_DB: graphql_db
      POSTGRES_USER: graphql_user
      POSTGRES_PASSWORD: graphql_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Start PostgreSQL:

```sh
docker compose up -d
docker ps

	CONTAINER ID   IMAGE         PORTS
	e89dfd         postgres:16   0.0.0.0:5433->5432/tcp
```

The volumes postgres_data: /var/lib/postgresql/data means that the database isn't tied to the lifetime of the container. Docker compose down will recreate the container while keeping the database data.  

```sh
docker exec -it graphql-postgres psql \
    -U graphql_user \
    -d graphql_db
    
graphql_db=# SELECT current_database();

	current_database 
	------------------
	 graphql_db
	(1 row)
	
graphql_db=# \dt
	
	Did not find any relations.

```

Later, Hibernate will create our tables.

## 2. Spring Data JPA

Our application already has Spring GraphQL. Now we need two additional pieces:

```txt
Spring Data JPA
      +
PostgreSQL JDBC Driver
```

For a Maven project, add these dependencies to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

The relevant dependencies might therefore look like:

```xml
<dependencies>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-graphql</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

</dependencies>
```

Spring Data JPA gives us the Java persistence abstraction:

```java
@Entity
@Id
JpaRepository
```

The PostgreSQL JDBC driver is what allows Java/Hibernate to actually communicate with PostgreSQL. 

```txt
Our Java code
      ↓
Spring Data JPA
      ↓
JPA
      ↓
Hibernate
      ↓
PostgreSQL JDBC Driver
      ↓
PostgreSQL
```

JPA itself is primarily a specification/API. Hibernate is the JPA implementation Spring Boot normally uses. 

## 3. Configurations

Add relevant configurations to the *src/main/resources/application.properties*:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/graphql_db
spring.datasource.username=graphql_user
spring.datasource.password=graphql_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**Datasource URL**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/graphql_db
```

It can be broken down as:

```txt
jdbc:postgresql://localhost:5432/graphql_db
│     │            │        │
│     │            │        └── database
│     │            └─────────── port
│     └──────────────────────── host
└────────────────────────────── JDBC
```

Because the Spring Boot application is running directly on your Ubuntu machine while PostgreSQL exposes:

```yaml
ports:
  - "5432:5432"
```

The application can access PostgreSQL through:

```txt
localhost:5432
```

**Hibernate**

This property is particularly important:

```properties
spring.jpa.hibernate.ddl-auto=update
```

It tells Hibernate to update the database schema according to your JPA entities. 
For example, in the next lesson we'll create something similar to: 

```java
@Entity
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    private String title;
}
```

Hibernate can then create the corresponding PostgreSQL table.

```txt
Java

@Entity
Book
 ├── id
 └── title

        ↓ Hibernate

PostgreSQL

book
+----+----------------+
| id | title          |
+----+----------------+
```

For learning and local development, `update` is convenient.

In production applications, schema changes are normally managed explicitly with migration tools such as Flyway or Liquibase rather than relying on Hibernate to modify the production schema automatically. 

**Display generated SQL**

These settings are very useful while learning JPA.

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

When Java code eventually executes:

```java
bookRepository.findAll();
```

you'll be able to see SQL generated by Hibernate, roughly:

```sql
select
    b1_0.id,
    b1_0.title
from
    book b1_0;
```

`JpaRepository` doesn't mean SQL has disappeared. Hibernate is generating much of it for us.

```txt
bookRepository.findAll()
        ↓
Hibernate generates SQL
        ↓
SELECT ... FROM book
        ↓
PostgreSQL
```

## 4. Start Spring Boot

Update `pom.xml` and `application.properties` and make sure PostgreSQL is running:

```sh
docker compose up -d
docker ps

./mvnw spring-boot:run
```

During startup you should see Hibernate and the datasource being initialized. You may see messages mentioning things such as:

```
HikariPool
Hibernate ORM
PostgreSQL
EntityManagerFactory
```

`HikariPool` is the database connection pool used by Spring Boot by default.

The application doesn't normally create a completely new database connection for every GraphQL request. Connections are maintained in a pool and reused.

```
Spring Boot
     │
     ↓
 HikariCP
 ┌─────────────┐
 │ connection 1│
 │ connection 2│
 │ connection 3│
 │ ...         │
 └─────────────┘
       │
       ↓
 PostgreSQL
```

GraphQL doesn't directly communicate with PostgreSQL. The following request does not become SQL automatically because GraphQL understands databases. 

```GraphQL
query {
  books {
    id
    title
  }
}
```

Instead, Spring GraphQL calls Java code. 

```java
@QueryMapping
public List<Book> books() {
    return bookService.findAll();
}
```

The service will call: 

```java
bookRepository.findAll();
```

And Hibernate will ultimately execute SQL against PostgreSQL.  

GraphQL handles the **API/query interface**, while JPA/Hibernate handles **persistence**. 