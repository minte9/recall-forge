## DB / MySQL / MySQL CLI / Schema & Session Navigation

### 1. USE & Switching Databases

`USE dbname` sets the current database for the session -- every
unqualified table reference after that resolves against it, the MySQL
equivalent of `psql`'s `\c`. Unlike some clients, `USE` doesn't open a new
connection, it just changes which database the existing connection
targets.

~~~sql
USE app;
SELECT * FROM orders;  -- resolves to app.orders
~~~

### 2. information_schema & SHOW GRANTS

`information_schema` is a set of standard read-only metadata views
(`TABLES`, `COLUMNS`, `STATISTICS`) queryable with plain `SELECT`, useful
when a `SHOW` command's output isn't filterable or joinable enough.
`SHOW GRANTS FOR user` lists exactly which privileges a user has, the
first thing to check when a query fails with an access-denied error.

~~~sql
SELECT table_name, column_name, data_type
FROM information_schema.columns WHERE table_schema = 'app';
SHOW GRANTS FOR 'app_user'@'%';
~~~
