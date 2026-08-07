## Python / Python / Concurrency & Async / Asyncio Basics

### 1. `async`/`await` Syntax

`async def` defines a coroutine function -- calling it returns a coroutine object without running the body. `await` pauses the current coroutine at a suspension point (usually IO), letting the event loop run other work until that awaited operation completes.

~~~python
import asyncio

async def fetch(url):
    await asyncio.sleep(1)   # simulates a non-blocking IO wait
    return f"data from {url}"

asyncio.run(fetch("api.example.com"))
~~~


### 2. The Event Loop

The event loop is a single-threaded scheduler that runs one coroutine until it hits an `await`, then switches to another ready one -- this is cooperative concurrency, not parallelism, so it's a good fit for many simultaneous IO waits, not CPU-heavy work.

~~~python
async def main():
    # both run "concurrently" -- event loop interleaves them
    # at each await, not truly simultaneously
    await asyncio.gather(fetch("a"), fetch("b"))
~~~


### 3. Coroutines vs Tasks

A bare coroutine only runs when awaited directly and does nothing on its own. `asyncio.create_task(coro)` schedules it to start running immediately in the background, letting the caller continue and await its result later -- this enables real concurrency between multiple awaits.

~~~python
async def main():
    task = asyncio.create_task(fetch("api"))   # starts now
    do_other_work()                            # runs meanwhile
    result = await task                        # wait for it later
~~~
