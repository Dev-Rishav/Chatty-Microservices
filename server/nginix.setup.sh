#!/bin/bash

PUBLIC_IP=$(curl -s http://checkip.amazonaws.com)

NGINX_CONF="/etc/nginx/sites-available/chatty"

sudo tee $NGINX_CONF > /dev/null <<EOF
server {
    listen 80;
    server_name $PUBLIC_IP;

    location /gateway/ {
        proxy_pass http://localhost:8081/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /eureka/ {
        proxy_pass http://localhost:8761/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /auth/ {
        proxy_pass http://localhost:8082/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /user/ {
        proxy_pass http://localhost:8084/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /chat/ {
        proxy_pass http://localhost:8085/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /notification/ {
        proxy_pass http://localhost:8086/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

# Enable the config
sudo ln -sf $NGINX_CONF /etc/nginx/sites-enabled/chatty

# Test nginx configuration
sudo nginx -t

# Restart nginx
sudo systemctl restart nginx

echo "Nginx configuration for Chatty services has been set up and reloaded."
echo "Access services via: http://$PUBLIC_IP/{gateway,eureka,auth,user,chat,notification}/"
