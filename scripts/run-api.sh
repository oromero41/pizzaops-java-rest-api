#!/usr/bin/env bash
set -euo pipefail
: "${PIZZAOPS_DB_PASSWORD:?Set PIZZAOPS_DB_PASSWORD for PostgreSQL before running}"
cd "$(dirname "$0")/.."
mvn spring-boot:run
