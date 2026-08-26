const Markdown = {
  render(md) {
    let html = marked.parse(md || "");

    html = html.replace(
      /<pre><code.*?>([\s\S]*?)<\/code><\/pre>/g,
`
<details class="code-block">
<summary>show code</summary>
<pre><code>$1</code></pre>
</details>
`
    );

    // Stored content can come from an uploaded file or a starter-pack copy of
    // another user's content — never trust it. Strip any raw HTML/script the
    // markdown source carries before it's injected into the DOM via v-html.
    return DOMPurify.sanitize(html);
  }
};
