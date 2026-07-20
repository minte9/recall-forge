## PVM / Python / Recursion / Fundamentals

### 1. Call Stack and Base Case

Each recursive call pushes a new frame onto the call stack; when a call returns, its frame pops and execution resumes at the caller. Every recursive function needs a base case that stops new calls -- without one, Python raises `RecursionError` once it hits the default ~1000-frame limit.

~~~python
def countdown(n):
    if n == 0:          # base case -- stops the recursion
        print("done")
        return
    print(n)
    countdown(n - 1)    # smaller subproblem, one new frame per call
~~~


### 2. Recursion vs Iteration

A recursive solution can mirror a mathematical definition closely, but each call costs a stack frame -- deep recursion (e.g. factorial of a few thousand) hits the recursion limit where an equivalent loop would not. Loops also tend to be faster in Python since they avoid function-call overhead entirely.

~~~python
def pow_recursive(x, n):
    if n == 0:
        return 1
    return pow_recursive(x, n - 1) * x  # n stack frames

def pow_iterative(x, n):
    res = x
    for _ in range(1, n):
        res *= x                        # one frame, no depth limit
    return res
~~~


### 3. Memoization

Naive recursive Fibonacci recomputes the same subproblems exponentially many times; memoization caches each result the first time it's computed so later calls with the same argument return instantly. `functools.lru_cache` gives this for free without hand-rolling a cache dict.

~~~python
from functools import lru_cache

@lru_cache()
def fib(n):
    if n <= 2:
        return 1
    return fib(n - 1) + fib(n - 2)  # cached calls short-circuit here
~~~


### 4. Head-Tail Decomposition

A common recursive pattern splits a sequence into its head (first element) and tail (the rest), recurses on the tail, then combines the result with the head. It turns "process the whole sequence" into "process one element, then let recursion handle a slightly smaller sequence."

~~~python
def reverse(s):
    if len(s) <= 1:
        return s
    head, tail = s[0], s[1:]
    return reverse(tail) + head  # recurse on the smaller tail first
~~~


### 5. Mental Checklist for Writing Recursive Functions

Before coding, answer: what's the smallest input (base case)? What does the function return there? How does each call shrink the problem toward that base case? What return type must every branch produce? Tracing one small example by hand (e.g. `sum_to(3)`) catches missing base cases before they become infinite recursion.

~~~python
def sum_to(n):
    if n == 1:              # smallest input
        return 1             # base case return value
    return n + sum_to(n - 1) # shrinks toward the base case, same return type
~~~
