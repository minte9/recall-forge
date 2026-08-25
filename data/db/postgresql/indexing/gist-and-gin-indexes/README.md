# DB / PostgreSQL / Indexing / GiST & GIN Indexes

## 1. GIN for JSONB/Full-Text/Arrays

A GIN (Generalized Inverted Index) maps each individual element (a JSONB
key, a word, an array element) to the rows containing it -- like a book's
index, not a sorted list. This makes "does this JSONB document contain
key X" or full-text search fast, at the cost of slower writes than a
B-tree, since one row change can touch many index entries.

```sql
CREATE INDEX idx_products_attrs ON products USING GIN (attributes);
SELECT * FROM products WHERE attributes @> '{"color": "red"}';
```

## 2. GiST for Geometric/Range Types

A GiST (Generalized Search Tree) index supports "nearest neighbor" and
overlap queries -- geometric shapes, IP ranges, `tstzrange` bookings --
that a B-tree's total ordering can't express efficiently. It's the index
type behind PostGIS spatial queries and behind exclusion constraints that
prevent overlapping ranges.

```sql
CREATE INDEX idx_bookings_range ON bookings USING GIST (during);
-- prevents overlapping bookings via an exclusion constraint
ALTER TABLE bookings ADD EXCLUDE USING GIST (room_id WITH =, during WITH &&);
```
