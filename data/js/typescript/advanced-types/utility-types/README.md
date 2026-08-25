# JS / TypeScript / Advanced Types / Utility Types

## 1. Partial & Required

`Partial<T>` makes every property of `T` optional — handy for update
payloads; `Required<T>` does the opposite, forcing every optional
property to be present.

```typescript
interface User { name: string; age?: number; }
function updateUser(id: string, changes: Partial<User>) { /* ... */ }
```


## 2. Pick & Omit

`Pick<T, Keys>` builds a new type with only the listed properties;
`Omit<T, Keys>` builds one with everything except the listed
properties.

```typescript
type UserPreview = Pick<User, "name">;
type UserWithoutAge = Omit<User, "age">;
```


## 3. Record

`Record<Keys, Type>` builds an object type mapping every key in `Keys`
to `Type` — a concise way to type a dictionary/lookup object.

```typescript
type Roles = "admin" | "editor";
const labels: Record<Roles, string> = {
  admin: "Administrator", editor: "Editor",
};
```
