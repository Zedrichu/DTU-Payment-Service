#!/bin/bash
set -e
mvn clean package
docker compose build cqrs-reporting