## JS / Node / File System / fs.promises & Directories

### 1. fs.promises API

`fs.promises` (or `require("fs/promises")`) mirrors the callback API but
returns Promises, letting file operations be written with `async`/
`await` instead of nested callbacks.

~~~javascript
const fs = require("fs/promises");
const data = await fs.readFile("data.txt", "utf8");
~~~


### 2. Working with Directories

`fs.readdir` lists directory contents; `fs.mkdir` with
`{ recursive: true }` creates nested directories in one call instead of
creating each level manually.

~~~javascript
await fs.mkdir("logs/2024", { recursive: true });
const files = await fs.readdir("logs");
~~~


### 3. Watching Files

`fs.watch` notifies a callback when a file or directory changes,
powering tools like dev-server auto-reload — but events can fire more
than once per change depending on the OS.

~~~javascript
fs.watch("config.json", (eventType) => {
  console.log(`config.json ${eventType}`);
});
~~~
