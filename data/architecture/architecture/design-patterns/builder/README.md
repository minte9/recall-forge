## Architecture / Architecture / Design Patterns / Builder

### 1. Telescoping Constructors

When a class has many optional fields, providing a constructor overload for every combination ("telescoping constructors") becomes unreadable fast -- callers can't tell which positional argument means what, and adding one more optional field multiplies the overload count further.

~~~java
class Pizza {
    Pizza(int size) {}
    Pizza(int size, boolean cheese) {}
    Pizza(int size, boolean cheese, boolean pepperoni) {} // and so on...
}

new Pizza(12, true, false); // which boolean means what? unreadable at the call site
~~~


### 2. Classic Builder

The Builder pattern replaces telescoping constructors with a separate object that accumulates settings through chained, named method calls, then produces the final immutable object with a single build() call. Every setting is self-documenting at the call site, and optional fields can simply be skipped.

~~~java
class Pizza {
    private final int size;
    private final boolean cheese;

    private Pizza(Builder b) { this.size = b.size; this.cheese = b.cheese; }

    static class Builder {
        private int size;
        private boolean cheese;

        Builder size(int size) { this.size = size; return this; }
        Builder cheese(boolean cheese) { this.cheese = cheese; return this; }
        Pizza build() { return new Pizza(this); }
    }
}

Pizza pizza = new Pizza.Builder().size(12).cheese(true).build(); // self-documenting
~~~


### 3. Fluent Builders in the JDK

The JDK itself is full of Builder-pattern APIs: StringBuilder accumulates characters through chained append() calls, and HttpRequest.newBuilder() configures an HTTP request the same way. Recognizing this shape -- chained calls returning this, ending in a terminal build()/toString() -- means you already know how to use any new fluent API.

~~~java
String s = new StringBuilder().append("Hello").append(", ").append("World").toString();

HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.example.com"))
        .GET()
        .build(); // same fluent-builder shape as StringBuilder
~~~
