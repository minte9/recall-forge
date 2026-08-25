# DB / SQL / SQL Antipatterns / Images

## 1. Storing Files Directly in the Database

Putting image/document bytes into a `BLOB` column keeps everything in one
place transactionally, but it bloats table/index size, slows down backups
and replication (now moving gigabytes of binary data alongside your rows),
and forces every read through the database instead of a CDN or static
file server built for exactly this.

```sql
CREATE TABLE products (id INT, thumbnail BLOB)  -- bytes in every row
```

## 2. Filesystem/Object Storage + a Reference Column

Storing the file in object storage (S3, a CDN, a filesystem) and keeping
only a path/URL in the database keeps rows small and lets the file be
served directly by infrastructure optimized for that -- caching, range
requests, parallel downloads -- none of which a database column gives you.

```sql
CREATE TABLE products (id INT, thumbnail_url TEXT)
```
