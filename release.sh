#!/bin/bash
gh workflow run release.yml --ref master
echo https://github.com/illepidus/diasync-server/actions/workflows/release.yml
