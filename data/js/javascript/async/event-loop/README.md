# JS / Javascript / Async / Event Loop

## 1. Call Stack & Event Loop

JS runs on a single thread with a call stack; async work is handed off to
the browser/Node APIs, and its callback only runs once the stack is
empty, via the event loop.

```javascript
console.log("1");
setTimeout(() => console.log("2"), 0);
console.log("3");
// logs: 1, 3, 2 -- setTimeout waits for the stack to clear
```


## 2. Microtasks vs Macrotasks

Promise callbacks are microtasks and run before the next macrotask (like
`setTimeout`), even a `setTimeout(fn, 0)` — microtasks always drain
first.

```javascript
setTimeout(() => console.log("macro"), 0);
Promise.resolve().then(() => console.log("micro"));
// logs: micro, macro
```


## 3. setTimeout vs Promises

`setTimeout(fn, 0)` doesn't run immediately — it queues `fn` as a
macrotask after the current script and all pending microtasks finish, so
timing is a floor, not a guarantee.

```javascript
Promise.resolve().then(() => console.log("a"));
setTimeout(() => console.log("b"), 0);
console.log("c");
// logs: c, a, b
```
