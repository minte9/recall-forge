# DB / MySQL / Storage Engines / MyISAM & Engine Choice

## 1. MyISAM Characteristics

MyISAM predates InnoDB as MySQL's original default engine: no
transactions, no foreign keys, and table-level (not row-level) locking --
a single writer blocks every reader and every other writer on that table.
It's not crash-safe either: an unclean shutdown mid-write can corrupt the
table, requiring a repair pass on restart.

```sql
CREATE TABLE logs (id INT, msg TEXT) ENGINE=MyISAM;
```

## 2. Choosing InnoDB by Default

InnoDB became MySQL's default engine in 5.5 (2010) specifically because
transactions, row-level locking, and crash recovery matter for almost any
real application. MyISAM still turns up in older schemas or for
specialized read-only/full-text-search use cases, but starting a new
table as MyISAM today needs a specific reason, not just habit.

```sql
SELECT table_name, engine FROM information_schema.tables
WHERE table_schema = 'app' AND engine != 'InnoDB';
```
