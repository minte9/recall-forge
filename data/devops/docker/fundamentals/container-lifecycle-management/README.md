## DevOps / Docker / Fundamentals / Container Lifecycle Management

### 1. Restart Policies

Tell Docker when to automatically restart a container -- on crash, always,
or only if it hasn't been manually stopped. This is what keeps a
long-running service like a database container up after a host reboot.

~~~bash
docker run -d --restart unless-stopped postgres
~~~

### 2. Stopping vs Killing

`docker stop` sends SIGTERM and waits (default 10s) for graceful
shutdown before force-killing; `docker kill` sends SIGKILL immediately.
Prefer `stop` so the app can flush buffers or close connections cleanly.

~~~bash
docker stop my-container    # graceful, waits then force-kills
docker kill my-container     # immediate, no grace period
~~~

### 3. Foreground vs Background Trade-offs

A foreground container ties up your terminal and dies if the terminal
closes; a detached one survives terminal closure but needs `docker logs`
to see its output after the fact instead of watching it live.

~~~bash
docker run nginx        # foreground: Ctrl+C stops it
docker run -d nginx      # background: survives terminal close
docker logs -f <id>       # tail a detached container's output
~~~
