## Python / Algorithms / Recursion / Tree Traversal

### 1. DFS Traversal (Preorder, Inorder, Postorder)

Depth-first search follows one branch as deep as possible before backtracking, and recursion maps onto it naturally since each call handles one subtree. The three orders differ only in when the current node is processed relative to its children: preorder visits it first, inorder visits it between the left and right subtrees, postorder visits it last.

~~~python
def inorder(node):
    if node is None:
        return []
    return inorder(node.left) + [node.value] + inorder(node.right)
    # preorder:  [node.value] + inorder(node.left) + inorder(node.right)
    # postorder: inorder(node.left) + inorder(node.right) + [node.value]
~~~


### 2. BFS Traversal

Breadth-first search visits a tree level by level, which needs a queue (FIFO) tracking every node discovered but not yet processed -- there's no single "next subtree" to recurse into, so BFS is naturally iterative rather than recursive. Use `collections.deque` and `popleft()`; popping from the front of a plain `list` is O(n) and makes the whole traversal O(n^2).

~~~python
from collections import deque

def bfs(root):
    queue = deque([root])
    order = []
    while queue:
        node = queue.popleft()       # O(1), unlike list.pop(0)
        order.append(node.value)
        if node.left: queue.append(node.left)
        if node.right: queue.append(node.right)
    return order
~~~
