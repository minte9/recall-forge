# JVM / Java / OOP / Constructors

## 1. Multiple constructors

A class can define multiple constructors, each with a different parameter list. This is known as constructor overloading and allows objects to be initialized in different ways depending on the available information.

```java
package oop.constructors;

public class MultipleConstructors {
    public static void main(String[] args) {

        new A();        // Default constructor used
        new A("aaa");   // Constructor 1 used - aaa
        new A(100);     // Constructor 2 used - 100
    }
}

class A {

    // Default contructor (written explicitly for clarity)
    public A() {
        System.out.println("Default constructor used");
    }

    // Constructor 1
    public A(String s) {
        System.out.println("Constructor 1 used: " + s);
    }

    // Constructor 2
    public A(int n) {
        System.out.println("Constructor 2 used: " + n);
    }
}
```


## 2. This method

The this() constructor call invokes another constructor in the same class. It is commonly used to reuse initialization logic and avoid duplicating code. When used, it must be the first statement in the constructor.

```java
package oop.constructors;

public class ThisMethod {
    public static void main(String[] args) {
        
        Rectangle a = new Rectangle();
        Rectangle b = new Rectangle(100, 200);
        Rectangle c = new Rectangle(300, 400, 11, 22); // Look Here

        System.out.println(a);  // W: 0 H: 0 x: 0 y: 0
        System.out.println(b);  // W: 100 H: 200 x: 0 y: 0
        System.out.println(c);  // W: 300 H: 400 x: 11 y: 22
    }
    
}

class Rectangle {

    private int x, y;
    private int width, height;
    
    public Rectangle() {
        this(0, 0, 0, 0); // Look Here
    }
    
    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public Rectangle(int width, int height, int x, int y) {
        this(width, height);
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return String.format(
            "W: %s H: %s x: %s y: %s", width, height, x, y
        );
    }
}
```


## 3. Super method

The super() constructor call invokes a constructor from the parent class. It is used to initialize the inherited part of an object before the subclass performs its own initialization. Like this(), it must be the first statement in the constructor. 

```java
package oop.constructors;

public class SuperMethod {
    public static void main(String[] args) {
        new MainClass();  // Parent constructor called.
    }
}

class BaseClass {
    public BaseClass() {
        System.out.println("Parent constructor called.");
    }
}

class MainClass extends BaseClass {
    public MainClass() {  // Subclass controller
        super();  // Parent constructor called
    }

    /*
        // This will not (void is the first statement)
        public void MainClass(String s) {
           super(s);
        }
    */
}
```
