// Colors code blocks in the original file view only. Set to false for plain
// code blocks there. The main markdown view stays plain either way, since
// colored code could distract while answering a question.
const CODE_HIGHLIGHT_ENABLED = true;

const Markdown = {
  // v-html's expression runs on every Vue re-render (e.g. each keystroke in
  // the answer box), so keep the last original-view result rather than
  // re-highlighting unchanged content.
  originalCache: { md: null, html: "" },

  // openCode: true renders code blocks as a plain, always-visible card with
  // no toggle at all (the original file view - meant for reference/detail,
  // not recall) instead of the usual collapsed "show code" <details> (the
  // main markdown view and code challenges, where hiding code supports the
  // review/practice purpose).
  render(md, { openCode = false } = {}) {
    if (openCode && this.originalCache.md === md) {
      return this.originalCache.html;
    }

    let html = marked.parse(md || "");

    html = html.replace(
      /<pre><code(.*?)>([\s\S]*?)<\/code><\/pre>/g,
      openCode
        ? (match, attributes, code) =>
`
<div class="code-block">
<pre><code>${this.highlightCode(attributes, code)}</code></pre>
</div>
`
        :
`
<details class="code-block">
<summary>show code</summary>
<pre><code>$2</code></pre>
</details>
`
    );

    // Stored content can come from an uploaded file or a starter-pack copy of
    // another user's content — never trust it. Strip any raw HTML/script the
    // markdown source carries before it's injected into the DOM via v-html.
    html = DOMPurify.sanitize(html);

    if (openCode) {
      this.originalCache = { md, html };
    }
    return html;
  },

  // code is the HTML-escaped text marked produced. It comes back unchanged
  // for untagged blocks, languages highlight.js doesn't know, or if its CDN
  // script failed to load.
  highlightCode(attributes, code) {
    if (!CODE_HIGHLIGHT_ENABLED || typeof hljs === "undefined") {
      return code;
    }

    const match = attributes.match(/language-([^\s"]+)/);
    const language = match && match[1].toLowerCase();
    if (!language || !hljs.getLanguage(language)) {
      return code;
    }

    // highlight.js takes raw text and does its own escaping. A textarea
    // decodes the entities without parsing the content as HTML.
    const decoder = document.createElement("textarea");
    decoder.innerHTML = code;
    return hljs.highlight(decoder.value, { language, ignoreIllegals: true }).value;
  }
};
