# Python / Python / Iterators, Generators & Comprehensions / Comprehensions

## 1. List/Dict/Set Comprehensions

A comprehension builds a new collection from an iterable in one expression -- `[x for x in xs]` for lists, `{k: v for ...}` for dicts, `{x for ...}` for sets. An optional `if` clause filters which elements are included.

```python
squares = [x * x for x in range(5)]           # [0, 1, 4, 9, 16]
evens = {x for x in range(10) if x % 2 == 0}    # {0, 2, 4, 6, 8}
lookup = {x: x * x for x in range(5)}           # dict comprehension
```


## 2. Nested Comprehensions & Readability

Comprehensions can nest for multiple loops or flattening, but readability drops fast past one level -- a comprehension that needs a comment to explain its own order of evaluation is a sign to rewrite it as a plain loop instead.

```python
matrix = [[1, 2], [3, 4]]
flat = [x for row in matrix for x in row]   # [1, 2, 3, 4]

# harder to read -- prefer a loop past this point:
pairs = [(x, y) for x in range(3) for y in range(3) if x != y]
```
