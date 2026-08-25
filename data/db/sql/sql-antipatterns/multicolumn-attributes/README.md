# DB / SQL / SQL Antipatterns / Multicolumn Attributes

## 1. tag1, tag2, tag3 Columns

Modeling a one-to-many relationship (a product can have several tags) as
a fixed set of numbered columns avoids a join, but hard-codes an
arbitrary maximum -- the fourth tag has nowhere to go without an `ALTER
TABLE`, and most rows waste unused columns as `NULL`.

```sql
-- caps every product at exactly 3 tags, most of them unused
CREATE TABLE products (id INT, tag1 TEXT, tag2 TEXT, tag3 TEXT)
```

## 2. A Child Table Instead

A separate `product_tags(product_id, tag)` table removes the arbitrary
cap -- a product can have any number of tags, adding one is just an
`INSERT`, and "does this product have tag X" becomes a single join
condition instead of an `OR` chain across every numbered column.

```sql
-- multicolumn: WHERE tag1 = 'sale' OR tag2 = 'sale' OR tag3 = 'sale'
-- normalized: WHERE tag = 'sale' (in a joined product_tags table)
SELECT p.id FROM products p
JOIN product_tags t ON t.product_id = p.id
WHERE t.tag = 'sale'
```
