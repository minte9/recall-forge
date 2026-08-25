# PVM / FastAPI / Async & Performance / ASGI & Uvicorn

## 1. ASGI vs WSGI

WSGI (used by Flask, Django's classic sync views) handles one request
per worker thread at a time and has no native concept of `async`/`await`.
ASGI is the async-native successor -- FastAPI is built on it from the
ground up, which is what makes `async def` routes and WebSocket support
possible without extra add-ons.

```py
# Flask/WSGI: one blocking request per thread
# FastAPI/ASGI: many concurrent requests per event loop
```

## 2. Uvicorn & Workers

Uvicorn is the ASGI server that actually runs a FastAPI app -- `async
def` gives concurrency for I/O-bound work on a single process, but
CPU-bound work still needs multiple worker processes (`--workers N`) to
use more than one CPU core, since Python's GIL limits one process to one
core for CPU-bound code regardless of async.

```
uvicorn main:app --workers 4
```
