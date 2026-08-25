# Architecture / Architecture / Distributed Systems / Service Discovery & Load Balancing

## 1. Client-side vs Server-side Discovery

In client-side discovery, the caller queries a registry (Consul, etcd) directly and picks an instance itself -- fast, but every client needs discovery logic. In server-side discovery, the caller always calls a fixed address, a load balancer, that looks up instances on its behalf -- simpler clients, at the cost of another hop.

```go
// Client-side: caller queries the registry itself
addr := registry.Lookup("inventory-service")
resp, _ := http.Get(addr + "/stock")

// Server-side: caller always hits a fixed load balancer address
resp, _ := http.Get("http://lb.internal/inventory/stock")
```

## 2. Load Balancing Algorithms

Round robin cycles through instances in order -- simple, but ignores that some requests are heavier than others. Least-connections routes to whichever instance currently has the fewest in-flight requests, adapting better to uneven request cost. Neither is "correct" universally; round robin is fine when requests are roughly uniform.

```go
func pickLeastConnections(instances []*Instance) *Instance {
    best := instances[0]
    for _, in := range instances[1:] {
        if in.ActiveConns < best.ActiveConns {
            best = in
        }
    }
    return best
}
```
