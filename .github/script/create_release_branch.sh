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
COMMIT_HASH=$(git rev-parse HEAD)
APP_VERSION="${BRANCH_NAME}@${COMMIT_HASH}"

git checkout -b "$BRANCH_NAME"
git push origin "$BRANCH_NAME"
sed -i "s/version = .*/version = '$APP_VERSION'/" build.gradle

echo "BRANCH_NAME=$BRANCH_NAME" >> "$GITHUB_ENV"
echo "APP_VERSION=$APP_VERSION" >> "$GITHUB_ENV"
