# DevOps / Docker / Container Management & Debugging / Copying & Env Config

## 1. docker cp

Copies files between a container's filesystem and the host in either
direction, without needing a shared volume -- handy for grabbing a log
file or config out of a container after the fact.

```bash
docker cp my-container:/app/error.log ./error.log
docker cp ./config.yml my-container:/app/config.yml
```

## 2. Env Vars (-e / --env-file)

`-e` sets one environment variable per flag; `--env-file` loads many at
once from a file -- the usual way to inject config (URLs, feature
flags) into a container without baking it into the image.

```bash
docker run -e NODE_ENV=production myapp
docker run --env-file .env.production myapp
```

## 3. Overriding CMD at Runtime

Any command appended after the image name in `docker run` replaces the
Dockerfile's `CMD` entirely -- useful for running a one-off shell or
script in the same image without changing the Dockerfile.

```bash
docker run myapp                 # runs the Dockerfile's CMD
docker run myapp npm test          # runs this instead
```
