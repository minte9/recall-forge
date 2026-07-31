## DB / MongoDB / Indexing / EXPLAIN & Query Optimization

### 1. explain() Execution Stats

`.explain('executionStats')` shows the winning plan MongoDB chose, plus
whatever rejected plans it considered and discarded -- `nReturned` vs
`totalDocsExamined` is the key ratio to watch, the same "estimated vs
actual" signal other engines' `EXPLAIN` output gives for a missing or
poorly matched index.

~~~javascript
db.orders.find({ status: 'PAID' }).explain('executionStats')
~~~

### 2. Covered Queries

A covered query is answered entirely from the index -- every field in
the query filter and the projection exists in the index, so MongoDB
never has to fetch the actual document. `explain()` reports this as
`totalDocsExamined: 0`, the fastest possible outcome for a query.

~~~javascript
db.orders.createIndex({ status: 1, total: 1 });
db.orders.find({ status: 'PAID' }, { total: 1, _id: 0 });  // covered
~~~
