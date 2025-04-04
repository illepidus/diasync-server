#!/bin/bash

set -e

if [ -z "$GITHUB_TOKEN" ]; then
  echo "Error: GITHUB_TOKEN is not set."
  exit 1
fi

if [ -z "$BRANCH_NAME" ]; then
  echo "Error: BRANCH_NAME is not set."
  exit 1
fi

git push origin --delete "$BRANCH_NAME"
echo "Release branch $BRANCH_NAME deleted due to workflow failure"
