# PVM / FastAPI / Pydantic & Validation / Nested Models & Serialization

## 1. Nested Models

A `BaseModel` field can be typed as another `BaseModel` (or a list of
them) -- Pydantic validates the nested structure recursively, so a
request body with a malformed nested object produces a validation error
pointing at the exact nested field, not just "invalid body".

```py
class Address(BaseModel):
    city: str

class Customer(BaseModel):
    name: str
    address: Address
```

## 2. model_dump & Serialization

`.model_dump()` converts a model instance back to a plain dict;
`exclude_unset=True` includes only the fields that were actually
provided in the input, not ones that fell back to a default -- the
standard way to implement a `PATCH` endpoint that updates only the
fields the client sent.

```py
def patch_item(item_id: int, patch: ItemUpdate):
    updates = patch.model_dump(exclude_unset=True)
    db[item_id].update(updates)
```
