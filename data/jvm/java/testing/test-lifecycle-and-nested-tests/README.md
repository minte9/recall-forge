## JVM / Java / Testing / Test Lifecycle and Nested Tests

### 1. PER_METHOD vs PER_CLASS Lifecycle

By default (PER_METHOD), JUnit creates a brand-new test instance for every test method, so instance fields never leak between tests. @TestInstance(Lifecycle.PER_CLASS) reuses a single instance for the whole class instead -- required if you want non-static @BeforeAll/@AfterAll methods, but means instance fields must be reset manually in @BeforeEach.

~~~java
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // one shared instance for the whole class
class ExpensiveSetupTest {
    @BeforeAll
    void setUp() { /* non-static now allowed */ }
}
~~~


### 2. @Nested Test Classes

@Nested groups related test methods into an inner class, producing more readable test output (nested class names describe a scenario, like "when the cart is empty") and letting each nested group have its own @BeforeEach setup specific to that scenario, without duplicating unrelated setup across the whole file.

~~~java
class ShoppingCartTest {
    @Nested
    class WhenCartIsEmpty {
        @Test
        void totalIsZero() { /* ... */ }
    }

    @Nested
    class WhenCartHasItems {
        @Test
        void totalMatchesSum() { /* ... */ }
    }
}
~~~


### 3. Test Execution Order

Test methods run in a nondeterministic order by default -- intentionally, so tests can never accidentally depend on execution order. @TestMethodOrder can force a specific order (alphabetical, @Order-annotated) when genuinely needed, but relying on order is usually a sign the tests aren't properly independent of each other.

~~~java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderedTest {
    @Test @Order(1) void first() { }
    @Test @Order(2) void second() { } // runs after first(), explicitly
}
~~~
