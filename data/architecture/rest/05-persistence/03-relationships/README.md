# API / REST / Persistence / Relationships

## 1. @OneToMany and @ManyToOne

`@ManyToOne` on the child owns the foreign key; `@OneToMany(mappedBy)` on
the parent is the inverse side. For APIs, the child side is usually what
you query; keep the collection side lazy and don't load it by default.

```java
@Entity class Book {
    @ManyToOne(fetch = FetchType.LAZY) Author author;  // owns FK
}
@Entity class Author {
    @OneToMany(mappedBy = "author") List<Book> books;  // inverse
}
```

## 2. REST Representations of Relationships

Decide per endpoint how to represent a relation: an id, a link, or an
embedded summary. Embed only what clients commonly need together, and
expose large or unbounded collections as their own sub-resource instead.

```json
{"id": 42, "title": "Dune",
 "author": {"id": 7, "name": "Frank Herbert"}}
// author's books: GET /authors/7/books, not embedded
```

## 3. Avoiding Serialization Problems

Serializing entities directly causes infinite recursion (book → author →
books → ...), `LazyInitializationException` outside the session, and N+1
queries. Mapping to DTOs inside the transaction avoids all three.

```java
// Book -> Author -> List<Book> -> Author ... StackOverflowError
record BookResponse(Long id, String title, String authorName) {
    static BookResponse from(Book b) {    // call inside @Transactional
        return new BookResponse(b.getId(), b.getTitle(),
                                b.getAuthor().getName());
    }
}
```
