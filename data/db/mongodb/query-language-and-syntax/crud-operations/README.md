# DB / MongoDB / Query Language & Syntax / CRUD Operations

## 1. insertOne/insertMany & Document _id

Every document needs a unique `_id` -- if one isn't supplied, MongoDB
generates an `ObjectId` automatically. `insertOne` and `insertMany` both
return the generated ids in their result, so there's no separate
`RETURNING id`/`LAST_INSERT_ID()` step needed.

```javascript
db.orders.insertOne({ customerId: 1, total: 99.90 })
// result.insertedId holds the generated ObjectId
```

## 2. updateOne/updateMany & Update Operators

An update needs an explicit operator describing *how* to change a field
-- `$set` replaces a value, `$inc` increments a number, `$push` appends
to an array -- unlike SQL's plain `column = value` assignment. Omitting
an operator (`updateOne(filter, { total: 100 })`) is an error in modern
driver versions specifically to prevent accidentally replacing the whole
document.

```javascript
db.orders.updateOne(
  { _id: orderId },
  { $set: { status: 'SHIPPED' }, $inc: { version: 1 } }
)
```
