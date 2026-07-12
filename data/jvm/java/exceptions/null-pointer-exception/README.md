## JVM / Java / Exceptions / NullPointerException

### 1. Common Causes

NullPointerException happens when code calls a method, accesses a field, or indexes an array on a reference that's null -- calling a method on the result of a lookup that returned nothing (a Map miss, a failed find), forgetting to initialize a field, or an unboxed null wrapper (see the Basics subcategory) are the usual culprits.

~~~java
Map<String, String> users = new HashMap<>();
String name = users.get("missing"); // returns null, not an exception yet
System.out.println(name.length()); // NullPointerException -- calling a method on null
~~~


### 2. Helpful NPE Messages

Since Java 14, NullPointerException messages describe exactly which variable or method call was null (e.g. "Cannot invoke String.length() because name is null"), instead of just a bare stack trace. This is enabled by default in modern JDKs and turns most NPEs into a one-glance diagnosis instead of a guessing game.

~~~java
// Java 14+ message, enabled by default:
// Exception: Cannot invoke "String.length()" because "name" is null
//     at Main.main(Main.java:5)
~~~


### 3. Avoiding NPEs

Objects.requireNonNull(value, "message") fails fast with a clear error at the point a null was received, instead of letting it propagate somewhere confusing later. Optional (see Versions / Java 8) makes the possible absence of a value explicit in a method's return type, so callers can't forget to check.

~~~java
void setName(String name) {
    this.name = Objects.requireNonNull(name, "name must not be null"); // fails at the source
}
~~~
