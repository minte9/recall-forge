# DB / MongoDB / Indexing / Index Types

## 1. Single-Field & Compound Indexes

A single-field index works like a B-tree index in a relational engine; a
compound index covers multiple fields in a defined order. The ESR rule
(Equality, Sort, Range) is the standard guideline for ordering a compound
index's fields: equality filters first, then sort fields, then range
filters last.

```javascript
db.orders.createIndex({ customerId: 1, status: 1, createdAt: -1 })
```

## 2. Multikey Indexes

Indexing a field that holds an array automatically creates a multikey
index -- one index entry per array element, no special syntax needed. A
query matching any element of the array can use it, but a compound index
can have at most one array field, since MongoDB can't cheaply index the
Cartesian product of two array fields together.

```javascript
db.products.createIndex({ tags: 1 })
db.products.find({ tags: 'sale' })  // uses the multikey index
```
