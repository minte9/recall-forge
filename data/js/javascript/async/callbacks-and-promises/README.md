## JS / Javascript / Async / Callbacks & Promises

### 1. Callback Pattern

Async operations historically took a callback function to run once work
completed; nesting many of these leads to "callback hell," which Promises
were designed to fix.

~~~javascript
fetchUser(id, (err, user) => {
  if (err) return handleError(err);
  fetchOrders(user.id, (err, orders) => { /* nested... */ });
});
~~~


### 2. Promise Basics

A `Promise` represents a value that will exist later, in one of three
states: pending, fulfilled, or rejected. `.then`/`.catch` react to the
eventual outcome.

~~~javascript
fetch("/api/user")
  .then(res => res.json())
  .catch(err => console.error(err));
~~~


### 3. Chaining Promises

Returning a value (or another promise) from `.then` passes it to the next
`.then`, flattening nested async steps into a linear chain instead of
pyramided callbacks.

~~~javascript
fetchUser(id)
  .then(user => fetchOrders(user.id))  // chained, not nested
  .then(orders => console.log(orders));
~~~
