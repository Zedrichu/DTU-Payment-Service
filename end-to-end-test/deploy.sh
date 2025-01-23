#!/bin/bash
set -e
docker image prune -f

docker compose up -d rabbitMq

sleep 3

docker compose up -d account-management token-management payment-management reporting

sleep 4

docker compose up -d dtupay-facade
