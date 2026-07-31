## DevOps / Docker / Fundamentals / Containers vs Virtual Machines

### 1. What a Container Is

A container packages an app with its dependencies but shares the host
machine's kernel, unlike a VM which virtualizes an entire guest OS.
That's why containers start in milliseconds and use a fraction of the
memory a VM needs.

~~~bash
docker run hello-world   # starts and exits almost instantly
~~~

### 2. Images vs Containers

An image is a read-only, versioned template (like a class); a container
is a running instance of that image (like an object). One image can back
many running containers at once, each with its own writable layer.

~~~bash
docker images         # list templates
docker ps -a           # list instances (running + stopped)
~~~

### 3. Namespaces & Cgroups

Linux namespaces give a container its own isolated view of processes,
network, and filesystem; cgroups cap how much CPU and memory it can use.
Docker doesn't invent isolation -- it packages these existing kernel
features behind a simple CLI.

~~~bash
docker run --memory=256m --cpus=0.5 myapp   # cgroup limits in practice
~~~
