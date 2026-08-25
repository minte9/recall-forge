# JS / Node / Testing / Testing in Node

## 1. Node's Built-in Test Runner

Since Node 18+, `node:test` provides `test()`/`describe()` and `assert`
built in, needing no dependency for basic unit tests — run with
`node --test`.

```javascript
const { test } = require("node:test");
const assert = require("node:assert");

test("adds numbers", () => {
  assert.strictEqual(1 + 2, 3);
});
```


## 2. Jest for Node

Jest works for plain Node code too, not just frontend — it adds a
richer matcher API, built-in mocking, and coverage reporting beyond what
`node:test` provides out of the box.

```javascript
test("adds numbers", () => {
  expect(1 + 2).toBe(3);
});
```


## 3. Mocking Modules

`jest.mock()` replaces a module (like a database client or `fetch`)
with a fake implementation for the duration of a test, isolating the
code under test from real I/O.

```javascript
jest.mock("./db");
const db = require("./db");
db.getUser.mockResolvedValue({ id: 1, name: "Ana" });
```
