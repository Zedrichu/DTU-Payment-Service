#!/bin/bash
set -e

pushd rest-simple-pay-quarkus-server
mvn package
docker compose build
docker compose up -d

# clean up images
docker image prune -f
popd

# delay server
sleep 2

pushd rest-simple-pay-client
mvn clean test
popd

# delay finish
sleep 1

cd rest-simple-pay-quarkus-server
docker compose down
