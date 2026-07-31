## PVM / FastAPI / Security & Auth / CORS & Middleware

### 1. CORSMiddleware

Browsers block a frontend on one origin from calling an API on another
unless the API explicitly allows it -- `CORSMiddleware` configures which
origins, methods, and headers are permitted. `allow_origins=["*"]` is
convenient in development but shouldn't ship to production alongside
credentialed requests.

~~~py
app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://app.example.com"],
    allow_methods=["*"],
    allow_credentials=True,
)
~~~

### 2. Custom Middleware

`@app.middleware("http")` wraps every request/response with custom
logic -- timing a request, adding a header, logging -- running code both
before the route handler executes and after it returns, similar in
spirit to a servlet filter.

~~~py
@app.middleware("http")
async def add_timing_header(request, call_next):
    start = time.time()
    response = await call_next(request)
    response.headers["X-Process-Time"] = str(time.time() - start)
    return response
~~~
