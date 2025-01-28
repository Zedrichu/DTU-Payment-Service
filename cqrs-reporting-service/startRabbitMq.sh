#!/bin/sh
set -e
docker run -d -p 5672:5672 --hostname ext-rabbit --name some-rabbit rabbitmq:3-management