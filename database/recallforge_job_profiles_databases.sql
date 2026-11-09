-- RecallForge: add Databases to Java Backend Engineer
-- PostgreSQL 16
-- Safe to re-run. Back up production before applying.
-- Maps by stable (environment, category), not numeric IDs.
-- Requires the DB/Databases row in category_order (created when the
-- template user, id 1, imports the Databases markdown).
--
-- Run:
--   docker compose exec -T postgres psql -U recallforge -d recallforge \
--     -v ON_ERROR_STOP=1 < database/recallforge_job_profiles_databases.sql

BEGIN;

WITH desired(profile_name, environment, category) AS (
    VALUES
      ('Java Backend Engineer', 'DB', 'Databases')
)
INSERT INTO job_profile_categories (profile_id, category_id)
SELECT jp.id, co.id
FROM desired d
JOIN job_profiles jp ON jp.user_id = 1 AND jp.name = d.profile_name
JOIN category_order co ON co.environment = d.environment
                      AND co.category = d.category
ON CONFLICT (profile_id, category_id) DO NOTHING;

UPDATE job_profiles
SET description = 'Java, Spring Boot, REST, Databases, Docker, Git, Security, HTTPS, OAuth2'
WHERE user_id = 1 AND name = 'Java Backend Engineer';

COMMIT;
