## PVM / FastAPI / Testing / Async Test Clients

### 1. AsyncClient for Async Tests

`httpx.AsyncClient` is the async counterpart to `TestClient`, needed when
a test itself must run inside an async function -- for example, testing
code that awaits another async dependency directly rather than only going
through the HTTP layer.

~~~py
async def test_read_root():
    async with AsyncClient(app=app, base_url="http://test") as ac:
        response = await ac.get("/")
    assert response.status_code == 200
~~~

### 2. pytest-asyncio Basics

An `async def` test function needs `@pytest.mark.asyncio` (or
`asyncio_mode = "auto"` in pytest config) to actually run under an event
loop -- without it, pytest either skips the test or fails with a warning
that a coroutine was never awaited.

~~~py
@pytest.mark.asyncio
async def test_something():
    result = await some_async_function()
    assert result == expected
~~~
