# PVM / FastAPI / Routing & Request Handling / Response Models & Status Codes

## 1. response_model

Declaring `response_model` filters the returned object down to exactly
the declared fields -- if the handler returns extra attributes (e.g. a
password hash on a user object), they're silently dropped from the
response instead of leaking to the client.

```py
class UserOut(BaseModel):
    id: int
    email: str

@app.get("/users/{id}", response_model=UserOut)
def get_user(id: int):
    return db_user  # extra fields on db_user are stripped automatically
```

## 2. Status Codes & HTTPException

`status_code` on the decorator sets the success response's status;
raising `HTTPException(status_code, detail)` anywhere in the handler
short-circuits with that status and a JSON error body -- the standard way
to signal a 404/400/403 without an if/else return-value convention.

```py
@app.get("/items/{id}", status_code=200)
def get_item(id: int):
    if id not in db:
        raise HTTPException(status_code=404, detail="Item not found")
    return db[id]
```
