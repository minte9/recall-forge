## 1. Graph fundamentals

A graph is a data structure that represents relationships between objects. Unlike trees, graphs can contain cycles, multiple paths between nodes, and disconnected components.

A graph consists of:

- Vertices (V): Objects such as cities, servers, or services.
- Edges (E): Connections between vertices, such as roads, network links, or dependencies.
    

Mathematically, a graph is written as \(G=(V,E)\).

![[Pasted image 20261110095735.png|572]]

5 vertices and 6 edges. Each line represents a relationship between two vertices.

Graphs are used in road navigation, social networks, package dependency resolution, microservices architecture, and network routing.

### Undirected graphs

An undirected graph has edges that work in both directions. If A is connected to B, then B is also connected to A.

For example, a two-way road between cities.

![[Pasted image 20261110095853.png|563]]

### Directed graphs

A directed graph, or digraph, uses edges with a direction. An edge A → B does not automatically imply B → A.

For example, Service A calls Service B, but Service B may not call Service A.

![[Pasted image 20261110095941.png|559]]

In Java, directed and undirected graphs differ primarily in how edges are stored.

```java
// Directed: A -> B
graph.get("A").add("B");

// Undirected: A <-> B
graph.get("A").add("B");
graph.get("B").add("A");
```

### Weighted graphs

A weighted graph assigns a numerical value to each edge, representing distance, cost, latency, or another metric.

For example, consider a road network where weights represent kilometers.

![[Pasted image 20261110100033.png|564]]

An undirected weighted graph, with distances in kilometers.

The shortest route from A to C is not necessarily the direct edge:

- Direct: A → C = 200 km
- Via B: A → B → C = 150 km
- Via D: A → D → C = 160 km
    

Therefore, A → B → C is the shortest route, with a total distance of 150 km.

This motivates shortest-path algorithms such as Dijkstra's algorithm.

## 2. Graph representations

There are two common ways to store graphs in memory: adjacency lists and adjacency matrices.

Consider this undirected graph:

```
    A ----- B
    |       |
    |       |
    C ----- D
```

### Adjacency list

Each vertex stores a list of its neighbors.

```
A -> [B, C]
B -> [A, D]
C -> [A, D]
D -> [B, C]
```

Java implementation using a map:

```java
Map<String, List<String>> graph = new HashMap<>();

graph.put("A", List.of("B", "C"));
graph.put("B", List.of("A", "D"));
graph.put("C", List.of("A", "D"));
graph.put("D", List.of("B", "C"));
```

Adjacency lists are efficient for sparse graphs, where relatively few edges exist compared with the number of possible connections.

Space complexity: \(O(V+E)\)

### Adjacency matrix

An adjacency matrix is a two-dimensional array. Each row and column represents a vertex, and a value of `1` indicates an edge.

![[Pasted image 20261110100424.png|619]]

Java representation:

```java
int[][] matrix = {
    {0, 1, 1, 0},
    {1, 0, 0, 1},
    {1, 0, 0, 1},
    {0, 1, 1, 0}
};
```

Check whether vertices A and C are connected:

```java
boolean connected = matrix[0][2] == 1;
System.out.println(connected); // true
```

An adjacency matrix allows constant-time edge lookup but always reserves space for all vertex pairs.

Space complexity: \(O(V^2)\)

### Comparison

|Operation|Adjacency list|Adjacency matrix|
|---|---|---|
|Space|O(V + E)|O(V²)|
|Check edge|O(degree(V))|O(1)|
|Iterate neighbors|O(degree(V))|O(V)|
|Add edge|O(1) amortized|O(1)|
|Best for|Sparse graphs|Dense graphs|

These adjacency-list bounds assume unsorted, array-backed neighbor lists. Using a `HashSet` instead can provide expected O(1) edge membership checks, with additional memory overhead.

## 3. Complete implementation — adjacency list

The following generic graph supports directed or undirected edges, vertex insertion, edge insertion, neighbor lookup, and edge checking.

Consider four vertices connected by four edges:

![[Pasted image 20261110101357.png|379]]

An undirected graph: A—B, A—C, B—D, C—D

We can represent this graph in Java as:

```
A -> [B, C]
B -> [A, D]
C -> [A, D]
D -> [B, C]
```

Each line describes a vertex and its direct neighbors.

This is called an **adjacency list**.

### Step 1 — Create the Graph class

Start with only two fields and a constructor.

```java
package example.datastructures.graphs;

import java.util.*;

public class Graph<T> {

    private final Map<T, Set<T>> adjacency =
            new LinkedHashMap<>();

    private final boolean directed;

    public Graph(boolean directed) {
        this.directed = directed;
    }
}
```

`Graph<T>` makes the class generic. You can create graphs with different vertex types:

```java
Graph<String> cities = new Graph<>(false);
Graph<Integer> numbers = new Graph<>(true);
```

`Map<T, Set<T>>` is the main data structure.

- `Map` stores a vertex as a key.
- `Set` stores that vertex's neighbors.
- `Set` prevents duplicate neighbors.
    
For example:

```java
Map<String, Set<String>> adjacency;
```

Conceptually:

|Key (vertex)|Value (neighbors)|
|---|---|
|A|B, C|
|B|A, D|
|C|A, D|
|D|B, C|

The `directed` flag determines whether an edge has a direction.

### Step 2 — Add vertices

Now add our first method inside `Graph<T>`:

```java
public void addVertex(T vertex) {
    adjacency.putIfAbsent(
        vertex,
        new LinkedHashSet<>()
    );
}
```

The key operation is:

```java
adjacency.putIfAbsent(vertex, new LinkedHashSet<>());
```

It means: add the vertex with an empty set of neighbors, but only if the vertex does not already exist.

Let's test it by adding a `main()` method:

```java
public static void main(String[] args) {

    Graph<String> graph = new Graph<>(false);

    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("A"); // ignored

    System.out.println(graph.adjacency);
}
```

Output:

```
{A=[], B=[], C=[]}
```

We now have three vertices, but no edges.

![[Pasted image 20261110102225.png|444]]

Three independent vertices with no connections.

Notice that calling `addVertex("A")` again does not create a duplicate vertex because the `Map` already contains that key.

### Step 3 — Add edges

Now implement the most important method:

```java
public void addEdge(T from, T to) {

    addVertex(from);
    addVertex(to);

    adjacency.get(from).add(to);

    if (!directed) {
        adjacency.get(to).add(from);
    }
}
```

First, we ensure both vertices exist.

```java
addVertex(from);
addVertex(to);
```

This allows us to call:

```java
graph.addEdge("A", "B");
```

without creating A and B manually first.

Second: connect A to B.

```java
adjacency.get(from).add(to);
```

For `from = "A"` and `to = "B"`:

```
Before:
A -> []
B -> []

After:
A -> [B]
B -> []
```

Add the reverse connection for undirected graphs.

```java
if (!directed) {
    adjacency.get(to).add(from);
}
```

Result:

```
A -> [B]
B -> [A]
```

Let's test the entire graph:

```java
package example.datastructures.graphs;

import java.util.*;

public class Graph<T> {

    private final Map<T, Set<T>> adjacency = 
            new LinkedHashMap<>();

    private final boolean directed;

    public Graph(boolean directed) {
        this.directed = directed;
    }

    public static void main(String[] args) {
        Graph<String> graph = new Graph<>(false);

        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");
        graph.addEdge("C", "D");

        System.out.println(graph.adjacency);
    }

    public void addVertex(T vertex) {
        adjacency.putIfAbsent(
            vertex, 
            new LinkedHashSet<>());
    }

    public void addEdge(T from, T to) {

        addVertex(from);
        addVertex(to);

        adjacency.get(from).add(to);

        if (!directed) {
            adjacency.get(to).add(from);
        }
    }
}
```

Output:

```
{
    A=[B, C],
    B=[A, D],
    C=[A, D],
    D=[B, C]
}
```

### Step 4 — Check whether an edge exists

Next, implement:

```java
public boolean hasEdge(T from, T to) {
    return adjacency.containsKey(from)
            && adjacency.get(from).contains(to);
}
```

Two checks are performed:

1. Does the source vertex exist?
2. Does its neighbor set contain the destination vertex?
    
For example:

```java
System.out.println(graph.hasEdge("A", "C")); // true
System.out.println(graph.hasEdge("A", "D")); // false
```

Even though A can reach D through B or C, there is no direct edge between A and D.

This distinction is important: `hasEdge()` checks direct adjacency, not whether a path exists.

The `&&` operator uses short-circuit evaluation. If `adjacency.containsKey(from)` is false, Java does not evaluate `adjacency.get(from).contains(to)`, avoiding a `NullPointerException`.

### Step 5 — Get neighbors safely

Implement:

```java
public Set<T> neighbors(T vertex) {
    return Collections.unmodifiableSet(
        adjacency.getOrDefault(vertex, Set.of())
    );
}
```

Example:

```java
System.out.println(graph.neighbors("A"));  // [B, C]
System.out.println(graph.neighbors("D"));  // [B, C]
System.out.println(graph.neighbors("X"));  // []
```

There are two useful concepts here.

`getOrDefault(vertex, Set.of())` returns an empty set if the vertex does not exist.

`Collections.unmodifiableSet(...)` prevents callers from changing the graph's internal data through the returned set.

For example:

```java
graph.neighbors("A").add("X");
```

This throws `UnsupportedOperationException`.

Without the unmodifiable wrapper, external code could modify the graph without using `addEdge()`, potentially breaking the consistency of an undirected graph.

### Step 6 — Print the graph

The final method is:

```java
public void printGraph() {
    adjacency.forEach((vertex, neighbors) ->
        System.out.println(
            vertex + " -> " + neighbors
        )
    );
}
```

This uses `Map.forEach()` with a lambda expression.

It is equivalent to:

```java
public void printGraph() {
    for (Map.Entry<T, Set<T>> entry
            : adjacency.entrySet()) {

        T vertex = entry.getKey();
        Set<T> neighbors = entry.getValue();

        System.out.println(
            vertex + " -> " + neighbors
        );
    }
}
```

The second version is longer but may be easier to understand while learning.

```java
graph.printGraph();
```

Output:

```
A -> [B, C]
B -> [A, D]
C -> [A, D]
D -> [B, C]
```

Complete implementation:

```java
package example.datastructures.graphs;

import java.util.*;

public class Graph<T> {

    private final Map<T, Set<T>> adjacency = 
            new LinkedHashMap<>();

    private final boolean directed;

    public Graph(boolean directed) {
        this.directed = directed;
    }

    public static void main(String[] args) {
        Graph<String> graph = new Graph<>(false);

        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");
        graph.addEdge("C", "D");

        System.out.println(graph.adjacency);
        // {A=[B, C], B=[A, D], C=[A, D], D=[B, C]}

        // Vertex has edges
        System.out.println(graph.hasEdge("A", "C")); // true
        System.out.println(graph.hasEdge("A", "D")); // false

        // Neighbors
        System.out.println(graph.neighbors("A"));  // [B, C]
        System.out.println(graph.neighbors("D"));  // [B, C]
        System.out.println(graph.neighbors("X"));  // []

        // Graph
        graph.printGraph();
    }

    public void addVertex(T vertex) {
        adjacency.putIfAbsent(
            vertex, 
            new LinkedHashSet<>());
    }

    public void addEdge(T from, T to) {

        addVertex(from);
        addVertex(to);

        adjacency.get(from).add(to);

        if (!directed) {
            adjacency.get(to).add(from);
        }
    }

    public boolean hasEdge(T from, T to) {
        return adjacency.containsKey(from) && 
               adjacency.get(from).contains(to);
    }

    public Set<T> neighbors(T vertex) {
        return Collections.unmodifiableSet(
            adjacency.getOrDefault(vertex, Set.of())
        );
    }

    public void printGraph() {
        adjacency.forEach((vertex, neighbors) -> 
            System.out.println(
                vertex + " -> " + neighbors
            )
        );
    }
}
```

`LinkedHashSet` prevents duplicate edges while preserving neighbor insertion order. The outer `HashMap` does not guarantee vertex iteration order.

The implementation has expected O(1) vertex and edge insertion and edge membership checks, assuming normal hash distribution.

## 4. Graph traversal

Traversal means visiting vertices systematically, typically while avoiding revisiting the same vertex.

Two fundamental traversal algorithms are:

- **Breadth-First Search (BFS)** - Explores vertices level by level using a queue.
- **Depth-First Search (DFS)** - Explores one branch deeply before backtracking, using recursion or a stack.

![[Pasted image 20261110120512.png|667]]

![[Pasted image 20261110120555.png|667]]

For a graph represented with adjacency lists, both algorithms take \(O(V+E)\) time when visiting the entire reachable graph.

### Breadth-First Search

BFS is useful for finding the minimum number of edges between vertices in an unweighted graph.

```java
public static <T> void bfs(Graph<T> graph, T start) {
    Queue<T> queue = new ArrayDeque<>();
    Set<T> visited = new HashSet<>();

    queue.offer(start);
    visited.add(start);

    while (!queue.isEmpty()) {
        T current = queue.poll();
        System.out.println(current);

        for (T neighbor : graph.neighbors(current)) {
            if (visited.add(neighbor)) {
                queue.offer(neighbor);
            }
        }
    }
}
```

The `visited.add(neighbor)` method returns `true` only when the neighbor was not already present, preventing repeated processing and infinite loops caused by cycles.

```java
public static void main(String[] args) {
	Graph<String> graph = new Graph<>(false);

	graph.addEdge("A", "B");
	graph.addEdge("A", "C");
	graph.addEdge("B", "D");
	graph.addEdge("C", "D");

	bfs(graph, "A");
}
```

Output:

```
A
B
C
D
```

### Depth-First Search

DFS is useful for cycle detection, connected components, dependency analysis, and topological sorting.

```java
public static <T> void dfs(Graph<T> graph, T start) {
    Set<T> visited = new HashSet<>();
    dfsRecursive(graph, start, visited);
}

private static <T> void dfsRecursive(
        Graph<T> graph,
        T current,
        Set<T> visited) {

    if (!visited.add(current)) {
        return;
    }

    System.out.println(current);

    for (T neighbor : graph.neighbors(current)) {
        dfsRecursive(graph, neighbor, visited);
    }
}
```

Recursive DFS is simple to understand, but very deep graphs can cause a `StackOverflowError`. An explicit stack is preferable when traversal depth may be large.

```java
public static void main(String[] args) {
	Graph<String> graph = new Graph<>(false);

	graph.addEdge("A", "B");
	graph.addEdge("A", "C");
	graph.addEdge("B", "D");
	graph.addEdge("C", "D");

	System.out.println("Breadth-First Search:");
	bfs(graph, "A");

	System.out.println("Depth-First Search:");
	dfs(graph, "A");
}
```

Output:

```
Breadth-First Search:
A
B
C
D
Depth-First Search:
A
B
D
C
```

## 5. Weighted graph

For weighted graphs, each edge needs a destination and weight.

Java 21 records make this convenient:

```java
record Edge<T>(T destination, int weight) {}
```

A graph can store:

```java
Map<String, List<Edge<String>>> graph = new HashMap<>();

graph.put("A", List.of(
    new Edge<>("B", 60),
    new Edge<>("C", 200),
    new Edge<>("D", 120)
));
```

A complete reusable implementation:

```java
package example.datastructures.graphs;

import java.util.*;

public class WeightedGraph<T> {

    public record Edge<T>(T destination, int weight) {}

    private final Map<T, List<Edge<T>>> adjacency = 
            new HashMap<>();

    public static void main(String[] args) {
        WeightedGraph<String> roads = new WeightedGraph<>();

        roads.addEdge("A", "B", 60);
        roads.addEdge("B", "C", 90);
        roads.addEdge("A", "C", 200);
        roads.addEdge("A", "D", 120);
        roads.addEdge("D", "C", 40);

        for (Edge<String> edge : roads.neighbors("A")) {
            System.out.printf(
                "A -> %s: %d km%n", 
                edge.destination(),
                edge.weight()
            );
        }
    } 
    
    public void addVertex(T vertex) {
        adjacency.putIfAbsent(vertex, new ArrayList<>());
    }

    public void addEdge(T from, T to, int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException(
                "Weight must be non-negative"
            );
        }

        addVertex(from);
        addVertex(to);

        adjacency.get(from).add(new Edge<>(to, weight));
        adjacency.get(to).add(new Edge<>(from, weight));
    }

    public List<Edge<T>> neighbors(T vertex) {
        return Collections.unmodifiableList(
            adjacency.getOrDefault(vertex, List.of())
        );
    }
}
```

Output:

```
A -> B: 60 km
A -> C: 200 km
A -> D: 120 km
```

This implementation represents an undirected weighted graph. It permits parallel edges and assumes non-negative weights, making it suitable for later Dijkstra examples.


## 6. Practical software engineering applications

### a) Road networks

Road networks are commonly modeled as weighted graphs:

- **Vertices**: Cities or intersections
- **Edges**: Roads connecting them
- **Weights**: Distance, travel time, or cost
    
```
Bucharest --60--> Ploiești
Ploiești --110--> Brașov
Bucharest --170--> Brașov
```

A navigation system may use Dijkstra or A* to find the best route. Road directions and travel-time estimates can make the underlying graph directed and time-dependent.

### b) Dependency graphs

Build systems and package managers use directed graphs to represent dependencies.

Arrows point from a component to its dependency. Shared dependencies form a directed graph.

Dependency graphs are useful for detecting circular dependencies, determining build order, and identifying components affected by a change.

A directed acyclic graph (DAG) can be processed using topological sorting.

### c) Microservices relationships

A microservices system can be represented as a directed graph.

```
API Gateway
    |
    v
Order Service ------> Payment Service
    |
    v
Inventory Service --> Database
```

Here, vertices represent services or infrastructure components, and edges represent calls or dependencies.

Graph analysis can help identify critical services, detect dependency cycles, and estimate the impact of failures.

For example, if Payment Service is unavailable, Order Service may be affected even when Order Service itself is healthy.

## 7. Key takeaways

Graphs generalize trees by allowing arbitrary relationships, cycles, and multiple paths. Directed graphs model one-way relationships, while weighted graphs associate costs with edges.

Adjacency lists are generally the preferred representation for sparse graphs, while adjacency matrices offer constant-time edge lookup at the cost of O(V²) storage.

BFS and DFS are the foundations for many more advanced algorithms, including shortest paths, cycle detection, topological sorting, and connectivity analysis.