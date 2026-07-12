## JVM / Java / Design Patterns / Singleton

### 1. Classic Singleton

The classic Singleton pattern hides its constructor behind private access and exposes a single shared instance through a static method, guaranteeing at most one instance of the class exists in the entire application. Useful for things that genuinely should be unique -- a configuration loader, a connection pool manager.

~~~java
class ConfigLoader {
    private static ConfigLoader instance;
    private ConfigLoader() {} // private: can't be constructed from outside

    public static ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }
}
~~~


### 2. Enum Singleton

The classic implementation above isn't thread-safe without extra synchronization -- two threads can both see instance == null and create two objects. An enum with a single constant sidesteps this entirely: the JVM guarantees enum instances are created exactly once, lazily, and thread-safely, with no synchronized keyword needed.

~~~java
enum ConfigLoader {
    INSTANCE;

    void loadConfig() { /* ... */ }
}

// Usage: ConfigLoader.INSTANCE.loadConfig();
// The JVM itself guarantees exactly one instance, thread-safely.
~~~


### 3. Singleton vs Spring Scope

In a Spring application, you rarely write a manual Singleton -- every bean is a singleton by default, managed by the container instead of a static field. See the dedicated Bean Scopes and Lifecycle topic in Spring / Framework for how Spring's singleton scope actually works.

~~~java
@Component // Spring manages this as a singleton automatically -- no static instance needed
class ConfigLoader {
    void loadConfig() { /* ... */ }
}
~~~
