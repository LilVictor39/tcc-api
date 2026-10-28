#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
umask 077
mkdir -p certs
if [ ! -f certs/.tls-password ]; then openssl rand -hex 24 > certs/.tls-password; fi
export TLS_PASSWORD="${TLS_PASSWORD:-$(cat certs/.tls-password)}"
if [ ! -f certs/localhost.p12 ]; then
 keytool -genkeypair -alias tcc-local -keyalg RSA -keysize 3072 -validity 365 \
  -storetype PKCS12 -keystore certs/localhost.p12 -storepass:env TLS_PASSWORD \
  -dname "CN=localhost, OU=TCC, O=Laboratorio, C=BR" \
  -ext 'SAN=dns:localhost,ip:127.0.0.1,ip:::1' -noprompt
fi
keytool -exportcert -rfc -alias tcc-local -keystore certs/localhost.p12 \
 -storepass:env TLS_PASSWORD -file certs/localhost.crt
printf 'Certificado local pronto. Certificado publico: certs/localhost.crt\n'
