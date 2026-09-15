const Markdown = {
  // openCode: true renders code blocks as a plain, always-visible card with
  // no toggle at all (the original file view - meant for reference/detail,
  // not recall) instead of the usual collapsed "show code" <details> (the
  // main markdown view and code challenges, where hiding code supports the
  // review/practice purpose).
  render(md, { openCode = false } = {}) {
    let html = marked.parse(md || "");

    html = html.replace(
      /<pre><code.*?>([\s\S]*?)<\/code><\/pre>/g,
      openCode
        ?
`
<div class="code-block">
<pre><code>$1</code></pre>
</div>
`
        :
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
