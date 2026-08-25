# JS / Node / Streams & Buffers / Streams

## 1. Readable & Writable Streams

Streams process data in chunks instead of loading it all into memory at
once — a `Readable` emits `"data"` chunks, a `Writable` accepts them via
`.write()`, ideal for large files.

```javascript
const fs = require("fs");
const readStream = fs.createReadStream("big-file.txt");
readStream.on("data", chunk => console.log(chunk.length));
```


## 2. Piping Streams

`.pipe()` connects a Readable directly to a Writable, automatically
forwarding chunks and handling flow control — far simpler and safer than
wiring `"data"` listeners manually.

```javascript
fs.createReadStream("in.txt")
  .pipe(fs.createWriteStream("out.txt"));
```


## 3. Backpressure

When a Writable can't keep up with an incoming Readable, `.write()`
returns `false` to signal backpressure; `.pipe()` handles this
automatically by pausing the source until drained.

```javascript
const ok = writable.write(chunk);
if (!ok) readable.pause();   // pipe() does this internally for you
```
