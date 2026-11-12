// Colors code blocks in the main and original file views (see the highlight
// option on render). Set to false for plain code blocks in both.
const CODE_HIGHLIGHT_ENABLED = true;

// Code challenge answers in a file with no code fence tags get their
// language guessed from these (highlightText). A short list keeps the guess
// reliable - auto-detecting across every language misreads short snippets.
// sql/bash are left out: plain-English answers score as SQL. Guesses scoring
// below the minimum relevance (prose scores 0-1, real code 2+) stay uncolored.
const AUTO_DETECT_LANGUAGES = ["java", "python", "javascript", "typescript", "php"];
const AUTO_DETECT_MIN_RELEVANCE = 2;

// Obsidian image embeds - ![[name.png]] or ![[name.png|300]] (display
// width). Stored markdown keeps this syntax as-is; it's resolved here, at
// render time, to the image uploaded with the vault (ImageController), so
// files imported before their images were uploaded need no re-import.
// A marked inline extension rather than a regex over the source, so embeds
// inside code spans/blocks are left as literal text.
const OBSIDIAN_IMAGE_EMBED = /^!\[\[([^\]|\n]+\.(?:png|jpe?g))(?:\|(\d{1,4})(?:x\d{1,4})?)?\]\]/i;

marked.use({
  extensions: [{
    name: "obsidianImageEmbed",
    level: "inline",
    start(src) {
      const index = src.indexOf("![[");
      return index >= 0 ? index : undefined;
    },
    tokenizer(src) {
      const match = OBSIDIAN_IMAGE_EMBED.exec(src);
      if (!match) {
        return undefined;
      }
      return {
        type: "obsidianImageEmbed",
        raw: match[0],
        filename: match[1].trim(),
        width: match[2] || null
      };
    },
    renderer(token) {
      const src = "/api/images/" + encodeURIComponent(token.filename);
      const alt = token.filename
        .replace(/&/g, "&amp;")
        .replace(/"/g, "&quot;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
      const width = token.width ? ` width="${token.width}"` : "";
      return `<img src="${src}" alt="${alt}"${width} loading="lazy">`;
    }
  }]
});

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

    // Plain-text fences (text, txt, plaintext - all one highlight.js
    // language) are diagrams/output, not code, so they don't get a vote -
    // otherwise two ```text diagrams outvote one ```java block.
    const plaintext = hljs.getLanguage("plaintext");
    const counts = {};
    for (const [, tag] of (md || "").matchAll(/^```([\w+#-]+)/gm)) {
      const language = tag.toLowerCase();
      const definition = hljs.getLanguage(language);
      if (definition && definition !== plaintext) {
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
  // Without a language (the file has no code fence tags), the language is
  // guessed from the text itself among AUTO_DETECT_LANGUAGES; a weak guess
  // (prose, a one-liner) is treated as no language.
  highlightText(text, language) {
    if (!CODE_HIGHLIGHT_ENABLED || typeof hljs === "undefined" || !(text || "").trim()) {
      return null;
    }

    if (language) {
      return DOMPurify.sanitize(hljs.highlight(text, { language, ignoreIllegals: true }).value);
    }

    const candidates = AUTO_DETECT_LANGUAGES.filter(l => hljs.getLanguage(l));
    const result = hljs.highlightAuto(text, candidates);
    if (!result.language || result.relevance < AUTO_DETECT_MIN_RELEVANCE) {
      return null;
    }
    return DOMPurify.sanitize(result.value);
  }
};
