# JS / Javascript / Testing / Jest Fundamentals

## 1. Writing Tests with Jest

Tests are grouped with `describe` and defined with `test`/`it`; each test
should set up state, call the code under test, and assert on the result.

```javascript
describe("add", () => {
  test("adds two numbers", () => {
    expect(add(2, 3)).toBe(5);
  });
});
```


## 2. Assertions & Matchers

`expect(value)` pairs with matchers like `.toBe` (strict equality),
`.toEqual` (deep equality for objects/arrays), and `.toThrow` (checks a
function throws).

```javascript
expect({ a: 1 }).toEqual({ a: 1 });    // deep equality
expect({ a: 1 }).not.toBe({ a: 1 });    // different references
expect(() => risky()).toThrow();
```


## 3. Mocking Functions

`jest.fn()` creates a mock you can pass as a callback and later assert on
— how many times it was called and with what arguments — without needing
the real implementation.

```javascript
const onSave = jest.fn();
triggerSave(onSave);
expect(onSave).toHaveBeenCalledWith("data");
```
