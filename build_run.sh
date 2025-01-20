#!/bin/bash
set -e
docker image prune -f

./build_all.sh

cd end-to-end-test

docker compose up -d rabbitMq

sleep 3

docker compose up -d dtupay-facade

sleep 1

docker compose up -d account-management token-management payment-management

mvn clean test

# delay finish
sleep 1

docker compose down
