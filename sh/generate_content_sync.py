#!/usr/bin/env python3
"""
Generates a SQL script that upserts the template user's (user_id=1)
content - markdown_files, topics, and the global ordering/reference
tables (allowed_environments, category_order, subcategory_order) -
read from the LOCAL database.

Matching rules mirror MarkdownFileService.syncTopicsFromMarkdown():
  - markdown_files matched by (user_id, environment, category,
    subcategory, topic_group); existing rows are UPDATEd in place,
    new ones INSERTed.
  - topics matched by (markdown_file_id, title, user_id); existing
    rows are UPDATEd in place (preserving id, memory_score,
    next_review_at, and review history), new ones INSERTed.
  - a topic whose title no longer appears under its still-existing
    file is DELETEd along with its reviews - same as a live re-upload
    of that file through the app.

Whole files that no longer exist locally are intentionally left
alone - never auto-deleted. That stays a manual decision.

Only ever touches rows owned by user_id=1; never reviews, accounts,
or content belonging to any other user.

Usage: python3 generate_content_sync.py > /tmp/content_sync.sql
"""
import json
import subprocess
import sys

CONTAINER = "recallforge-postgres"
DB_USER = "recallforge"
DB_NAME = "recallforge"
TEMPLATE_USER_ID = 1


def query_json(sql):
    result = subprocess.run(
        ["docker", "exec", CONTAINER, "psql", "-U", DB_USER, "-d", DB_NAME,
         "-t", "-A", "-c", sql],
        capture_output=True, text=True, check=True,
    )
    data = result.stdout.strip()
    return json.loads(data) if data else []


def sql_str(s):
    return "'" + s.replace("'", "''") + "'"


def sql_order(value):
    return str(value) if value is not None else "NULL"


def dollar_quote(content, tag_hint):
    n = 0
    while True:
        candidate = tag_hint if n == 0 else f"{tag_hint}_{n}"
        delim = f"${candidate}$"
        if delim not in content:
            return f"{delim}{content}{delim}"
        n += 1


def emit_file_block(out, index, f, file_topics):
    var = f"file_id_{index}"

    out.append("DO $sync$")
    out.append("DECLARE")
    out.append(f"  {var} bigint;")
    out.append("BEGIN")
    out.append(f"  SELECT id INTO {var} FROM markdown_files")
    out.append(f"  WHERE user_id = {TEMPLATE_USER_ID}")
    out.append(f"    AND environment = {sql_str(f['environment'])}")
    out.append(f"    AND category = {sql_str(f['category'])}")
    out.append(f"    AND subcategory = {sql_str(f['subcategory'])}")
    out.append(f"    AND topic_group = {sql_str(f['topic_group'])};")
    out.append("")
    out.append(f"  IF {var} IS NULL THEN")
    out.append("    INSERT INTO markdown_files")
    out.append("      (filename, content, content_hash, uploaded_at, environment,")
    out.append("       category, subcategory, topic_group, visibility, user_id, display_order)")
    out.append("    VALUES")
    out.append(f"      ({sql_str(f['filename'])},")
    out.append(f"       {dollar_quote(f['content'], f'mf{index}')},")
    out.append(f"       {sql_str(f['content_hash'])}, now(),")
    out.append(f"       {sql_str(f['environment'])}, {sql_str(f['category'])},")
    out.append(f"       {sql_str(f['subcategory'])}, {sql_str(f['topic_group'])},")
    out.append(f"       {sql_str(f['visibility'])}, {TEMPLATE_USER_ID}, {sql_order(f['display_order'])})")
    out.append(f"    RETURNING id INTO {var};")
    out.append(f"    RAISE NOTICE 'created file: % / % / % (id %)',")
    out.append(f"      {sql_str(f['category'])}, {sql_str(f['subcategory'])}, {sql_str(f['topic_group'])}, {var};")
    out.append("  ELSE")
    out.append("    UPDATE markdown_files SET")
    out.append(f"      content = {dollar_quote(f['content'], f'mf{index}')},")
    out.append(f"      content_hash = {sql_str(f['content_hash'])},")
    out.append(f"      visibility = {sql_str(f['visibility'])},")
    out.append(f"      display_order = {sql_order(f['display_order'])}")
    out.append(f"    WHERE id = {var};")
    out.append("  END IF;")
    out.append("")

    title_list = ", ".join(sql_str(t["title"]) for t in file_topics) or "NULL"

    for j, t in enumerate(file_topics):
        out.append(f"  IF EXISTS (SELECT 1 FROM topics WHERE markdown_file_id = {var}")
        out.append(f"             AND title = {sql_str(t['title'])} AND user_id = {TEMPLATE_USER_ID}) THEN")
        out.append("    UPDATE topics SET")
        out.append(f"      content = {dollar_quote(t['content'], f'tc{index}_{j}')},")
        out.append(f"      environment = {sql_str(t['environment'])},")
        out.append(f"      category = {sql_str(t['category'])},")
        out.append(f"      subcategory = {sql_str(t['subcategory'])},")
        out.append(f"      file_title = {sql_str(t['file_title'])},")
        out.append(f"      visibility = {sql_str(t['visibility'])},")
        out.append("      updated_at = now()")
        out.append(f"    WHERE markdown_file_id = {var} AND title = {sql_str(t['title'])}")
        out.append(f"      AND user_id = {TEMPLATE_USER_ID};")
        out.append("  ELSE")
        out.append("    INSERT INTO topics")
        out.append("      (title, content, memory_score, next_review_at, created_at, updated_at,")
        out.append("       environment, category, subcategory, file_title, markdown_file_id,")
        out.append("       user_id, visibility)")
        out.append("    VALUES")
        out.append(f"      ({sql_str(t['title'])}, {dollar_quote(t['content'], f'tc{index}_{j}')},")
        out.append("       0.6, now(), now(), now(),")
        out.append(f"       {sql_str(t['environment'])}, {sql_str(t['category'])},")
        out.append(f"       {sql_str(t['subcategory'])}, {sql_str(t['file_title'])}, {var},")
        out.append(f"       {TEMPLATE_USER_ID}, {sql_str(t['visibility'])});")
        out.append("  END IF;")
        out.append("")

    out.append("  DELETE FROM reviews WHERE topic_id IN (")
    out.append(f"    SELECT id FROM topics WHERE markdown_file_id = {var}")
    out.append(f"      AND user_id = {TEMPLATE_USER_ID} AND title NOT IN ({title_list})")
    out.append("  );")
    out.append(f"  DELETE FROM topics WHERE markdown_file_id = {var}")
    out.append(f"    AND user_id = {TEMPLATE_USER_ID} AND title NOT IN ({title_list});")
    out.append("END $sync$;")
    out.append("")


def main():
    files = query_json(f"""
        SELECT json_agg(row_to_json(m)) FROM (
            SELECT id, filename, content, content_hash, environment,
                   category, subcategory, topic_group, display_order,
                   visibility
            FROM markdown_files WHERE user_id = {TEMPLATE_USER_ID}
            ORDER BY id
        ) m;
    """)
    topics = query_json(f"""
        SELECT json_agg(row_to_json(t)) FROM (
            SELECT markdown_file_id, title, content, environment,
                   category, subcategory, file_title, visibility
            FROM topics WHERE user_id = {TEMPLATE_USER_ID}
            ORDER BY markdown_file_id, id
        ) t;
    """)
    allowed_environments = query_json("""
        SELECT json_agg(row_to_json(a)) FROM (
            SELECT name, description, display_order FROM allowed_environments
        ) a;
    """)
    category_order = query_json("""
        SELECT json_agg(row_to_json(c)) FROM (
            SELECT environment, category, display_order FROM category_order
        ) c;
    """)
    subcategory_order = query_json("""
        SELECT json_agg(row_to_json(s)) FROM (
            SELECT category, subcategory, display_order FROM subcategory_order
        ) s;
    """)

    if not files:
        print("-- No content found for user_id=1 locally - nothing to sync.", file=sys.stderr)
        sys.exit(1)

    topics_by_file = {}
    for t in topics:
        topics_by_file.setdefault(t["markdown_file_id"], []).append(t)

    out = []
    out.append("-- Generated by sh/generate_content_sync.py - do not edit by hand.")
    out.append(f"-- Syncs user_id={TEMPLATE_USER_ID}'s content: {len(files)} files, {len(topics)} topics.")
    out.append("")

    for i, f in enumerate(files):
        emit_file_block(out, i, f, topics_by_file.get(f["id"], []))

    if allowed_environments:
        out.append("-- allowed_environments")
        for a in allowed_environments:
            out.append(
                "INSERT INTO allowed_environments (name, description, display_order) VALUES "
                f"({sql_str(a['name'])}, {sql_str(a['description'])}, {sql_order(a['display_order'])}) "
                "ON CONFLICT (name) DO UPDATE SET "
                "description = excluded.description, display_order = excluded.display_order;"
            )
        out.append("")

    if category_order:
        out.append("-- category_order")
        for c in category_order:
            out.append(
                "INSERT INTO category_order (environment, category, display_order) VALUES "
                f"({sql_str(c['environment'])}, {sql_str(c['category'])}, {sql_order(c['display_order'])}) "
                "ON CONFLICT (environment, category) DO UPDATE SET display_order = excluded.display_order;"
            )
        out.append("")

    if subcategory_order:
        out.append("-- subcategory_order")
        for s in subcategory_order:
            out.append(
                "INSERT INTO subcategory_order (category, subcategory, display_order) VALUES "
                f"({sql_str(s['category'])}, {sql_str(s['subcategory'])}, {sql_order(s['display_order'])}) "
                "ON CONFLICT (category, subcategory) DO UPDATE SET display_order = excluded.display_order;"
            )
        out.append("")

    print("\n".join(out))


if __name__ == "__main__":
    main()
