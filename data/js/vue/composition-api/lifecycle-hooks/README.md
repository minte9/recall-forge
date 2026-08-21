## JS / Vue.js / Composition API / Lifecycle Hooks

### 1. Mounting Hooks

`onBeforeMount` runs before the DOM is rendered; `onMounted` runs after
— the right place for DOM access, third-party library init, or initial
data fetching.

~~~javascript
onMounted(() => {
  fetchData();   // safe: DOM and refs are ready
});
~~~


### 2. Update & Unmount Hooks

`onUpdated` fires after reactive data changes and the DOM re-renders;
`onUnmounted` fires when the component is destroyed — the place to clean
up timers, subscriptions, or listeners.

~~~javascript
const timer = setInterval(tick, 1000);
onUnmounted(() => clearInterval(timer));  // avoid a leaked timer
~~~


### 3. Lifecycle in Composition API

Composition API lifecycle hooks are imported functions (`onMounted`,
etc.) called inside `setup()`, rather than Options API's object keys
(`mounted() {}`) — both map to the same underlying hooks.

~~~javascript
import { onMounted, onUnmounted } from "vue";
// vs Options API: mounted() { ... }
~~~
