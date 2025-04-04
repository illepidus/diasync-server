#!/bin/bash

set -e

if [ -z "$DOMAIN" ]; then
  echo "Error: DOMAIN environment variable is missing."
  exit 1
fi

echo "Checking /ping endpoint..."
TIMEOUT=300
INTERVAL=10
ELAPSED=0
while [ $ELAPSED -lt $TIMEOUT ]; do
  if curl -s "https://${DOMAIN}/ping" | grep -q "pong"; then
    echo "Deployment successful: /ping endpoint responded with 'pong'"
    break
  fi
  echo "Waiting for /ping endpoint... ($ELAPSED/$TIMEOUT seconds)"
  sleep $INTERVAL
  ELAPSED=$((ELAPSED + INTERVAL))
done

if [ $ELAPSED -ge $TIMEOUT ]; then
  echo "Deployment failed: /ping endpoint did not respond with 'pong' within 5 minutes"
  exit 1
fi

echo "Checking WebSocket connectivity to wss://${DOMAIN}/graphql..."

npm install -g wscat

timeout 10s wscat -c "wss://${DOMAIN}/graphql" <<EOF
EOF

if [ $? -eq 0 ]; then
  echo "WebSocket connection successful"
else
  echo "WebSocket connection failed"
  exit 1
fi
