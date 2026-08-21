## JS / Javascript / Classes & Prototypes / Class Basics

### 1. Class Syntax

`class` is syntactic sugar over JS's existing prototype system — it
doesn't introduce a new inheritance model, just a cleaner way to write
one.

~~~javascript
class User {
  constructor(name) { this.name = name; }
  greet() { return `Hi, ${this.name}`; }
}
new User("Ana").greet();  // "Hi, Ana"
~~~


### 2. Constructors & Methods

`constructor` runs once on `new`; regular methods are defined on the
class's prototype, so all instances share one copy instead of each
getting its own.

~~~javascript
const a = new User("Ana");
const b = new User("Bob");
a.greet === b.greet;   // true -- same method, shared via prototype
~~~


### 3. Static Members

`static` members belong to the class itself, not to instances — used for
factory methods or utilities related to the class as a whole.

~~~javascript
class User {
  static fromJSON(json) { return new User(JSON.parse(json).name); }
}
User.fromJSON('{"name":"Ana"}');
~~~
