## Docker

With time container runtime data growing under /var/lib/containerd and /var/lib/docker:

- old image layers
- build cache
- stopped containers
- unused volumes
- container logs

How to prevent it:

~~~sh
df -h

docker system df -v
docker container prune
docker image prune -a
docker volume prune
docker builder prune
~~~

### 1. Docker Image

~~~sh
# Run
docker compose build app

# Check
docker image ls minte9/recallforge
docker image ls

# Cleanup
docker image prune

# Disk usage
docker system df

    # TYPE            TOTAL     ACTIVE    SIZE      RECLAIMABLE
    # Images          12        12        4.621GB   812.1MB (17%)
    # Containers      13        13        131.8kB   0B (0%)
    # Local Volumes   16        6         6.613GB   5.36GB (81%)
    # Build Cache     87        0         560.9MB   560.9MB

# Remove unused
docker system prune
docker system prune -a

# Volumes
docker volume ls
docker system df -v
docker volume prune -f
~~~


## 2. Docker Hub

~~~sh
docker login
    # Your password will be stored unencrypted in
    # /home/catalin/.docker/config.json.

docker images
    # REPOSITORY                              TAG
    # your-dockerhub-user/recall-forge        1.0.0

docker compose push
~~~

Your production Compose file should not contain:

~~~sh
build: .

# It should contain only the published image:
app:
  image: minte9/recallforge:1.0.1
~~~


### 3. Deployment

Then deployment becomes:

~~~sh
docker compose -f compose.prod.yml pull
docker compose -f compose.prod.yml up -d
~~~

On production, create deploy.sh:

~~~sh
#!/usr/bin/env bash

set -euo pipefail

COMPOSE_FILE="compose.prod.yml"

docker compose -f "$COMPOSE_FILE" pull app
docker compose -f "$COMPOSE_FILE" up -d app
docker compose -f "$COMPOSE_FILE" ps
~~~

Make it executable:

~~~sh
chmod +x deploy.sh
~~~


### 4. Docker Hub Workflow

~~~sh
docker login
docker compose push

docker compose -f compose.prod.yml pull
docker compose -f compose.prod.yml up -d

# sh sh/deploy_with_docker.sh
~~~
