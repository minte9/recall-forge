## JS / Node.js / Error Handling / Error Handling in Node

### 1. Error-First Callbacks

Node's classic callback convention passes an error as the first
argument (`null` if none) — checking `if (err)` first is the idiom
nearly every core async API before Promises followed.

~~~javascript
fs.readFile("data.txt", (err, data) => {
  if (err) return console.error(err);
  console.log(data.toString());
});
~~~


### 2. uncaughtException

`process.on("uncaughtException")` catches errors that escape every
`try/catch`, but the process is left in an unknown state — the
recommended use is logging and exiting, not resuming normal operation.

~~~javascript
process.on("uncaughtException", (err) => {
  logger.fatal(err);
  process.exit(1);   // don't try to keep running after this
});
~~~


### 3. unhandledRejection

A rejected Promise with no `.catch()` fires `"unhandledRejection"` on
`process` — in modern Node this terminates the process by default,
unlike older versions that only warned.

~~~javascript
process.on("unhandledRejection", (reason) => {
  logger.error("Unhandled rejection:", reason);
});
~~~
