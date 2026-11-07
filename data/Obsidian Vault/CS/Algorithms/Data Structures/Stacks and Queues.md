## 1. Overview

Stacks and queues are linear data structures that control the order in which elements are inserted and removed.

Unlike arrays and lists, which allow access to elements by index, stacks and queues are designed around specific access patterns.

- Stack — LIFO: Last In, First Out.
- Queue — FIFO: First In, First Out.
- Deque: Double-ended queue, supporting insertion and removal at both ends.
    
These structures appear frequently in real software systems:

- Undo/redo functionality in editors.
- Function calls and expression evaluation.
- Background task processing.
- Breadth-first search (BFS).
- Parsing JSON, XML, and programming languages.
- Message processing and job scheduling.
    
## 2. Stacks — Last In, First Out

A stack allows insertion and removal only at one end, called the top.  
Think of a stack of plates: the last plate placed on top is the first one removed.  
### Core operations

|Operation|Description|Time|
|---|---|---|
|`push(x)`|Add element to top|O(1)|
|`pop()`|Remove top element|O(1)|
|`peek()`|Read top element|O(1)|
|`isEmpty()`|Check if empty|O(1)|

These complexities assume an array-backed stack with amortized growth, or a suitable linked implementation.

### Java 21 implementation using ArrayDeque

Java provides the legacy `Stack` class, but `ArrayDeque` is generally preferred for modern applications.

```java
package example.datastructures.stacks_queues;

import java.util.ArrayDeque;
import java.util.Deque;

public class StackExample {
    public static void main(String[] args) {
     
        Deque<String> stack = new ArrayDeque<>();

        stack.push("Open document");
        stack.push("Edit title");
        stack.push("Delete paragraph");

        System.out.println("Top: " + stack.peek());

        while(!stack.isEmpty()) {
            String removed = stack.pop();
            
            System.out.println("Undo: " + removed);
        }
    }
}
```

Output:

```
Top: Delete paragraph
Undo: Delete paragraph
Undo: Edit title
Undo: Open document
```

The last operation is undone first.

`ArrayDeque` does not permit null elements, and its stack operations have amortized \(O(1)\) time complexity.

## 3. Implementing a stack from scratch

To understand how a stack works internally, let's implement one using a dynamically resized array.

### Complete implementation

```java
package example.datastructures.stacks_queues;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class ArrayStack {

    private int[] elements = new int[2];
    private int size = 0;

    public static void main(String[] args) {
        ArrayStack stack = new ArrayStack();

        stack.push(10);
        stack.push(20);
        stack.push(30);

        System.out.println("Top: " + stack.peek());

        System.out.println(stack.pop());
        System.out.println(stack.pop());
        System.out.println(stack.pop());

        System.out.println("Empty: " + stack.isEmpty());

        System.out.println(stack.elements[0]);  // 0
    }    

    public void push(int value) {
        if (size == elements.length) {
            elements = Arrays.copyOf(
                elements, elements.length * 2
            );
        }

        elements[size++] = value;
    }

    public int peek() {
        if (size == 0) {
            throw new NoSuchElementException("Stack is empty");
        }

        return elements[size - 1];
    }

    public int pop() {
        if (size == 0) {
            throw new NoSuchElementException("Stack is empty");
        }

        int value = elements[--size];
        elements[size] = 0; // Optional clearing

        return value;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
```

Output:

```
Top: 30
30
20
10
Empty: true
```

For the `int[]` implementation, clearing is optional. 

```java
public int pop() {
    if (size == 0) {
        throw new NoSuchElementException("Stack is empty");
    }

    int value = elements[--size];
    elements[size] = 0;  // Look Here

    return value;
}
```

However, when using an array of objects, clearing is important:

```java
package example.datastructures.stacks_queues;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class ArrayStack<T> {

    private Object[] elements = new Object[2];
    private int size = 0;

    public static void main(String[] args) {
        ArrayStack<Integer> stack = new ArrayStack<>();

        stack.push(10);
        stack.push(20);

        System.out.println(stack.pop()); // 20

        System.out.println(stack.elements[1]);  // null
        System.out.println(stack.size);  // 1
    }    

    public void push(T value) {
        if (size == elements.length) {
            elements = Arrays.copyOf(
                elements, elements.length * 2
            );
        }

        elements[size++] = value;
    }
	
	@SuppressWarnings("unchecked")
    public T pop() {
        if (size == 0) {
            throw new NoSuchElementException("Stack is empty");
        }

        T value = (T) elements[--size]; // Unchecked cast
        elements[size] = null; // Look Here

        return value;
    }
}
```

This allows the garbage collector to reclaim the removed object if nothing else references it. 
Notice that `Integer` is used instead of `int`, because Java generics cannot use primitive types.

### Complexity analysis

Let \(n\) be the number of stored elements.

- `push()` — \(O(1)\) amortized, \(O(n)\) when resizing.
- `pop()` — \(O(1)\).
- `peek()` — \(O(1)\).
- `isEmpty()` — \(O(1)\).
- Space — \(O(n)\).
    
This implementation does not shrink its backing array after removals, so its allocated capacity can remain larger than the current stack size.

## 4. Practical example — Validating parentheses

Stacks are particularly useful for checking whether nested expressions are correctly balanced.

For example:

```
{[()]}    Valid
{[(])}    Invalid
((()))    Valid
(()       Invalid
```

### Algorithm

1. Read characters from left to right.
2. Push opening brackets onto the stack.
3. For each closing bracket, pop the latest opening bracket.
4. Verify that the bracket types match.
5. At the end, the stack must be empty.
6. 
### Complete Java implementation

```java
package example.datastructures.stacks_queues;

import java.util.ArrayDeque;
import java.util.Deque;

public class BalancedBrackets {
    public static void main(String[] args) {
        
        String[] expressions = {
            "{[()]}",
            "{[(])}",
            "((()))",
            "(()"
        };

        for (String expression : expressions) {
            System.out.printf(
                "%s -> %s\n",
                expression,
                isBalanced(expression)
            );
        }
    }

    static boolean isBalanced(String expression) {

        Deque<Character> stack = new ArrayDeque<>();

        for (char c : expression.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
                continue;
            }

            if (c == ')' || c == ']' || c == '}') {
                
                if (stack.isEmpty()) {
                    return false;
                }

                char open = stack.pop();
                
                if (c == ')' && open != '(' ||
                    c == ']' && open != '[' ||
                    c == '}' && open != '{') {
                        return false;
                }
            }
        }

        return stack.isEmpty();
    }
}
```

Output:

```
{[()]} -> true
{[(])} -> false
((())) -> true
(() -> false
```

### Complexity

For an expression containing \(n\) characters:

- Time: \(O(n)\), because each character is processed once.
- Space: \(O(n)\), because all characters could be opening brackets.
    
This technique is commonly used in compilers, interpreters, configuration parsers, and syntax validation.

## 5. Queues — First In, First Out

A queue processes elements in the order they arrive.  
Think of customers waiting in line: the first customer to arrive is served first.  

### Core operations

|Operation|Description|Time|
|---|---|---|
|`offer(x)`|Add to rear|O(1)*|
|`poll()`|Remove from front|O(1)|
|`peek()`|Read front|O(1)|
|`isEmpty()`|Check if empty|O(1)|

* Amortized for a dynamically resized circular-array queue.

### Java 21 implementation using ArrayDeque

```java
package example.datastructures;

import java.util.ArrayDeque;
import java.util.Queue;

public class QueueExample {

    public static void main(String[] args) {

        Queue<String> tasks = new ArrayDeque<>();

        tasks.offer("Import CSV");
        tasks.offer("Validate records");
        tasks.offer("Save to database");

        System.out.println("Next: " + tasks.peek());

        while (!tasks.isEmpty()) {
            System.out.println("Processing: " + tasks.poll());
        }
    }
}
```

Output:

```
Next: Import CSV
Processing: Import CSV
Processing: Validate records
Processing: Save to database
```

The first task added is processed first.

## 6. Implementing a circular queue from scratch

A naive queue backed by an array might shift all elements left after each removal.

That would make dequeue \(O(n)\).

A better implementation uses a circular array, where the rear wraps around to the beginning when it reaches the array's end.

Circular buffer — example with capacity 3

The next index after 2is 0. The buffer reuses free positions instead of shifting elements.

### Complete implementation

```java
package example.datastructures.stacks_queues;

import java.util.NoSuchElementException;

public class CircularQueue {

    private final int[] data;
    private int front = 0;
    private int size = 0;

    public CircularQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                "Capacity must be positive"
            );
        }

        data = new int[capacity];
    }

    public static void main(String[] args) {
        CircularQueue queue = new CircularQueue(3);

        queue.offer(10);
        queue.offer(20);
        queue.offer(30);

        System.out.println(queue.poll());  // 10

        queue.offer(40);  // Wraps around

        while(!queue.isEmpty()) {
            System.out.println(queue.poll());
        }
    }

    public void offer(int value) {
        if (size == data.length) {
            throw new IllegalStateException(
                "Queue is full"
            );
        }

        int rear = (front + size) % data.length;  // Look here

        data[rear] = value;
        size++;
    }

    public int poll() {
        if (isEmpty()) {
            throw new NoSuchElementException(
                "Queue is empty"
            );
        }

        int value = data[front];

        front = (front + 1) % data.length;  // Look here
        size--;

        return value;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }    
}
```

Output:

```
10
20
30
40
```

The key to understanding a circular queue is that the array never moves its elements. Instead, we move two positions logically:

- `front` — index of the next element to remove.
- `rear` — index where the next element will be inserted.
- `size` — number of elements currently in the queue.
    
The `%` (modulo) operator allows these positions to wrap around to index `0` when they reach the end of the array.

### Complexity analysis

All core operations are \(O(1)\), because no element shifting is required.

- `offer()` — \(O(1)\).
- `poll()` — \(O(1)\).
- `peek()` — \(O(1)\).
- Space — \(O(C)\), where \(C\) is the fixed capacity.
    
A production queue may also support dynamic resizing, concurrency control, and blocking behavior.

## 7. Practical example — Background task processing

Suppose a Spring Boot application receives requests to import files.  

Instead of processing everything immediately, requests are placed in a queue.  

A worker processes them in arrival order.  

```java
package example.datastructures.stacks_queues;

import java.util.ArrayDeque;
import java.util.Queue;

public class ImportTaskProcessor {
    
    private static final Queue<ImportTask> queue = new ArrayDeque<>();

    record ImportTask(long id, String filename) {}
    
    public static void main(String[] args) {

        ImportTaskProcessor processor = new ImportTaskProcessor();

        processor.submit(new ImportTask(1, "customers.csv"));
        processor.submit(new ImportTask(2, "transactions.csv"));
        processor.submit(new ImportTask(3, "products.csv"));

        while(!queue.isEmpty()) {
            processNext();
        }
    }  

    void submit(ImportTask task) {
        queue.offer(task);
    }

    static void processNext() {
        ImportTask task = queue.poll();

        if (task == null) {
            return;
        }

        System.out.printf(
            "Importing task %d: %s%n",
            task.id(),
            task.filename()
        );
    }
}
```

Output:

```
Importing task 1: customers.csv
Importing task 2: transactions.csv
Importing task 3: products.csv
```

### Complexity

For \(n\) submitted tasks:

- Enqueue: amortized \(O(1)\) per task.
- Dequeue: \(O(1)\) per task.
- Processing all tasks: \(O(n)\), excluding the actual import work.
- Space: \(O(n)\) for queued tasks.
    
Important production consideration: `ArrayDeque` is not thread-safe and does not persist tasks. For a multithreaded application, consider `BlockingQueue`; for durable, distributed processing, consider a database-backed job system or a message broker such as RabbitMQ or Kafka.

## 8. Practical example — Breadth-first search (BFS)

Queues are essential for breadth-first search, an algorithm that explores a graph level by level.

Imagine a network of servers:

![[Pasted image 20261106133210.png]]

BFS processes all directly connected nodes before exploring more distant nodes.

### Complete implementation

```java
package example.datastructures.stacks_queues;

import java.util.*;

public class BreadFirstSearch {
    public static void main(String[] args) {
        
        Map<String, List<String>> graph = Map.of(
            "A", List.of("B", "C"),
            "B", List.of("D", "E"),
            "C", List.of("F", "G"),
            "G", List.of("XXX", "YYY")
        );

        bfs(graph, "A");
    }

    public static void bfs(
            Map<String, List<String>> graph,
            String start) {

        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        queue.offer(start);
        visited.add(start); // A visited

        while(!queue.isEmpty()) {
            String node = queue.poll();
            System.out.println("Visit: " + node);

            for (String neighbor : 
                    graph.getOrDefault(node, List.of())) {

                if (visited.add(neighbor)) { // B visited
                    queue.offer(neighbor);
                }
            }
        }
    }
}
```

Output:

```
Visit: A
Visit: B
Visit: C
Visit: D
Visit: E
Visit: F
Visit: G
Visit: XXX
Visit: YYY
```

### Complexity analysis

Let:

- \(V\) = number of vertices (nodes).
- \(E\) = number of edges (connections).
    
Every vertex is visited at most once, and each adjacency list is examined once.

```java
T(V,E) = O(V+E)
```

Additional space is \(O(V)\) for the queue and visited set.

BFS is useful for finding shortest paths in unweighted graphs, network discovery, dependency exploration, and social-network connections. We will study it in more detail in the Graph Algorithms subcategory.

## 9. Deques — Double-ended queues

A deque supports insertion and removal from both ends.

```java
Deque<Integer> deque = new ArrayDeque<>();

deque.addFirst(10);
deque.addLast(20);
deque.addFirst(5);

System.out.println(deque); // [5, 10, 20]

deque.removeFirst(); // 5
deque.removeLast();  // 20
```

A deque can behave as either a stack or a queue.

|Usage|Add|Remove|
|---|---|---|
|Stack (LIFO)|`push()`|`pop()`|
|Queue (FIFO)|`offerLast()`|`pollFirst()`|
|Double-ended queue|`addFirst()` / `addLast()`|`removeFirst()` / `removeLast()`|

`ArrayDeque` is a good default for non-concurrent stack and queue implementations.

## 10. Stack vs Queue vs Deque

|Property|Stack|Queue|Deque|
|---|---|---|---|
|Ordering|LIFO|FIFO|Both ends|
|Insert|Top|Rear|Either end|
|Remove|Top|Front|Either end|
|Typical Java type|`Deque`|`Queue`|`Deque`|
|Common implementation|`ArrayDeque`|`ArrayDeque`|`ArrayDeque`|
|Example|Undo history|Job processing|Sliding window|

### When to choose each

Use a stack when the most recently added item must be processed first, such as undo operations or nested expression evaluation.

Use a queue when items must be processed in arrival order, such as a task scheduler or BFS traversal.

Use a deque when both ends must be accessible efficiently, such as sliding-window algorithms or double-ended buffers.

## 11. Common mistakes

 **1. Using `Stack` instead of `ArrayDeque`**

Java's legacy `Stack` extends `Vector` and uses synchronized methods. For ordinary single-threaded stack operations, `ArrayDeque` is generally preferred.

**2. Implementing a queue using `ArrayList.remove(0)`**

Removing the first element shifts the remaining elements, costing \(O(n)\). Use `ArrayDeque` instead.

**3. Forgetting empty collection behavior**

`pop()` and `remove()` throw exceptions on empty collections, while `poll()` returns `null`. `peek()` returns `null` when empty.

**4. Confusing BFS and DFS**

BFS uses a queue to explore level by level. Iterative DFS typically uses a stack to explore deeply before backtracking.

**5. Assuming an in-memory queue is durable**

If the application restarts, an ordinary `ArrayDeque` loses its contents. Persistence requires additional infrastructure.

**6. Assuming FIFO means completion order**

With multiple workers, tasks may start in FIFO order but finish in a different order.

## 13. Key takeaways

- A stack follows LIFO ordering and is useful for undo, parsing, and DFS.
- A queue follows FIFO ordering and is useful for task processing and BFS.
- A deque supports efficient operations at both ends.    
- `ArrayDeque` is usually the preferred Java 21 implementation for non-concurrent stacks and queues.
- Circular buffers avoid expensive element shifting.
- Stack and queue operations are generally \(O(1)\), with amortized costs when resizing is involved.
- BFS uses a queue and runs in \(O(V+E)\) time.
