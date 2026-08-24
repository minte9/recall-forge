## Security / Security / Secure Coding Practices / Least Privilege and Defense in Depth

### 1. Principle of Least Privilege

Least privilege means every component -- a user, a service, a database credential -- gets only the permissions it needs to do its job, nothing more. A reporting service that only reads data should use a read-only database user; if it's ever compromised, the blast radius is limited to what that narrow permission set allows.

~~~sql
-- Reporting service: read-only credential, can't write or drop
GRANT SELECT ON orders TO reporting_user;
~~~

### 2. Defense in Depth

Defense in depth assumes any single control can fail, so it layers multiple independent defenses -- input validation, parameterized queries, and least-privilege database access all guarding against the same injection risk from different angles. If one layer is bypassed, the next one still has a chance to stop the attack.

~~~java
// Three independent layers against the same risk:
validateInput(name);              // 1. validation
stmt.setString(1, name);          // 2. parameterized query
// 3. DB user has no DROP/DELETE privilege at all
~~~
