## PVM / Python / Data Structures / Stacks

### 1. Stacks Basics

A stack only allows adding and removing from the top -- LIFO (Last In, First Out) -- so there's no way to touch the middle or read everything freely. Python has no dedicated Stack type, but a list already behaves like one: `append()` pushes, `pop()` pops. It's the same model behind undo/redo and a browser's back history.

~~~py
undo_stack = []
undo_stack.append("Type Hello")
undo_stack.append("Type World")
undo_stack.append("Delete World")

last_action = undo_stack.pop()  # LIFO -- removes "Delete World"
print(last_action, undo_stack)
~~~


### 2. Custom Stack

A raw list technically allows breaking stack rules (`stack.insert(0, ...)`, `stack[1] = ...`), so wrapping it in a class that only exposes `push`/`pop`/`peek` enforces the LIFO discipline and makes intent clearer.

~~~py
class Stack:
    def __init__(self):
        self.items = []

    def push(self, item):
        self.items.append(item)

    def pop(self):
        return self.items.pop() if self.items else None

    def peek(self):
        return self.items[-1] if self.items else None

undo_stack = Stack()
undo_stack.push("Type: Hello")
undo_stack.push("Type: World")
undo_stack.push("Delete: World")
undo_stack.pop()          # removes "Delete: World"
print(undo_stack.peek())  # Type: World
~~~


### 3. Collections Deque

`collections.deque` behaves like the custom stack above but is built-in and optimized, and unlike a plain list it also supports O(1) operations from the front. A `maxlen` bounds it automatically, handy for something like a fixed-size browser history.

~~~py
from collections import deque

history = deque(maxlen=3)
history.append("google.com")
history.append("github.com")
history.append("python.org")
history.append("minte9.com")  # oldest ("google.com") drops automatically

print(list(history))   # ['github.com', 'python.org', 'minte9.com']
print(history.pop())   # minte9.com -- back button
~~~
