# JVM / Java / Collections / Lists

## 1. List Basics

`List` is an ordered collection that allows duplicates and preserves
insertion order. `ArrayList` is the go-to implementation, backed by a
resizable array (unlike arrays, it stores objects, not primitives).
Index access is O(1); insert/remove at the end is O(1) amortized, but
O(n) in the middle since later elements shift.

```java
List<String> a = new ArrayList<>();
a.add("x"); a.add("y"); a.add("y");  // duplicates allowed
a.remove("y");                       // remove by value
a.remove(0);                         // remove by index
System.out.println(a);               // [y]
System.out.println(a.contains("y")); // true
System.out.println(a.indexOf("y"));  // 0
```

## 2. ArrayList vs LinkedList

Both preserve order and allow duplicates, but the internal structure
differs. `ArrayList` uses a backing array, so `get(i)` is O(1) but
inserting/removing in the middle shifts elements. `LinkedList` uses
doubly-linked nodes, so middle insert/remove is O(1) once you're at
the node, but `get(i)` must walk from the ends — O(n).

```java
List<String> arr = new ArrayList<>(List.of("A", "B"));
List<String> lnk = new LinkedList<>(List.of("A", "B"));

arr.get(1);      // fast: direct array index
lnk.get(1);      // slower: traverses nodes

arr.add(1, "X"); // slower: shifts B and beyond
lnk.add(1, "X"); // faster: just relinks neighbors
```

## 3. Sorting a List

`Collections.sort` (or the newer `list.sort`) orders elements by their
natural order, which custom types define via `Comparable`. Pass a
`Comparator` instead to sort by something else, or to reverse order,
without touching the class.

```java
List<String> names = new ArrayList<>(List.of("Bea", "Al", "Cy"));
Collections.sort(names);               // [Al, Bea, Cy] - natural order
names.sort(Comparator.reverseOrder());  // [Cy, Bea, Al]

// Comparable: type defines its own order
class Song implements Comparable<Song> {
    String title;
    public int compareTo(Song o) { return title.compareTo(o.title); }
}
```

## 4. Fixed-Size vs Immutable Lists

`Arrays.asList(arr)` returns a fixed-size *view* backed by `arr` —
`set()` writes through to the array, but `add()`/`remove()` throw.
`List.of(...)` (Java 9+) is truly immutable — every mutating call
throws. Wrap either in `new ArrayList<>(...)` to get an independent,
resizable copy.

```java
String[] raw = {"A", "B"};
List<String> view = Arrays.asList(raw);
view.set(0, "Z");             // OK, raw[0] is now "Z"
// view.add("C");              // UnsupportedOperationException

List<String> immutable = List.of("A", "B");
// immutable.set(0, "Z");      // UnsupportedOperationException

List<String> copy = new ArrayList<>(immutable);
copy.add("C");                 // OK, independent resizable copy
```
