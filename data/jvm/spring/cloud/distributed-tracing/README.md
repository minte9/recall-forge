# JVM / Spring / Cloud / Distributed Tracing

## 1. Why Distributed Tracing

A single user request in a microservices architecture might touch five different services -- when something's slow or fails, log files scattered across five separate services make root-causing painful. Distributed tracing stitches those separate service calls back into one coherent timeline for a single originating request.

```java
// Without tracing: 5 separate, disconnected log entries across 5 services
// With tracing: one trace ID ties all 5 spans together into one timeline
```


## 2. Trace IDs and Span IDs

A trace ID identifies one end-to-end request across every service it touches; each individual service call within that request gets its own span ID, nested under the same trace. Propagating these IDs through HTTP headers on every downstream call is what lets a tracing tool reconstruct the full call graph.

```java
// Automatically propagated via HTTP headers on outgoing calls:
// traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
//                  ^ trace ID (shared)              ^ span ID (per call)
```


## 3. Micrometer Tracing

Micrometer Tracing (Spring Boot 3+'s replacement for Sleuth) automatically instruments outgoing HTTP calls, adds trace/span IDs to log output, and exports traces to a backend like Zipkin or Jaeger for visualization -- mostly configuration-only, requiring the tracing starter dependency plus an exporter.

```java
// build.gradle:
// implementation("io.micrometer:micrometer-tracing-bridge-brave")
// implementation("io.zipkin.reporter2:zipkin-reporter-brave")

// Logs automatically include [traceId, spanId] once configured -- no manual code needed
```
