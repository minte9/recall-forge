## PVM / FastAPI / Testing / TestClient Basics

### 1. TestClient

`TestClient` (backed by `httpx`) sends requests directly into the app
in-process -- no running server or open port needed, and it's fast
enough to use in every unit test. It exposes the same request API as a
normal `httpx` client, just pointed at the app object instead of a URL.

~~~py
client = TestClient(app)

def test_read_root():
    response = client.get("/")
    assert response.status_code == 200
~~~

### 2. Testing Path Operations

A typical test asserts both the status code and the response JSON shape
-- `response.json()` returns the parsed body directly, so assertions read
like plain dict/list comparisons rather than parsing raw text.

~~~py
def test_create_item():
    response = client.post("/items/", json={"name": "widget", "price": 9.99})
    assert response.status_code == 200
    assert response.json()["name"] == "widget"
~~~
