# Python / Python / Concurrency & Async / Async Patterns

## 1. `asyncio.gather`

`asyncio.gather(*coros)` runs multiple coroutines concurrently and waits for all of them, returning their results in the same order they were passed -- the idiomatic way to fan out several independent awaits instead of awaiting them one at a time.

```python
results = await asyncio.gather(
    fetch("a"), fetch("b"), fetch("c")
)   # all three run concurrently, not one after another
```


## 2. Async Context Managers & Iterators

`async with` and `async for` are the async equivalents of `with`/`for`, used when setup, cleanup, or iteration itself needs to await (e.g. a database connection). A class supports them via `__aenter__`/`__aexit__` or `__aiter__`/`__anext__` instead of the sync dunders.

```python
async with connection_pool.acquire() as conn:   # async setup
    async for row in conn.query("SELECT * FROM t"):
        process(row)
```
