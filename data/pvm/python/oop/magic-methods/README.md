# Python / Python / OOP / Magic Methods

## 1. `__repr__` vs `__str__`

`__repr__` returns an unambiguous, developer-facing representation (ideally valid Python to recreate the object); `__str__` returns a readable, user-facing string. `print()` and `str()` prefer `__str__`, falling back to `__repr__` if it's missing -- never the reverse.

```python
class Point:
    def __init__(self, x, y):
        self.x, self.y = x, y

    def __repr__(self):
        return f"Point({self.x}, {self.y})"   # debug-friendly

    def __str__(self):
        return f"({self.x}, {self.y})"        # display-friendly
```


## 2. `__eq__` & `__hash__`

Without `__eq__`, `==` falls back to identity comparison (`is`). Defining `__eq__` to compare values disables the default `__hash__` -- Python assumes value-based equality isn't safe to hash -- so it must be redefined too if instances go in a set or dict key.

```python
class Point:
    def __init__(self, x, y):
        self.x, self.y = x, y

    def __eq__(self, other):
        return (self.x, self.y) == (other.x, other.y)

    def __hash__(self):
        return hash((self.x, self.y))   # required for set/dict use
```


## 3. Operator Overloading

Dunder methods like `__add__`, `__lt__`, and `__len__` let custom objects respond to `+`, `<`, and `len()` the same way built-ins do. Python calls `a + b` as `a.__add__(b)`, falling back to `b.__radd__(a)` if the left operand doesn't support it.

```python
class Vector:
    def __init__(self, x, y):
        self.x, self.y = x, y

    def __add__(self, other):
        return Vector(self.x + other.x, self.y + other.y)

v = Vector(1, 2) + Vector(3, 4)   # calls __add__
```
