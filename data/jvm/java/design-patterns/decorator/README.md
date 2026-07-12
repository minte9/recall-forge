## JVM / Java / Design Patterns / Decorator

### 1. Decorator Basics

The Decorator pattern wraps an object in another object implementing the same interface, adding behavior before or after delegating to the wrapped object. Decorators can be stacked, so features can be composed at runtime by wrapping in different combinations, instead of needing a subclass for every combination of features.

~~~java
interface Coffee { double cost(); }

class SimpleCoffee implements Coffee { public double cost() { return 2.0; } }

class MilkDecorator implements Coffee {
    private final Coffee inner;
    MilkDecorator(Coffee inner) { this.inner = inner; }
    public double cost() { return inner.cost() + 0.5; } // adds behavior, then delegates
}

Coffee order = new MilkDecorator(new SimpleCoffee()); // 2.5
~~~


### 2. Java I/O Streams as Decorator

java.io is the canonical real-world example of Decorator: BufferedReader wraps a Reader to add buffering, and each wrapper implements the same base type it wraps, so decorators stack arbitrarily deep.

~~~java
Reader reader = new BufferedReader(       // adds buffering
        new InputStreamReader(            // adds character decoding
                new FileInputStream("file.txt"))); // raw bytes
~~~


### 3. Decorator vs Inheritance

Subclassing to add every combination of optional behavior leads to a combinatorial explosion of subclasses (BufferedInputStream, BufferedInputStreamWithLineNumbers, ...). Decorator solves this by composing behavior at runtime through wrapping instead of at compile time through subclassing, so any combination of features can be assembled without a dedicated class for each one.

~~~java
// Inheritance explosion: a new subclass for every combination
// class BufferedFileReader, class BufferedLineNumberFileReader, ...

// Decorator: compose combinations at runtime instead
Reader r = new LineNumberReader(new BufferedReader(new FileReader("file.txt")));
~~~
