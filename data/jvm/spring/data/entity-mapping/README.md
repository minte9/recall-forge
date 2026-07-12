## JVM / Spring / Data / Entity Mapping

### 1. @Entity, @Id, @GeneratedValue

@Entity marks a class as a JPA-managed table mapping; @Id marks its primary key field; @GeneratedValue(strategy = GenerationType.IDENTITY) delegates ID generation to the database's own auto-increment column, the most common strategy with Postgres/MySQL. Every entity needs exactly one @Id field.

~~~java
@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
}
~~~


### 2. @Column and Nullable Constraints

@Column customizes how a field maps to its database column -- name, nullable, unique, length -- though a field with no annotation still maps using sensible defaults (the field name, nullable). nullable = false enforces a NOT NULL constraint at the schema level, not just in application code.

~~~java
@Entity
class User {
    @Column(nullable = false, unique = true)
    private String email; // enforced at the database level, not just in Java

    @Column(name = "display_name", length = 100)
    private String displayName;
}
~~~


### 3. Relationships (@ManyToOne, @OneToMany)

@ManyToOne marks the "many" side of a relationship (many Topics belong to one MarkdownFile) and owns the foreign key column via @JoinColumn. @OneToMany marks the inverse "one" side and is usually mappedBy the other side's field name -- getting this direction right (and defaulting to LAZY fetch) matters a lot for query performance.

~~~java
@Entity
class Topic {
    @ManyToOne(fetch = FetchType.LAZY) // many Topics -> one MarkdownFile
    @JoinColumn(name = "markdown_file_id")
    private MarkdownFile markdownFile;
}

@Entity
class MarkdownFile {
    @OneToMany(mappedBy = "markdownFile") // inverse side, no extra column
    private List<Topic> topics;
}
~~~
