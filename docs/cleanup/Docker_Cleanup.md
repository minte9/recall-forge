With time container runtime data growing under /var/lib/containerd and /var/lib/docker:

- old image layers
- build cache
- stopped containers
- unused volumes
- container logs

How to prevent it:

df -h

docker system df -v
docker container prune
docker image prune -a
docker volume prune
docker builder prune