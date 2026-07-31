## DB / Antipatterns / Logical Design / Parent ID (Naive Trees)

### 1. The Adjacency List Model

Modeling a tree with a single `parent_id` column pointing at the same
table is the obvious first design -- cheap to write, one row per node. The
catch is that it only directly answers "who is my immediate parent/child";
anything about depth or full ancestry needs extra work the model doesn't
give you for free.

~~~sql
CREATE TABLE categories (
  id INT PRIMARY KEY,
  parent_id INT REFERENCES categories(id)  -- adjacency list
)
~~~

### 2. Better Tree Models

A recursive CTE fixes the immediate query problem without changing the
schema. For read-heavy trees, a different model pays off further: nested
sets or path enumeration answer "all descendants" with a single indexed
range scan, and a closure table (one row per ancestor-descendant pair)
trades storage for the fastest reads at write-time cost.

~~~sql
WITH RECURSIVE descendants AS (
  SELECT id FROM categories WHERE id = 5
  UNION ALL
  SELECT c.id FROM categories c JOIN descendants d ON c.parent_id = d.id
)
SELECT * FROM descendants
~~~
