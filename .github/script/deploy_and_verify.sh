#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_SCRIPT="$SCRIPT_DIR/deploy_to_server.sh"
VERIFY_SCRIPT="$SCRIPT_DIR/verify_deployment.sh"

MAX_ATTEMPTS=3
RETRY_DELAY=5

for attempt in $(seq 1 "$MAX_ATTEMPTS"); do
  echo "=== Attempt $attempt of $MAX_ATTEMPTS: Deploying and verifying ==="

  if bash "$DEPLOY_SCRIPT" && bash "$VERIFY_SCRIPT"; then
    echo "Deployment successful on attempt $attempt"
    exit 0
  else
    echo "Attempt $attempt failed"
    if [ "$attempt" -lt "$MAX_ATTEMPTS" ]; then
      echo "Retrying in ${RETRY_DELAY}s..."
      sleep "$RETRY_DELAY"
    fi
  fi
done

echo "Deployment failed after $MAX_ATTEMPTS attempts."
exit 1
