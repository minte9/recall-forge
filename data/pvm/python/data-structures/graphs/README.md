## PVM / Python / Data Structures / Graphs

### 1. Graph Basics

A graph represents relationships as vertices connected by edges, with no single "top" or "front" like a stack or queue has -- any vertex can connect to any number of others. Real-world examples include social networks and flight routes.

~~~py
friends = {
    'Alice': ['Bob', 'Diana'],
    'Bob': ['Alice', 'Cynthia'],
    'Cynthia': ['Bob'],
    'Diana': ['Alice'],
}
print(friends['Alice'])  # ['Bob', 'Diana']
~~~


### 2. Directed Graphs

Relationships aren't always mutual -- a directed graph's edges point one way, which is how a "follows" relationship on social media works: Alice can follow Bob without Bob following her back.

~~~py
followees = {'Alice': ['Bob'], 'Bob': ['Cynthia'], 'Cynthia': ['Bob']}

assert 'Bob' in followees['Alice']
assert 'Alice' not in followees['Bob']  # one-way
~~~


### 3. Object-Oriented Graphs

A dict-of-lists works for quick scripts, but a `Vertex` class scales better once nodes need their own behavior -- `add_adjacent` keeps both sides of an undirected edge in sync instead of updating two dicts by hand.

~~~py
class Vertex:
    def __init__(self, name):
        self.value = name
        self.adjacent = []

    def add_adjacent(self, vertex):
        self.adjacent.append(vertex)
        if self not in vertex.adjacent:
            vertex.adjacent.append(self)  # keep both sides in sync

a, b = Vertex("Alice"), Vertex("Bob")
a.add_adjacent(b)
print([v.value for v in a.adjacent])  # ['Bob']
~~~


### 4. Weighted Graphs

When a connection has a cost -- distance, price, time -- store it alongside the edge instead of just the neighbor, e.g. a dict mapping neighbor to weight instead of a plain list.

~~~py
class WeightedVertex:
    def __init__(self, name):
        self.value = name
        self.adjacent = {}

    def add_adjacent(self, vertex, weight):
        self.adjacent[vertex.value] = weight

dallas, toronto = WeightedVertex('Dallas'), WeightedVertex('Toronto')
dallas.add_adjacent(toronto, 138)
print(dallas.adjacent)  # {'Toronto': 138}
~~~
