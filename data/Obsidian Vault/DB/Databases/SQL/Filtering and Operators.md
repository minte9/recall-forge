## 1. SQL Filtering

SQL filtering allows us to retrieve only the rows that satisfy specific conditions.

The `WHERE` clause is commonly used with comparison operators, logical operators, range checks, pattern matching, and NULL checks.

We'll continue using the `customers`.

```sql
CREATE TABLE customers (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    city TEXT,
    active INTEGER DEFAULT 1
);
```

Sample data:

|id|name|city|active|
|---|---|---|---|
|1|Alice|London|1|
|2|Bob|Paris|1|
|3|Charlie|London|0|
|4|Diana|Berlin|1|
|5|Eve|Paris|1|

### Comparison Operators

Comparison operators compare values and return a condition that SQL can evaluate.

|Operator|Meaning|Example|
|---|---|---|
|`=`|Equal|`city = 'Paris'`|
|`<>` or `!=`|Not equal|`city <> 'Paris'`|
|`>`|Greater than|`id > 3`|
|`<`|Less than|`id < 3`|
|`>=`|Greater or equal|`id >= 3`|
|`<=`|Less or equal|`id <= 3`|

Example:

```sql
SELECT id, name
FROM customers
WHERE id >= 3;
```

Result:

```
3 | Charlie
4 | Diana
5 | Eve
```

Important: SQL uses `=` for equality, not `==` as in Java. SQLite also accepts `==`, but `=` is standard SQL.

### Logical Operators — AND, OR, NOT

Logical operators combine multiple conditions.

### AND

Both conditions must be true.

```sql
SELECT name, city
FROM customers
WHERE city = 'London'
  AND active = 1;
```

Result: Alice.

### OR

At least one condition must be true.

```sql
SELECT name, city
FROM customers
WHERE city = 'London'
   OR city = 'Paris';
```

Result: Alice, Bob, Charlie, Eve.

### NOT

Negates a condition.

```sql
SELECT name, city
FROM customers
WHERE NOT city = 'Paris';
```

Result: Alice, Charlie, Diana.

### Operator precedence

SQL evaluates `NOT` before `AND`, and `AND` before `OR`.

```sql
SELECT *
FROM customers
WHERE city = 'London'
   OR city = 'Paris'
  AND active = 1;
```

This means:

```sql
WHERE city = 'London'
   OR (city = 'Paris' AND active = 1)
```

Charlie is included even though inactive.

To require all customers to be active:

```sql
WHERE (city = 'London' OR city = 'Paris')
  AND active = 1
```


## 2. IN — Match Multiple Values

`IN` checks whether a value belongs to a set.

```sql
SELECT name, city
FROM customers
WHERE city IN ('London', 'Paris');
```

Equivalent to:

```sql
WHERE city = 'London'
   OR city = 'Paris'
```

To exclude values:

```sql
SELECT name, city
FROM customers
WHERE city NOT IN ('London', 'Paris');
```

Result: Diana.

`IN` is especially useful when filtering by multiple IDs in backend applications.

```sql
SELECT *
FROM customers
WHERE id IN (1, 3, 5);
```

NULL warning: `NOT IN` can produce surprising results if the list or subquery contains `NULL`. We'll revisit this when studying subqueries.

### BETWEEN — Filter Ranges

`BETWEEN` checks whether a value is within an inclusive range.

```sql
SELECT id, name
FROM customers
WHERE id BETWEEN 2 AND 4;
```

Equivalent to:

```sql
WHERE id >= 2 AND id <= 4
```

Result: Bob, Charlie, Diana.

It also works with dates stored in ISO 8601 text format in SQLite.

```sql
SELECT *
FROM orders
WHERE order_date BETWEEN '2026-11-01' 
                     AND '2026-11-30';
```

This works for dates stored as `YYYY-MM-DD`. For timestamps containing hours and minutes, prefer a half-open interval:

```sql
WHERE created_at >= '2026-11-01'
  AND created_at < '2026-12-01'
```

This avoids accidentally excluding orders placed later on November 30.

## 3. LIKE — Pattern Matching

`LIKE` searches for text matching a pattern.

Two special wildcard characters are available:

|Wildcard|Meaning|
|---|---|
|`%`|Zero or more characters|
|`_`|Exactly one character|

Examples:

```sql
-- Names beginning with A
SELECT * FROM customers
WHERE name LIKE 'A%';

-- Names ending with e
SELECT * FROM customers
WHERE name LIKE '%e';

-- Names containing li
SELECT * FROM customers
WHERE name LIKE '%li%';

-- Four-character names
SELECT * FROM customers
WHERE name LIKE '____';
```


## 4. NULL — Missing or Unknown Values

`NULL` represents a missing or unknown value.

It is not equivalent to `0`, `FALSE`, or an empty string.

Suppose we insert another customer:

```sql
INSERT INTO customers (name, email, city)
VALUES ('Frank', 'frank@example.com', NULL);
```

The following query is incorrect:

```sql
SELECT *
FROM customers
WHERE city = NULL;
```

Instead, use:

```sql
SELECT *
FROM customers
WHERE city IS NULL;
```

And to retrieve customers with known cities:

```sql
SELECT *
FROM customers
WHERE city IS NOT NULL;
```

### SQL three-valued logic

SQL conditions can evaluate to `TRUE`, `FALSE`, or `UNKNOWN` (`NULL`).

|Expression|Result|
|---|---|
|`1 = 1`|TRUE|
|`1 = 2`|FALSE|
|`NULL = 1`|UNKNOWN|
|`NULL = NULL`|UNKNOWN|
|`NULL IS NULL`|TRUE|

A `WHERE` clause only keeps rows where the condition evaluates to TRUE.

For example:

```sql
SELECT *
FROM customers
WHERE city <> 'Paris';
```

Frank is not returned, because `NULL <> 'Paris'` evaluates to UNKNOWN.

To include customers with missing cities:

```sql
SELECT *
FROM customers
WHERE city <> 'Paris'
   OR city IS NULL;
```

## 5. COALESCE — Handle NULL Values

`COALESCE` returns the first non-NULL expression.

```sql
SELECT
    name,
    COALESCE(city, 'Unknown') AS city
FROM customers;
```

For Frank, the returned city is `Unknown`.

This is useful when generating API responses or reports.

```sql
SELECT
    id,
    name,
    COALESCE(city, 'Not provided') AS display_city
FROM customers;
```

Note that `COALESCE` changes the query result, not the underlying stored data.

### Practical Backend Example — Dynamic Search

Imagine a REST endpoint:

```
GET /api/customers?city=Paris&active=true&name=bo
```

A corresponding SQLite query could be:

```sql
SELECT id, name, email, city
FROM customers
WHERE city = :city
  AND active = :active
  AND name LIKE '%' || :name || '%'
ORDER BY name;
```

With parameters:

```sql
city   = Paris
active = 1
name   = bo
```

Result: Bob.

Here, `:city`, `:active`, and `:name` are named SQL parameters supported by SQLite APIs and many database libraries.

Security: Use parameter binding instead of concatenating user input into SQL strings. This prevents SQL injection and handles quoting safely.

### Optional filters

Suppose the API allows users to omit some filters:

```sql
SELECT id, name, email, city
FROM customers
WHERE (:city IS NULL OR city = :city)
  AND (:active IS NULL OR active = :active)
ORDER BY name;
```

If `:city` is `NULL`, that filter is disabled.

This approach is convenient for simple queries, although dynamically constructing the required predicates can produce better index usage for larger datasets.

## 6. Key Takeaways

- Comparison operators filter by value.
- `AND`, `OR`, and `NOT` combine conditions; parentheses control precedence.
- `IN` matches multiple values.
- `BETWEEN` includes both boundaries.
- `LIKE` supports text pattern matching with `%` and `_`.
- `NULL` must be checked with `IS NULL` or `IS NOT NULL`.
- `COALESCE` provides fallback values for NULL expressions.
- Prepared statements protect backend applications from SQL injection.