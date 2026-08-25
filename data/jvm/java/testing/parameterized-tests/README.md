# JVM / Java / Testing / Parameterized Tests

## 1. @ParameterizedTest Basics

@ParameterizedTest runs the same test method multiple times with different arguments, replacing several near-identical @Test methods that only differ in input/expected values. It must be paired with a source annotation (@ValueSource, @CsvSource, @MethodSource) that supplies the actual argument values for each run.

```java
@ParameterizedTest
@ValueSource(ints = {1, 2, 3, 4})
void isPositive(int number) {
    assertTrue(number > 0); // runs 4 times, once per value
}
```


## 2. @ValueSource and @CsvSource

@ValueSource supplies a single simple argument per run (ints, strings, etc.); @CsvSource supplies multiple arguments per run as comma-separated values, one line per test invocation -- useful when a test needs an input paired with its expected output.

```java
@ParameterizedTest
@CsvSource({
    "2, 3, 5",
    "10, -5, 5",
    "0, 0, 0"
})
void addsCorrectly(int a, int b, int expected) {
    assertEquals(expected, new Calculator().add(a, b));
}
```


## 3. @MethodSource for Complex Cases

@MethodSource points to a static method returning a Stream (or List) of arguments, used when test data is too complex for @CsvSource's flat strings -- constructing actual objects, or reusing the same data across several test methods.

```java
@ParameterizedTest
@MethodSource("orderScenarios")
void processesOrder(Order order, boolean expectedValid) {
    assertEquals(expectedValid, validator.isValid(order));
}

static Stream<Arguments> orderScenarios() {
    return Stream.of(
        Arguments.of(new Order(100), true),
        Arguments.of(new Order(-1), false)
    );
}
```
