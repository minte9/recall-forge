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

### 2. Real Columns, Not EAV

Model the attributes that are actually common to every product as typed,
constrained columns -- `weight NUMERIC`, `color TEXT`. For the genuinely
open-ended remainder, a single `JSONB` column keeps the same flexibility
EAV offered, but as one document per row instead of N rows per attribute,
and modern engines can still index specific JSON paths.

~~~sql
CREATE TABLE products (
  id INT, weight NUMERIC, color TEXT,  -- common attributes: real columns
  extra JSONB                           -- rare/custom attributes
)
~~~
