## JVM / Java / Generics / Wildcards

### 1. Unbounded Wildcards

A wildcard `?` represents an unknown type, used where a generic type is *referenced*, not declared -- it can't define a new generic class/method. `List<?>` means "some type, we don't know which" -- fine for reading elements generically, but only `null` can be added since the real type is unknown.

~~~java
static void printAll(List<?> list) {
    for (Object item : list) System.out.println(item); // read-only is always safe
}

printAll(List.of("a", "b"));
printAll(List.of(1, 2, 3));
// list.add("x"); // compile error: cannot add to a List<?>
~~~


### 2. Upper Bounded Wildcards (? extends)

`List<? extends Number>` means "some type that's Number or a subtype" -- accepting `List<Integer>`, `List<Double>`, or `List<Number>` as a read-only source. Adding elements isn't allowed (the compiler can't guarantee the exact subtype), but reading is always safe, since anything in there is-a `Number`.

~~~java
static double sum(List<? extends Number> numbers) {
    double total = 0;
    for (Number n : numbers) total += n.doubleValue(); // safe: reading only
    return total;
    // numbers.add(1); // compile error
}

sum(List.of(1, 2, 3));   // works with List<Integer>
sum(List.of(1.5, 2.5));  // works with List<Double>
~~~


### 3. Lower Bounded Wildcards (? super)

`List<? super Integer>` means "some type that's Integer or a supertype" -- accepting `List<Integer>`, `List<Number>`, or `List<Object>` as a safe place to add `Integer`s. Reading is less useful here, since elements are only guaranteed to be at least an `Object`.

~~~java
static void addNumbers(List<? super Integer> list) {
    list.add(1); // safe: Integer is-a Integer, Number, and Object
    list.add(2);
}

List<Number> numbers = new ArrayList<>();
addNumbers(numbers); // works
~~~
