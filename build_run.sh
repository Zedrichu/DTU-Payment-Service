#!/bin/bash
set -e

pushd dtupay-quarkus-server
mvn package
docker compose build
docker compose up -d

# clean up images
docker image prune -f
popd

# delay server
sleep 2

pushd rest-dtupay-client
mvn clean test
popd

# delay finish
sleep 1

cd dtupay-quarkus-server
docker compose down
