# JS / Node / Event Loop & Async / The Event Loop

## 1. libuv & Event Loop Phases

Node delegates async I/O to libuv, which drives the event loop through
ordered phases (timers, pending callbacks, poll, check, close) —
callbacks scheduled in different phases don't necessarily run in
registration order.

```javascript
setTimeout(() => console.log("timer"), 0);
setImmediate(() => console.log("immediate"));
// order between these two isn't guaranteed at the top level
```


## 2. setImmediate vs process.nextTick

`process.nextTick` runs before the event loop continues to the next
phase, ahead of Promise microtasks; `setImmediate` runs in the loop's
check phase, after I/O callbacks.

```javascript
process.nextTick(() => console.log("nextTick"));
Promise.resolve().then(() => console.log("promise"));
setImmediate(() => console.log("immediate"));
// order: nextTick, promise, immediate
```


## 3. Blocking the Event Loop

Synchronous CPU-heavy work (a tight loop, `JSON.parse` on huge data,
sync `fs` calls) blocks the single thread entirely, freezing every other
request a Node server is handling.

```javascript
// blocks everything until done -- avoid in a request handler
for (let i = 0; i < 1e10; i++) {}
```
