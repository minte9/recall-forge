# Security / Security / OWASP Top 10 / Injection Attacks

## 1. SQL Injection

SQL injection happens when untrusted input is concatenated directly into a query string, letting an attacker change the query's meaning -- a login field containing `' OR '1'='1` can turn a password check into something that's always true. Any place user input reaches the database through string concatenation is a potential injection point, no matter how "trusted" the input feels.

```java
// Vulnerable: input becomes part of the SQL itself
String sql = "SELECT * FROM users WHERE name = '" + name + "'";
stmt.executeQuery(sql);
```

## 2. Preventing Injection with Parameterized Queries

Parameterized queries send the SQL structure and the user's data separately -- the database treats the input strictly as a value, never as SQL syntax, so `' OR '1'='1` just becomes a literal string to search for instead of altering the query. This is the actual fix; escaping or blocklisting characters is a weaker, error-prone substitute.

```java
String sql = "SELECT * FROM users WHERE name = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, name); // treated as data, never as SQL
```
