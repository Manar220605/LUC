#!/bin/sh
set -eu

: "${KEYCLOAK_LINKEDIN_CLIENT_ID:?KEYCLOAK_LINKEDIN_CLIENT_ID is required — set it in .env}"
: "${KEYCLOAK_LINKEDIN_CLIENT_SECRET:?KEYCLOAK_LINKEDIN_CLIENT_SECRET is required — set it in .env}"

# Defaults target Mailhog on the compose network. Override in .env to point at a real
# provider such as Mailgun without touching realm-export.json.
SMTP_HOST="${KEYCLOAK_SMTP_HOST:-mailhog}"
SMTP_PORT="${KEYCLOAK_SMTP_PORT:-1025}"
SMTP_FROM="${KEYCLOAK_SMTP_FROM:-noreply@luc.local}"
SMTP_SSL="${KEYCLOAK_SMTP_SSL:-false}"
SMTP_STARTTLS="${KEYCLOAK_SMTP_STARTTLS:-false}"
SMTP_AUTH="${KEYCLOAK_SMTP_AUTH:-false}"
SMTP_USER="${KEYCLOAK_SMTP_USER:-}"
SMTP_PASSWORD="${KEYCLOAK_SMTP_PASSWORD:-}"

# Backslash, ampersand and the "|" delimiter are special in a sed replacement, and SMTP
# passwords routinely contain them.
escape_sed() {
  printf '%s' "$1" | sed -e 's/[\\&|]/\\&/g'
}

mkdir -p /opt/keycloak/data/import

sed \
  -e "s|\${KEYCLOAK_LINKEDIN_CLIENT_ID}|$(escape_sed "$KEYCLOAK_LINKEDIN_CLIENT_ID")|g" \
  -e "s|\${KEYCLOAK_LINKEDIN_CLIENT_SECRET}|$(escape_sed "$KEYCLOAK_LINKEDIN_CLIENT_SECRET")|g" \
  -e "s|\${KEYCLOAK_SMTP_HOST}|$(escape_sed "$SMTP_HOST")|g" \
  -e "s|\${KEYCLOAK_SMTP_PORT}|$(escape_sed "$SMTP_PORT")|g" \
  -e "s|\${KEYCLOAK_SMTP_FROM}|$(escape_sed "$SMTP_FROM")|g" \
  -e "s|\${KEYCLOAK_SMTP_SSL}|$(escape_sed "$SMTP_SSL")|g" \
  -e "s|\${KEYCLOAK_SMTP_STARTTLS}|$(escape_sed "$SMTP_STARTTLS")|g" \
  -e "s|\${KEYCLOAK_SMTP_AUTH}|$(escape_sed "$SMTP_AUTH")|g" \
  -e "s|\${KEYCLOAK_SMTP_USER}|$(escape_sed "$SMTP_USER")|g" \
  -e "s|\${KEYCLOAK_SMTP_PASSWORD}|$(escape_sed "$SMTP_PASSWORD")|g" \
  /opt/keycloak/data/import-src/realm.json \
  > /opt/keycloak/data/import/realm-export.json

exec /opt/keycloak/bin/kc.sh "$@"
