// Colors code blocks in the main and original file views (see the highlight
// option on render). Set to false for plain code blocks in both.
const CODE_HIGHLIGHT_ENABLED = true;

const Markdown = {
  // v-html's expression runs on every Vue re-render (e.g. each keystroke in
  // the answer box), so keep each file view's last result rather than
  // re-highlighting unchanged content.
  fileViewCache: {
    main: { md: null, html: "" },
    original: { md: null, html: "" }
  },

  // openCode: true renders code blocks as a plain, always-visible card with
  // no toggle at all (the original file view - meant for reference/detail,
  // not recall) instead of the usual collapsed "show code" <details> (the
  // main markdown view and code challenges, where hiding code supports the
  // review/practice purpose).
  // highlight: true colors code blocks. Only the file views pass it, which
  // also keeps per-message callers like the Recall Assistant out of the cache.
  render(md, { openCode = false, highlight = false } = {}) {
    const cacheKey = openCode ? "original" : "main";
    if (highlight && this.fileViewCache[cacheKey].md === md) {
      return this.fileViewCache[cacheKey].html;
    }

    let html = marked.parse(md || "");

    html = html.replace(
      /<pre><code(.*?)>([\s\S]*?)<\/code><\/pre>/g,
      (match, attributes, code) => {
        const body = highlight ? this.highlightCode(attributes, code) : code;

        return openCode
          ?
`
<div class="code-block">
<pre><code>${body}</code></pre>
</div>
`
          :
`
<details class="code-block">
<summary>show code</summary>
<pre><code>${body}</code></pre>
</details>
`;
      }
    );

    // Stored content can come from an uploaded file or a starter-pack copy of
    // another user's content — never trust it. Strip any raw HTML/script the
    // markdown source carries before it's injected into the DOM via v-html.
    html = DOMPurify.sanitize(html);

    if (highlight) {
      this.fileViewCache[cacheKey] = { md, html };
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
  },

  // The most common language tag on the markdown's code fences, or null if
  // it has none highlight.js knows. Code challenge answers are raw text with
  // no tag of their own, so they borrow the language of their file.
  mainLanguage(md) {
    if (!CODE_HIGHLIGHT_ENABLED || typeof hljs === "undefined") {
      return null;
    }

    const counts = {};
    for (const [, tag] of (md || "").matchAll(/^```([\w+#-]+)/gm)) {
      const language = tag.toLowerCase();
      if (language !== "txt" && hljs.getLanguage(language)) {
        counts[language] = (counts[language] || 0) + 1;
      }
    }

    const languages = Object.keys(counts);
    if (languages.length === 0) {
      return null;
    }
    return languages.reduce((best, language) => counts[language] > counts[best] ? language : best);
  },

  // Sanitized HTML for raw code, or null when there's no language to color it
  // with - callers then show the text as they would without highlighting.
  highlightText(text, language) {
    if (!language || !(text || "").trim()) {
      return null;
    }

    return DOMPurify.sanitize(hljs.highlight(text, { language, ignoreIllegals: true }).value);
  }
};
