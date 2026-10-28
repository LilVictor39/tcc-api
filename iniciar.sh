#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [ -z "${JWT_SECRET:-}" ]; then
 if [ ! -f .jwt-secret ]; then (umask 077; openssl rand -base64 32 > .jwt-secret); fi
 export JWT_SECRET="$(cat .jwt-secret)"
fi
bash preparar_https.sh
export TLS_PASSWORD="${TLS_PASSWORD:-$(cat certs/.tls-password)}"
# HTTPS e o modo padrao desta entrega. Nao abre uma segunda porta HTTP.
export SPRING_PROFILES_ACTIVE=https
export SERVER_PORT="${SERVER_PORT:-8443}"
docker compose up -d --wait
mvn spring-boot:run
