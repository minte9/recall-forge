## JVM / Spring / Boot / DevTools

### 1. Automatic Restart on Classpath Changes

`spring-boot-devtools` restarts the app automatically when it detects a classpath change (e.g. after recompiling in your IDE), using two classloaders so only your application classes reload -- not third-party libraries -- making restarts much faster than a full JVM relaunch.

~~~java
// build.gradle: developmentOnly("org.springframework.boot:spring-boot-devtools")
// Save a file in your IDE -> app restarts automatically, libraries stay loaded
~~~


### 2. LiveReload

DevTools also runs an embedded LiveReload server that triggers a browser refresh automatically after a restart, when paired with a LiveReload browser extension -- closing the loop from "save file" to "see the change" without manually reloading the page.

~~~java
// No code needed -- install a LiveReload browser extension,
// DevTools' embedded server notifies it after each restart.
~~~


### 3. Property Defaults for Local Development

DevTools disables template caching and other production-oriented optimizations by default in development, so template changes show up without a restart. It only activates when the app is run from an IDE/exploded classpath -- it silently does nothing in a packaged production JAR, so it's safe to leave on the classpath.

~~~properties
# DevTools automatically sets sensible dev defaults, e.g.:
spring.thymeleaf.cache=false   # no restart needed to see template edits
~~~
