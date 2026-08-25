# JVM / Java / Testing / Mockito Basics

## 1. @Mock and @InjectMocks

@Mock creates a fake implementation of a dependency with no real behavior until stubbed; @InjectMocks creates the real object under test and automatically wires the @Mock fields into its constructor or setters. Together with @ExtendWith(MockitoExtension.class), this replaces manually calling Mockito.mock() and constructing objects by hand.

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock PaymentGateway paymentGateway; // fake, no real payment processing

    @InjectMocks OrderService orderService; // real object, paymentGateway injected automatically
}
```


## 2. Stubbing with when/thenReturn

when(mock.method(args)).thenReturn(value) defines what a mock returns for a specific call, so the code under test behaves as if the real dependency returned that value, without any real logic running. thenThrow(exception) stubs a mock to throw instead, for testing error-handling paths without triggering the real failure.

```java
when(paymentGateway.charge(100)).thenReturn(true); // stub the mock's behavior

boolean success = orderService.checkout(100);
assertTrue(success); // relies on the stubbed charge() result, not a real payment
```


## 3. Verifying Interactions with verify()

verify(mock).method(args) checks that a specific call actually happened on the mock, which matters when a method's job is a side effect (like sending an email) rather than returning a value to assert on. verify(mock, never()) and verify(mock, times(n)) check that a call didn't happen, or happened exactly n times.

```java
orderService.checkout(100);

verify(paymentGateway).charge(100);          // confirms charge() was actually called
verify(emailService, never()).sendReceipt(); // confirms it was NOT called, e.g. before shipping
```
