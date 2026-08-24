## Security / Security / Secure Coding Practices / Secrets Management

### 1. Hardcoded Secrets

A secret committed to source control is compromised the moment it's pushed, permanently -- even deleting it in a later commit leaves it in git history, readable by anyone who clones the repo or finds it through a leaked mirror. This is why secret scanning in CI exists: catching it before the commit ever reaches a shared branch.

~~~java
// Never do this -- lives forever in git history
static final String DB_PASSWORD = "hunter2";
~~~

### 2. Externalized Secrets

Externalized secrets live outside the codebase entirely -- environment variables, a secrets manager, or a vault service -- so they can be rotated, scoped per environment, and access-controlled independently of who has read access to the repository.

~~~java
String dbPassword = System.getenv("DB_PASSWORD");
// rotated independently, never touches source control
~~~
