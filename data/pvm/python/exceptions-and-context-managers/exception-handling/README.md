## Python / Python / Exceptions & Context Managers / Exception Handling

### 1. try/except/else/finally

`except` catches matching errors; `else` runs only if no exception was raised; `finally` always runs, whether or not an exception occurred, and is the right place for cleanup that must happen regardless. Python checks `except` clauses top to bottom, so specific exceptions must come before general ones.

~~~python
try:
    result = risky_call()
except ValueError:
    print("bad value")
else:
    print("no error:", result)   # only if try succeeded
finally:
    print("always runs")          # cleanup, e.g. closing a file
~~~


### 2. `raise ... from` (Exception Chaining)

Raising a new exception inside an `except` block normally shows both errors as "during handling of the above, another occurred." `raise NewError(...) from original` makes that link explicit and intentional, showing a clean cause chain instead of an implied one.

~~~python
try:
    int("not a number")
except ValueError as e:
    raise RuntimeError("config parsing failed") from e
    # traceback shows: caused by the original ValueError
~~~


### 3. Custom Exceptions

Subclassing `Exception` creates a specific, catchable error type instead of overloading a generic one -- callers can catch just that type without accidentally swallowing unrelated errors. A custom `__init__` can attach extra context for the handler to use.

~~~python
class InsufficientFundsError(Exception):
    def __init__(self, balance, amount):
        super().__init__(f"need {amount}, have {balance}")
        self.balance = balance

if balance < amount:
    raise InsufficientFundsError(balance, amount)
~~~
