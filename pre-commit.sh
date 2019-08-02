#!/usr/bin/env bash

set -e

./gradlew --daemon --parallel clean build intTest installDist 2>&1

./startup.sh skip-build

./kill-all-the-services.sh

echo SUCCESS!
