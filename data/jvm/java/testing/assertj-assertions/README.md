## JVM / Java / Testing / AssertJ Assertions

### 1. Fluent Assertions Basics

AssertJ's assertThat(actual) returns a fluent assertion object with readable, chainable methods like isEqualTo(), isTrue(), isNotNull() -- reading almost like English, and producing much more detailed failure messages than plain JUnit assertEquals(). It's a drop-in addition alongside JUnit's own assertions, not a replacement for the test framework itself.

~~~java
assertThat(result).isEqualTo(5);
assertThat(name).isNotNull().isEqualTo("Alice"); // chainable, reads naturally
~~~


### 2. Collection Assertions

AssertJ has dedicated collection assertions -- containsExactly() checks exact elements and order, contains() checks elements are present regardless of order, hasSize() checks the count, isEmpty() checks emptiness -- all far more expressive than manually comparing lists with assertEquals().

~~~java
assertThat(names).containsExactly("Ann", "Bob", "Cleo"); // exact elements, exact order
assertThat(names).contains("Bob").hasSize(3);            // order-independent checks
~~~


### 3. Exception Assertions

assertThatThrownBy(() -> code()) captures an exception thrown during a lambda and lets you assert on its type and message fluently, in one readable expression -- an alternative to JUnit's assertThrows() that reads more naturally when chained with further assertions on the exception itself.

~~~java
assertThatThrownBy(() -> orderService.checkout(-1))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("negative");
~~~
