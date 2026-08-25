# JS / Vue.js / Reactivity / toRef & toRefs

## 1. toRef

`toRef()` creates a single ref linked to one property of a reactive
object — reading/writing the ref stays in sync with the source property,
unlike a plain destructure.

```javascript
const state = reactive({ count: 0 });
const countRef = toRef(state, "count");
countRef.value++;   // also updates state.count
```


## 2. toRefs

`toRefs()` converts every property of a reactive object into a linked
ref at once, commonly used right before returning state from a
composable so callers can destructure safely.

```javascript
function useCounter() {
  const state = reactive({ count: 0 });
  return toRefs(state);   // safe to destructure now
}
const { count } = useCounter();
```


## 3. Destructuring Reactive Objects

Plain destructuring of `reactive()` breaks the link to the source;
always go through `toRefs()` (or `toRef()` for a single property) first
if the destructured values need to stay reactive.

```javascript
const { count } = reactive({ count: 0 }); // broken, plain number
const { count } = toRefs(reactive({ count: 0 })); // still reactive
```
