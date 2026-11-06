-- RecallForge: add Security, HTTPS and OAuth2 to Java Backend Engineer
-- PostgreSQL 16
-- Safe to re-run. Back up production before applying.
-- Maps by stable (environment, category), not numeric IDs.
--
-- Run:
--   docker compose exec -T postgres psql -U recallforge -d recallforge \
--     -v ON_ERROR_STOP=1 < database/recallforge_job_profiles_security.sql

BEGIN;

WITH desired(profile_name, environment, category) AS (
    VALUES
      ('Java Backend Engineer', 'Security', 'Security'),
      ('Java Backend Engineer', 'Security', 'HTTPS'),
      ('Java Backend Engineer', 'Security', 'OAuth2')
)
INSERT INTO job_profile_categories (profile_id, category_id)
SELECT jp.id, co.id
FROM desired d
JOIN job_profiles jp ON jp.user_id = 1 AND jp.name = d.profile_name
JOIN category_order co ON co.environment = d.environment
                      AND co.category = d.category
ON CONFLICT (profile_id, category_id) DO NOTHING;

UPDATE job_profiles
SET description = 'Java, Spring Boot, REST, Docker, Git, Security, HTTPS, OAuth2'
WHERE user_id = 1 AND name = 'Java Backend Engineer';

COMMIT;
