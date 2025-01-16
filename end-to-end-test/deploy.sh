#!/bin/bash
set -e
docker image prune -f

docker-compose up -d rabbitMq

docker-compose up -d account-management dtupay-facade