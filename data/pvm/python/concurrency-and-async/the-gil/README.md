# Python / Python / Concurrency & Async / The GIL

## 1. What the GIL Is

CPython's Global Interpreter Lock allows only one thread to execute Python bytecode at a time, even on a multi-core machine. It exists to keep memory management (reference counting) thread-safe without fine-grained locking everywhere, at the cost of true parallel execution within one process.

```python
# Two threads doing pure Python work don't run truly in
# parallel -- the GIL serializes bytecode execution between them,
# even though both are "running" concurrently.
```


## 2. CPU-Bound vs IO-Bound Implications

The GIL releases automatically during blocking I/O (network, disk, `time.sleep`), so threads still help for IO-bound work. For CPU-bound work (heavy computation), threads don't speed anything up -- `multiprocessing` (separate processes, separate GILs) is needed for real parallelism.

```python
# IO-bound: threading helps (GIL released during I/O wait)
# CPU-bound: threading doesn't help (GIL blocks parallel compute)
# CPU-bound fix: use multiprocessing instead
```
