#!/bin/bash
for var in SSH_HOST SSH_PORT SSH_USER SSH_KEY DOMAIN EMAIL; do
  [ -z "${!var}" ] && { echo "Error: $var is not set."; exit 1; }
done

echo "$SSH_KEY" > ssh_key
chmod 600 ssh_key

SSH_CMD="ssh -i ssh_key -o StrictHostKeyChecking=no -p $SSH_PORT $SSH_USER@$SSH_HOST"
SCP_CMD="scp -i ssh_key -P $SSH_PORT -o StrictHostKeyChecking=no"

echo "Installing Docker and Docker Compose on remote server..."
$SSH_CMD << 'EOF'
  command -v docker >/dev/null || {
    apt update && apt install -y docker.io
    systemctl start docker
    systemctl enable docker
  }

  command -v docker-compose >/dev/null || {
    apt update && apt install -y docker-compose
  }
EOF

echo "Transferring Docker image..."
docker save diasync-server:latest | $SSH_CMD docker load || { echo "Failed to transfer Docker image"; exit 1; }

echo "Creating remote directory and copying files..."
$SSH_CMD "mkdir -p ~/diasync"
$SCP_CMD .github/docker/{Dockerfile,Caddyfile,docker-compose.yml} "$SSH_USER@$SSH_HOST:~/diasync/"

echo "Starting Docker Compose..."
$SSH_CMD << EOF
  cd ~/diasync
  echo "DOMAIN=$DOMAIN" >> .env
  echo "EMAIL=$EMAIL" >> .env
  docker-compose down
  docker-compose up -d
EOF

echo "Cleaning up unused Docker images on remote server..."
$SSH_CMD << 'EOF'
  docker image prune -f
EOF

rm ssh_key
