## DevOps / Docker / Fundamentals / Running Your First Container

### 1. docker run Basics

Pulls the image if it isn't already local, creates a container from it,
and starts it -- all in one command. Under the hood it's really
`docker create` + `docker start` combined.

~~~bash
docker run nginx           # pulls (if needed), creates, starts
docker run -d nginx         # same, but detached (background)
~~~

### 2. Interactive vs Detached Mode

`-it` attaches your terminal to the container's stdin/stdout, useful for
shells and REPLs. `-d` runs it in the background instead and hands
control straight back to your terminal.

~~~bash
docker run -it ubuntu bash   # interactive shell inside the container
docker run -d nginx           # background, returns immediately
~~~

### 3. Container Lifecycle States

A container moves through created -> running -> (paused) -> exited ->
removed. Stopping doesn't delete it -- a stopped container can be
restarted later and still holds its writable-layer state.

~~~bash
docker ps -a          # includes exited containers
docker start <id>      # resume a stopped one
~~~
