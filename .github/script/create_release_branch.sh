#!/bin/bash
required_vars=("GITHUB_TOKEN")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: Required environment variable $var is missing."
        exit 1
    fi
done

DATE=$(date +%Y-%m-%d)
COUNT=$(git branch -r | grep "release-$DATE" | wc -l | xargs)
NEW_COUNT=$((COUNT + 1))
BRANCH_NAME="release-$DATE-$NEW_COUNT"

git checkout -b "$BRANCH_NAME"
git push origin "$BRANCH_NAME"

echo "BRANCH_NAME=$BRANCH_NAME" >> "$GITHUB_ENV"
