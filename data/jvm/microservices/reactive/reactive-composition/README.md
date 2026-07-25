## JVM / Microservices / Reactive / Reactive Composition

### 1. Reactive Stack Shift

Going reactive swaps the whole stack, not just the return type: Tomcat becomes Netty, `MongoRepository` becomes `ReactiveMongoRepository`, and blocking calls that return `List`/`Object` become non-blocking calls returning `Flux`/`Mono`. Mixing one blocking piece back in defeats the point.

### 2. Mono vs Flux

`Mono<T>` represents 0-or-1 async result (a single product lookup); `Flux<T>` represents 0-to-N (a stream of reviews). These are Project Reactor's two core types, and picking the wrong one is the most common reactive-code review comment: a single-item endpoint should never return a `Flux`.

### 3. Why Reactive Matters for Composite Calls

A composite service that blocks on product, then reviews, then recommendations sequentially pays the sum of all three latencies. Reactive lets those three downstream calls be combined and awaited concurrently, so total latency approaches the slowest single call instead of the sum.

~~~java
Mono.zip(productMono, reviewsFlux.collectList(), recsFlux.collectList())
    .map(t -> new ProductAggregate(t.getT1(), t.getT2(), t.getT3()));
~~~
