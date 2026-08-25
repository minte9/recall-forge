# DB / MongoDB / Indexing / Text & Geospatial Indexes

## 1. Text Indexes

A text index enables basic full-text search across string fields --
stemming and stop-word removal built in, scored by relevance via
`$meta: 'textScore'`. It's a genuinely useful built-in feature, but not a
replacement for a dedicated search engine (Elasticsearch) once ranking
sophistication or scale requirements grow past what it offers.

```javascript
db.products.createIndex({ description: 'text' })
db.products.find({ $text: { $search: 'wireless headphones' } })
```

## 2. Geospatial (2dsphere) Indexes

A `2dsphere` index supports queries on GeoJSON data -- points, lines,
polygons -- for "find documents near this point" or "find documents
within this area", using spherical geometry that accounts for the
Earth's curvature rather than flat-plane math.

```javascript
db.stores.createIndex({ location: '2dsphere' })
const point = { type: 'Point', coordinates: [-73.9, 40.7] };
db.stores.find({ location: { $near: { $geometry: point } } })
```
