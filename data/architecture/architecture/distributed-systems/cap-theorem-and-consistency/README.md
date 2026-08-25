# Architecture / Architecture / Distributed Systems / CAP Theorem & Consistency

## 1. CAP Theorem

CAP says a distributed system can only guarantee two of three properties during a network partition: Consistency (every read gets the latest write), Availability (every request gets a response), and Partition tolerance (the system keeps working despite dropped messages). Since partitions are unavoidable in practice, the real choice is CP vs AP when one occurs.

```go
// CP: reject the read rather than risk stale data
if !quorumReached {
    return errors.New("unavailable: no quorum")
}

// AP: serve whatever the local replica has, even if stale
return localReplica.Read(key)
```

## 2. Strong vs Eventual Consistency

Strong consistency means every read reflects the latest write immediately, usually at the cost of blocking until replicas agree. Eventual consistency accepts a window where replicas disagree, in exchange for lower latency and higher availability. Choosing between them is a per-feature decision -- a bank balance wants strong consistency, a "like" count usually doesn't.

```go
// Eventual: read may return a stale replica, but never blocks
count := cache.Get("likes:post123") // could be a few seconds old
```
