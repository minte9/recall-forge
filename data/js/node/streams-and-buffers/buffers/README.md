## JS / Node / Streams & Buffers / Buffers

### 1. Buffer Basics

A `Buffer` is a fixed-size chunk of raw binary memory, used whenever
Node handles data that isn't text — file bytes, network packets, images
— before it's optionally decoded to a string.

~~~javascript
const buf = Buffer.from("hi");
buf;          // <Buffer 68 69>
buf.length;    // 2 -- byte length, not char count for multi-byte text
~~~


### 2. Encoding & Buffers

Converting between a `Buffer` and a string requires an explicit encoding
(default `"utf8"`); picking the wrong encoding (e.g. `"ascii"` on
multi-byte characters) silently corrupts data.

~~~javascript
const buf = Buffer.from("café", "utf8");
buf.toString("utf8");   // "café"
buf.toString("ascii");   // corrupted -- wrong encoding
~~~


### 3. Buffer vs String

Strings in JS are immutable and encoding-aware; Buffers are mutable raw
bytes with no concept of characters. Use Buffers for binary/network/file
I/O, strings for text you'll display or parse.

~~~javascript
const buf = Buffer.alloc(4);
buf[0] = 0xff;   // mutate bytes directly -- not possible with a string
~~~
