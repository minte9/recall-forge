## JVM / Java / Lambdas / Lambdas vs Anonymous Classes

### 1. Syntax and Readability Differences

An anonymous class needs the interface name, `new`, an `@Override`d method with full signature, and braces -- a lot of ceremony for one line of logic. A lambda skips all that since the compiler already knows the target method. For short behavior, lambdas read far better; long logic may still favor a named class.

~~~java
names.sort(new Comparator<String>() { // anonymous class: lots of ceremony
    public int compare(String a, String b) { return a.compareTo(b); }
});

names.sort((a, b) -> a.compareTo(b)); // equivalent lambda
names.sort(String::compareTo);        // equivalent method reference
~~~


### 2. this Binding Differences

An anonymous class is a real separate class -- `this` inside it refers to itself, and reaching the enclosing instance needs `Outer.this`. A lambda has no such distinction: `this` inside a lambda always means the enclosing instance directly, same as any other code in that method.

~~~java
class Example {
    String label = "outer";

    Runnable asAnonymous() {
        return new Runnable() {
            String label = "anonymous";
            public void run() {
                System.out.println(this.label);            // "anonymous"
                System.out.println(Example.this.label);    // "outer" -- needs qualification
            }
        };
    }

    Runnable asLambda() {
        return () -> System.out.println(this.label); // always the outer instance, "outer"
    }
}
~~~


### 3. Performance and Bytecode (invokedynamic)

A lambda isn't compiled like an anonymous class under the hood -- instead of generating a separate `.class` file per lambda, the compiler emits an `invokedynamic` call site, deferring class creation to runtime via `LambdaMetafactory`. This avoids bloating class-file counts, though for typical app code the difference rarely matters.

~~~java
// This lambda compiles to an invokedynamic call site backed by LambdaMetafactory,
// NOT a separate named .class file the way an anonymous class would generate
// (e.g. Example$1.class).
Supplier<String> greeting = () -> "Hello from a lambda";
~~~
