## DevOps / Docker / Images & Dockerfiles / Image Distribution

### 1. docker push / pull

`push` uploads a locally tagged image to a registry; `pull` downloads
one. The tag's registry/namespace prefix (or its absence) determines
which registry a push or pull actually targets.

~~~bash
docker tag myapp:1.0 myuser/myapp:1.0
docker push myuser/myapp:1.0
docker pull myuser/myapp:1.0
~~~

### 2. Docker Hub vs Private Registries

Docker Hub is the default public registry and needs no prefix; a
private registry (self-hosted, ECR, GCR) needs its hostname in the tag
so Docker knows where to push/pull instead of defaulting to Hub.

~~~bash
docker push myuser/myapp:1.0              # Docker Hub
docker push registry.company.com/myapp:1.0  # private registry
~~~

### 3. Tags vs Digests

A tag like `1.0` or `latest` can be reassigned to a different image
later; a digest (`sha256:...`) is an immutable, content-addressed
reference to one exact image, guaranteeing you get the exact bytes
expected.

~~~bash
docker pull myapp@sha256:9f2a1b...   # exact image, never changes
~~~
