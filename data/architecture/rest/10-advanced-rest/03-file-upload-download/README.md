# API / REST / Advanced REST / File Upload and Download

## 1. multipart/form-data

File uploads use `multipart/form-data`: the body is split into parts,
each with its own headers, so one request can carry a file plus other
fields. JSON can't carry raw binary efficiently (Base64 adds ~33%).

```bash
curl -F "file=@cover.png" -F "title=Dune" \
     http://localhost:8080/books/42/cover
# Content-Type: multipart/form-data; boundary=----abc
```

## 2. MultipartFile

Spring binds each part to a `MultipartFile` via `@RequestParam` or
`@RequestPart`. Validate size and content type, never trust the client
filename for storage paths, and set limits with
`spring.servlet.multipart.max-file-size`.

```java
@PostMapping(value = "/books/{id}/cover",
             consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
ResponseEntity<Void> upload(@PathVariable Long id,
                            @RequestParam MultipartFile file) {
    if (!"image/png".equals(file.getContentType()))
        return ResponseEntity.status(415).build();
    storage.save(id, file.getInputStream());   // own name, not client's
    return ResponseEntity.noContent().build();
}
```

## 3. File Download Responses

Return the bytes as a `Resource` with the right `Content-Type`,
`Content-Length`, and `Content-Disposition` (`attachment` to download,
`inline` to display). Streaming from a `Resource` avoids loading the
whole file into memory.

```java
@GetMapping("/books/{id}/cover")
ResponseEntity<Resource> cover(@PathVariable Long id) {
    Resource file = storage.load(id);
    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"cover-" + id + ".png\"")
        .body(file);
}
```
