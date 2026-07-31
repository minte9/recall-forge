## DB / Antipatterns / Logical Design / Polymorphic Associations

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

### 2. Why the Database Can't Enforce It

A standard foreign key references exactly one table, so `parent_id` here
can't have a real FK constraint at all -- nothing stops it from pointing
at a row that never existed in either table. The two common fixes are
exclusive-arc tables (a nullable FK per parent type, one non-null at a
time) or a separate join table per parent type.

~~~sql
CREATE TABLE comments (
  id INT,
  post_id INT REFERENCES posts(id),
  photo_id INT REFERENCES photos(id)
  -- CHECK: exactly one of post_id/photo_id is non-null
)
~~~
