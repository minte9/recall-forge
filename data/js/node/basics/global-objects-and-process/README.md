## JS / Node / Basics / Global Objects & process

### 1. global

`global` is Node's equivalent of the browser's `window` — properties
attached to it are accessible everywhere, though relying on it is
discouraged in favor of explicit imports/exports.

~~~javascript
global.appName = "MyApp";
// any other file in the process can read global.appName
~~~


### 2. process.argv

`process.argv` is an array of command-line arguments; the first two
entries are always the Node binary path and script path, so user
arguments start at index 2.

~~~javascript
// $ node app.js --port 3000
process.argv[0];  // path to node
process.argv[1];  // path to app.js
process.argv[2];  // "--port"
~~~


### 3. process.env

`process.env` exposes environment variables as strings (never numbers or
booleans), commonly used for config like `NODE_ENV` or secrets that
shouldn't be hardcoded.

~~~javascript
const port = process.env.PORT || 3000;
process.env.NODE_ENV === "production";
~~~
