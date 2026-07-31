## DB / SQL / SQL Antipatterns / Polymorphic Associations

### 1. A Single FK Column Pointing at Multiple Tables

A `comments` table that can attach to either a `post` or a `photo` often
gets modeled with `parent_id` plus a `parent_type` string column deciding
which table `parent_id` refers to -- one comments table instead of two,
at the cost of the FK no longer pointing at a single well-defined table.

~~~sql
CREATE TABLE comments (
  id INT, parent_type TEXT, parent_id INT  -- 'post' or 'photo'
)
~~~

### 2. Exclusive-Arc Foreign Keys

Give each parent type its own nullable FK column with a `CHECK` that
exactly one is non-null -- both `post_id` and `photo_id` get real,
enforceable foreign keys. When the set of parent types is large or grows
often, a separate join table per parent type is the more scalable version
of the same idea.

~~~sql
CREATE TABLE comments (
  id INT,
  post_id INT REFERENCES posts(id),
  photo_id INT REFERENCES photos(id)
  -- CHECK: exactly one of post_id/photo_id is non-null
)
~~~
