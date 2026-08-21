## JS / Javascript / Functions / Higher-Order Functions

### 1. Functions as Values

Functions are first-class: they can be assigned to variables, passed as
arguments, and returned from other functions. This enables patterns like
callbacks and function factories.

~~~javascript
function withLogging(fn) {
  return (...args) => {
    console.log("calling", fn.name);
    return fn(...args);
  };
}
const loggedAdd = withLogging((a, b) => a + b);
~~~


### 2. Rest & Spread Parameters

Rest (`...args`) gathers extra arguments into an array inside a function
signature; spread (`...arr`) expands an array into individual arguments at
a call site. Same syntax, opposite direction.

~~~javascript
function sum(...nums) { return nums.reduce((a, b) => a + b, 0); }
const values = [1, 2, 3];
sum(...values);   // spread: 1, 2, 3 as separate args
~~~


### 3. Default Parameters

Parameters can declare a default used when the argument is `undefined`
(not just missing) — passing `undefined` explicitly still triggers it,
but `null` does not.

~~~javascript
function greet(name = "Guest") { return `Hi, ${name}`; }
greet();           // "Hi, Guest"
greet(undefined);  // "Hi, Guest" -- default still applies
greet(null);       // "Hi, null"  -- null is a real value
~~~
