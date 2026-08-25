# PVM / FastAPI / Dependency Injection / Depends Fundamentals

## 1. Depends() & Dependency Functions

A parameter typed with `Depends(some_function)` tells FastAPI to call
`some_function` before the route handler and pass its return value in --
a plain function-based DI system, no container or registration step
needed. The same dependency function can be reused across any number of
routes just by referencing it.

```py
def get_query_token(token: str):
    return token

@app.get("/items/")
def read_items(token: str = Depends(get_query_token)):
    return {"token": token}
```

## 2. Sub-Dependencies

A dependency function can itself declare `Depends()` parameters, forming
a chain -- FastAPI resolves the whole tree automatically. Within a single
request, a dependency used by multiple sub-dependencies is only actually
called once by default; its result is cached and reused for the rest of
that request.

```py
def get_db():
    return Database()

def get_current_user(db=Depends(get_db), token: str = Depends(get_query_token)):
    return db.find_user_by_token(token)
```
