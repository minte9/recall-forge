## DevOps / Docker / Volumes & Networking / Data Persistence

### 1. Volumes vs Bind Mounts

A volume is storage Docker manages itself (lives under Docker's data
dir); a bind mount maps a specific host path straight into the
container. Volumes are portable and easier to back up; bind mounts are
better for live-editing source code on the host.

~~~bash
docker run -v mydata:/var/lib/data postgres      # named volume
docker run -v $(pwd)/src:/app/src node             # bind mount
~~~

### 2. Named Volumes

A named volume persists independently of any single container's
lifecycle -- deleting the container that used it doesn't delete the
data, which is exactly what you want for a database's storage.

~~~bash
docker volume create mydata
docker run -v mydata:/var/lib/postgresql/data postgres
~~~

### 3. When Containers Lose Data

Anything written inside a container's writable layer (not a volume or
bind mount) disappears the moment the container is removed -- `docker
rm`, not just `stop`. Stateful data always needs to live outside that
layer.

~~~bash
docker run postgres          # data dies with `docker rm`
docker run -v pgdata:/var/lib/postgresql/data postgres  # data survives
~~~
