## JVM / Java / Design Patterns / Strategy

### 1. Strategy Basics

The Strategy pattern extracts an algorithm into its own interface, so different implementations can be swapped in at runtime without changing the code that uses them. A class holds a reference to the strategy interface and delegates to whichever implementation was injected, rather than hardcoding one specific algorithm.

~~~java
interface DiscountStrategy { double apply(double price); }

class NoDiscount implements DiscountStrategy { public double apply(double price) { return price; } }
class TenPercentOff implements DiscountStrategy { public double apply(double price) { return price * 0.9; } }

class Checkout {
    private final DiscountStrategy discount;
    Checkout(DiscountStrategy discount) { this.discount = discount; } // swappable algorithm
}
~~~


### 2. Strategy as a Lambda

Since Strategy is just a single-method interface plugged in from outside, it's a textbook fit for a lambda instead of a named class -- Comparator, Runnable, and most of java.util.function are the Strategy pattern under a different name. See the dedicated Lambdas subcategory for the syntax itself.

~~~java
List<String> names = new ArrayList<>(List.of("Cleo", "Ann", "Bob"));

names.sort((a, b) -> a.compareTo(b)); // Comparator IS a Strategy, passed as a lambda
~~~


### 3. Strategy vs if/else Chains

Without Strategy, varying behavior is usually handled with a growing if/else or switch chain inside one method, which must be edited every time a new variant is added. Strategy inverts this: adding a new algorithm means adding a new implementation, with zero changes to existing code (the Open-Closed Principle in practice).

~~~java
// Without Strategy: every new discount type means editing this method
double applyDiscount(String type, double price) {
    if (type.equals("none")) return price;
    if (type.equals("ten")) return price * 0.9;
    // adding a new type means editing this method again
    return price;
}
~~~
