#!/bin/bash
set -e
docker image prune -f

for dir in messaging-utilities dtupay-facade account-management-service token-management-service payment-management-service; do
  pushd $dir
  ./build.sh
  popd
done