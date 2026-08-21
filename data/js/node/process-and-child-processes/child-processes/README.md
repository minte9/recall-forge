## JS / Node.js / Process & Child Processes / Child Processes

### 1. spawn vs exec

`spawn` streams a child process's output incrementally, good for
long-running or large-output commands; `exec` buffers the entire output
in memory and returns it once the process finishes.

~~~javascript
const { spawn } = require("child_process");
const ls = spawn("ls", ["-la"]);
ls.stdout.on("data", chunk => console.log(chunk.toString()));
~~~


### 2. fork

`fork()` spawns a new Node process running a given script and sets up a
built-in message channel between parent and child — specialized for
running other Node scripts, not arbitrary commands.

~~~javascript
const { fork } = require("child_process");
const child = fork("worker.js");
child.send({ task: "process-data" });
~~~


### 3. Communicating with a Child Process

Forked processes exchange structured messages via `.send()`/`"message"`
events; `spawn`/`exec`'d processes only expose raw stdin/stdout/stderr
streams, with no built-in message protocol.

~~~javascript
child.on("message", (result) => console.log("from child:", result));
~~~
