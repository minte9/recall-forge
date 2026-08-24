## Python / Algorithms / Algorithms / Graph Traversal

### 1. Depth-First Search (Graphs)

Unlike a tree, a graph can have cycles, so DFS must track visited vertices to avoid looping forever. It follows one path as deep as possible via recursion, backtracking once a vertex's neighbors are all visited or already seen.

~~~py
class Vertex:
    def __init__(self, name):
        self.value = name
        self.adjacent_vertices = []

    def add(self, vertex):
        self.adjacent_vertices.append(vertex)
        if self not in vertex.adjacent_vertices:  # mutual
            vertex.adjacent_vertices.append(self)
        return self

a = Vertex('Alice')
b = Vertex('Bob')
c = Vertex('Candy')
d = Vertex('Derek')
e = Vertex('Elaine')
f = Vertex('Fred')
g = Vertex('Gina')
h = Vertex('Helen')
i = Vertex('Irena')

a.add(b).add(c).add(d).add(e)
b.add(f); c.add(h); d.add(e).add(g); e.add(d)
f.add(h)
g.add(i)
h.add(c)

def dfs_traverse(vertex, visited=[]):
    visited.append(vertex.value)

    for v in vertex.adjacent_vertices:
        if v.value in visited:
            continue
        dfs_traverse(v, visited)  # recursion
    return visited

print(dfs_traverse(a))
print(dfs_traverse(f, visited=[]))

"""
    ['Alice', 'Bob', 'Candy', 'Derek', 'Elaine', 'Fred', 'Helen', 'Gina', 'Irena']
    ['Fred', 'Bob', 'Helen', 'Alice', 'Candy', 'Derek', 'Elaine', 'Gina', 'Irena']
"""
~~~


### 2. Breadth-First Search (Graphs)

BFS explores level by level instead of following one path deep: it uses a queue instead of recursion, visiting all of a vertex's neighbors before moving further out. It reaches nearby vertices first, which makes it the right choice when you want to stay close to the starting point rather than jump far away quickly (DFS's strength).

~~~py
from collections import deque

def bfs_traverse(starting_vertex):
    queue = deque()
    visited = [starting_vertex.value]
    queue.append(starting_vertex)

    while queue:
        current_vertex = queue.popleft()

        for v in current_vertex.adjacent_vertices:
            if v.value in visited:
                continue
            visited.append(v.value)
            queue.append(v)

    return visited

print(bfs_traverse(a))

"""
    ['Alice', 'Bob', 'Candy', 'Derek', 'Elaine', 'Fred', 'Helen', 'Gina', 'Irena']
"""
~~~


### 3. Flood Fill

Flood fill starts at one cell and recursively spreads to its four neighbors (north, east, south, west) as long as they match the target condition (here, a blank space) -- the same pattern as DFS, just applied to a 2D grid instead of a vertex graph. It's the algorithm behind a paint bucket tool: click one pixel and the fill spreads until it hits a border.

~~~py
A = [
    list('||||||||||||||||||||||||||||||'),
    list('||                  ||     |||'),
    list('||        |||||    ||     ||||'),
    list('||||||    ||  ||   ||   ||||||'),
    list('||||     ||    ||   ||     |||'),
    list('|||       ||||||      ||   |||'),
    list('||||||||            ||||||||||'),
    list('||||||||||||||||||||||||||||||'),
]

def flood_fill(A, start):
    y, x = start
    A[y][x] = '0'

    top, right, bottom, left = (y - 1, x), (y, x + 1), (y + 1, x), (y, x - 1)

    for ny, nx in (top, right, bottom, left):
        if A[ny][nx] == ' ':
            A[ny][nx] = '0'
            flood_fill(A, (ny, nx))  # recursive

    return A  # base case

flood_fill(A, (1, 4))
for row in A:
    print(''.join(row))

"""
    ||||||||||||||||||||||||||||||
    ||000000000000000000||     |||
    ||00000000|||||0000||     ||||
    ||||||0000||  ||000||   ||||||
    ||||00000||    ||000||     |||
    |||0000000||||||000000||   |||
    ||||||||000000000000||||||||||
    ||||||||||||||||||||||||||||||
"""
~~~
