# API / REST / Advanced REST / Async Operations

## 1. Long-Running Operations

Work that takes longer than a few seconds (imports, report generation,
video processing) shouldn't hold an HTTP request open: proxies time out,
retries duplicate the work, and threads are wasted. Accept the request
and process it in the background.

```
POST /reports       <- would take 3 minutes
-> don't block; start a job and return immediately
```

## 2. 202 Accepted

202 means "request accepted, not finished yet". Return a status
resource URL in `Location` so the client can check progress. The job
itself becomes a REST resource with its own lifecycle.

```
POST /reports {"year":2026}
-> 202 Accepted
   Location: /reports/jobs/81
   {"jobId": 81, "status": "PENDING"}
```

## 3. Polling for Status

The client GETs the job URL until it reaches a terminal state, honoring
`Retry-After` to avoid hammering the server. When done, the job points to
the result; webhooks are the push alternative to polling.

```
GET /reports/jobs/81 -> 200 {"status":"RUNNING"}  Retry-After: 5
GET /reports/jobs/81 -> 200 {"status":"DONE",
                             "result":"/reports/2026"}
GET /reports/2026    -> 200 (the report)
```
