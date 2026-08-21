## JS / Vue / Basics / Computed & Watchers

### 1. Computed Properties

`computed()` derives a cached value from reactive state; it only
re-evaluates when a dependency changes, unlike a method, which re-runs on
every render.

~~~javascript
const price = ref(10);
const quantity = ref(2);
const total = computed(() => price.value * quantity.value);
~~~


### 2. watch

`watch()` observes specific reactive sources and runs a callback with the
old and new values — ideal for reacting to a change with a side effect,
like an API call.

~~~javascript
watch(searchTerm, (newVal, oldVal) => {
  fetchResults(newVal);   // runs only when searchTerm changes
});
~~~


### 3. watchEffect

`watchEffect()` runs immediately and automatically tracks whatever
reactive values it reads inside — no explicit source list needed, at the
cost of less precise control.

~~~javascript
watchEffect(() => {
  console.log(`count is ${count.value}`);  // reruns if count changes
});
~~~
