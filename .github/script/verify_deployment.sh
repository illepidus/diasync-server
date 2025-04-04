#!/bin/bash
required_vars=("DOMAIN" "EXPECTED_VERSION")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: Required environment variable $var is missing."
        exit 1
    fi
done

echo "Checking /version endpoint..."

DEPLOYED_VERSION=$(curl -s --retry 24 --retry-delay 5 "https://${DOMAIN}/version")
if [ "$DEPLOYED_VERSION" != "$EXPECTED_VERSION" ]; then
  echo "Deployment verification failed: expected $EXPECTED_VERSION, but got $DEPLOYED_VERSION"
  exit 1
else
  echo "Deployment verified: version $DEPLOYED_VERSION matches expected $EXPECTED_VERSION"
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
