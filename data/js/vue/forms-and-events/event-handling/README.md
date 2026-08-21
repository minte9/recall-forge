## JS / Vue.js / Forms & Events / Event Handling

### 1. Listening to Events (v-on)

`v-on`/`@` binds a DOM event to a method or inline expression; the
native event object is available as `$event` when needed alongside
custom arguments.

~~~vue
<button @click="increment">+1</button>
<button @click="setCount($event, 5)">Set to 5</button>
~~~


### 2. Event Modifiers

Modifiers like `.stop`, `.prevent`, and `.once` handle common
event-handling boilerplate (`stopPropagation`, `preventDefault`)
declaratively in the template instead of in the handler body.

~~~vue
<form @submit.prevent="onSubmit">
<a @click.stop="onClick">
~~~


### 3. Key Modifiers

Keyboard event modifiers (`.enter`, `.esc`, `.tab`) filter a
`keyup`/`keydown` handler to a specific key, avoiding manual
`if (event.key === 'Enter')` checks.

~~~vue
<input @keyup.enter="submit">
~~~
