#!/bin/bash

set -e

if [ -z "$DOMAIN" ]; then
  echo "Error: DOMAIN environment variable is missing."
  exit 1
fi

echo "Checking /version endpoint..."

DEPLOYED_VERSION=$(curl -s --retry 24 --retry-delay 5 "https://${DOMAIN}/version")
if [ "$DEPLOYED_VERSION" != "$EXPECTED_COMMIT" ]; then
  echo "Deployment verification failed: expected $EXPECTED_COMMIT, but got $DEPLOYED_VERSION"
  exit 1
else
  echo "Deployment verified: version $DEPLOYED_VERSION matches expected $EXPECTED_COMMIT"
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
