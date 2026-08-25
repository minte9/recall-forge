# DB / PostgreSQL / Data Types & Configuration / JSONB & UUID

## 1. JSONB vs JSON

`JSON` stores the exact input text and re-parses it on every access;
`JSONB` stores a decomposed binary representation -- slightly slower to
write, but faster to query and the only one of the two that can be
indexed (with a GIN index) for containment queries. Default to `JSONB`
unless preserving the exact original text/whitespace/key order matters.

```sql
CREATE TABLE products (id INT, attributes JSONB);
CREATE INDEX idx_attrs ON products USING GIN (attributes);
SELECT * FROM products WHERE attributes @> '{"color": "red"}';
```

## 2. Native UUID Type & Generation

`UUID` is a real 16-byte type, not a `CHAR(36)` string -- smaller storage,
faster comparison, and the type system rejects a malformed value instead
of silently accepting bad text. `gen_random_uuid()` (built in since
Postgres 13) generates a random v4 UUID without needing a separate
extension.

```sql
CREATE TABLE sessions (id UUID PRIMARY KEY DEFAULT gen_random_uuid());
```
