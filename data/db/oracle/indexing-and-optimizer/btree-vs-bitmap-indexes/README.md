# DB / Oracle / Indexing & Optimizer / B-Tree vs Bitmap Indexes

## 1. B-Tree Indexes

Oracle's default index type, same structure as most other engines --
efficient for high-cardinality columns (unique or near-unique values) and
for OLTP workloads with frequent single-row inserts/updates, since each
index entry is small and updates touch a small, localized part of the
tree.

```sql
CREATE INDEX idx_orders_customer ON orders (customer_id);
```

## 2. Bitmap Indexes & When They Win

A bitmap index stores a bitmap per distinct value, marking which rows
match -- compact and extremely fast to combine with `AND`/`OR` across
multiple bitmap indexes, but a poor fit for high-write OLTP tables since
a single row update can lock a large segment of the bitmap. Best suited
to low-cardinality columns (status, gender, region) in read-heavy/
reporting workloads.

```sql
CREATE BITMAP INDEX idx_orders_status ON orders (status);
```
