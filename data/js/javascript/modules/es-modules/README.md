## JS / Javascript / Modules / ES Modules

### 1. import & export

ES modules export values with `export` and pull them into other files
with `import`; each module has its own scope, so nothing leaks globally
by default.

~~~javascript
// math.js
export function add(a, b) { return a + b; }
// app.js
import { add } from "./math.js";
~~~


### 2. Default vs Named Exports

A module can have one `default` export (imported without braces, any
local name) alongside any number of named exports (imported with braces,
matching names).

~~~javascript
// user.js
export default class User {}
export const ROLE_ADMIN = "admin";
// app.js
import User, { ROLE_ADMIN } from "./user.js";
~~~


### 3. CommonJS vs ES Modules

Node's original `require`/`module.exports` (CommonJS) loads synchronously
and resolves modules at runtime; ES modules (`import`/`export`) are
statically analyzed, enabling tree-shaking and top-level `await`.

~~~javascript
// CommonJS
const { add } = require("./math");
// ES Modules
import { add } from "./math.js";
~~~
