#!/usr/bin/env bash

set -e

ROOT_DIR="$(dirname "$0")"
cd "$ROOT_DIR"

cp ../../../verify-dev-pki/src/main/resources/dev-keys/sample_rp_signing_primary.* .
cp ../../../verify-dev-pki/src/main/resources/dev-keys/sample_rp_signing_secondary.* .

openssl pkcs8 -in sample_rp_signing_primary.pk8 -inform DER -nocrypt -out sample_rp_signing_primary.private.pem
openssl pkcs8 -in sample_rp_signing_secondary.pk8 -inform DER -nocrypt -out sample_rp_signing_secondary.private.pem
