## JS / Javascript / Basics / Control Flow

### 1. Conditionals

`if/else` branches on truthiness; the ternary `? :` is an expression form
for simple two-way choices. Avoid nesting ternaries — they read poorly
compared to `if/else`.

~~~javascript
const status = age >= 18 ? "adult" : "minor";
if (status === "adult") {
  grantAccess();
} else {
  denyAccess();
}
~~~


### 2. Loops

`for` and `while` are classic counted/conditional loops; `for...of`
iterates values of an iterable, `for...in` iterates enumerable keys —
avoid it on arrays, since order and inherited keys aren't guaranteed.

~~~javascript
const items = ["a", "b", "c"];
for (const item of items) console.log(item);  // values
for (const i in items) console.log(i);           // keys: "0","1","2"
~~~


### 3. Switch

`switch` compares with `===` and falls through to the next case unless
`break` stops it — a common source of bugs when a `break` is forgotten.

~~~javascript
switch (role) {
  case "admin":
  case "owner":
    grantFullAccess();  // falls through: both cases match
    break;
  default:
    grantReadOnly();
}
~~~
