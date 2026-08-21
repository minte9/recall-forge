## JS / Node.js / File System / Working with Files

### 1. Sync vs Async fs

Every `fs` method has an async, callback-based version and a `*Sync`
blocking version; sync methods block the whole event loop, so prefer
async (or `fs.promises`) in servers.

~~~javascript
const fs = require("fs");
fs.readFile("data.txt", "utf8", (err, data) => { /* async */ });
const data = fs.readFileSync("data.txt", "utf8");  // blocks
~~~


### 2. Reading & Writing Files

`fs.readFile`/`fs.writeFile` handle whole files in memory at once;
passing an encoding like `"utf8"` returns a string instead of a raw
`Buffer`.

~~~javascript
fs.writeFile("out.txt", "hello", (err) => {
  if (err) throw err;
});
~~~


### 3. Working with Paths

The `path` module builds file paths portably across operating systems —
`path.join` normalizes separators, avoiding fragile manual string
concatenation.

~~~javascript
const path = require("path");
path.join("data", "users", "list.json");
// POSIX: "data/users/list.json"
// Windows: "data\users\list.json"
~~~
