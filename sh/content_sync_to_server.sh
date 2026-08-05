#!/usr/bin/env bash
set -euo pipefail

# Syncs ONLY the template user's (user_id=1) content - markdown_files,
# topics, and the global ordering/reference tables - from the local
# database to the server. Never touches other users' accounts, reviews,
# or content. Existing content is matched and updated in place, so
# review history is preserved; see generate_content_sync.py for the
# exact matching rules.
#
# Unlike psql_export_to_server.sh, this never drops the database and
# runs as a single transaction against the live server, so the app
# never needs to stop - it either commits a consistent update or rolls
# back entirely, with no downtime either way.

SERVER="catalin@72.62.152.27"
REMOTE_CONTAINER="recallforge-postgres"
DB_USER="recallforge"
DB_NAME="recallforge"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

LOCAL_SQL="$(mktemp /tmp/recallforge-content-sync.XXXXXX.sql)"
REMOTE_SQL="/tmp/recallforge-content-sync-$$.sql"

cleanup() {
  rm -f "$LOCAL_SQL"
  ssh "$SERVER" "rm -f '$REMOTE_SQL'" >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "Generating content sync script from local database (user_id=1)..."
python3 "$SCRIPT_DIR/generate_content_sync.py" > "$LOCAL_SQL"
echo "Generated $(wc -l < "$LOCAL_SQL") lines of SQL."

LOCAL_SUM="$(sha256sum "$LOCAL_SQL" | awk '{print $1}')"
echo "Local checksum: $LOCAL_SUM"

echo "Copying to server..."
scp -q "$LOCAL_SQL" "$SERVER:$REMOTE_SQL"

REMOTE_SUM="$(ssh "$SERVER" "sha256sum '$REMOTE_SQL' | awk '{print \$1}'")"
if [ "$LOCAL_SUM" != "$REMOTE_SUM" ]; then
  echo "ERROR: checksum mismatch after transfer (local=$LOCAL_SUM remote=$REMOTE_SUM)."
  echo "Nothing was applied."
  exit 1
fi
echo "Checksums match. Applying on server (single transaction, no downtime)..."

ssh "$SERVER" \
  "docker exec -i '$REMOTE_CONTAINER' psql -U '$DB_USER' -d '$DB_NAME' -v ON_ERROR_STOP=1 --single-transaction < '$REMOTE_SQL'"

echo "Template content synced to server."
