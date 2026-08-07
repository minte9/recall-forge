#!/usr/bin/env bash
set -euo pipefail

SERVER="catalin@72.62.152.27"
REMOTE_CONTAINER="recallforge-postgres"
LOCAL_CONTAINER="recallforge-postgres"
DB_USER="recallforge"
DB_NAME="recallforge"

LOCAL_DUMP="$(mktemp /tmp/recallforge-dump.XXXXXX.sql)"
FILTERED_DUMP="$(mktemp /tmp/recallforge-dump-filtered.XXXXXX.sql)"
REMOTE_DUMP="/tmp/recallforge-dump-$$.sql"

cleanup() {
  rm -f "$LOCAL_DUMP" "$FILTERED_DUMP"
  ssh "$SERVER" "rm -f '$REMOTE_DUMP'" >/dev/null 2>&1 || true
  echo "Starting local app..."
  docker compose up -d app
}
trap cleanup EXIT

echo "Checking local Postgres container..."
docker compose up -d postgres

echo "Dumping server database..."
REMOTE_SUM="$(ssh "$SERVER" "docker exec '$REMOTE_CONTAINER' pg_dump -U '$DB_USER' -d '$DB_NAME' > '$REMOTE_DUMP' && sha256sum '$REMOTE_DUMP' | awk '{print \$1}'")"
echo "Remote dump checksum: $REMOTE_SUM"

# Dump to a file and transfer it with scp, instead of piping pg_dump
# straight through ssh into psql. A live pipe gives no way to tell a
# transfer hiccup apart from a real data problem; a file + checksum does.
echo "Copying dump from server..."
scp -q "$SERVER:$REMOTE_DUMP" "$LOCAL_DUMP"

LOCAL_SUM="$(sha256sum "$LOCAL_DUMP" | awk '{print $1}')"

if [ "$LOCAL_SUM" != "$REMOTE_SUM" ]; then
  echo "ERROR: checksum mismatch after transfer (remote=$REMOTE_SUM local=$LOCAL_SUM)."
  echo "Nothing was imported."
  exit 1
fi
echo "Checksums match ($LOCAL_SUM)."

# pg_dump wraps its output in \restrict/\unrestrict guards (psql-only
# meta-commands understood only by a matching, recent psql version).
# Strip them so a local/remote psql version mismatch can't break the
# restore - we trust this dump, it's our own DB.
grep -Ev '^\\(restrict|unrestrict)\b' "$LOCAL_DUMP" > "$FILTERED_DUMP"

echo "Pulling server database into local..."

echo "Stopping local app..."
docker compose stop app || true

echo "Waiting for local Postgres..."
until docker exec "$LOCAL_CONTAINER" pg_isready -U "$DB_USER" -d postgres >/dev/null 2>&1; do
  sleep 1
done

echo "Recreating local database..."
docker exec -i "$LOCAL_CONTAINER" psql -U "$DB_USER" -d postgres -v ON_ERROR_STOP=1 <<SQL
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = '$DB_NAME' AND pid <> pg_backend_pid();

DROP DATABASE IF EXISTS $DB_NAME;
CREATE DATABASE $DB_NAME OWNER $DB_USER;
SQL

echo "Restoring local database..."
docker exec -i "$LOCAL_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" \
  -v ON_ERROR_STOP=1 --single-transaction < "$FILTERED_DUMP"

echo "Pulled server DB into local."
