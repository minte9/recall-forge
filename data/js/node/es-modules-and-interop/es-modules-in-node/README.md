## JS / Node.js / ES Modules & Interop / ES Modules in Node

### 1. import & export in Node

Node supports native ES modules using standard `import`/`export`
syntax, resolved asynchronously and statically analyzed — a different
loading model than CommonJS's synchronous `require`.

~~~javascript
// math.mjs
export function add(a, b) { return a + b; }
// app.mjs
import { add } from "./math.mjs";
~~~


### 2. CommonJS vs ESM

CommonJS modules use `require`/`module.exports` and resolve
synchronously; ESM uses `import`/`export` and resolves asynchronously.
Importing CJS from ESM works directly, but the reverse needs a dynamic
`import()`.

~~~javascript
// ESM importing CJS: works directly
import pkg from "commonjs-package";
// CJS importing ESM: needs dynamic import
const mod = await import("./esm-module.mjs");
~~~


### 3. package.json "type" Field

`"type": "module"` in `package.json` makes `.js` files parse as ESM by
default; `.cjs`/`.mjs` extensions override the default explicitly
regardless of the `type` field.

~~~javascript
// package.json
{ "type": "module" }
// now app.js is ESM; use app.cjs to force CommonJS
~~~
