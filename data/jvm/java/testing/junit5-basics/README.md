## JVM / Java / Testing / JUnit 5 Basics

### 1. @Test and Assertions

@Test marks a method as a test case, run automatically by the JUnit platform. Assertions like assertEquals(expected, actual), assertTrue(condition), and assertThrows(Exception.class, ...) verify the outcome -- if any assertion fails, the test fails immediately and reports exactly which one, with the expected vs actual values.

~~~java
class CalculatorTest {
    @Test
    void addsTwoNumbers() {
        int result = new Calculator().add(2, 3);
        assertEquals(5, result); // fails with a clear "expected 5, was X" message
    }
}
~~~


### 2. @BeforeEach and @AfterEach

@BeforeEach runs before every test method in the class, typically to set up fresh test data or objects so tests don't leak state into each other; @AfterEach runs after every test, for cleanup like closing a resource. A new test instance is created per test method by default, so fields reset automatically too.

~~~java
class OrderServiceTest {
    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(); // fresh instance before every test
    }

    @Test
    void placesOrder() {
        assertTrue(service.placeOrder("item"));
    }
}
~~~


### 3. @BeforeAll and @AfterAll

@BeforeAll runs once before any test in the class, and @AfterAll runs once after all tests finish -- useful for expensive setup like starting a shared resource that all tests can reuse. Both must be static by default, since they run before a test instance even exists (unless the class-level lifecycle is changed to PER_CLASS).

~~~java
class DatabaseTest {
    @BeforeAll
    static void startDatabase() { // static: runs once, before any instance exists
        // start a shared test database connection
    }

    @AfterAll
    static void stopDatabase() {
        // clean up once, after all tests finish
    }
}
~~~
