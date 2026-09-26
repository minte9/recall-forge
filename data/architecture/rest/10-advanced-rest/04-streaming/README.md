# API / REST / Advanced REST / Streaming

## 1. Streaming Responses

A normal handler builds the full body in memory, then sends it. Streaming
writes the body in chunks as data is produced, so the client starts
receiving immediately and server memory stays flat regardless of size.

```
buffered:  build 500 MB list -> serialize -> send   (OOM risk)
streamed:  read row -> write row -> read row -> ...  (constant memory)
```

## 2. Large Payloads

Exports, reports and big collections shouldn't be materialized as one
`List`. Stream from the database (`Stream<T>` in a read-only transaction)
directly to the response, or offer paging/async export instead of one
huge response.

```java
@Transactional(readOnly = true)
@Query("select b from Book b")
Stream<Book> streamAll();          // cursor-backed, close after use
```

## 3. StreamingResponseBody

`StreamingResponseBody` lets you write to the output stream on a separate
thread after the handler returns, freeing the request thread. It's the
simple MVC tool for large downloads and CSV exports.

```java
@GetMapping(value = "/books/export", produces = "text/csv")
StreamingResponseBody export() {
    return out -> {
        var w = new PrintWriter(out);
        exporter.forEachBook(b -> w.println(b.id() + "," + b.title()));
        w.flush();
    };
}
```
