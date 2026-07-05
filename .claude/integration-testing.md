# Integration Testing Guide

Goal:
Verify the interaction between Spring components.

Use integration tests when:
- Testing REST endpoints
- Repository queries
- Spring Security
- Transaction boundaries
- Markdown upload flow
- Complete review flow

Rules

- Prefer existing Spring Boot test annotations.
- Load only the required Spring context.
- Use Testcontainers only if already part of the project.
- Avoid mocking Spring-managed beans unless required.
- Test real component interactions.
- Verify both success and failure scenarios.
- Keep tests independent.

Before writing tests:
- Explain what integration is being verified.
- List the Spring components involved.

After writing:
- Run only the affected integration tests.
- Report execution result.