#!/bin/sh
set -eu

: "${KEYCLOAK_LINKEDIN_CLIENT_ID:?KEYCLOAK_LINKEDIN_CLIENT_ID is required — set it in .env}"
: "${KEYCLOAK_LINKEDIN_CLIENT_SECRET:?KEYCLOAK_LINKEDIN_CLIENT_SECRET is required — set it in .env}"

mkdir -p /opt/keycloak/data/import

sed \
  -e "s|\${KEYCLOAK_LINKEDIN_CLIENT_ID}|${KEYCLOAK_LINKEDIN_CLIENT_ID}|g" \
  -e "s|\${KEYCLOAK_LINKEDIN_CLIENT_SECRET}|${KEYCLOAK_LINKEDIN_CLIENT_SECRET}|g" \
  /opt/keycloak/data/import-src/realm.json \
  > /opt/keycloak/data/import/realm-export.json

exec /opt/keycloak/bin/kc.sh "$@"
