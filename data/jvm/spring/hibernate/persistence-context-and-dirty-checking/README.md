## JVM / Spring / Hibernate / Persistence Context and Dirty Checking

### 1. The Persistence Context

The persistence context (EntityManager's first-level cache) tracks every entity loaded or saved during a transaction -- fetching the same entity twice by ID within one transaction returns the exact same in-memory object, not two separate copies, and any changes to it are tracked automatically for the duration of that transaction.

~~~java
@Transactional
void example() {
    Author a1 = entityManager.find(Author.class, 1L);
    Author a2 = entityManager.find(Author.class, 1L);
    System.out.println(a1 == a2); // true -- same object, from the persistence context
}
~~~


### 2. Dirty Checking

Hibernate automatically detects changes made to a managed entity's fields and generates the corresponding UPDATE statement at flush time, with no explicit save() call needed -- this is "dirty checking." It only applies to entities still attached to an open persistence context; changes to a detached entity are silently ignored.

~~~java
@Transactional
void updateName(Long id, String newName) {
    Author author = authorRepository.findById(id).orElseThrow();
    author.setName(newName); // no explicit save() call -- Hibernate detects and persists this change
}
~~~


### 3. flush() vs commit()

flush() synchronizes pending changes to the database immediately (running the SQL) without ending the transaction; commit() ends the transaction, making those already-flushed changes permanent and visible to other transactions. Flushing manually is occasionally needed to force order (e.g. before a native query that reads what was just written).

~~~java
authorRepository.save(newAuthor);
entityManager.flush(); // forces the INSERT to run now, before the native query below
jdbcTemplate.queryForList("SELECT * FROM authors WHERE name = ?", newAuthor.getName());
~~~
