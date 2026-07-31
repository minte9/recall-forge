## DevOps / Docker / Docker Compose Fundamentals / Compose Networking

### 1. Default Compose Network

Compose automatically creates one shared network per project and joins
every service to it, without any explicit `networks:` config -- this is
what's happening by default before you ever customize networking.

~~~yaml
services:
  web:
    image: nginx
  api:
    image: myapi
# both land on the same auto-created default network
~~~

### 2. Service Name Resolution

Any service can reach any other by its service name as a hostname --
Compose's default network provides this DNS resolution for free. The
JVM/Microservices/Docker Compose subcategory covers the applied version
of this same mechanism (host/port env vars wired to a real service).

~~~yaml
services:
  api:
    environment:
      DB_HOST: db   # resolves to the "db" service, no IP needed
  db:
    image: postgres
~~~
