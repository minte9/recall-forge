## JS / Node.js / Basics / CommonJS Modules

### 1. require & module.exports

`require()` synchronously loads a module and returns whatever it
assigned to `module.exports`; anything not explicitly exported stays
private to that file.

~~~javascript
// math.js
module.exports = { add: (a, b) => a + b };
// app.js
const { add } = require("./math");
~~~


### 2. Module Caching

`require()` caches modules by resolved file path — requiring the same
file twice returns the same object instance, so mutating an exported
object affects every importer.

~~~javascript
const a = require("./config");
const b = require("./config");
a === b;   // true -- same cached instance
~~~


### 3. __dirname & __filename

`__dirname` and `__filename` give the absolute path of the current
module's directory/file — essential for building file paths that work
regardless of where the process was launched from.

~~~javascript
const path = require("path");
const configPath = path.join(__dirname, "config.json");
~~~
