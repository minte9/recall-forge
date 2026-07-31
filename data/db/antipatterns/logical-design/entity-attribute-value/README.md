## DB / Antipatterns / Logical Design / Entity-Attribute-Value

### 1. The EAV Temptation

EAV models "any entity can have any attribute" as rows in a generic
`(entity_id, attribute_name, value)` table instead of fixed columns --
appealing when the attribute set looks unbounded or user-defined (product
specs, custom fields), since adding a new attribute never needs a schema
change.

~~~sql
CREATE TABLE product_attributes (
  product_id INT, attribute_name TEXT, attribute_value TEXT
)
-- (1, 'color', 'red'), (1, 'weight', '2kg'), ...
~~~

### 2. The Cost: No Types, No Constraints, No Simple Queries

Every value is stored as text, so the database can't enforce that
`weight` is numeric or `color` is one of a known set. Reading a product
with all its attributes as normal columns needs a self-join per attribute
(or a pivot), and a query like "products where weight > 1kg" needs a cast
on every row instead of an index range scan.

~~~sql
SELECT p.name
FROM products p
JOIN product_attributes a ON a.product_id = p.id
WHERE a.attribute_name = 'weight'
  AND a.attribute_value::numeric > 1  -- cast text on every row
~~~
