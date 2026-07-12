## JVM / Java / Basics / Equality

### 1. == for Primitives vs References

For primitives, == compares values directly. For reference types, == compares whether two variables point to the exact same object in memory -- not whether their contents are equal. Use equals() to compare object content; reserve == for reference types only when you specifically want identity comparison.

~~~java
int a = 5, b = 5;
System.out.println(a == b); // true -- same value

String s1 = new String("hi");
String s2 = new String("hi");
System.out.println(s1 == s2);      // false -- different objects
System.out.println(s1.equals(s2)); // true -- same content
~~~


### 2. Overriding equals() and hashCode() Together

If you override equals(), you must also override hashCode() -- objects considered equal must produce the same hash code, or they'll behave inconsistently in HashMap/HashSet (silently "lost" lookups). IDEs and Lombok's @EqualsAndHashCode generate both together for exactly this reason; never override just one.

~~~java
class Point {
    int x, y;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point p)) return false;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y); // must be consistent with equals()
    }
}
~~~


### 3. The equals() Contract (Reflexive, Symmetric, Transitive)

A correct equals() implementation must be reflexive (x.equals(x) is true), symmetric (x.equals(y) implies y.equals(x)), transitive (x.equals(y) and y.equals(z) implies x.equals(z)), and consistent (repeated calls return the same result). Breaking these silently corrupts behavior in collections -- e.g. comparing against a subclass often breaks symmetry.

~~~java
// Symmetry violation: Point.equals(ColorPoint) may be true,
// but ColorPoint.equals(Point) is false because it also checks color.
// The fix is usually to compare getClass() instead of using instanceof,
// or to favor composition over extending an equals()-bearing class.
~~~
