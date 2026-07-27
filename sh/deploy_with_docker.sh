#!/usr/bin/env bash

set -e

COMPOSE_FILE="compose.prod.yml"

docker compose -f "$COMPOSE_FILE" pull app
docker compose -f "$COMPOSE_FILE" up -d app
docker compose -f "$COMPOSE_FILE" ps