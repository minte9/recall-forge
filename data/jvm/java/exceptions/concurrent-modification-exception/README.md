## JVM / Java / Exceptions / ConcurrentModificationException

### 1. Why It Happens

Modifying a collection (add/remove) directly while iterating over it with a for-each loop or Iterator throws ConcurrentModificationException -- despite the name, this has nothing to do with multithreading, it happens just as reliably on a single thread. The collection tracks a modification count that the iterator checks on every next() call.

~~~java
List<String> names = new ArrayList<>(List.of("Ann", "Bob", "Cleo"));

for (String name : names) {
    if (name.equals("Bob")) {
        names.remove(name); // throws ConcurrentModificationException on the next iteration
    }
}
~~~


### 2. Fail-Fast Iterators

Most JDK collection iterators are "fail-fast": they detect concurrent structural modification and throw immediately rather than risk returning corrupted or inconsistent results silently. This is a deliberate safety trade-off -- failing loudly and immediately is considered better than an iterator quietly skipping or repeating elements after a modification.

~~~java
// Fail-fast: throws immediately rather than silently producing wrong results
Iterator<String> it = names.iterator();
while (it.hasNext()) {
    String name = it.next();
    // names.remove(name); // would throw on the next it.next() call
}
~~~


### 3. Safe Removal During Iteration

Iterator.remove() is the correct way to remove elements while iterating -- it updates the iterator's own bookkeeping so no exception is thrown. Alternatively, Collection.removeIf(predicate) or building a separate list of items to remove and deleting them after the loop both sidestep the problem entirely.

~~~java
Iterator<String> it = names.iterator();
while (it.hasNext()) {
    if (it.next().equals("Bob")) {
        it.remove(); // safe -- removes through the iterator itself
    }
}

names.removeIf(name -> name.equals("Bob")); // even simpler, same safety
~~~
