#!/usr/bin/env bash
set -euo pipefail

SERVER="catalin@72.62.152.27"
LOCAL_CONTAINER="recallforge-postgres"
REMOTE_CONTAINER="recallforge-postgres"

DB_USER="recallforge"
DB_NAME="recallforge"

REMOTE_COMPOSE_FILE="/srv/docker/recall-forge/compose.yml"
REMOTE_COMPOSE_DIR="/srv/docker/recall-forge"

echo "Dumping local database and pushing to server..."

docker exec "$LOCAL_CONTAINER" \
  pg_dump -U "$DB_USER" -d "$DB_NAME" \
| ssh "$SERVER" "
  set -euo pipefail

  cd '$REMOTE_COMPOSE_DIR'

  echo 'Stopping app container...'
  docker compose -f '$REMOTE_COMPOSE_FILE' stop app

  echo 'Recreating remote database...'
  docker exec -i '$REMOTE_CONTAINER' psql -U '$DB_USER' -d postgres <<'SQL'
DROP DATABASE IF EXISTS $DB_NAME;
CREATE DATABASE $DB_NAME OWNER $DB_USER;
SQL

  echo 'Restoring database dump...'
  docker exec -i '$REMOTE_CONTAINER' psql -U '$DB_USER' -d '$DB_NAME'

  echo 'Starting app container...'
  docker compose -f '$REMOTE_COMPOSE_FILE' start app
"

echo "Pushed local DB to server."