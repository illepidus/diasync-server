#!/bin/bash

if [ -z "$SSH_HOST" ] || [ -z "$SSH_PORT" ] || [ -z "$SSH_USER" ] || [ -z "$SSH_KEY" ] || [ -z "$DOMAIN" ] || [ -z "$EMAIL" ]; then
  echo "Error: One or more required environment variables are missing."
  exit 1
fi

echo "$SSH_KEY" > ssh_key
chmod 600 ssh_key

ssh -i ssh_key -o StrictHostKeyChecking=no -p "$SSH_PORT" "$SSH_USER@$SSH_HOST" << 'EOF'
  if ! command -v docker >/dev/null 2>&1; then
    apt-get update
    apt-get install -y docker.io
    systemctl start docker
    systemctl enable docker
  fi
  if ! command -v docker-compose >/dev/null 2>&1; then
    apt-get update
    apt-get install -y docker-compose
  fi
EOF

docker save diasync-server:latest | ssh -i ssh_key -o StrictHostKeyChecking=no -p "$SSH_PORT" "$SSH_USER@$SSH_HOST" docker load

ssh -i ssh_key -o StrictHostKeyChecking=no -p "$SSH_PORT" "$SSH_USER@$SSH_HOST" "mkdir -p ~/diasync-deploy"
scp -i ssh_key -P "$SSH_PORT" -o StrictHostKeyChecking=no deploy/Dockerfile deploy/Caddyfile deploy/docker-compose.yml "$SSH_USER@$SSH_HOST:~/diasync-deploy/"
ssh -i ssh_key -o StrictHostKeyChecking=no -p "$SSH_PORT" "$SSH_USER@$SSH_HOST" << EOF
  cd ~/diasync-deploy
  echo "DOMAIN=$DOMAIN" > .env
  echo "EMAIL=$EMAIL" >> .env
  docker-compose down
  docker-compose up -d
EOF

rm ssh_key
