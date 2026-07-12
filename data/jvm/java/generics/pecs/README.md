## JVM / Java / Generics / The PECS Principle

### 1. Producer Extends

PECS -- "Producer Extends, Consumer Super" -- is Joshua Bloch's rule for picking a wildcard. When a type is only ever *read from* (it "produces" values), use `? extends T`: it accepts `T` or any subtype as a safe read-only source, at the cost of no adding (except `null`).

~~~java
static <T> void copyInto(List<? extends T> source, List<T> destination) {
    for (T item : source) destination.add(item); // source only produces, never mutated
}

List<Integer> integers = List.of(1, 2, 3);
List<Number> destination = new ArrayList<>();
copyInto(integers, destination); // List<Integer> as a producer of Number
~~~


### 2. Consumer Super

When a type is only ever *written to* (it "consumes" values), use `? super T`: it accepts `T` or any supertype as a safe destination to add into, at the cost of only reading elements back as `Object`. This keeps API signatures as flexible as possible for callers.

~~~java
static void addAll(List<? super Integer> destination, Integer... values) {
    for (Integer value : values) destination.add(value); // destination only consumes
}

List<Number> destination = new ArrayList<>();
addAll(destination, 1, 2, 3); // List<Number> as a consumer of Integer
~~~


### 3. Applying PECS to API Design

A type that's both read and written should use no wildcard -- just `T`. That's why `Collections.copy(List<? super T> dest, List<? extends T> src)` applies PECS to each parameter separately: `src` is a pure producer, `dest` a pure consumer -- as general as the signature can get.

~~~java
List<Integer> source = List.of(1, 2, 3);
List<Number> destination = new ArrayList<>(List.of(0, 0, 0));

Collections.copy(destination, source); // src=producer (extends), dest=consumer (super)
~~~
