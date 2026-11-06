-- RecallForge: database content cleanup
-- PostgreSQL 16
-- One BEGIN/COMMIT block per cleanup; append new cleanups at the end.
-- Deletes run child-first because no foreign key here cascades. Tables linked
-- by category name (question_generations, subcategory_order) have no FK at all.
-- Each block is safe to re-run. Back up production before applying.
--
-- Run:
--   docker compose exec -T postgres psql -U recallforge -d recallforge \
--     -v ON_ERROR_STOP=1 < database/recallforge_cleanup.sql

-- Remove category: JVM / Kotlin (2026-11-06)
BEGIN;
DELETE FROM job_profile_categories WHERE category_id IN
  (SELECT id FROM category_order WHERE environment = 'JVM' AND category = 'Kotlin');
DELETE FROM reviews WHERE topic_id IN
  (SELECT id FROM topics WHERE environment = 'JVM' AND category = 'Kotlin');
DELETE FROM topics WHERE environment = 'JVM' AND category = 'Kotlin';
DELETE FROM markdown_files WHERE environment = 'JVM' AND category = 'Kotlin';
DELETE FROM question_generations WHERE category = 'Kotlin';
DELETE FROM subcategory_order WHERE category = 'Kotlin';
DELETE FROM category_order WHERE environment = 'JVM' AND category = 'Kotlin';
COMMIT;
