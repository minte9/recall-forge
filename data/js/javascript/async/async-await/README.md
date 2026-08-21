## JS / Javascript / Async / Async & Await

### 1. async/await Syntax

`async` functions always return a Promise; `await` pauses execution until
that Promise settles, letting async code read like synchronous code.

~~~javascript
async function loadUser(id) {
  const res = await fetch(`/api/user/${id}`);
  return res.json();
}
~~~


### 2. Handling Rejections

A rejected `await`ed Promise throws, so wrap it in `try/catch` just like
synchronous errors — an unhandled rejection otherwise fails silently or
crashes Node.

~~~javascript
async function loadUser(id) {
  try {
    const res = await fetch(`/api/user/${id}`);
    return await res.json();
  } catch (err) {
    return null;
  }
}
~~~


### 3. Promise.all & Promise.race

`Promise.all` waits for every promise to fulfill (rejecting fast if any
does); `Promise.race` settles as soon as the first promise settles,
useful for timeouts.

~~~javascript
const [user, orders] = await Promise.all([
  fetchUser(id), fetchOrders(id),
]);
~~~
