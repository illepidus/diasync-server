#!/bin/bash
required_vars=("BRANCH_NAME" "GITHUB_TOKEN")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: Required environment variable $var is missing."
        exit 1
    fi
done

git push origin --delete "$BRANCH_NAME"
echo "Release branch $BRANCH_NAME deleted due to workflow failure"
