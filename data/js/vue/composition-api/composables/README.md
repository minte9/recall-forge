## JS / Vue / Composition API / Composables

### 1. Extracting Composables

A composable is a function that encapsulates reusable stateful logic
using Composition API primitives (`ref`, `computed`, `watch`) — the
modern replacement for mixins.

~~~javascript
function useMouse() {
  const x = ref(0), y = ref(0);
  window.addEventListener("mousemove", e => { x.value = e.pageX; });
  return { x, y };
}
~~~


### 2. Composables vs Mixins

Unlike mixins, composables make the source of every property explicit at
the call site (`const { x } = useMouse()`), avoiding mixins' silent
property merging and naming collisions.

~~~javascript
// Mixin: unclear where `x` came from
// Composable: explicit
const { x, y } = useMouse();
~~~


### 3. Naming Conventions

Composables are conventionally named with a `use` prefix (`useMouse`,
`useFetch`) — a signal, like React hooks, that the function uses
reactive/lifecycle APIs internally.

~~~javascript
function useFetch(url) { /* ... */ }
const { data } = useFetch("/api/user");
~~~
