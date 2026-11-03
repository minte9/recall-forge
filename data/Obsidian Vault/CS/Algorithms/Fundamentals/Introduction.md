## 1. What is an Algorithm?

An algorithm is a finite sequence of well-defined steps that solves a problem. It receives input, processes it according to specific rules, and produces an output. Algorithms are independent of programming languages: the same algorithm can be implemented in Java, Python, C++, or even executed manually.

A good algorithm should have three important properties: correctness (produces the expected result), termination (finishes after a finite number of steps), and efficiency (uses reasonable time and memory). Two algorithms may produce identical results but have very different performance characteristics.

Consider finding the shortest road between two cities. You could examine every possible route, but this becomes impractical as the number of cities increases. Algorithms such as Dijkstra's solve the problem more efficiently by systematically exploring promising routes.

## 2. Algorithms and Data Structures

Algorithms operate on data structures. Choosing the right representation can significantly affect performance.

For example, a road network can be represented as a graph, where cities are vertices and roads are weighted edges.

Example graph: five cities connected by roads, with distances in kilometers.

![[Pasted image 20261103132828.png]]

The graph allows us to calculate distances without needing actual geographic coordinates. For example, the shortest route from A to E is A → B → D → E, with a total distance of 270 km.

The same graph structure is useful for computer networks, dependency management, social networks, and delivery systems.

## 3. Simple Algorithm in Java

Let's begin with a simple problem: find the minimum value in an array.

The algorithm starts with the first element as the current minimum, then examines every remaining element. Whenever it finds a smaller value, it updates the minimum.

```java
public class MinimumFinder {

    public static void main(String[] args) {
        int[] values = {12, 5, 8, 2, 19, 4};

        System.out.println(findMinimum(values));
        // Output: 2
    }
	
	public static int findMinimum(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("Empty array");
        }

        int min = numbers[0];

        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < minimum) {
                min = numbers[i];
            }
        }

        return min;
    }
}
```

This algorithm has several useful properties:

- Correctness: After each iteration, `min` contains the smallest value examined so far.
- Termination: The loop ends after examining all elements.
- Time complexity: O(n), because the algorithm examines every element.
- Space complexity: O(1), because it only uses a fixed amount of additional memory.
    
## 4. Practical Exercise — Route Selection

Before implementing Dijkstra's algorithm, consider a simpler problem.

You have three possible routes between two cities.  
Choose the optimization criterion: Shortest distance  / Lowest toll.

![[Pasted image 20261103134016.png]]

Selected: Route B — 270 km. Changing the objective changes which solution is optimal.

This introduces an important idea in algorithm design: an optimal solution depends on the objective being optimized.

## 5. Key Concepts for RecallForge

| Concept          | What to remember                                      |
| ---------------- | ----------------------------------------------------- |
| Algorithm        | Finite sequence of steps to solve a problem           |
| Correctness      | Produces the required result for valid inputs         |
| Termination      | Completes after a finite number of operations         |
| Time complexity  | How execution work grows with input size              |
| Space complexity | How memory requirements grow with input size          |
| Data structure   | How information is organized for efficient operations |
| Optimization     | Finding the best solution according to an objective   |