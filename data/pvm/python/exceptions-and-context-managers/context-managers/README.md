## Python / Python / Exceptions & Context Managers / Context Managers

### 1. `with` Statement Basics

The `with` statement guarantees a resource's cleanup code runs even if an exception occurs inside the block -- `open(path) as f` closes the file automatically on exit, replacing a manual try/finally around it.

~~~python
with open("data.txt") as f:
    contents = f.read()
# file is closed here, even if read() raised
~~~


### 2. Writing `__enter__`/`__exit__`

A class becomes usable with `with` by implementing `__enter__` (setup, returns the value bound by `as`) and `__exit__` (cleanup, receives exception info if one occurred). Returning `True` from `__exit__` suppresses the exception instead of letting it propagate.

~~~python
class Timer:
    def __enter__(self):
        self.start = time.time()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        print(f"elapsed: {time.time() - self.start:.2f}s")
        return False   # don't suppress exceptions
~~~


### 3. `contextlib.contextmanager`

`@contextmanager` turns a generator function into a context manager without writing a class -- code before `yield` runs as `__enter__`, code after runs as `__exit__`, and the yielded value is what `as` binds to.

~~~python
from contextlib import contextmanager

@contextmanager
def timer():
    start = time.time()
    yield                      # this is the __enter__/__exit__ split
    print(f"elapsed: {time.time() - start:.2f}s")

with timer():
    do_work()
~~~
