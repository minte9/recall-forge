# JS / Node / Event Loop & Async / EventEmitter

## 1. EventEmitter Basics

`EventEmitter` is the pattern behind most of Node's async APIs (streams,
servers): `.on()` registers a listener, `.emit()` fires an event
synchronously to every registered listener.

```javascript
const { EventEmitter } = require("events");
const bus = new EventEmitter();
bus.on("greet", name => console.log(`Hi ${name}`));
bus.emit("greet", "Ana");
```


## 2. once vs on

`.on()` keeps a listener attached for every future emission; `.once()`
automatically removes the listener after it fires the first time —
useful for one-time setup or handshake events.

```javascript
bus.once("ready", () => console.log("ready fired once"));
bus.emit("ready");  // logs
bus.emit("ready");  // silent, listener already removed
```


## 3. Error Events

An `EventEmitter` with no listener for its `"error"` event throws and
crashes the process when one is emitted — always attach an `"error"`
handler on emitters that might fail.

```javascript
bus.on("error", (err) => console.error("handled:", err.message));
bus.emit("error", new Error("boom"));  // caught, no crash
```
