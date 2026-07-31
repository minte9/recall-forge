## DevOps / Docker / Volumes & Networking / Docker Networking Basics

### 1. Default Bridge Network

Without extra config, containers join a default bridge network with
their own private IP, isolated from the host's network by default --
that's why a service inside a container isn't reachable from outside
until a port is explicitly published.

~~~bash
docker network inspect bridge   # see connected containers + IPs
~~~

### 2. Publishing Ports (-p)

Maps a port on the host to a port inside the container, making an
otherwise-isolated service reachable from outside. Without `-p`, the
port only exists inside Docker's private network.

~~~bash
docker run -p 8080:80 nginx   # host:8080 -> container:80
~~~

### 3. Container-to-Container DNS

Containers on the same user-defined network can reach each other by
container/service name -- Docker runs an internal DNS resolver, so
`http://api:3000` works without hardcoding an IP.

~~~bash
docker network create app-net
docker run --network app-net --name api myapi
docker run --network app-net alpine ping api   # resolves by name
~~~
