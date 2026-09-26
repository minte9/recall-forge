# API / REST / Testing / Integration Tests

## 1. @SpringBootTest

`@SpringBootTest` starts the whole application context: controllers,
services, repositories, security, config. It's slower than slice tests,
but it catches wiring and configuration bugs that mocks hide.

```java
@SpringBootTest
class BookApiIT {
    @Autowired BookService service;   // real beans, real wiring
}
```

## 2. Full HTTP Tests

With `webEnvironment = RANDOM_PORT`, a real server starts, and a client
such as `TestRestTemplate` or `RestTestClient` calls it over the network,
exercising filters, serialization and security exactly as production does.

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BookApiIT {
    @Autowired TestRestTemplate rest;

    @Test void createsBook() {
        var res = rest.postForEntity("/books",
            new NewBook("Dune", 7L), BookResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
```

## 3. Testing Layers Together

Integration tests verify the flow controller → service → repository →
database in one go. Keep them few and focused on critical paths; push
edge cases down to faster unit and slice tests.

```
many  unit tests         (service logic)
some  @WebMvcTest        (HTTP contract)
few   @SpringBootTest    (end-to-end flows)
```
