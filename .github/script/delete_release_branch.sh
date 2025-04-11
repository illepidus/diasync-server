#!/bin/bash
for var in BRANCH_NAME GITHUB_TOKEN; do
  [ -z "${!var}" ] && { echo "Error: $var is not set."; exit 1; }
done

git push origin --delete "$BRANCH_NAME"
echo "Release branch $BRANCH_NAME deleted due to workflow failure"
