## 1. Trees

A tree is a hierarchical data structure consisting of nodes connected by edges.

Unlike arrays, linked lists, stacks, and queues, trees are nonlinear: one node can connect to multiple child nodes.

Trees are widely used in software engineering for:

- Database indexes (B-Trees and B+ Trees)
- Filesystem directory structures
- HTML Document Object Model (DOM)
- Search and sorting algorithms
- Expression parsing and evaluation
- Hierarchical configuration data
- In-memory indexes and ordered collections
    

A Binary Search Tree (BST) is a special binary tree that maintains an ordering property, allowing efficient searching, insertion, and deletion when its height is small.

## 2. Tree terminology

Consider the following tree:

![[Pasted image 20261108172702.png]]

|Term|Meaning|Example|
|---|---|---|
|Node|An element of the tree|A, B, C|
|Root|Topmost node|A|
|Edge|Connection between nodes|A → B|
|Parent|Node with children|B is parent of D|
|Child|Node below a parent|D is child of B|
|Leaf|Node without children|D, E, F, G|
|Sibling|Nodes with the same parent|B and C|
|Subtree|A node and all its descendants|B, D, E|
|Depth|Number of edges from root|Depth of D = 2|
|Height|Longest downward path in edges|Height of A = 2|

A tree with `n` nodes has exactly `n - 1` edges.

The height of a tree strongly influences the performance of tree-based algorithms.

## 3. Binary Trees

A binary tree is a tree where each node has at most two children:

- Left child
- Right child
    

A binary tree does not necessarily maintain any ordering between its nodes.

### Types of binary trees

![[Pasted image 20261108172817.png|570]]


## 4. Binary Search Trees (BST)

A Binary Search Tree is a binary tree with an additional ordering property.

For every node:

- All values in the left subtree are smaller than the node's value.
- All values in the right subtree are greater than the node's value.
- Both subtrees must also satisfy the BST property.
    

In this lesson, we use unique integer keys.

Consider inserting:

`50, 30, 70, 20, 40, 60, 80`

![[Pasted image 20261108173045.png|496]]

This ordering allows us to eliminate an entire subtree at each comparison.

### Searching for 60

![[Pasted image 20261108173206.png|501]]

![[Pasted image 20261108173240.png|501]]

![[Pasted image 20261108173324.png|501]]

Only three nodes are visited instead of examining all seven.

For a balanced BST, searching requires approximately `O(log n)` comparisons. For a skewed BST, it may require `O(n)`.

## 5. BST implementation

We will implement a complete BST supporting:

- Insertion
- Search
- Deletion
- Inorder, preorder, and postorder traversals
- Minimum and maximum
- Height and size
    
The BST rule is simple:

- Values smaller than a node go into its left subtree.
- Values larger than a node go into its right subtree.

### Step 1 — Create the Node structure

Every tree consists of nodes. Each node needs three things:

1. An integer value.
2. A reference to its left child.
3. A reference to its right child.
    
```java
package example.datastructures.trees;

public class BinarySearchTree {

    private Node root;

    private static class Node {

        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        BinarySearchTree bst = new BinarySearchTree();
    }
}
```

### Step 2 — Insert the first node (root)

Before implementing the full insertion algorithm, we'll handle the simplest case: inserting into an empty tree.

```java
package example.datastructures.trees;

public class BinarySearchTree {

    private Node root;

    private static class Node {

        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        BinarySearchTree bst = new BinarySearchTree();

        bst.insert(50);

        System.out.println(bst.root.value);
    }

    public void insert(int value) {

        // Check whether the tree is empty
        if (root == null) {
            Node node = new Node(value);
            root = node;
        }
    }
}
```

### Step 3 — Insert a second node

Now let's extend your algorithm to handle inserting a value when the tree already contains a root.

```java
package example.datastructures.trees;

public class BinarySearchTree {

    private Node root;

    private static class Node {

        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        BinarySearchTree bst = new BinarySearchTree();

        // Insert root
        bst.insert(50);

        // Insert children
        bst.insert(30);
        bst.insert(70);

        System.out.println(bst.root.value);        // 50
        System.out.println(bst.root.left.value);   // 30
        System.out.println(bst.root.right.value);  // 70
    }

    public void insert(int value) {

        // Check whether the tree is empty
        if (root == null) {
            root = new Node(value);
            return;
        }

        if (value < root.value) {
            root.left = new Node(value);
        }

        if (value > root.value) {
            root.right = new Node(value);
        }
    }
}
```

### Step 4 — Inserting into a deeper tree

The current implementation works for inserting `30`, but what happens when you insert `20`?

```java
bst.insert(50);
bst.insert(30);
bst.insert(70);
bst.insert(20);
```

Because `20 < 50`, your code replaces `root.left` with a new node containing `20`.

You lose the reference to node `30`!

![[Pasted image 20261108180820.png|612]]

#### The solution: Navigate using a current node

Instead of always comparing against `root`, we need a variable representing the node we're currently examining.

```java
Node current = root;
```

This does not create a new node. It creates another reference to the existing root. 

We can navigate down the tree:

```java
current = current.left;
```

The important difference is that `root` remains unchanged.

#### Step 4.1 - Let's implement only the left-side navigation first.

Think about the following insertion sequence:

Trace inserting 20

1. `current = 50`. Since `20 < 50`, move left to `30`.
2. `current = 30`. Since `20 < 30`, inspect its left child.
3. `current.left == null`. Create node `20` there and stop.


![[Pasted image 20261108182136.png|549]]

Implementation:

```java
package example.datastructures.trees;

public class BinarySearchTree {

    private Node root;

    private static class Node {

        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        BinarySearchTree bst = new BinarySearchTree();

        // Insert root
        bst.insert(50);

        // Insert children
        bst.insert(30);
        bst.insert(20);

        System.out.println(bst.root.value);             // 50
        System.out.println(bst.root.left.value);        // 30
        System.out.println(bst.root.left.left.value);   // 20

    }

    public void insert(int value) {  // Look here

        // Check whether the tree is empty
        if (root == null) {
            root = new Node(value);
            return;
        }

        Node current = root;

        while (current != null) {

            if (value < current.value) {

                // If the current left is null
                if (current.left == null) {

                    // Create a new node at left
                    current.left = new Node(value);
                    break;
                
                } else {
                    // Move current to its left
                    current = current.left;
                }
            }
        }
        
    }
}
```

### Step 4.2 — Implement right-side insertion

The goal is to construct:

![[Pasted image 20261108182522.png|545]]

Implementation:

```java
package example.datastructures.trees;

public class BinarySearchTree {

    private Node root;

    private static class Node {

        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        BinarySearchTree bst = new BinarySearchTree();

        bst.insert(50); // root
        bst.insert(30);
        bst.insert(70);
        bst.insert(20);
        bst.insert(40);
        bst.insert(60);
        bst.insert(80);

        bst.insert(50); // Duplicate
        bst.insert(30); // Duplicate

        System.out.println(bst.root.value);             // 50

        System.out.println(bst.root.left.value);        // 30
        System.out.println(bst.root.left.left.value);   // 20
        System.out.println(bst.root.left.right.value);  // 40

        System.out.println(bst.root.right.value);        // 70
        System.out.println(bst.root.right.left.value);   // 60
        System.out.println(bst.root.right.right.value);  // 80

    }

    public void insert(int value) {  // Look here

        if (root == null) {
            root = new Node(value);
            return;
        }

        Node current = root;

        while (current != null) {

            // If value is smaller
            if (value < current.value) {
                if (current.left == null) {
                    current.left = new Node(value);
                    break;
                
                } else {
                    current = current.left;
                }
            } else 

            // Otherwise if value is larger
            if (value > current.value) {
                if (current.right == null) {
                    current.right = new Node(value);
                    break;
                
                } else {
                    current = current.right;
                }
            } else {

            // Otherwise if value already exists
                break;
            }
        }
        
    }
}
```

### Step 5 — Implement BST search

Now we'll implement `search(int value)` using the same navigation principles.

The goal is to determine whether a value exists in the tree.

For example:

```
bst.search(40);  // true
bst.search(90);  // false
```

We want the method to return a `boolean`.  

Notice that searching doesn't require visiting every node. At each step, the BST property tells us which direction to follow.  

```java
package example.datastructures.trees;

public class BinarySearchTree {

    private Node root;

    private static class Node {

        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        BinarySearchTree bst = new BinarySearchTree();

        bst.insert(50); // root
        bst.insert(30);
        bst.insert(70);
        bst.insert(20);
        bst.insert(40);
        bst.insert(60);
        bst.insert(80);

        System.out.println(bst.search(40));  // true
        System.out.println(bst.search(60));  // true
        System.out.println(bst.search(90));  // false
        System.out.println(bst.search(10));  // false
    }

    public void insert(int value) {

        if (root == null) {
            root = new Node(value);
            return;
        }

        Node current = root;

        while (current != null) {

            // If value is smaller
            if (value < current.value) {
                if (current.left == null) {
                    current.left = new Node(value);
                    break;
                
                } else {
                    current = current.left;
                }
            } else 

            // Otherwise if value is larger
            if (value > current.value) {
                if (current.right == null) {
                    current.right = new Node(value);
                    break;
                
                } else {
                    current = current.right;
                }
            } else {

            // Otherwise if value already exists
                break;
            }
        }
        
    }

    public boolean search(int value) {  // Look here

        Node current = root;

        while (current != null) {

            if (value == current.value) {
                return true;
            }

            if (value < current.value) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        return false;
    }
}
```

### Step 6 — Implement in-order traversal

Now we'll learn how to visit every node in the tree.

Unlike search, traversal visits all nodes.

There are three common depth-first traversal orders: in-order, pre-order, and post-order. We'll start with in-order traversal, because it produces sorted values in a BST.

Your tree currently looks like this:

![[Pasted image 20261108184655.png|590]]

### The in-order rule

For every node:

1. Visit its left subtree.
2. Process the current node.
3. Visit its right subtree.
    
The expected output is:

```
20 30 40 50 60 70 80
```

### Why recursion?

Each subtree is itself a tree.

For example, the left subtree of `50` is rooted at `30`, and we can traverse it using exactly the same algorithm.

This makes recursion a natural solution.

```java
package example.datastructures.trees;

public class BinarySearchTree {

    private Node root;

    private static class Node {

        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        BinarySearchTree bst = new BinarySearchTree();

        bst.insert(50); // root
        bst.insert(30);
        bst.insert(70);
        bst.insert(20);
        bst.insert(40);
        bst.insert(60);
        bst.insert(80);

        bst.printInOrder();
    }

    public void insert(int value) {

        if (root == null) {
            root = new Node(value);
            return;
        }

        Node current = root;

        while (current != null) {

            // If value is smaller
            if (value < current.value) {
                if (current.left == null) {
                    current.left = new Node(value);
                    break;
                
                } else {
                    current = current.left;
                }
            } else 

            // Otherwise if value is larger
            if (value > current.value) {
                if (current.right == null) {
                    current.right = new Node(value);
                    break;
                
                } else {
                    current = current.right;
                }
            } else {

            // Otherwise if value already exists
                break;
            }
        }
        
    }

    public boolean search(int value) {

        Node current = root;

        while (current != null) {

            if (value == current.value) {
                return true;
            }

            if (value < current.value) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        return false;
    }

    public void printInOrder() {
        traverseInOrder(root);
    }

    private void traverseInOrder(Node node) {

        // Base case
        if (node == null) {
            return;
        }

        // Recursively traverse the left subtree
        traverseInOrder(node.left);

        // Print the current node's value
        System.out.println(node.value);

        // Recursively traverse the right subtree
        traverseInOrder(node.right);
    }
}
```

Output:

```
20 30 40 50 60 70 80
```

### Step 7 — Pre-order traversal

Now let's learn a second traversal algorithm.

The difference between in-order and pre-order traversal is when we process the current node.

|Traversal|Order|
|---|---|
|In-order|Left → Node → Right|
|Pre-order|Node → Left → Right|
|Post-order|Left → Right → Node|

For pre-order traversal, we process the current node before visiting its children.

```java
package example.datastructures.trees;

public class BinarySearchTree {

    private Node root;

    private static class Node {

        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        BinarySearchTree bst = new BinarySearchTree();

        bst.insert(50); // root
        bst.insert(30);
        bst.insert(70);
        bst.insert(20);
        bst.insert(40);
        bst.insert(60);
        bst.insert(80);

        //bst.printInOrder();
        bst.printPreOrder();
    }

    public void insert(int value) {

        if (root == null) {
            root = new Node(value);
            return;
        }

        Node current = root;

        while (current != null) {

            // If value is smaller
            if (value < current.value) {
                if (current.left == null) {
                    current.left = new Node(value);
                    break;
                
                } else {
                    current = current.left;
                }
            } else 

            // Otherwise if value is larger
            if (value > current.value) {
                if (current.right == null) {
                    current.right = new Node(value);
                    break;
                
                } else {
                    current = current.right;
                }
            } else {

            // Otherwise if value already exists
                break;
            }
        }
        
    }

    public boolean search(int value) {

        Node current = root;

        while (current != null) {

            if (value == current.value) {
                return true;
            }

            if (value < current.value) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        return false;
    }

    public void printInOrder() {
        traverseInOrder(root);
    }

    public void printPreOrder() {
        traversePreOrder(root);
    }

    private void traverseInOrder(Node node) {

        // Base case
        if (node == null) {
            return;
        }

        // Recursively traverse the left subtree
        traverseInOrder(node.left);

        // Print the current node's value
        System.out.println(node.value);

        // Recursively traverse the right subtree
        traverseInOrder(node.right);
    }

    private void traversePreOrder(Node node) {

        // Base case
        if (node == null) {
            return;
        }

        System.out.println(node.value);

        traversePreOrder(node.left);
        traversePreOrder(node.right);
    }
}
```
