## DB / MongoDB / mongosh CLI / Shell Basics

### 1. show dbs & show collections

`show dbs` lists every database on the server; `show collections` lists
collections in whichever database is currently selected -- the mongosh
equivalents of `SHOW DATABASES`/`\l` and `SHOW TABLES`/`\dt` in SQL
clients.

~~~javascript
show dbs
use app
show collections
~~~

### 2. use & Switching Databases

`use dbname` switches the shell's current database context -- and
unlike SQL's `CREATE DATABASE`, a MongoDB database (and a collection
within it) is created implicitly the first time something is actually
written to it. `use` alone, with no write following, leaves nothing
behind on disk.

~~~javascript
use reporting          // no database created yet
db.events.insertOne({}) // "reporting" now exists
~~~
