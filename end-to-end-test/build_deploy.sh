#!/bin/bash
set -e
docker image prune -f

cd ..

# Setup by Adrian Zvizdenco (s204683)
for dir in messaging-utilities dtupay-facade account-management-service token-management-service token-management-service payment-management-service; do
  pushd $dir
  ./build.sh
  popd
done

cd end-to-end-test

docker compose up -d rabbitMq

sleep 2

docker compose up -d account-management payment-management token-management dtupay-facade
