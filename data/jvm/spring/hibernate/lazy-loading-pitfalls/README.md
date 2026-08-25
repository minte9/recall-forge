# JVM / Spring / Hibernate / Lazy Loading Pitfalls

## 1. LazyInitializationException

Accessing a lazy-loaded association after the persistence context/session that fetched the entity has already closed throws LazyInitializationException -- a very common bug when an entity is fetched in a repository, returned up to a controller, and only then does the view or JSON serializer try to access the unloaded relation.

```java
@GetMapping("/authors/{id}")
Author getAuthor(@PathVariable Long id) {
    Author author = authorRepository.findById(id).orElseThrow();
    return author; // if books is lazy, serializing this later can throw LazyInitializationException
}
```


## 2. Open Session in View

Open Session in View (enabled by default in Spring Boot) keeps the Hibernate session open through the entire web request, including view rendering/JSON serialization, which papers over LazyInitializationException at the cost of doing database work outside the service/transaction layer. Many teams deliberately disable it to force explicit, intentional fetching instead.

```properties
# Disabling OSIV forces lazy access to happen inside a @Transactional service method,
# not accidentally during serialization -- surfaces lazy-loading bugs earlier
spring.jpa.open-in-view=false
```


## 3. DTO Projections to Avoid Lazy Loading

Mapping entities to a plain DTO/record inside the transactional service method (before the session closes) sidesteps lazy-loading issues entirely, since the DTO holds already-fetched plain values instead of a lazy proxy. This is also generally better practice than returning JPA entities directly from a REST layer.

```java
record AuthorSummary(String name, int bookCount) {}

@Transactional(readOnly = true)
AuthorSummary getAuthorSummary(Long id) {
    Author author = authorRepository.findById(id).orElseThrow();
    return new AuthorSummary(author.getName(), author.getBooks().size()); // fetched while session is open
}
```
