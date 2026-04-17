#!/usr/bin/env bash
# Creates all databases listed in POSTGRES_MULTIPLE_DATABASES (comma-separated).
# Each database is owned by POSTGRES_USER and is created only if it does not exist.
set -euo pipefail

create_database() {
    local db="$1"
    echo "  Creating database '$db'..."
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        SELECT 'CREATE DATABASE $db OWNER $POSTGRES_USER'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
EOSQL
}

if [ -n "${POSTGRES_MULTIPLE_DATABASES:-}" ]; then
    echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
    IFS=',' read -ra DBS <<< "$POSTGRES_MULTIPLE_DATABASES"
    for db in "${DBS[@]}"; do
        db="$(echo "$db" | xargs)"   # trim whitespace
        create_database "$db"
    done
    echo "All databases created."
fi
