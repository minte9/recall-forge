## DevOps / Docker / Container Management & Debugging / Managing Containers

### 1. docker ps / stop / rm

`ps` lists running containers (`-a` includes stopped ones); `stop` ends
a running one gracefully; `rm` deletes a stopped container's metadata
and writable layer entirely.

~~~bash
docker ps -a
docker stop my-container
docker rm my-container
~~~

### 2. docker exec

Runs an additional command inside an already-running container, most
often to open a debugging shell -- distinct from `docker run`, which
starts a brand-new container from an image.

~~~bash
docker exec -it my-container sh   # shell into a running container
~~~

### 3. docker logs

Shows the stdout/stderr a container's main process has produced since
it started -- the first place to look when a container is misbehaving
and there's no shell access yet.

~~~bash
docker logs my-container
docker logs -f my-container   # follow, like tail -f
~~~
