#!/bin/bash
set -e
docker image prune -f

cd ..

for dir in messaging-utilities dtupay-facade account-management-service token-management-service payment-management-service reporting-service; do
  pushd $dir
  ./build.sh
  popd
done

cd end-to-end-test

docker compose up -d rabbitMq

sleep 3

docker compose up -d account-management token-management payment-management reporting

sleep 1

docker compose up -d dtupay-facade

mvn clean test

# delay finish
sleep 1

docker compose down
