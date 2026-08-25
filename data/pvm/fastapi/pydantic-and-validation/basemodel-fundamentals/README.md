# PVM / FastAPI / Pydantic & Validation / BaseModel Fundamentals

## 1. BaseModel & Field Types

A `BaseModel` subclass declares its shape purely through type-annotated
attributes -- Pydantic parses and validates incoming data against those
annotations automatically, raising a validation error listing every
field that failed, rather than stopping at the first one.

```py
class Item(BaseModel):
    name: str
    price: float
    tags: list[str] = []
```

## 2. Field() & Validators

`Field(...)` attaches extra constraints beyond the plain type: `gt=0`,
`max_length=50`, a default value, or documentation text shown in the
OpenAPI schema. A `@field_validator` decorator runs custom logic a
type hint alone can't express, like cross-checking two fields.

```py
class Item(BaseModel):
    price: float = Field(gt=0)

    @field_validator("price")
    def price_is_reasonable(cls, v):
        if v > 1_000_000:
            raise ValueError("price too high")
        return v
```
