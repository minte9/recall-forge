## Python / Python / Recursion / Memoization & Head-Tail Decomposition

### 1. Memoization

Naive recursive Fibonacci recomputes the same subproblems exponentially many times; memoization caches each result the first time it's computed so later calls with the same argument return instantly. `functools.lru_cache` gives this for free without hand-rolling a cache dict.

~~~python
from functools import lru_cache

@lru_cache()
def fib(n):
    if n <= 2:
        return 1
    return fib(n - 1) + fib(n - 2)  # cached calls short-circuit here
~~~


### 2. Head-Tail Decomposition

A common recursive pattern splits a sequence into its head (first element) and tail (the rest), recurses on the tail, then combines the result with the head. It turns "process the whole sequence" into "process one element, then let recursion handle a slightly smaller sequence."

~~~python
def reverse(s):
    if len(s) <= 1:
        return s
    head, tail = s[0], s[1:]
    return reverse(tail) + head  # recurse on the smaller tail first
~~~
