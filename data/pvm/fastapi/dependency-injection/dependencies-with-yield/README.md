# PVM / FastAPI / Dependency Injection / Dependencies with yield

## 1. Dependencies with yield

A dependency function using `yield` instead of `return` splits into a
setup phase (before the `yield`) and a teardown phase (after it, which
runs once the response has been sent) -- the standard pattern for a
per-request resource like a database session that must always be closed,
even if the handler raises.

```py
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
```

## 2. Class-Based Dependencies

A callable class (one implementing `__call__`) can be used as a
dependency, letting it be configured with constructor arguments --
useful for a dependency whose behavior needs to vary per route, like a
pagination helper with a different default page size.

```py
class PaginationParams:
    def __init__(self, default_limit: int = 20):
        self.default_limit = default_limit

    def __call__(self, limit: int = None):
        return limit or self.default_limit

paginate = PaginationParams(default_limit=50)
```
