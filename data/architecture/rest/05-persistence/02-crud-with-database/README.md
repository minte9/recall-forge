# API / REST / Persistence / CRUD with Database

## 1. Creating Persisted Resources

On POST, map the request DTO to a new entity, `save()` it, and return the
generated id in `Location`. The id comes from the database, which is why
create is POST to the collection rather than PUT to an item.

```java
@Transactional
public Book create(NewBook dto) {
    Book b = new Book(dto.title(), dto.authorId());
    return repo.save(b);           // id assigned by the database
}
```

## 2. Reading and Updating Entities

Load by id and translate "missing" into a 404-mapped exception. Inside a
transaction, modifying a managed entity is enough: dirty checking flushes
the change on commit, with no explicit `save()` needed.

```java
@Transactional
public Book rename(Long id, String title) {
    Book b = repo.findById(id)
        .orElseThrow(() -> new BookNotFoundException(id));
    b.setTitle(title);             // flushed automatically at commit
    return b;
}
```

## 3. Deleting Entities

Decide what DELETE of a missing id means (404 or idempotent 204) and apply
it consistently. Watch foreign keys: deleting a parent with children
either needs cascade/orphan rules or should be rejected with 409.

```java
@Transactional
public void delete(Long id) {
    if (!repo.existsById(id)) throw new BookNotFoundException(id);
    repo.deleteById(id);   // FK violation -> map to 409 Conflict
}
```
