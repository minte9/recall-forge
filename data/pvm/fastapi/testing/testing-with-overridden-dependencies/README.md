## PVM / FastAPI / Testing / Testing with Overridden Dependencies

### 1. Overriding Dependencies in Tests

Setting `app.dependency_overrides` before creating the `TestClient` (and
clearing it after) lets a test swap out a real dependency -- a live
database, an external API client -- for a fake or in-memory version, so
tests exercise the route logic without depending on the real backend
being available.

~~~py
app.dependency_overrides[get_db] = lambda: fake_db
client = TestClient(app)
~~~

### 2. Fixtures for the Test Client

A pytest fixture is the standard way to set up and tear down overrides
per test -- yielding a configured `TestClient`, then clearing
`dependency_overrides` afterward so one test's overrides don't leak into
the next.

~~~py
@pytest.fixture
def client():
    app.dependency_overrides[get_db] = lambda: fake_db
    yield TestClient(app)
    app.dependency_overrides.clear()
~~~
