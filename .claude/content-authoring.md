# Content Authoring Guide

For creating or editing markdown topic content (new categories, subcategories, or
files under `data/`) and importing it into the database.

## 1. Workflow

1. **Propose first.** Give a list of proposed files with titles and topic
   breakdowns (a table works well). Wait for approval before writing anything.
2. **Check for overlap** against existing subcategories before finalizing the
   topic list (see §5). Flag anything that looks redundant in the proposal
   itself, don't discover it after writing.
3. **Write the files** to disk under `data/<environment>/<category>/<subcategory-slug>/README.md`,
   one folder per file/topic-group.
4. **Import separately**, only after the files are written and (if asked)
   reviewed. Don't assume "write the files" also means "import them" unless
   told so explicitly — these have been two distinct approval points in past
   sessions.
5. **Ask about visibility** (`PUBLIC` vs `PRIVATE`) if the subcategory doesn't
   already have an established convention — check existing files in the same
   subcategory first; don't assume.

## 2. Markdown format

```
## <Environment> / <Category> / <Subcategory> / <FileTitle>

### 1. <Topic Title>

2-3 sentences, ~45-60 words. This is a spaced-repetition recall app, not a
knowledgebase article — the paragraph needs to be readable in one glance,
not teach the topic from scratch.

~~~java
// short, focused example with inline comments explaining WHY, not WHAT
~~~

### 2. <Next Topic Title>
...
```

- The importer (`MarkdownTopicImporter`) parses the `##` line by splitting on
  `/` into exactly 4 parts (Environment/Category/Subcategory/FileTitle) — the
  header line is the single source of truth for where content lands in the
  DB. The folder path is cosmetic/organizational only.
- Each `###` line starts a new topic; everything until the next `###` or `##`
  is that topic's content.
- **Topic count: 2-5 per file, no fixed number.** Only include topics that
  are genuinely important. Don't pad to hit 3 "because that's what other
  files have."
- **Code should be short and commented**, not a full runnable-app dump.
  Explain the non-obvious part with a comment; trim boilerplate (imports,
  full class ceremony) unless it's the point of the topic.
- **Keep code lines ≤ 80 characters, including trailing comments.** The
  review panel renders code in a fixed-width, non-wrapping `<pre>` block
  (~75-80 usable characters at desktop width) — longer lines trigger a
  horizontal scrollbar instead of wrapping. If an inline comment would push
  a line past 80 chars, put the comment on its own line above the code
  instead of trailing it, or shorten it.
- File titles should be short and specific — prefer full names over
  ambiguous abbreviations (`Inversion of Control`, not `IoC`), but don't let
  them run long (`Boot Testing`, not `Testing with @SpringBootTest`).

## 3. Checking for overlap (§5 reference — do this before writing)

Before finalizing a topic list, check whether the same ground is already
covered by another subcategory. This has come up repeatedly:
- Java version files (`Versions/java8`) vs. dedicated `Streams`/`Lambdas`/`Generics`
  subcategories created later.
- `OOP/Composition`'s "Dependency Inversion" vs. `SOLID/Dependency Inversion`.
- `OOP/Polymorphism`'s "Dependency Injection" vs. `Spring/Framework/Dependency Injection`.
- `Spring/Framework`'s "Spring Boot Auto-Configuration" vs. a later dedicated
  `Spring/Boot` subcategory (in that case, moved rather than duplicated).

How to resolve, depending on context:
- **New content, nothing to lose**: just remove or rewrite the redundant topic.
- **A retrospective/historical file** (e.g. a Java version overview) that would
  feel incomplete without mentioning the feature: shrink the topic to 2-3
  sentences + a tiny snippet, and point to the dedicated subcategory for depth,
  rather than dropping it or fully re-teaching it.
- **Existing content with real review history** (see §4): shorten in place,
  don't delete. Same treatment as above — brief mention + cross-reference.

## 4. Editing existing content safely

**Always check review history before touching topic count.**

- **Zero reviews everywhere** → free to delete the `markdown_files`/`topics`
  rows and re-import cleanly. Simplest path, no ID bookkeeping needed.
- **Any review history** → never delete/recreate. Update `content`/`title` in
  place by topic ID, preserving `memory_score`, `next_review_at`, and the
  review rows. The stale-content trap and the exact query to check review
  counts are in the local companion guide (see §7 below) — read it before
  touching anything with review history.

## 5. Direct database import (bypassing the app's upload UI)

Content can be imported directly into Postgres, mirroring what
`MarkdownFileService.importMarkdown()` does, for cases the app's upload UI
can't cover (e.g. importing as a user other than the one currently logged
in). The mechanics — SQL patterns, `docker compose exec` commands, which
user ID content lands under — are operational details, not editorial ones;
see the local companion guide (§7 below).

## 6. Ordering

Two independent, additive mechanisms — neither requires touching content:

- **`markdown_files.display_order`** (nullable int): controls which file
  shows first *within* a subcategory. `NULL` falls back to insertion order.
  `findMarkdownSummaries` sorts `... subcategory, display_order ASC NULLS LAST, markdownFile.id`.
- **`subcategory_order` table** (`category`, `subcategory`, `display_order`,
  unique on the pair): controls which subcategory shows first *within* a
  category. Applied as a stable sort in `DashboardService.getMarkdownSummaries()`
  (Java-side, not a SQL join, since subcategory is just a string column with
  no entity). Any subcategory with no row falls back to its alphabetical
  position.

When adding a new subcategory, decide whether it needs an explicit order
(does a real prerequisite/difficulty relationship exist between it and its
siblings?) — if not, alphabetical is a fine default and was the explicit
decision for a while before ordering was built.

## 7. Operational guide (local)

Direct database access (import, editing via SQL) and Docker mechanics
(rebuild/restart commands, `ddl-auto` behavior) are covered in
`.claude/content-authoring.local.md`. That file is gitignored — it's
machine/operator-specific, not team-wide editorial policy. Read it before
any task that touches the database directly or requires a container
rebuild.
