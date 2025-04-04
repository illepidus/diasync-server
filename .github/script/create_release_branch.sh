#!/bin/bash

set -e

if [ -z "$GITHUB_TOKEN" ]; then
  echo "Error: GITHUB_TOKEN is not set."
  exit 1
fi

DATE=$(date +%Y-%m-%d)
COUNT=$(git branch -r | grep "release-$DATE" | wc -l | xargs)
NEW_COUNT=$((COUNT + 1))
BRANCH_NAME="release-$DATE-$NEW_COUNT"

git checkout -b "$BRANCH_NAME"
git push origin "$BRANCH_NAME"

if [ -n "$GITHUB_OUTPUT" ]; then
  echo "branch_name=$BRANCH_NAME" >> "$GITHUB_ENV"
else
  echo "Created branch: $BRANCH_NAME"
fi
