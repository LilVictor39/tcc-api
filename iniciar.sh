#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [ -z "${JWT_SECRET:-}" ]; then
 if [ ! -f .jwt-secret ]; then (umask 077; openssl rand -base64 32 > .jwt-secret); fi
 export JWT_SECRET="$(cat .jwt-secret)"
fi
docker compose up -d --wait
mvn spring-boot:run
