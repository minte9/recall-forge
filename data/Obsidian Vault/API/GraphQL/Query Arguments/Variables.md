## 1. Arguments vs Variables

Until now, we passed arguments directly inside the query:

```GraphQL
query GetUser {
    user(id: "USER-123") {  # hardcoded
        name
        email
    }
}
```

GraphQL variables let us separate the query structure from the actual values:

```GraphQL
query GetUser($id: ID!) {
    user(id: $id) {
        name
        email
    }
}
```

This is the normal approach when arguments come from user input, application state, IDs, filters, and other dynamic values.

```sh
query GetUser($id: ID!)
│     │       │    │
│     │       │    └── variable type
│     │       │
│     │       └── variable name
│     │
│     └── operation name
│
└── operation type
```

GraphQL variable names begin with `$`.
Notice the two different `id`s:

```GraphQL
user(id: $id)
     │    │
     │    └── variable named $id
     │
     └── argument named id
```

They do not technically need to have the same name. 
For example, this is valid: 

```graphql
query GetUser($userId: ID!) {
    user(id: $userId) {
        name
    }
}
```

Here:

```
$userId
   │
   └── client variable

id
│
└── schema argument
```

The variable is simply being passed into the argument.

## 2. Sending Variables

GraphQL variables are normally sent separately from the query in the HTTP JSON body.  
Instead of only: 

```json
{
  "query": "query GetUser { user(id: \"USER-123\") { name } }"
}
```

We send:  

```GraphQL
{
  "query": "query GetUser($id: ID!) { user(id: $id) { name } }",
  "variables": {
    "id": "USER-123"
  }
}
```

The query became:

```sh
curl -X POST http://localhost:9191/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query GetUser($id: ID!) { user(id: $id) { id name email age } }",
    "variables": {
      "id": "USER-123"
    }
  }' | jq
```

Response:

```json
{
  "data": {
    "user": {
      "id": "USER-123",
      "name": "John Smith",
      "email": "john@example.com",
      "age": 40
    }
  }
}
```
