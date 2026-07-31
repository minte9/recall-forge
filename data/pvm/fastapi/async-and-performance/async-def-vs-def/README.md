## PVM / FastAPI / Async & Performance / async def vs def

### 1. async def vs def Path Operations

FastAPI runs a plain `def` path operation in a separate threadpool
automatically, so a synchronous blocking call inside it doesn't stall
other requests. An `async def` path operation runs directly on the event
loop -- faster and lighter, but only safe if everything inside it is
genuinely non-blocking (`await`-based I/O).

~~~py
@app.get("/sync")
def sync_route():
    time.sleep(1)  # fine: FastAPI runs this in a threadpool

@app.get("/async")
async def async_route():
    await asyncio.sleep(1)  # fine: non-blocking
~~~

### 2. When Sync Blocks the Event Loop

Calling a blocking function (a synchronous DB driver, `time.sleep`)
directly inside an `async def` route blocks the entire event loop --
every other concurrent request on that worker stalls until it returns,
since nothing else can run on that loop meanwhile.

~~~py
@app.get("/broken")
async def broken_route():
    time.sleep(1)  # blocks the whole event loop, not just this request
~~~
