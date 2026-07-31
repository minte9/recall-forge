## PVM / FastAPI / Routing & Request Handling / Path Operations & Parameters

### 1. Path Operations & Decorators

A route is declared with a decorator naming the HTTP method --
`@app.get`, `@app.post`, etc. -- and path parameters are typed directly
in the function signature (`item_id: int`); FastAPI validates and
converts them automatically, returning a 422 error if the value doesn't
match the type.

~~~py
@app.get("/items/{item_id}")
def read_item(item_id: int):
    return {"item_id": item_id}
~~~

### 2. Query Parameters & Request Body

A function parameter not present in the path URL is treated as a query
parameter by default; a parameter typed as a Pydantic `BaseModel` is
parsed from the JSON request body instead. FastAPI infers which is which
purely from the parameter's declared type and its presence in the path.

~~~py
class Item(BaseModel):
    name: str

@app.post("/items/")
def create_item(item: Item, notify: bool = False):  # notify is a query param
    return item
~~~
