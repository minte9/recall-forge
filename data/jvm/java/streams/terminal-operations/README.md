## JVM / Java / Streams / Terminal Operations

### 1. collect and forEach

Terminal operations actually trigger a pipeline to run -- without one, prior `filter`/`map`/`sorted` calls are just an unexecuted plan. `collect(Collector)` is the most flexible, building a List/Set/Map/String via `Collectors`. `forEach(Consumer)` just performs a side effect per element and returns nothing.

~~~java
List<String> names = List.of("Ann", "Bob", "Cleo");

names.stream()
        .map(String::toUpperCase)
        .forEach(System.out::println); // side effect only, returns nothing

String joined = names.stream()
        .collect(Collectors.joining(", ", "[", "]")); // [Ann, Bob, Cleo]
~~~


### 2. reduce

`reduce` folds all elements into one result via a `BinaryOperator`. The one-arg overload returns `Optional<T>` (empty stream = no result); the two-arg overload takes an identity value and returns `T` directly. Built-ins like `sum()`/`max()` on primitive streams are conceptually specialized reduces.

~~~java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);

int sum = numbers.stream().reduce(0, Integer::sum); // identity = 0, returns int directly
System.out.println(sum); // 15

Optional<Integer> max = numbers.stream().reduce(Integer::max); // no identity -> Optional
System.out.println(max.orElse(0)); // 5
~~~


### 3. anyMatch, findFirst, and count

`anyMatch`/`allMatch`/`noneMatch` short-circuit -- they stop as soon as the answer is known, which matters on large streams. `findFirst()` also short-circuits, returning `Optional<T>`. `count()` generally can't short-circuit since it must see every element, though an unfiltered `count()` can sometimes read the source's known size instead.

~~~java
List<Integer> numbers = List.of(2, 4, 6, 7, 8);

boolean hasOdd = numbers.stream().anyMatch(n -> n % 2 != 0); // true

Optional<Integer> firstOdd = numbers.stream()
        .filter(n -> n % 2 != 0)
        .findFirst(); // stops at the first match, doesn't scan the rest

long count = numbers.stream().filter(n -> n > 4).count(); // 3
~~~
