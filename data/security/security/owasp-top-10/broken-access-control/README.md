## Security / Security / OWASP Top 10 / Broken Access Control

### 1. Broken Access Control

Broken access control happens when an application checks that a user is authenticated but doesn't verify they're authorized for the specific resource they're requesting -- any logged-in user can act on any other user's data just by knowing or guessing an ID. It's consistently OWASP's #1 risk because it's easy to introduce and easy to miss in testing.

~~~java
// Broken: no check that the invoice belongs to this user
@GetMapping("/invoices/{id}")
Invoice get(@PathVariable Long id) {
    return invoiceRepository.findById(id).orElseThrow();
}
~~~

### 2. Insecure Direct Object References

An Insecure Direct Object Reference (IDOR) is the specific pattern of exposing an internal ID, like a database primary key, directly in a URL without checking that the current user actually owns that object -- incrementing `/invoices/42` to `/invoices/43` returns someone else's invoice. The fix is an explicit ownership check on every access, not just an authentication check.

~~~java
@GetMapping("/invoices/{id}")
Invoice get(@PathVariable Long id, Principal user) {
    Invoice invoice = invoiceRepository.findById(id).orElseThrow();
    if (!invoice.getOwnerId().equals(currentUserId(user))) {
        throw new AccessDeniedException("Not your invoice");
    }
    return invoice;
}
~~~
