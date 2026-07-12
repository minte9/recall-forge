## JVM / Java / Testing / Test Doubles

### 1. Mock vs Stub vs Fake

A stub returns canned answers to calls but has no expectations about how it's used; a mock additionally verifies that specific interactions actually happened (see verify()); a fake is a lightweight working implementation (like an in-memory list standing in for a database) rather than a Mockito-generated object. All three replace a real dependency for different reasons.

~~~java
// Stub: just returns a value, nobody checks if it was called
when(repository.findById(1)).thenReturn(user);

// Mock: also verifies the interaction happened
verify(emailService).send(any());

// Fake: a real, simplified implementation, not a Mockito object
class InMemoryUserRepository implements UserRepository { /* ... */ }
~~~


### 2. When to Use Each

Reach for a stub when a test just needs a dependency to return something so the test can proceed; reach for a mock when the test's actual point is confirming a side-effecting call happened; reach for a fake when a real (if simplified) implementation is easier to reason about than dozens of stubbed calls.

~~~java
// Testing "checkout succeeds" -- stub is enough, don't need to verify anything
when(paymentGateway.charge(100)).thenReturn(true);

// Testing "checkout sends a confirmation email" -- the point IS the interaction, use a mock
verify(emailService).sendConfirmation(order);
~~~


### 3. Why Over-Mocking Hurts Tests

A test that mocks every single collaborator and asserts on dozens of verify() calls becomes tightly coupled to the implementation's internal structure -- refactoring the internals (even without changing behavior) breaks the test, even though nothing observably wrong happened. Prefer asserting on outcomes over verifying every internal call where possible.

~~~java
// Brittle: breaks if the internal call sequence changes, even if behavior is unchanged
verify(a).step1(); verify(b).step2(); verify(c).step3();

// More robust: asserts on the observable outcome instead
assertThat(orderService.checkout(100)).isTrue();
~~~
