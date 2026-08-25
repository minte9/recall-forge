# Python / Python / Basics / Truthiness & Control Flow

## 1. Truthy & Falsy Values

Every object has a boolean value even without an explicit bool -- `0`, `0.0`, `""`, `[]`, `{}`, `set()`, and `None` are all falsy; virtually everything else is truthy. `if container:` is the idiomatic emptiness check, preferred over `if len(container) == 0:`.

```python
items = []
if items:            # falsy: empty list
    print("has items")
else:
    print("empty")    # this branch runs
```


## 2. Conditional (Ternary) Expressions

`value_if_true if condition else value_if_false` evaluates to one expression, useful for a simple either/or assignment without a full if/else block. Only reach for it when both branches are short -- nesting ternaries hurts readability fast.

```python
age = 15
status = "adult" if age >= 18 else "minor"
```


## 3. `match`-`case` Pattern Matching

Since Python 3.10, `match` compares a value against a series of patterns, including literals, types, and structural shapes (unpacking tuples/dicts directly in the pattern). It reads like a more powerful `switch`, and `case _:` acts as the default branch.

```python
match command.split():
    case ["go", direction]:
        move(direction)
    case ["look"]:
        describe_room()
    case _:
        print("unknown command")
```


## 4. `for`/`while`-`else`

The `else` clause on a loop runs only if the loop finished without hitting a `break` -- it's built for "search and report not-found" logic without a separate found-flag variable.

```python
for n in candidates:
    if is_prime(n):
        print(n, "is prime")
        break
else:
    print("no primes found")   # runs only if no break
```
