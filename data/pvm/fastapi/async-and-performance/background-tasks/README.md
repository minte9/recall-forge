# PVM / FastAPI / Async & Performance / Background Tasks

## 1. BackgroundTasks

Adding a `BackgroundTasks` parameter lets a route schedule work (like
sending a confirmation email) to run *after* the response has already
been sent to the client -- fire-and-forget, with no separate task queue
or broker needed for simple cases.

```py
def send_email(to: str):
    ...  # slow work, doesn't delay the response

@app.post("/signup")
def signup(email: str, tasks: BackgroundTasks):
    tasks.add_task(send_email, email)
    return {"status": "ok"}
```

## 2. When You Need a Real Task Queue

`BackgroundTasks` runs in the same process as the web server -- if that
process crashes or restarts, the task is simply lost, and there's no
retry, scheduling, or distributed worker support. Anything that needs
retries, persistence across restarts, or scaling workers independently
of the API needs Celery, arq, or a similar dedicated task queue instead.

```py
# BackgroundTasks: fine for "send one email"
# Celery/arq: needed for "retry 5 times, run on a separate worker fleet"
```
