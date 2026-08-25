# PVM / FastAPI / Dependency Injection / Overriding Dependencies for Testing

## 1. dependency_overrides

`app.dependency_overrides[real_dependency] = fake_dependency` swaps a
dependency for the whole app without touching any route code -- routes
still just declare `Depends(get_db)`; the override dict decides what
`get_db` actually resolves to at request time.

```py
def override_get_db():
    yield TestSessionLocal()

app.dependency_overrides[get_db] = override_get_db
```

## 2. Common Use: Test Databases

The most common override swaps a production database dependency for one
pointing at a throwaway test database (often SQLite in-memory, or a
dedicated test Postgres schema) -- so integration tests exercise real
query code without touching production data.

```py
def test_create_item(client):
    response = client.post("/items/", json={"name": "widget"})
    assert response.status_code == 200
```
