## JS / Javascript / DOM & Events / Event Handling Patterns

### 1. Bubbling & Capturing

Events fire on the target, then bubble up through ancestors by default
(or optionally capture down first) — `addEventListener(type, fn, true)`
listens during the capture phase.

~~~javascript
parent.addEventListener("click", () => console.log("parent"));
child.addEventListener("click", () => console.log("child"));
// clicking child logs: "child", then "parent" -- bubbling
~~~


### 2. Event Delegation

Attaching one listener to a common ancestor and checking `event.target`
handles events for many (including future) children without binding a
listener to each.

~~~javascript
list.addEventListener("click", (e) => {
  if (e.target.matches("li")) console.log(e.target.textContent);
});
~~~


### 3. preventDefault & stopPropagation

`preventDefault()` stops the browser's default action (like a link
navigating); `stopPropagation()` stops the event from continuing to
bubble — they solve different problems and are often confused.

~~~javascript
link.addEventListener("click", (e) => {
  e.preventDefault();     // stops navigation
  e.stopPropagation();     // stops bubbling to parent
});
~~~
