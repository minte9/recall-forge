# Security / Security / Authentication & Authorization / Authorization Models

## 1. Role-Based Access Control (RBAC)

RBAC grants permissions to roles, then assigns roles to users -- an `ADMIN` role might grant delete access, an `EDITOR` role edit-only. It's simple to reason about and audit, but it gets coarse-grained fast: if two admins need slightly different permissions, RBAC pushes you toward creating more and more roles.

```java
@PreAuthorize("hasRole('ADMIN')")
void deleteUser(Long id) { ... }
```

## 2. Attribute-Based Access Control (ABAC)

ABAC evaluates a policy against attributes of the user, the resource, and the context at request time -- "an editor can edit a document only if they're in the same department as its owner and it's not locked." It's far more flexible than RBAC's fixed role list, at the cost of policies that are harder to audit at a glance.

```java
boolean allowed = user.getDepartment().equals(doc.getDepartment())
    && !doc.isLocked();
```
