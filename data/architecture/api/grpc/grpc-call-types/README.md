# Architecture / API / gRPC / gRPC Call Types

## 1. Unary

The simplest call: one request, one response, like a normal function invocation over the network. It covers the majority of RPCs -- fetch an entity, submit a command -- and still benefits from gRPC's codegen, deadlines, and HTTP/2 multiplexing without any streaming complexity.

```proto
rpc GetOrder(GetOrderRequest) returns (Order);
```

## 2. Server & Client Streaming

Server streaming sends one request and receives a stream of responses -- good for large result sets or a live feed the client consumes as it arrives. Client streaming is the reverse: the client pushes many messages (an upload, a batch of metrics) and gets one summary response when it half-closes.

```proto
rpc ListOrders(Query) returns (stream Order);        // server stream
rpc UploadEvents(stream Event) returns (UploadAck);  // client stream
```

## 3. Bidirectional Streaming

Both sides read and write independently on the same connection, ordered per direction but otherwise decoupled. It suits long-lived interactive sessions -- chat, real-time sync, a control channel -- where either party may speak at any time. Application-level flow control matters, since neither side blocks the other.

```proto
rpc Chat(stream ChatMessage) returns (stream ChatMessage);
```
