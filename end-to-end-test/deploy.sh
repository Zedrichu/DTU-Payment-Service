#!/bin/bash
set -e
docker image prune -f

docker compose up -d rabbitMq

sleep 10

docker compose up -d dtupay-facade

sleep 2

docker compose up -d account-management
