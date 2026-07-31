## DB / Oracle / Concurrency & High Availability / Locking in Oracle

### 1. Row-Level Locking & SELECT FOR UPDATE

Oracle locks at the row level by default -- writers never block readers
(readers use undo-based consistent reads instead of waiting), and two
transactions only block each other if they try to modify the same row.
`SELECT ... FOR UPDATE` explicitly locks rows for a later update in the
same transaction, the same read-then-write pattern as other engines.

~~~sql
SELECT balance FROM accounts WHERE id = 1 FOR UPDATE;
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
~~~

### 2. The ITL (Interested Transaction List)

Each data block reserves a small number of slots (the ITL) tracking which
transactions currently hold locks on rows in that block. If more
concurrent transactions try to lock rows in the same block than there are
ITL slots, later ones wait even though they want different rows --
usually fixed by increasing `INITRANS` on frequently contended tables.

~~~sql
ALTER TABLE orders INITRANS 4;
~~~
