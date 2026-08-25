# Architecture / Architecture / Distributed Systems / Resilience Patterns

## 1. Circuit Breaker

A circuit breaker tracks failures to a downstream service and, past a threshold, "opens" -- failing fast locally instead of waiting on a service that's already struggling. This protects the caller from cascading timeouts and gives the downstream service room to recover instead of being hit by retries from every caller at once.

```go
type Breaker struct {
    failures int
    open     bool
}

func (b *Breaker) Call(fn func() error) error {
    if b.open {
        return errors.New("circuit open")
    }
    if err := fn(); err != nil {
        b.failures++
        if b.failures > 5 {
            b.open = true
        }
        return err
    }
    b.failures = 0
    return nil
}
```

## 2. Retry with Backoff

Retrying a failed call immediately just hits the same overloaded service again. Exponential backoff spaces retries out (100ms, 200ms, 400ms...) so transient failures get a real chance to clear, and adding jitter -- a small random offset -- stops many clients from retrying in lockstep and re-creating the spike they were backing off from.

```go
for attempt := 0; attempt < 3; attempt++ {
    if err := call(); err == nil {
        return nil
    }
    wait := time.Duration(1<<attempt) * 100 * time.Millisecond
    time.Sleep(wait + jitter())
}
```

## 3. Timeout and Cancellation via context.Context

`context.Context` carries a deadline through a call chain so a slow downstream call doesn't hang the caller forever -- once the deadline passes, everything downstream that respects the context stops working and returns an error, freeing the goroutine instead of leaking it.

```go
ctx, cancel := context.WithTimeout(ctx, 2*time.Second)
defer cancel()
resp, err := client.Do(req.WithContext(ctx))
```
