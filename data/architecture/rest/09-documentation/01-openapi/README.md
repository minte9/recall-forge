# API / REST / Documentation / OpenAPI

## 1. The OpenAPI Specification

OpenAPI is a standard, machine-readable description of a REST API: its
paths, operations, parameters, bodies, responses and security. Tools
use one spec to generate docs, client SDKs, mocks and contract tests.

```yaml
openapi: 3.1.0
info: { title: Books API, version: 1.0.0 }
paths:
  /books/{id}:
    get: { summary: Get a book }
```

## 2. Describing Endpoints and Schemas

Each operation lists parameters and responses per status code; payload
shapes live once under `components/schemas` and are referenced with
`$ref`, so the same `Book` model is reused everywhere.

```yaml
/books/{id}:
  get:
    parameters:
      - { name: id, in: path, required: true, schema: {type: integer} }
    responses:
      "200": { content: { application/json:
                 { schema: { $ref: "#/components/schemas/Book" } } } }
      "404": { description: Book not found }
```

## 3. OpenAPI JSON/YAML

The same spec can be written as YAML (readable, hand-edited) or JSON
(generated, served by the app). Code-first projects generate it from the
controllers; design-first projects write it first and generate server
stubs.

```
code-first:   controllers -> springdoc -> /v3/api-docs (JSON)
design-first: books.yaml  -> openapi-generator -> interfaces + clients
```
