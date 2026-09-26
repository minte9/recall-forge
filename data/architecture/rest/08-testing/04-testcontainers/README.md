# API / REST / Testing / Testcontainers

## 1. Testcontainers Basics

Testcontainers starts real dependencies (PostgreSQL, Kafka, Redis) in
throwaway Docker containers for the duration of a test. You test against
the same database engine as production instead of H2, which differs in
SQL dialect and behavior.

```java
@Container
static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:17");
// started before tests, removed after; needs Docker available
```

## 2. Running a Real Database for Tests

Each test run gets a clean database, so schema migrations, constraints,
native queries and JSON columns are all exercised for real. Reuse one
container per test class (static field) or per suite to keep runs fast.

```java
@Testcontainers
@SpringBootTest
class BookRepositoryIT {
    @Container
    static PostgreSQLContainer<?> db =
        new PostgreSQLContainer<>("postgres:17");   // shared by class
}
```

## 3. Spring Boot Integration

`@ServiceConnection` wires the container's URL and credentials into the
Spring context automatically, with no `@DynamicPropertySource`
boilerplate. Boot detects the container type and configures the
matching connection.

```java
@Container
@ServiceConnection            // sets spring.datasource.* for you
static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:17");
```
