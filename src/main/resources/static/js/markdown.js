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

    return html;
  }
};
