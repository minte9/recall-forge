# Python / Algorithms / Data Structures / Linked Lists

## 1. Linked List Basics

A linked list is a chain of nodes, each holding data and a reference to the next node -- nodes are NOT stored next to each other in memory. There's no direct index access like an array, so reading the nth item means following `next` links one at a time: O(n).

```py
class Node:
    def __init__(self, data):
        self.data = data
        self.next = None

class LinkedList:
    def __init__(self, first):
        self.first = first

    def read(self, index):
        current = self.first
        for _ in range(index):
            current = current.next  # follow links -- O(n)
        return current

items = LinkedList(Node("a"))
items.first.next = Node("b")
print(items.read(1).data)  # b
```


## 2. Insert at Head vs. Tail

Inserting at the head is O(1) -- just point the new node's `next` at the old first node. Inserting at the tail is O(n) because there's no direct reference to the last node; the list has to be walked end to end first to find it.

```py
def insert_at_head(self, value):
    new_node = Node(value)
    new_node.next = self.first
    self.first = new_node          # O(1)

def insert_at_tail(self, value):
    new_node = Node(value)
    current = self.first
    while current.next is not None:
        current = current.next     # walk to the end -- O(n)
    current.next = new_node
```
