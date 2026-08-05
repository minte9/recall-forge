## Python / Python / Concurrency & Async / Threading & Multiprocessing

### 1. `threading` Basics

`threading.Thread` runs a function concurrently within the same process, sharing memory with the main thread -- good for overlapping IO-bound waits. Shared mutable state between threads needs a `Lock` to avoid race conditions, since the GIL only protects individual bytecode operations, not multi-step logic.

~~~python
import threading

def download(url):
    ...  # network wait releases the GIL

threads = [threading.Thread(target=download, args=(u,))
           for u in urls]
for t in threads: t.start()
for t in threads: t.join()   # wait for all to finish
~~~


### 2. `multiprocessing` for CPU-Bound Work

`multiprocessing.Process` (or `Pool`) runs code in separate OS processes, each with its own interpreter and GIL, giving true parallelism for CPU-bound work. The cost is heavier startup and no shared memory by default -- data passed between processes is pickled.

~~~python
from multiprocessing import Pool

def square(n):
    return n * n

with Pool(4) as pool:
    results = pool.map(square, range(10))   # runs on 4 processes
~~~
