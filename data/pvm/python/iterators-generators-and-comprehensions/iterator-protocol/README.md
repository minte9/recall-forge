# Python / Python / Iterators, Generators & Comprehensions / Iterator Protocol

## 1. Iterable vs Iterator

An iterable is anything `iter()` can be called on (lists, strings, dicts) -- it produces a fresh iterator each time. An iterator is a stateful object that remembers its position and produces values one at a time via `next()`, raising `StopIteration` when exhausted.

```python
nums = [1, 2, 3]      # iterable
it = iter(nums)         # iterator, fresh position each time
next(it)   # 1
next(it)   # 2
list(nums)  # [1, 2, 3] -- the list itself is untouched
```


## 2. `__iter__` & `__next__`

A custom class becomes iterable by implementing `__iter__` (returning an iterator, often `self`) and `__next__` (returning the next value or raising `StopIteration`). This is exactly the protocol a `for` loop relies on under the hood.

```python
class Countdown:
    def __init__(self, start):
        self.current = start

    def __iter__(self):
        return self

    def __next__(self):
        if self.current <= 0:
            raise StopIteration
        self.current -= 1
        return self.current + 1

list(Countdown(3))   # [3, 2, 1]
```
