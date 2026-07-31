## DB / SQL / SQL Antipatterns / Mandatory ID

### 1. Forcing a Surrogate Key Everywhere

Adding an `id` column to every table "because that's the rule" ignores
tables that already have a perfectly good key. A join/intersection table
like `(student_id, course_id)` gets a redundant `id` column plus a
now-unenforced uniqueness rule on the pair that actually defines the row.

~~~sql
-- redundant id: the real key is (student_id, course_id)
CREATE TABLE enrollments (
  id INT PRIMARY KEY,
  student_id INT, course_id INT
)
~~~

### 2. When a Natural Key Is Enough

If a composite of existing columns is already unique and stable, it can be
the primary key directly -- no surrogate needed, and the constraint that
matters (`no duplicate enrollment`) is enforced by the key itself instead
of a separate `UNIQUE` constraint bolted on next to an unrelated `id`.

~~~sql
CREATE TABLE enrollments (
  student_id INT, course_id INT,
  PRIMARY KEY (student_id, course_id)  -- the real uniqueness rule
)
~~~
