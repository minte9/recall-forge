# DB / PostgreSQL / Storage & MVCC / Tuple Versioning & Heap Storage

## 1. How Postgres MVCC Works

Unlike Oracle/MySQL, which reconstruct old versions from a separate undo
log, Postgres writes a full new tuple directly into the heap on every
`UPDATE` -- the old tuple stays in place until vacuumed. This makes
writes simpler (no undo bookkeeping) at the cost of bloat: every update
leaves a dead row version behind that has to be cleaned up later.

```sql
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
-- old tuple stays on disk, marked dead; a new tuple is appended
```

## 2. xmin/xmax Visibility

Every tuple carries hidden `xmin` (the transaction ID that created it)
and `xmax` (the transaction ID that deleted/superseded it, if any).
Whether a given transaction can see a tuple is decided by comparing its
own snapshot against these two values -- no separate lock or flag needed,
just a comparison against the tuple's own metadata.

```sql
SELECT xmin, xmax, * FROM accounts WHERE id = 1;
```
