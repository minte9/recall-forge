## DevOps / Docker / Images & Dockerfiles / Optimizing Images

### 1. Multi-Stage Builds

Uses multiple `FROM` stages in one Dockerfile -- one with full build
tools to compile the app, a final slim stage that copies in only the
built artifact. The compiler and build deps never ship in the final
image.

~~~dockerfile
FROM golang:1.22 AS build
RUN go build -o app .

FROM alpine
COPY --from=build /app /app   # only the binary, not the Go toolchain
~~~

### 2. Choosing a Base Image

`alpine` and `-slim` variants trade some compatibility (musl libc,
fewer preinstalled tools) for a much smaller image than a full `debian`
or `ubuntu` base -- smaller images pull faster and shrink attack surface.

~~~dockerfile
FROM python:3.12-slim   # vs python:3.12 (full, much larger)
~~~

### 3. Minimizing Layers

Each `RUN` adds a layer; chaining related commands with `&&` into one
`RUN` avoids leaving intermediate layers (like apt cache) baked into the
image history.

~~~dockerfile
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
~~~
