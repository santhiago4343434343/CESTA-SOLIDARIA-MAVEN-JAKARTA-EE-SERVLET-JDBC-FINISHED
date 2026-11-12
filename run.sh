#!/usr/bin/env bash
set -a
source .env
set +a
mvn package cargo:run
