## JS / Node.js / HTTP & Servers / Express Fundamentals

### 1. App & Routes

`express()` creates an app instance; `.get`/`.post`/etc. register route
handlers per method and path, replacing the manual `req.url` branching
of the raw `http` module.

~~~javascript
const app = express();
app.get("/users/:id", (req, res) => {
  res.json({ id: req.params.id });
});
app.listen(3000);
~~~


### 2. Middleware

Middleware functions run in order before the route handler, each
calling `next()` to continue the chain — used for logging, auth, parsing
JSON bodies, and more.

~~~javascript
app.use(express.json());          // parses JSON bodies
app.use((req, res, next) => {
  console.log(req.method, req.url);
  next();                            // must call next() to continue
});
~~~


### 3. Error-Handling Middleware

An Express middleware with four parameters `(err, req, res, next)` is
treated specially as an error handler, catching errors passed to
`next(err)` from anywhere upstream.

~~~javascript
app.use((err, req, res, next) => {
  res.status(500).json({ error: err.message });
});
~~~
