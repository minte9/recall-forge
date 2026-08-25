# DB / PostgreSQL / Query Planning / Planner Cost Model

## 1. random_page_cost & seq_page_cost

The planner picks a plan by estimating cost in abstract units, not real
time -- `seq_page_cost` (default 1.0) is the cost of a sequential page
read, `random_page_cost` (default 4.0) is the cost of a random one. That
4x default assumes spinning disks; on SSD-backed storage, lowering
`random_page_cost` (often to ~1.1) better reflects reality and makes the
planner favor index scans more readily.

```sql
SET random_page_cost = 1.1;  -- SSD-appropriate
```

## 2. work_mem's Effect on Plan Choice

`work_mem` caps how much memory a single sort or hash operation can use
before spilling to disk -- too low, and the planner may avoid a hash join
or hash aggregate it would otherwise pick, or a sort spills to slow disk
merge passes. It's per-operation, not per-query, so a complex query can
use several multiples of `work_mem` at once.

```sql
SET work_mem = '64MB';
```
