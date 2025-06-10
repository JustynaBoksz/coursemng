#!/bin/bash

echo "🔄 Budowanie i uruchamianie środowiska..."
docker-compose --env-file .env up --build