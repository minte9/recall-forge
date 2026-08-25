# JS / Node / Process & Child Processes / The process Object

## 1. process Lifecycle Events

`process.on("exit", ...)` runs synchronous cleanup right before the
process exits; `"SIGINT"`/`"SIGTERM"` let a process handle Ctrl+C or a
shutdown signal gracefully (e.g. closing DB connections).

```javascript
process.on("SIGTERM", () => {
  server.close(() => process.exit(0));  // graceful shutdown
});
```


## 2. Exit Codes

`process.exit(code)` ends the process immediately; `0` means success,
any nonzero code signals failure to whatever launched the process (shell
scripts, CI, process managers).

```javascript
if (!config.isValid()) {
  console.error("Bad config");
  process.exit(1);   // nonzero: failure
}
```


## 3. Environment Config Patterns

Reading all config through `process.env` at startup (rather than
scattered throughout the code) makes a Node app's external configuration
explicit and easy to audit in one place.

```javascript
const config = {
  port: process.env.PORT || 3000,
  dbUrl: process.env.DATABASE_URL,
};
```
