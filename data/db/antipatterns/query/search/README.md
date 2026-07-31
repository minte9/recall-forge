## DB / Antipatterns / Query / Search

### 1. Chaining LIKE/OR for Multi-Keyword Search

Building a search feature out of `WHERE col LIKE '%a%' OR col LIKE '%b%'`
chains works for a demo, but it can't rank results by relevance, can't
handle stemming/synonyms, and each additional keyword makes the query
slower and uglier -- there's no notion of "how well" a row matched, only
whether it did.

~~~sql
WHERE description LIKE '%wireless%' OR description LIKE '%bluetooth%'
-- no ranking, no stemming, gets worse with every added keyword
~~~

### 2. When You Actually Need a Search Engine

Built-in full-text indexes (`tsvector`/`to_tsquery` in Postgres,
`FULLTEXT` + `MATCH ... AGAINST` in MySQL) add relevance ranking, stemming,
and stop-word handling without leaving the database. For search that needs
faceting, typo tolerance, or scale beyond one database, an external engine
(Elasticsearch, OpenSearch) is the next step up.

~~~sql
SELECT * FROM products
WHERE to_tsvector(description) @@ to_tsquery('wireless & bluetooth')
~~~
