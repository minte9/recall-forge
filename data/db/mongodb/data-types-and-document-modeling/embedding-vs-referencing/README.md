## DB / MongoDB / Data Types & Document Modeling / Embedding vs Referencing

### 1. Embedding Related Data

MongoDB's default schema-design instinct is the opposite of a normalized
relational schema: embed related data as a subdocument or array inside
the parent document, so a single `find()` returns everything needed with
no join. This trades some duplication for read speed and atomic
single-document updates.

~~~javascript
db.orders.insertOne({
  customer: { name: 'Alice', email: 'alice@example.com' },  // embedded
  items: [{ sku: 'ABC', qty: 2 }]
})
~~~

### 2. Referencing with ObjectId

Referencing (storing another document's `_id` and looking it up
separately, or via `$lookup`) is the better fit when embedded data would
grow unbounded (a customer's entire order history), when the same data is
shared and updated independently across many parents, or when the
embedded side is accessed on its own far more often than through the
parent.

~~~javascript
db.orders.insertOne({ customerId: ObjectId('...'), items: [...] })
~~~
