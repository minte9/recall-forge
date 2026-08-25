# JS / Vue.js / Components / Emits

## 1. Declaring Emits

`defineEmits()` declares the custom events a component can fire,
documenting its API and enabling validation — similar in spirit to
declaring props for inputs.

```javascript
const emit = defineEmits(["update", "close"]);
```


## 2. emit in Practice

Calling the function returned by `defineEmits` fires the event with
optional payload arguments; the parent listens with `@event-name` just
like a native DOM event.

```javascript
emit("update", newValue);
// parent: <MyInput @update="handleUpdate" />
```


## 3. v-model on Components

`v-model` on a custom component is sugar for binding a `modelValue` prop
and listening for an `update:modelValue` event — implementing both makes
a component work seamlessly with `v-model`.

```javascript
defineProps(["modelValue"]);
const emit = defineEmits(["update:modelValue"]);
```

```vue
<input :value="modelValue"
  @input="emit('update:modelValue', $event.target.value)">
```
