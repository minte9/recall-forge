## DB / MongoDB / Query Language & Syntax / Find & Query Operators

### 1. find() & Query Operators

A query is a document describing what to match, not a `WHERE` clause --
`{ status: 'PAID' }` matches by equality, while comparison needs explicit
operators: `$gt`, `$lt`, `$in`, `$regex`. Multiple fields in one query
document are implicitly ANDed together; `$or`/`$and` combine conditions
explicitly.

~~~javascript
db.orders.find({ total: { $gt: 100 }, status: { $in: ['PAID', 'SHIPPED'] } })
~~~

### 2. Dot Notation & Nested Field Queries

A field inside a nested document or array is queried with dot notation
(`"address.city"`) as a string key, not a join -- MongoDB reaches into
the document structure directly. Querying an array field this way matches
if *any* element satisfies the condition, unless `$elemMatch` is used to
require the same array element to satisfy multiple conditions together.

~~~javascript
db.customers.find({ 'address.city': 'NYC' })
db.orders.find({ items: { $elemMatch: { sku: 'ABC', qty: { $gte: 2 } } } })
~~~
