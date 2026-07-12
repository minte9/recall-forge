## JVM / Java / Versions / Java 5

### 1. Generics

Java 5's biggest addition was generics -- type-safe parameterized classes like `List<String>` that eliminate manual casting. It reshaped the entire Collections Framework. See the dedicated Generics subcategory for the full breakdown (bounds, wildcards, PECS, erasure).

~~~java
List names = new ArrayList();           // pre-Java 5: raw type, unsafe cast needed
String first = (String) names.get(0);

List<String> typed = new ArrayList<>(); // Java 5+: type-safe, no cast
String second = typed.get(0);
~~~


### 2. Enhanced for-each Loop

The for-each loop simplified iterating over arrays and any `Iterable`, replacing manual index tracking or explicit `Iterator` calls. It became the default choice for read-only traversal, cutting out a common source of off-by-one bugs.

~~~java
List<String> fruits = List.of("Apple", "Banana", "Orange");

for (int i = 0; i < fruits.size(); i++) {  // before: manual index
    System.out.println(fruits.get(i));
}

for (String fruit : fruits) {              // for-each: no index, no iterator calls
    System.out.println(fruit);
}
~~~


### 3. Enums and Annotations

Java 5 replaced int-constant hacks with real enum types -- type-safe constants that can carry fields and methods. It also introduced annotations, letting tools and frameworks read structured metadata via `@`, like `@Override` catching signature typos at compile time.

~~~java
enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    boolean isWeekend() { // enums can carry methods, unlike int constants
        return this == SATURDAY || this == SUNDAY;
    }
}

class Dog {
    @Override // compiler now verifies this actually overrides a superclass method
    public String toString() {
        return "Dog";
    }
}
~~~
