## DevOps / Docker / Volumes & Networking / Network Drivers

### 1. Bridge vs Host vs None

`bridge` (default) gives each container its own private IP behind NAT.
`host` removes network isolation entirely, sharing the host's network
stack directly. `none` gives the container no networking at all.

~~~bash
docker run --network bridge nginx   # default, isolated + NAT
docker run --network host nginx      # shares host's ports directly
docker run --network none alpine     # no network access
~~~

### 2. Creating Custom Networks

A user-defined bridge network gives containers name-based DNS
resolution that the default bridge network doesn't provide, and lets
you group related containers (e.g. an app and its database) together.

~~~bash
docker network create app-net
docker run --network app-net --name db postgres
docker run --network app-net --name api myapi   # can reach "db" by name
~~~
