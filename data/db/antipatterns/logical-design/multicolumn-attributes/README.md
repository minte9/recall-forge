## DB / Antipatterns / Logical Design / Multicolumn Attributes

### 1. tag1, tag2, tag3 Columns

Modeling a one-to-many relationship (a product can have several tags) as
a fixed set of numbered columns avoids a join, but hard-codes an
arbitrary maximum -- the fourth tag has nowhere to go without an `ALTER
TABLE`, and most rows waste unused columns as `NULL`.

~~~sql
-- caps every product at exactly 3 tags, most of them unused
CREATE TABLE products (id INT, tag1 TEXT, tag2 TEXT, tag3 TEXT)
~~~

### 2. Querying Against a Fixed Number of Slots

"Does this product have tag X" becomes an `OR` chain across every column
instead of a single condition, and it only gets worse as more columns are
added. A separate `product_tags(product_id, tag)` table turns both
problems -- unlimited tags, simple queries -- into a normal join.

~~~sql
-- multicolumn: WHERE tag1 = 'sale' OR tag2 = 'sale' OR tag3 = 'sale'
-- normalized: WHERE tag = 'sale' (in a joined product_tags table)
SELECT p.id FROM products p
JOIN product_tags t ON t.product_id = p.id
WHERE t.tag = 'sale'
~~~
