# DB / SQL / SQL Antipatterns / Wildcard

## 1. Leading-Wildcard LIKE Searches

`LIKE '%term%'` (or `'%term'`) can't use a standard B-tree index, because
the index is sorted by prefix and a leading `%` means there's no fixed
prefix to seek to -- the engine falls back to scanning every row. `LIKE
'term%'` (no leading wildcard) can still use the index normally.

```sql
WHERE name LIKE '%smith%'   -- full scan, no index can help
WHERE name LIKE 'smith%'    -- can seek via a normal B-tree index
```

## 2. Alternatives for Text Search

For genuine substring/fuzzy search, a trigram index (`pg_trgm` in
Postgres) or a proper full-text index (`FULLTEXT` in MySQL, `tsvector` in
Postgres) handles leading-wildcard-style queries efficiently -- built for
exactly this, unlike a B-tree index repurposed for text matching.

```sql
CREATE INDEX idx_name_trgm ON customers USING gin (name gin_trgm_ops);
```
