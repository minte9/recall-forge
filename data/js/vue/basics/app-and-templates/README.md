## JS / Vue.js / Basics / App & Templates

### 1. Creating an App

Every Vue app starts with `createApp`, passing a root component, then
`.mount()` to attach it to a DOM element. Nothing renders until `.mount()`
runs — `createApp` alone only builds the app instance.

~~~javascript
import { createApp } from "vue";
import App from "./App.vue";

createApp(App).mount("#app");  // renders into <div id="app">
~~~


### 2. Template Syntax

Templates use `{{ }}` for text interpolation and `v-bind`/`:` for binding
attributes to JS expressions. Expressions are single JS expressions, not
full statements — no `if`/`for` blocks directly inside `{{ }}`.

~~~vue
<p>{{ message }}</p>
<img :src="imageUrl">
<!-- {{ if (x) {} }}  -- invalid: not an expression -->
~~~


### 3. Directives Overview

Directives (`v-` prefixed attributes) apply reactive behavior to the DOM:
`v-if` toggles rendering, `v-on`/`@` binds events, `v-model` binds form
input two-way. Shorthand `:` and `@` exist for the two most common ones.

~~~vue
<button v-if="show" @click="show = false">Hide</button>
<input v-model="name">
~~~
