#!/bin/bash
for var in DOMAIN APP_VERSION; do
  [ -z "${!var}" ] && { echo "Error: $var is not set."; exit 1; }
done

echo "Checking deployed version endpoint"
DEPLOYED_VERSION=$(curl -s --retry 24 --retry-delay 5 "https://${DOMAIN}/api/v1/version")
if [ "$DEPLOYED_VERSION" != "$APP_VERSION" ]; then
  echo "Deployment verification failed: expected $APP_VERSION, but got $DEPLOYED_VERSION"
  exit 1
else
  echo "Deployment verified: version $DEPLOYED_VERSION matches expected $APP_VERSION"
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
