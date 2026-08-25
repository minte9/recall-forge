# JS / Vue.js / Forms & Events / Form Input Bindings

## 1. v-model Basics

`v-model` is sugar for binding a value and listening for its update
event in one directive, keeping form input and reactive state in sync
in both directions.

```vue
<input v-model="name">
<!-- sugar for -->
<input :value="name" @input="name = $event.target.value">
```


## 2. v-model Modifiers

`.lazy` syncs on `change` instead of every keystroke; `.number` casts
the input to a number; `.trim` strips whitespace — all avoid writing
that logic manually in a handler.

```vue
<input v-model.lazy="name">
<input v-model.number="age">
```


## 3. v-model on Different Inputs

`v-model` adapts its underlying event/prop per element type: text
inputs use `value`, checkboxes use `checked`, and `<select>` uses the
selected `value` — one directive, several bindings under the hood.

```vue
<input type="checkbox" v-model="checked">
<select v-model="selected"><option value="a">A</option></select>
```
