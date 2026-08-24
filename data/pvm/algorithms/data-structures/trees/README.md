## Python / Algorithms / Data Structures / Trees

### 1. Tree Basics

A tree is nodes connected by edges: one root at the top, leaves at the bottom, and every non-root node has exactly one parent. Edges never form a loop -- that's what separates a tree from a general graph.

~~~py
root = {'name': 'A', 'children': []}
child = {'name': 'B', 'children': []}
root['children'] = [child]  # B's only parent is A
~~~


### 2. Representing Trees in Python

Nested dicts work for quick scripts, but a `Node` class with a `children` list (or a library like `anytree`) scales better -- it gives each node identity, makes traversal code readable, and lets a subtree be detached by clearing one reference.

~~~py
class Node:
    def __init__(self, name, children=None):
        self.name = name
        self.children = children or []

root = Node('A', [Node('B'), Node('C')])
print([c.name for c in root.children])  # ['B', 'C']
~~~
