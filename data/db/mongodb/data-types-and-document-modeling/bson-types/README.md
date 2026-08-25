# DB / MongoDB / Data Types & Document Modeling / BSON Types

## 1. BSON vs JSON

MongoDB stores documents as BSON (Binary JSON), not plain JSON text --
`mongosh` displays documents as JSON for readability, but on disk and
over the wire it's a binary format with a richer type set than JSON
alone offers: `ObjectId`, `Date`, `Decimal128` (exact decimal, unlike
JSON's plain floating-point numbers), and `BinData` for raw bytes.

```javascript
db.orders.insertOne({ total: NumberDecimal('99.90'), placedAt: new Date() })
```

## 2. ObjectId Structure

The default `_id` type, `ObjectId`, packs a 4-byte creation timestamp, a
5-byte random value, and a 3-byte incrementing counter into 12 bytes --
which means ObjectIds are roughly sortable by creation time without a
separate timestamp field, and collisions across machines are extremely
unlikely without any central coordination.

```javascript
ObjectId('507f1f77bcf86cd799439011').getTimestamp()
```
