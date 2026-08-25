# PVM / FastAPI / Routing & Request Handling / Routers & Path Prefixes

## 1. APIRouter

`APIRouter` lets routes be defined in a separate module and mounted into
the main app via `include_router` -- the FastAPI equivalent of splitting
a large route file into feature-based modules, without needing the app
instance itself inside each module.

```py
# routers/items.py
router = APIRouter()

@router.get("/items/")
def list_items():
    return items

# main.py
app.include_router(router)
```

## 2. Path Prefixes & Tags

`include_router(router, prefix="/v1", tags=["items"])` adds a URL prefix
to every route in that router and groups them under a named section in
the auto-generated OpenAPI docs -- useful for API versioning and for
keeping the interactive docs organized as the number of routes grows.

```py
app.include_router(items.router, prefix="/v1", tags=["items"])
```
