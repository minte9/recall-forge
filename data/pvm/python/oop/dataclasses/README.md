# Python / Python / OOP / Dataclasses

## 1. `@dataclass` Basics

`@dataclass` auto-generates `__init__`, `__repr__`, and `__eq__` from type-annotated class attributes, removing the boilerplate of hand-writing them for simple data-holding classes. Field order in the class body becomes the constructor's parameter order.

```python
from dataclasses import dataclass

@dataclass
class Point:
    x: int
    y: int

Point(1, 2)   # Point(x=1, y=2) -- __repr__ for free
Point(1, 2) == Point(1, 2)   # True -- __eq__ for free
```


## 2. `frozen` & Default Factories

`frozen=True` makes instances immutable after creation (any attribute assignment raises), which also makes them hashable. Mutable defaults like `[]` can't be used directly as a field default -- `field(default_factory=list)` builds a fresh instance per object instead of sharing one.

```python
from dataclasses import dataclass, field

@dataclass(frozen=True)
class Config:
    tags: list = field(default_factory=list)   # new list each time
    debug: bool = False

c = Config()
c.debug = True   # FrozenInstanceError
```
