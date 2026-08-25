# Python / Algorithms / Data Structures / Binary Search Trees

## 1. BST Ordering Rule

A binary search tree is a tree where every node has at most two children, and values are ordered: everything in the left subtree is smaller, everything in the right subtree is larger. It's like a library shelf where books before "M" go left and books after "M" go right, making any value findable without scanning the whole shelf.

```py
class Tree:
    def __init__(self, value, left=None, right=None):
        self.value = value
        self.left = left
        self.right = right

root = Tree(50, Tree(25), Tree(75))
assert root.value > root.left.value   # left is smaller
assert root.value < root.right.value  # right is larger
```


## 2. Searching a BST

Searching compares the target to the current node and goes left or right based on the ordering rule, halving the remaining nodes to check at each step. That's O(log n) on a balanced tree, but degrades to O(n) if the tree is skewed (effectively a linked list).

```py
def search(value, node):
    if node is None or value == node.value:
        return node
    if value < node.value:
        return search(value, node.left)
    return search(value, node.right)
```
