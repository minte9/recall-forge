# API / REST / Persistence / Transactions

## 1. @Transactional

`@Transactional` wraps a method in a database transaction: all changes
commit together, or everything rolls back on a runtime exception. It's
proxy-based, so it only applies to public methods called from another
bean, not self-invocation.

```java
@Transactional
public Loan borrow(Long bookId, Long userId) {
    Book b = books.get(bookId);
    b.markOnLoan();                 // both changes commit together
    return loans.save(new Loan(b, userId));
}
```

## 2. Transaction Boundaries

Put the boundary on the service method that implements one use case, the
same unit as one REST operation. Not on the controller (HTTP concerns)
and not per repository call (partial writes on failure).

```
POST /loans  ->  LoanService.borrow()   <- one transaction
                   books.update + loans.insert
```

## 3. Read-Only and Rollback Rules

Mark queries `@Transactional(readOnly = true)` to skip dirty checking and
signal intent. Checked exceptions do not trigger rollback by default; add
`rollbackFor` if a service throws them.

```java
@Transactional(readOnly = true)
public List<Book> search(String q) { ... }

@Transactional(rollbackFor = IOException.class)
public void importCsv(InputStream in) throws IOException { ... }
```
