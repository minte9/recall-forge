## JS / Vue.js / State Management / Pinia Basics

### 1. Defining a Store

`defineStore(id, options)` creates a reusable store; the `id` must be
unique across the app since it's used internally for devtools and SSR
state hydration.

~~~javascript
export const useCounterStore = defineStore("counter", {
  state: () => ({ count: 0 }),
});
~~~


### 2. State, Getters & Actions

`state` holds reactive data, `getters` are cached derived values (like
`computed`), and `actions` are methods that can mutate state, including
async logic.

~~~javascript
defineStore("counter", {
  state: () => ({ count: 0 }),
  getters: { double: (s) => s.count * 2 },
  actions: { increment() { this.count++; } },
});
~~~


### 3. Using a Store in Components

Calling the store function (`useCounterStore()`) inside `setup()`
returns a reactive store instance — destructuring it directly loses
reactivity, so use `storeToRefs()` instead.

~~~javascript
const store = useCounterStore();
store.increment();                     // fine, direct call
const { count } = storeToRefs(store);  // safe destructuring
~~~
