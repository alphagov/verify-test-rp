#!/usr/bin/env bash

if test -e local.env; then
    set -a
    source local.env
    set +a
else
    printf "$(tput setaf 1)No local environment found. Use verify-local-startup or openssl to generate a local.env file\n$(tput sgr0)"
    echo "verify-local-startup$ ruby generate-env.rb -f ../verify-test-rp/local.env"
    exit
fi

source ../verify-local-startup/lib/services.sh
source ../verify-local-startup/config/env.sh

build_service ../verify-test-rp
start_service verify-test-rp ../verify-test-rp configuration/local/test-rp.yml $TEST_RP_PORT
wait
