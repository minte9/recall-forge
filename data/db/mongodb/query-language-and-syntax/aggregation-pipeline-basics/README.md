## DB / MongoDB / Query Language & Syntax / Aggregation Pipeline Basics

### 1. The Aggregation Pipeline

An aggregation is an array of stages, each transforming the documents
that flow through it -- `$match` filters, `$group` aggregates, `$project`
reshapes output -- composed in sequence like piping commands together,
rather than SQL's single declarative statement. Order matters: putting
`$match` first lets it use an index and reduces work for every stage
after it.

~~~javascript
db.orders.aggregate([
  { $match: { status: 'PAID' } },
  { $group: { _id: '$customerId', total: { $sum: '$amount' } } }
])
~~~

### 2. $lookup for Joins

`$lookup` performs a left outer join against another collection --
MongoDB's closest equivalent to SQL's `JOIN`, but it's used sparingly on
purpose: the document model favors embedding related data instead, and a
`$lookup` typically can't use an index on the foreign collection as
efficiently as embedding would avoid the join entirely.

~~~javascript
db.orders.aggregate([
  { $lookup: {
      from: 'customers', localField: 'customerId',
      foreignField: '_id', as: 'customer'
  } }
])
~~~
