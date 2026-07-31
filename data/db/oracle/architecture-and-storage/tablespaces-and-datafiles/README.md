## DB / Oracle / Architecture & Storage / Tablespaces & Datafiles

### 1. Logical vs Physical Storage

A tablespace is a logical storage unit that tables and indexes are
created within; each tablespace maps to one or more physical datafiles on
disk. This indirection lets you move, resize, or add storage to a
tablespace without the objects inside it (or the SQL that references
them) ever needing to change.

~~~sql
CREATE TABLESPACE app_data
  DATAFILE '/oradata/app_data01.dbf' SIZE 500M AUTOEXTEND ON;
~~~

### 2. Segments, Extents & Blocks

A table (or index) is a *segment*; a segment grows by allocating
*extents* (contiguous groups of blocks); a *block* is the smallest unit
Oracle reads/writes, typically 8KB. Understanding this hierarchy explains
why a table's actual disk usage is chunkier than its row count would
suggest -- space is reserved an extent at a time, not row by row.

~~~sql
SELECT segment_name, bytes FROM user_segments WHERE segment_name = 'ORDERS';
~~~
