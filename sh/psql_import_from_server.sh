#!/usr/bin/env bash
set -euo pipefail

SERVER="catalin@72.62.152.27"
REMOTE_CONTAINER="recallforge-postgres"
LOCAL_CONTAINER="recallforge-postgres"
DB_USER="recallforge"
DB_NAME="recallforge"

echo "Checking local Postgres container..."
docker compose up -d postgres

echo "Pulling server database into local..."

ssh "$SERVER" \
  "docker exec '$REMOTE_CONTAINER' pg_dump -U '$DB_USER' -d '$DB_NAME'" \
| {
  echo "Stopping local app..."
  docker compose stop app || true

  echo "Waiting for local Postgres..."
  until docker exec "$LOCAL_CONTAINER" pg_isready -U "$DB_USER" -d postgres >/dev/null 2>&1; do
    sleep 1
  done

  echo "Recreating local database..."
  docker exec -i "$LOCAL_CONTAINER" psql -U "$DB_USER" -d postgres <<SQL
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = '$DB_NAME';

DROP DATABASE IF EXISTS $DB_NAME;
CREATE DATABASE $DB_NAME OWNER $DB_USER;
SQL

  echo "Restoring local database..."
  docker exec -i "$LOCAL_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME"

  echo "Starting local app..."
  docker compose up -d app
}

echo "Pulled server DB into local."