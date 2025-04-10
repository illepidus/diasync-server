#!/bin/bash

required_vars=("SSH_HOST" "SSH_PORT" "SSH_USER" "SSH_KEY" "DOMAIN" "EMAIL")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: Required environment variable $var is missing."
        exit 1
    fi
done

echo "$SSH_KEY" > ssh_key
chmod 600 ssh_key

SSH_CMD="ssh -i ssh_key -o StrictHostKeyChecking=no -p $SSH_PORT $SSH_USER@$SSH_HOST"
SCP_CMD="scp -i ssh_key -P $SSH_PORT -o StrictHostKeyChecking=no"

echo "Installing Docker and Docker Compose on remote server..."
$SSH_CMD << 'EOF'
    if ! command -v docker >/dev/null 2>&1; then
        apt update
        apt install -y docker.io
        systemctl start docker
        systemctl enable docker
    fi
    if ! command -v docker-compose >/dev/null 2>&1; then
        apt update
        apt install -y docker-compose
    fi
EOF

echo "Transferring Docker image..."
docker save diasync-server:latest | $SSH_CMD docker load || { echo "Failed to transfer Docker image"; exit 1; }


echo "Creating remote directory and copying files..."
$SSH_CMD "mkdir -p ~/diasync"
$SCP_CMD .github/docker/{Dockerfile,Caddyfile,docker-compose.yml} "$SSH_USER@$SSH_HOST:~/diasync/"

echo "Starting Docker Compose..."
$SSH_CMD << EOF
    cd ~/diasync
    printf "DOMAIN=%s\nEMAIL=%s\n" "$DOMAIN" "$EMAIL" > .env
    docker-compose down
    docker-compose up -d
EOF

echo "Cleaning up unused Docker images on remote server..."
$SSH_CMD << 'EOF'
    docker image prune -f
EOF

rm ssh_key
