# DB / Oracle / Architecture & Storage / Instance vs Database

## 1. SGA & PGA

An Oracle *instance* is memory + background processes; the *database* is
the physical files on disk -- one instance normally serves one database,
but the distinction matters for clustering (RAC). The SGA (System Global
Area) is shared memory (buffer cache, shared pool, redo log buffer); the
PGA (Program Global Area) is per-session private memory (sort space,
cursor state).

```sql
SELECT name, value FROM v$sga;
```

## 2. Background Processes (PMON, SMON, DBWn, LGWR)

PMON cleans up after failed sessions (releasing locks, rolling back).
SMON performs instance recovery on startup and coalesces free space.
DBWn writes dirty buffers from the SGA to datafiles. LGWR flushes the
redo log buffer to disk on commit -- the process that actually makes
`COMMIT` durable.

```sql
SELECT name, description FROM v$bgprocess WHERE paddr != '00';
```
