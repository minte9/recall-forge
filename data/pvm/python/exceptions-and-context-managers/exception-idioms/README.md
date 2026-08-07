## Python / Python / Exceptions & Context Managers / Exception Idioms

### 1. EAFP vs LBYL

EAFP ("easier to ask forgiveness than permission") tries the operation and handles the exception if it fails; LBYL ("look before you leap") checks preconditions first. Python favors EAFP -- it avoids a race between the check and the action, and skips the check entirely in the success case.

~~~python
# LBYL
if "key" in d:
    value = d["key"]

# EAFP -- idiomatic in Python
try:
    value = d["key"]
except KeyError:
    value = None
~~~


### 2. Catching Specific Exceptions

A bare `except:` (or `except Exception:`) catches everything, including bugs it shouldn't hide -- a typo'd variable name (`NameError`) gets silently swallowed along with the error you meant to handle. Catching the narrowest exception type keeps unrelated failures visible.

~~~python
try:
    value = int(user_input)
except ValueError:          # only catches what's expected
    value = 0
# except Exception: value = 0   # would also hide real bugs
~~~
