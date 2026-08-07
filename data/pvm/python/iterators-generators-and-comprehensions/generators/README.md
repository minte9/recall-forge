## Python / Python / Iterators, Generators & Comprehensions / Generators

### 1. `yield` & Generator Functions

A function containing `yield` becomes a generator -- calling it doesn't run the body, it returns a generator object that runs up to the next `yield` each time `next()` is called, pausing with its local state intact in between.

~~~python
def countdown(n):
    while n > 0:
        yield n     # pauses here, resumes on next call
        n -= 1

for value in countdown(3):
    print(value)   # 3, 2, 1
~~~


### 2. Generator Expressions

`(expr for x in iterable)` is a generator's equivalent of a list comprehension -- it produces values lazily, one at a time, instead of building the whole list in memory. Swap the brackets for parentheses and the rest of the syntax is identical.

~~~python
squares = (x * x for x in range(1_000_000))   # lazy, no list built
next(squares)   # 0 -- computed on demand
sum(x * x for x in range(10))   # parens optional as sole arg
~~~


### 3. `yield from`

`yield from subgenerator` delegates to another iterable, yielding each of its values in turn without an explicit loop -- useful for flattening nested generators or splitting a large generator into composable pieces.

~~~python
def inner():
    yield 1
    yield 2

def outer():
    yield from inner()   # equivalent to: for v in inner(): yield v
    yield 3

list(outer())   # [1, 2, 3]
~~~
