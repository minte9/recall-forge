## 1. What Is Recursion?

Recursion is a programming technique in which a function calls itself to solve smaller instances of the same problem. Instead of using a loop to repeat operations, a recursive algorithm divides the problem into simpler subproblems until it reaches a condition where no further calls are necessary.

Every recursive algorithm requires two fundamental components:

- Base case: The condition that stops recursion.
- Recursive case: The operation that reduces the problem and calls the function again.
    
Consider calculating the factorial of a number:

```java
5! = 5 * 4 * 3 * 2 * 1 = 120
```

The recursive definition is:

![[Pasted image 20261103163801.png]]

Java implementation;

```java
public static long factorial(int n) {
    if (n < 0) {
        throw new IllegalArgumentException("Negative number");
    }

    if (n == 0) {
        return 1; // Base case
    }

    return n * factorial(n - 1); // Recursive case
}
```

This implementation is suitable for `n` from 0 to 20.  Larger values overflow Java's `long` type.

## 2. Understanding the Call Stack

Every recursive call creates a new stack frame containing local variables, parameters, and information needed to return to the calling method.

The calls continue until the base case is reached. Then the results return in reverse order.

Recursion consumes stack memory proportional to its maximum call depth. Excessive recursion can produce a `StackOverflowError` in Java.

## 3. Recursion vs. Iteration

Many recursive algorithms can also be implemented using loops.

### Recursive implementation

```java
public static long factorialRecursive(int n) {
    if (n <= 1) return 1;

    return n * factorialRecursive(n - 1);
}
```

### Iterative implementation

```java
public static long factorialIterative(int n) {
    long result = 1;

    for (int i = 2; i <= n; i++) {
        result *= i;
    }

    return result;
}
```

Both examples assume a nonnegative `n` no greater than 20.

|Property|Recursion|Iteration|
|---|---|---|
|Repetition mechanism|Method calls|Loops|
|Time complexity|O(n)|O(n)|
|Auxiliary space|O(n)|O(1)|
|Stack overflow risk|Yes|Not from loop depth|
|Best suited for|Trees, divide and conquer|Simple sequential operations|

Java does not guarantee tail-call optimization. Even tail-recursive functions can consume additional stack memory.

Recommendation: Use iteration for simple linear operations. Prefer recursion when it naturally expresses the structure of the problem and the maximum depth is manageable.

## 4. Types of Recursion

### Direct recursion

A method calls itself directly:

```java
void countdown(int n) {
    if (n <= 0) return;

    System.out.println(n);
    countdown(n - 1);
}
```

### Indirect recursion

Two or more methods call each other:

```java
boolean isEven(int n) {
    if (n == 0) return true;
    return isOdd(n - 1);
}

boolean isOdd(int n) {
    if (n == 0) return false;
    return isEven(n - 1);
}
```

These examples assume nonnegative integers.

### Multiple recursion

A method makes multiple recursive calls:

```java
long fibonacci(int n) {
    if (n <= 1) return n;

    return fibonacci(n - 1) + fibonacci(n - 2);
}
```

The naive Fibonacci implementation has exponential time complexity, approximately O(φⁿ), because many subproblems are calculated repeatedly.

Memoization or iteration can reduce this to O(n).

## 5. Practical Application — Traversing Directory Trees

A directory can contain files and other directories, which themselves can contain additional files and directories.

This hierarchical structure makes recursion a natural choice.

```java
import java.io.IOException;
import java.nio.file.*;

public class DirectoryExplorer {

    public static void explore(Path directory)
            throws IOException {

        try (DirectoryStream<Path> entries =
                     Files.newDirectoryStream(directory)) {

            for (Path entry : entries) {
                System.out.println(entry);

                if (Files.isDirectory(entry,
                        LinkOption.NOFOLLOW_LINKS)) {
                    explore(entry);
                }
            }
        }
    }

    public static void main(String[] args)
            throws IOException {

        explore(Path.of("./documents"));
    }
}
```

The algorithm visits each entry and recursively explores subdirectories. It does not follow symbolic links to directories, avoiding link-induced cycles. It assumes the directory is accessible and does not change during traversal.

Time complexity: O(V + E) for the directory tree, excluding filesystem operation costs, where V represents entries and E represents parent-child relationships.

Auxiliary space: O(h + w), where `h` is recursion depth and `w` represents directory entries buffered during traversal, depending on the filesystem API implementation.

For very deep directory structures, an iterative approach with an explicit stack may be safer.

## 6. Practical Application — Exploring Connected Cities

Suppose we have a network of cities connected by roads. We want to find every city reachable from a starting city.

This can be solved using Depth-First Search (DFS), a recursive graph traversal algorithm.

City network:

![[Pasted image 20261103165340.png]]

Java implementation:

```java
import java.util.*;

public class CityExplorer {

    static void dfs(
            String city,
            Map<String, List<String>> graph,
            Set<String> visited) {

        if (!visited.add(city)) {
            return;
        }

        System.out.println(city);

        for (String neighbor : graph.getOrDefault(city, List.of())) {
            dfs(neighbor, graph, visited);
        }
    }

    public static void main(String[] args) {
        Map<String, List<String>> graph = Map.of(
            "A", List.of("B", "C"),
            "B", List.of("A", "D"),
            "C", List.of("A", "D"),
            "D", List.of("B", "C", "E"),
            "E", List.of("D")
        );

        dfs("A", graph, new HashSet<>());
    }
}
```

Output:

```
A
B
D
C
E
```

The `visited` set prevents infinite recursion when roads form cycles.

Time complexity: O(V + E), because each city and road connection is processed a bounded number of times.

Auxiliary space: O(V), for the visited set and recursive call stack.

DFS discovers reachable cities but does not guarantee the shortest route. Later, we'll use BFS and Dijkstra's algorithm for shortest-path problems.

## 7. Common Recursion Mistakes

|Mistake|Consequence|Solution|
|---|---|---|
|Missing base case|Infinite recursion|Define a stopping condition|
|No progress toward base case|Stack overflow|Reduce problem size each call|
|Repeated subproblems|Poor performance|Use memoization|
|Excessive recursion depth|Stack overflow|Use an iterative algorithm|
|Cycles in graph traversal|Repeated visits|Track visited nodes|
## 8. Key Takeaways

- Recursion solves problems by repeatedly reducing them to smaller instances.
- Every recursive algorithm requires a base case and progress toward it.
- Recursive calls consume stack memory.
- Recursion is particularly useful for hierarchical structures, trees, graphs, and divide-and-conquer algorithms.
- Iteration is often more memory-efficient for simple sequential operations.
- Memoization can eliminate repeated recursive computations.