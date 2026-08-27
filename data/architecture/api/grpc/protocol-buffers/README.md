# Architecture / API / gRPC / Protocol Buffers

## 1. Messages & Field Numbers

A `.proto` file declares messages as typed fields, each with a unique number. The number, not the name, goes on the wire, so fields can be renamed freely but a number must never be reused. Fields 1--15 use a one-byte tag, so reserve those for the most frequently set fields.

```proto
message Order {
  int64 id = 1;
  string status = 2;
  repeated int64 line_ids = 3;
}
```

## 2. Wire Format & Efficiency

Protobuf serialises to a compact binary form: varint-encoded integers, length-prefixed strings, no field names. Payloads are typically 3--10x smaller than equivalent JSON and parse faster because there is no text scanning. The trade-off is that the bytes are unreadable without the schema.

```
JSON:     {"id":42,"status":"OPEN"}   # 24 bytes, self-describing
protobuf: 08 2A 12 04 4F 50 45 4E     # ~8 bytes, needs .proto
```

## 3. Schema Evolution

Adding a field with a new number is backward and forward compatible: old code ignores unknown fields, new code sees defaults for missing ones. Never change a field's type or number; to drop a field, `reserved` its number and name so nobody reuses them. This keeps rolling deploys safe.

```proto
message Order {
  reserved 4; reserved "coupon_code";  // retired, never reuse
  int64 id = 1;
}
```
