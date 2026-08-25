# Python / Python / Functions & Decorators / Decorators

## 1. Function Decorators (Basics)

A decorator is a function that wraps another function, adding behavior before/after the call without changing the original's code. `@decorator` above a function definition is shorthand for `func = decorator(func)`.

```python
def log_calls(func):
    def wrapper(*args, **kwargs):
        print(f"calling {func.__name__}")
        return func(*args, **kwargs)
    return wrapper

@log_calls
def add(a, b):
    return a + b
```


## 2. Decorators With Arguments

A decorator that itself takes arguments needs an extra layer of nesting -- the outer function accepts the decorator's arguments and returns the actual decorator, which then wraps the target function.

```python
def retry(times):
    def decorator(func):
        def wrapper(*args, **kwargs):
            for _ in range(times):
                try:
                    return func(*args, **kwargs)
                except Exception:
                    continue
        return wrapper
    return decorator

@retry(times=3)
def flaky(): ...
```


## 3. `functools.wraps`

Without `@wraps`, a wrapped function loses its original `__name__`, `__doc__`, and signature -- introspection tools and debuggers see `wrapper` instead of the real function. `functools.wraps(func)` copies that metadata onto the wrapper automatically.

```python
from functools import wraps

def log_calls(func):
    @wraps(func)          # preserves add.__name__ == "add"
    def wrapper(*args, **kwargs):
        return func(*args, **kwargs)
    return wrapper
```
