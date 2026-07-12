## JVM / Java / Streams / Collectors

### 1. Collectors.toList/toSet/toMap

`Collectors.toList()`/`toSet()` gather elements into a List/Set; `toMap(keyFn, valueFn)` builds a Map from derived keys/values -- it throws `IllegalStateException` on a duplicate key unless a merge function is supplied. Since Java 16, `Stream.toList()` is a shorthand, though it returns an unmodifiable list.

~~~java
List<String> names = List.of("Ann", "Bob", "Ann", "Cleo");

Set<String> asSet = names.stream().collect(Collectors.toSet()); // dedupes via toSet()

Map<String, Integer> nameLengths = names.stream()
        .distinct()
        .collect(Collectors.toMap(name -> name, String::length)); // key -> length

System.out.println(nameLengths.get("Cleo")); // 4
~~~


### 2. Collectors.groupingBy

`groupingBy(classifier)` partitions elements into `Map<K, List<T>>` keyed by a classifier function -- e.g. grouping words by length. A downstream collector as a second argument changes what happens per group instead of collecting to a List -- `groupingBy(fn, counting())` for group sizes is a common combo.

~~~java
List<String> words = List.of("fig", "pear", "kiwi", "apple", "plum");

Map<Integer, List<String>> byLength = words.stream()
        .collect(Collectors.groupingBy(String::length));
        // {3=[fig], 4=[pear, kiwi, plum], 5=[apple]}

Map<Integer, Long> countByLength = words.stream()
        .collect(Collectors.groupingBy(String::length, Collectors.counting())); // group sizes
~~~


### 3. Collectors.joining and partitioningBy

`joining(delimiter, prefix, suffix)` concatenates a stream of strings, sparing the manual `StringBuilder` loop. `partitioningBy(predicate)` is a specialized `groupingBy` into exactly two groups (true/false), always returning both keys even if one group is empty -- a better fit than `groupingBy` for yes/no splits.

~~~java
List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);

Map<Boolean, List<Integer>> partitioned = numbers.stream()
        .collect(Collectors.partitioningBy(n -> n % 2 == 0));

System.out.println(partitioned.get(true));  // [2, 4, 6]
System.out.println(partitioned.get(false)); // [1, 3, 5]
~~~
