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

### 2. Querying Depth-Unknown Trees

"Get all descendants of category X" can't be a single flat query against
an adjacency list without knowing the tree's max depth in advance -- it
needs a recursive CTE, or a different model entirely (nested sets, path
enumeration, closure table) chosen based on whether reads or writes
dominate.

~~~sql
WITH RECURSIVE descendants AS (
  SELECT id FROM categories WHERE id = 5
  UNION ALL
  SELECT c.id FROM categories c JOIN descendants d ON c.parent_id = d.id
)
SELECT * FROM descendants
~~~
