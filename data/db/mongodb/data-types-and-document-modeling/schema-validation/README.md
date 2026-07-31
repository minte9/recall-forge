## DB / MongoDB / Data Types & Document Modeling / Schema Validation

### 1. Schemaless by Default

A MongoDB collection has no enforced structure by default -- two
documents in the same collection can have completely different fields
and types. This gives real flexibility for evolving data shapes, but it
also means nothing stops inconsistent or malformed documents from being
inserted unless the application (or an explicit validator) enforces
consistency itself.

~~~javascript
db.orders.insertOne({ total: 99.90 });
db.orders.insertOne({ total: 'ninety-nine' });  // allowed, no error
~~~

### 2. $jsonSchema Validation

A collection can opt into structural enforcement with a `$jsonSchema`
validator -- required fields, types, and constraints, checked on every
insert/update -- the closer MongoDB gets to a relational schema's
guarantees, applied only where it's actually needed instead of globally.

~~~javascript
db.createCollection('orders', {
  validator: { $jsonSchema: {
    required: ['total'],
    properties: { total: { bsonType: 'number', minimum: 0 } }
  } }
})
~~~
