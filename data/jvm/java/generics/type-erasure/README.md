## JVM / Java / Generics / Type Erasure

### 1. How Erasure Works

Java generics use type erasure: type parameters exist only in source code and are checked by the compiler, then erased from bytecode -- unbounded `T` becomes `Object`, a bounded `<T extends Number>` becomes `Number`. This preserved backward compatibility when generics were added in Java 5, but means generic type info doesn't exist at runtime.

~~~java
List<String> strings = new ArrayList<>();
List<Integer> integers = new ArrayList<>();

// At runtime, both share the exact same erased class
System.out.println(strings.getClass() == integers.getClass()); // true
~~~


### 2. Consequences: No Generic Arrays, No instanceof with Type Parameters

Because type info is erased: you can't create an array of a generic type (`new T[10]` fails to compile), since the JVM has no way to enforce it at runtime; and you can't `instanceof` a parameterized type (`obj instanceof List<String>`) -- only the raw `instanceof List` is allowed.

~~~java
Object obj = List.of("a", "b");

// if (obj instanceof List<String>) {} // compile error: illegal generic type for instanceof
if (obj instanceof List<?>) {           // only the wildcard form compiles
    System.out.println("It's some kind of List");
}
~~~


### 3. Bridge Methods

When a generic class overrides a method using a more specific erased signature, the compiler auto-generates a synthetic "bridge method" with the original erased signature that just delegates to the real one, preserving polymorphism after erasure. Invisible in source, but explains oddities seen via reflection.

~~~java
interface Container<T> {
    void set(T value); // erases to set(Object) in bytecode
}

class IntContainer implements Container<Integer> {
    public void set(Integer value) { /* real method */ }
    // compiler also generates a synthetic set(Object) bridge that casts and delegates here
}
~~~
