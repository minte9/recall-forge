## JS / Node / Basics / Node Runtime

### 1. What Node.js Is

Node.js is a JavaScript runtime built on Chrome's V8 engine that executes
JS outside the browser, adding APIs for file systems, networking, and
processes that browsers don't expose (and omitting DOM/browser APIs Node
doesn't need).

~~~javascript
// browser: has `window`, `document`
// node: has `process`, `require`, `__dirname`
console.log(typeof window);   // undefined in Node
~~~


### 2. The V8 Engine

V8 compiles JavaScript to native machine code (JIT) rather than purely
interpreting it, which is why Node performs comparably to compiled
languages for CPU-bound JS work despite being "just JavaScript."

~~~javascript
process.version;      // Node version
process.versions.v8;  // e.g. "11.3.244.8" -- bundled V8 version
~~~


### 3. Running a Script

`node file.js` executes a file top to bottom as a CommonJS module by
default; the process exits automatically once the event loop has no more
pending work, without an explicit `exit()` call.

~~~javascript
// app.js
console.log("start");
setTimeout(() => console.log("done"), 100);
// $ node app.js  -- process stays alive until the timeout fires
~~~
