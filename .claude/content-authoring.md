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

```sql
SELECT t.id, t.title, count(r.id) AS review_count
FROM topics t
LEFT JOIN reviews r ON r.topic_id = t.id
WHERE t.markdown_file_id = <id>
GROUP BY t.id, t.title;
```

- **Zero reviews everywhere** → free to delete the `markdown_files`/`topics`
  rows and re-import cleanly. Simplest path, no ID bookkeeping needed.
- **Any review history** → never delete/recreate. Update `content`/`title` in
  place by topic ID, preserving `memory_score`, `next_review_at`, and the
  review rows. Recompute `content_hash` (SHA-256 of the full file content) and
  update `markdown_files.content` too — if you only change the `topics` rows
  and leave the stale header text in `markdown_files.content`, the next
  edit-and-save through the app UI will silently revert your change (it
  re-parses the `##` header from `content` on every save).

## 5. Direct database import (bypassing the app's upload UI)

The app's REST API only accepts uploads from the currently OAuth2-logged-in
user — there's no way to import as an arbitrary user via the API. Import
directly into Postgres instead, mirroring exactly what
`MarkdownFileService.importMarkdown()` does:

- `user_id = 1` is the template/admin user whose content `StarterPackService`
  clones for other users by category.
- One `markdown_files` row per file, one `topics` row per `###` section.
- `memory_score = 0.6`, `next_review_at`/`created_at`/`updated_at = now()` for
  new topics (matches the app's own defaults).
- Use dollar-quoting (`$md$...$md$`) for content in generated SQL to avoid
  escaping issues with quotes/backslashes in code samples.
- Pattern for a clean multi-file import:
  ```sql
  WITH new_file AS (
      INSERT INTO markdown_files (filename, content, content_hash, uploaded_at,
          environment, category, subcategory, topic_group, user_id, visibility)
      VALUES (...)
      RETURNING id
  )
  INSERT INTO topics (title, content, memory_score, next_review_at, created_at,
      updated_at, environment, category, subcategory, file_title,
      markdown_file_id, user_id, visibility)
  SELECT ... FROM new_file
  UNION ALL
  SELECT ... FROM new_file;
  ```
- Run it via `docker compose exec -T postgres psql -U recallforge -d recallforge -v ON_ERROR_STOP=1 < file.sql`.
- **Always verify after import**: re-query row/topic counts, spot-check the
  header text landed correctly, and (if editing existing content) confirm
  review history/memory scores are unchanged.
- A Python script that parses the `##`/`###` structure exactly like
  `MarkdownTopicImporter` and emits this SQL is the reliable way to do this
  for more than 1-2 files — don't hand-write the SQL for a batch.

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

## 7. Docker mechanics

- `data/` is **not** read by the running app at all — it's purely an authoring
  workspace. The database is the only thing the app actually serves from.
- Frontend static files (`src/main/resources/static/`) are bind-mounted —
  edits take effect immediately, no rebuild needed.
- Backend Java changes (new entity, repository, service logic) need:
  ```
  docker compose build app && docker compose up -d app
  ```
  Hibernate's `ddl-auto: update` creates/alters tables automatically on
  startup — no manual migration needed, but check the startup log for errors
  and confirm the new column/table exists afterward.
