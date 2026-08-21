## JS / Javascript / Basics / Functions Basics

### 1. Function Declarations

Declared with `function name() {}` and hoisted entirely, including the
body, so they can be called before their definition appears in the file.

~~~javascript
greet();               // works: declaration is hoisted
function greet() {
  console.log("hi");
}
~~~


### 2. Function Expressions

Assigned to a variable; only the variable binding is hoisted (as
`undefined` for `var`), not the function body, so calling before
assignment throws.

~~~javascript
// sayHi();  // TypeError: sayHi is not a function
const sayHi = function () {
  console.log("hi");
};
~~~


### 3. Arrow Functions

Shorter syntax with implicit return for single expressions. Unlike regular
functions, arrows don't have their own `this`, `arguments`, or
`prototype` — they inherit `this` from the enclosing scope.

~~~javascript
const double = x => x * 2;               // implicit return
const add = (a, b) => { return a + b; };
~~~
