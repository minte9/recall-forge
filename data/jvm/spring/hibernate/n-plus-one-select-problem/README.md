## JVM / Spring / Hibernate / N+1 Select Problem

### 1. What Causes It

The N+1 problem happens when fetching N parent entities triggers one query for the parents, then N additional queries -- one per parent -- to lazily fetch an associated collection or relation, instead of a single joined query. It's invisible in code review since each individual query looks fine; the damage only shows up under load.

~~~java
List<Author> authors = authorRepository.findAll(); // 1 query
for (Author author : authors) {
    author.getBooks().size(); // N additional queries -- one lazy fetch per author
}
~~~


### 2. Detecting N+1 Queries

N+1 is easy to miss in development with a handful of test rows, but shows up clearly by logging actual SQL (spring.jpa.show-sql=true, or a tool like p6spy/datasource-proxy) and counting queries per request. A repeating identical SELECT ... WHERE author_id = ? pattern, once per loop iteration, is the tell-tale sign.

~~~properties
# application.properties -- log every SQL statement Hibernate actually executes
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
~~~


### 3. Fixing It with JOIN FETCH

JOIN FETCH in a JPQL query eagerly loads the association in the same single query instead of a separate lazy query per row, collapsing N+1 queries into one. @EntityGraph is an annotation-based alternative that achieves the same thing without hand-writing JPQL, useful when a repository already relies on derived query methods.

~~~java
@Query("select a from Author a join fetch a.books") // one query, books loaded eagerly
List<Author> findAllWithBooks();

@EntityGraph(attributePaths = "books") // annotation-based alternative
List<Author> findAll();
~~~
