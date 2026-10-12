## 1. What is

MCP resources allow an MCP server to expose information that clients can discover and read. Unlike tools, which execute operations, resources primarily provide context to AI applications.

For example, an MCP server could expose application documentation, configuration files, database records, or Markdown learning notes. An AI client can retrieve this information and use it to answer questions without requiring the information to be included manually in every conversation.

Resources are particularly useful when information is relatively stable, reusable, or naturally represented as documents.

Examples:

- Application documentation
- Markdown learning materials
- Configuration information
- Database-backed reference data
- API specifications
    
## 2. Resource Discovery

Every MCP resource has a URI that uniquely identifies it. Unlike a normal REST URL, an MCP resource URI does not necessarily represent an HTTP endpoint.

Examples:

```txt
docs://spring-boot/introduction
docs://spring-boot/security
config://application/settings
notes://java/streams
```

An MCP client discovers resources using the `resources/list` operation and reads their contents using `resources/read`.

A typical interaction is:

1. The client sends `resources/list`
2. The server returns available resources, including their URIs
3. The client requests a specific URI using `resources/read`
4. The server returns the resource content

For example, discovery might return:

```json
{
  "resources": [
    {
      "uri": "docs://java/streams",
      "name": "Java Streams",
      "mimeType": "text/markdown"
    }
  ]
}
```

The client can subsequently request:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "resources/read",
  "params": {
    "uri": "docs://java/streams"
  }
}
```

And receive a response containing the document:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "contents": [
      {
        "uri": "docs://java/streams",
        "mimeType": "text/markdown",
        "text": "# Java Streams\n\nStreams process collections declaratively."
      }
    ]
  }
}
```

These are MCP protocol messages, not REST requests to `/resources/read`.

## 3. Resources vs Tools

Both tools and resources expose capabilities through an MCP server, but they serve different purposes.

|Feature|Resources|Tools|
|---|---|---|
|Purpose|Provide context|Execute operations|
|Discovery|`resources/list`|`tools/list`|
|Invocation|`resources/read`|`tools/call`|
|Identifier|URI|Tool name|
|Typical use|Read documentation|Calculate, search, update|
|Side effects|Generally read-only|May modify state|

For example, suppose you're building an MCP server for a learning application.

A resource could expose a Markdown lesson:

```
notes://java/concurrency
```

A tool could execute an operation:

```
@Tool(description = "Search learning topics by keyword")
public List<String> searchTopics(String keyword) {
    return topicService.search(keyword);
}
```

The resource provides learning content, while the tool performs a search.

An important distinction is that resources are generally selected and read by the client application, whereas tools can be made available for the AI model to invoke. Actual behavior depends on the MCP client.

## 4. Practical example

First, we'll inspect MCP resources at the protocol level. In the next lesson, we'll implement resource registration in Spring AI.

Start your existing MCP Spring Boot server, then open MCP Inspector:

```sh
npx @modelcontextprotocol/inspector
```

Connect using your configured transport and server address.

What to check in MCP Inspector

1. Connect to the MCP server.
2. Open the Resources tab.
3. Request the list of available resources.
4. Select a resource and read its content, if one exists.
    
If your server currently exposes only `@Tool` methods, the Resources tab may show an empty list. This is expected: tools are not automatically registered as resources.