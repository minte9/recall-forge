# DB / PostgreSQL / psql CLI / Schema & Database Navigation

## 1. \l & \c: Listing and Switching Databases

`\l` lists every database on the server, the psql equivalent of MySQL's
`SHOW DATABASES`. `\c dbname` opens a new connection to a different
database -- unlike MySQL's `USE`, this is a genuinely new connection,
since Postgres databases (unlike schemas) can't be queried across without
one.

```
\l
\c app
```

## 2. \dn & \du: Schemas & Roles

`\dn` lists schemas within the current database -- Postgres's namespace
layer between database and table, with no direct MySQL equivalent (MySQL
conflates "database" and "schema"). `\du` lists roles (users and groups)
and their attributes -- superuser, can-login, and role memberships.

```
\dn
\du
```
