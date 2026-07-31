## DevOps / Docker / Images & Dockerfiles / Writing a Dockerfile

### 1. FROM / COPY / RUN / CMD

`FROM` picks the base image, `COPY` brings files in, `RUN` executes
commands at build time (each becomes a layer), and `CMD` sets the
default command a container runs when started.

~~~dockerfile
FROM node:20-alpine
COPY . /app
RUN npm install --production
CMD ["node", "/app/index.js"]
~~~

### 2. ENTRYPOINT vs CMD

`CMD` sets default arguments that `docker run` can override entirely.
`ENTRYPOINT` fixes the executable itself; anything passed to `docker
run` is appended as arguments instead of replacing it.

~~~dockerfile
ENTRYPOINT ["node"]
CMD ["/app/index.js"]
# `docker run img other.js` replaces just this default arg
~~~

### 3. Layer Caching

Each instruction creates a cached layer; Docker reuses layers unchanged
since the last build. Ordering matters -- put rarely-changing steps
(like dependency install) before frequently-changing ones (like source
copy) to maximize cache hits.

~~~dockerfile
COPY package.json .
RUN npm install        # cached unless package.json changes
COPY . .                # invalidated on every source change
~~~
