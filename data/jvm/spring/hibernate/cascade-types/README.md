## JVM / Spring / Hibernate / Cascade Types

### 1. CascadeType.ALL and Friends

cascade = CascadeType.ALL on a relationship propagates every operation (persist, merge, remove, refresh, detach) from the parent to its associated children automatically -- saving an Author also saves any new Book objects attached to it, with no separate save() call needed for each child.

~~~java
@Entity
class Author {
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Book> books;
}

author.getBooks().add(newBook);
authorRepository.save(author); // saves newBook too, thanks to CascadeType.ALL
~~~


### 2. orphanRemoval

orphanRemoval = true deletes a child entity from the database automatically when it's removed from its parent's collection, even without an explicit delete call on the child -- distinct from CascadeType.REMOVE, which only deletes children when the parent itself is deleted.

~~~java
@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Book> books;

author.getBooks().remove(book); // book is deleted from the database on next flush, no explicit delete()
~~~


### 3. Cascade Pitfalls

CascadeType.ALL on a @ManyToOne (the "many" side) is a common mistake -- deleting one Book would cascade-delete the shared Author, likely destroying data other Books still reference. Cascades generally make sense only on the "owning" side of a relationship where children genuinely can't exist without that specific parent.

~~~java
// Dangerous: deleting one Book could cascade and delete the shared Author
@ManyToOne(cascade = CascadeType.ALL) // wrong side for this cascade
private Author author;
~~~
