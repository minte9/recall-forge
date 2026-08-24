## Python / Algorithms / Data Structures / Queues

### 1. Queue Basics

A queue only allows adding at the back and removing from the front -- FIFO (First In, First Out) -- like a print job queue or a line at a movie theater. A plain list can act as one, but popping from the front with `pop(0)` is O(n) because every remaining element has to shift left.

~~~py
class Queue:
    def __init__(self):
        self.items = []

    def enqueue(self, item):
        self.items.append(item)     # O(1) -- add at back

    def dequeue(self):
        return self.items.pop(0) if self.items else None  # O(n) -- shifts everything left

queue = Queue()
queue.enqueue(1)
queue.enqueue(2)
print(queue.dequeue())  # 1
~~~


### 2. Queue with deque

`collections.deque` gives true O(1) enqueue/dequeue from both ends, unlike a list's O(n) `pop(0)` -- the reason `deque` is the standard choice for queues in Python instead of a plain list.

~~~py
from collections import deque

print_queue = deque()
print_queue.append("report.pdf")
print_queue.append("invoice.docx")

job = print_queue.popleft()  # O(1) -- report.pdf
print(job)
~~~
