#!/bin/bash
cat << 'EOF' > /home/ubuntu/startup.sh
#!/bin/bash
exec > >(tee /var/log/user-data.log) 2>&1

# Update package manager and install packages without prompts
DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y docker.io

# Start and enable Docker service
systemctl start docker
systemctl enable docker

# Add ubuntu user to docker group
usermod -aG docker ubuntu

# Install Docker Compose without prompts
DEBIAN_FRONTEND=noninteractive apt-get install -y docker-compose

# Wait for Docker to be ready
until docker info > /dev/null 2>&1; do
    echo "Waiting for Docker to start..."
    sleep 1
done

# Start your services
cd /home/ubuntu/microservice/authservicedocker
docker-compose up -d
EOF

chmod +x /home/ubuntu/startup.sh
/home/ubuntu/startup.sh