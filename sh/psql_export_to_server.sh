#!/usr/bin/env bash
set -euo pipefail

# GUARD: this does a full DROP DATABASE + restore - it overwrites every
# user's accounts, reviews, and content, not just the template user's.
# For pushing authored content to prod, use content_sync_to_server.sh
# instead - it's scoped to user_id=1 and never touches anyone else's data.
#
# Only if you really mean to replace the whole database, delete/comment
# out the line below, run this script, then put it back.
echo "Refusing to run: use sh/content_sync_to_server.sh for routine content pushes." >&2
echo "This script replaces the ENTIRE database - see the GUARD comment at the top to override." >&2
exit 1

SERVER="catalin@72.62.152.27"
LOCAL_CONTAINER="recallforge-postgres"
REMOTE_CONTAINER="recallforge-postgres"

DB_USER="recallforge"
DB_NAME="recallforge"

REMOTE_COMPOSE_FILE="/srv/docker/recall-forge/compose.yml"
REMOTE_COMPOSE_DIR="/srv/docker/recall-forge"

LOCAL_DUMP="$(mktemp /tmp/recallforge-dump.XXXXXX.sql)"
REMOTE_DUMP="/tmp/recallforge-dump-$$.sql"

cleanup() {
  rm -f "$LOCAL_DUMP"
}
trap cleanup EXIT

echo "Dumping local database..."

# pg_dump wraps its output in \restrict/\unrestrict guards (psql-only
# meta-commands understood only by a matching, recent psql version).
# Strip them so an older psql on the server doesn't choke on them -
# we trust this dump, it's our own DB.
docker exec "$LOCAL_CONTAINER" pg_dump -U "$DB_USER" -d "$DB_NAME" \
  | grep -Ev '^\\(restrict|unrestrict)\b' \
  > "$LOCAL_DUMP"

LOCAL_SUM="$(sha256sum "$LOCAL_DUMP" | awk '{print $1}')"
echo "Local dump checksum: $LOCAL_SUM"

# Dump to a file and transfer it with scp, instead of piping pg_dump
# straight through ssh into psql. A live three-way pipe
# (docker exec | ssh | docker exec) gives no way to tell a transfer
# hiccup apart from a real data problem; a file + checksum does.
echo "Copying dump to server..."
scp -q "$LOCAL_DUMP" "$SERVER:$REMOTE_DUMP"

echo "Verifying transfer integrity..."
REMOTE_SUM="$(ssh "$SERVER" "sha256sum '$REMOTE_DUMP' | awk '{print \$1}'")"

if [ "$LOCAL_SUM" != "$REMOTE_SUM" ]; then
  echo "ERROR: checksum mismatch after transfer (local=$LOCAL_SUM remote=$REMOTE_SUM)."
  echo "Nothing was restored. Removing remote copy."
  ssh "$SERVER" "rm -f '$REMOTE_DUMP'"
  exit 1
fi

echo "Checksums match ($LOCAL_SUM). Restoring on server..."

ssh "$SERVER" "
  set -euo pipefail

  cd '$REMOTE_COMPOSE_DIR'

  echo 'Stopping app container...'
  docker compose -f '$REMOTE_COMPOSE_FILE' stop app

  # Always bring the app back up and clean up the remote dump file,
  # no matter how this script exits - a failed restore should leave
  # the site running on the old data, not stuck down.
  trap 'docker compose -f $REMOTE_COMPOSE_FILE start app; rm -f $REMOTE_DUMP' EXIT

  echo 'Recreating remote database...'
  docker exec -i '$REMOTE_CONTAINER' psql -U '$DB_USER' -d postgres -v ON_ERROR_STOP=1 <<'SQL'
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = '$DB_NAME' AND pid <> pg_backend_pid();

DROP DATABASE IF EXISTS $DB_NAME;
CREATE DATABASE $DB_NAME OWNER $DB_USER;
SQL

  echo 'Restoring database dump...'
  docker exec -i '$REMOTE_CONTAINER' psql -U '$DB_USER' -d '$DB_NAME' \
    -v ON_ERROR_STOP=1 --single-transaction < '$REMOTE_DUMP'

  echo 'Restore complete.'
"

echo "Pushed local DB to server."
