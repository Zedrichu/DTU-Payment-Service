#!/bin/bash
set -e
docker image prune -f

cd ..

# Setup by Adrian Zvizdenco (s204683)
for dir in messaging-utilities dtupay-facade account-management-service; do
  pushd $dir
  ./build.sh
  popd
done

cd end-to-end-test

docker compose up -d rabbitMq

sleep 5

docker compose up -d account-management dtupay-facade
