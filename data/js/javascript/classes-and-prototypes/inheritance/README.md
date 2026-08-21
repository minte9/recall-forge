## JS / Javascript / Classes & Prototypes / Inheritance

### 1. extends & super

`extends` sets up inheritance between classes; `super()` must be called
in a subclass constructor before using `this`, and `super.method()` calls
the parent's version.

~~~javascript
class Admin extends User {
  constructor(name) {
    super(name);          // must run before using `this`
    this.role = "admin";
  }
}
~~~


### 2. Prototypal Inheritance

Every object has an internal `[[Prototype]]` link; property lookups walk
up this chain until found or the chain ends at `null`. Classes are built
on top of this mechanism.

~~~javascript
const animal = { eats: true };
const dog = Object.create(animal);  // dog's prototype is animal
dog.eats;   // true -- found via the prototype, not own property
~~~


### 3. The Prototype Chain

`instanceof` and method lookups both traverse the chain; shadowing a
property on the instance hides the prototype's version without deleting
it.

~~~javascript
dog.eats = false;         // own property shadows the prototype's
dog.eats;                  // false
delete dog.eats;
dog.eats;                  // true again -- prototype shows through
~~~
