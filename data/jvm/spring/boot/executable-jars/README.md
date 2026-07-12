## JVM / Spring / Boot / Executable JARs

### 1. The Fat/Executable JAR

`./gradlew bootJar` (or the Maven plugin) packages the app plus all its dependencies into one self-contained "fat" JAR, runnable with `java -jar app.jar` -- no classpath setup needed on the target machine. A plain `jar` task, by contrast, produces a JAR with no dependencies bundled, which won't run standalone.

~~~java
// ./gradlew bootJar  -> build/libs/app.jar (includes all dependencies)
// java -jar build/libs/app.jar             -> runs standalone, no extra classpath
~~~


### 2. Layered JARs for Docker Caching

A fat JAR changes on every build even if only application code changed, invalidating Docker's dependency layer cache. Spring Boot's layered JAR support splits the JAR into layers (dependencies, resources, application classes) so a Dockerfile can `COPY` and cache each layer separately -- only the app-code layer rebuilds on most changes.

~~~dockerfile
# Dockerfile using layered JARs -- dependency layers cached across builds
COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./   # only this layer changes on most rebuilds
~~~


### 3. bootRun and bootJar

`bootRun` (Gradle) or `spring-boot:run` (Maven) runs the app directly from source during development, without packaging a JAR first -- faster feedback than `bootJar` + `java -jar`. `bootJar`/`spring-boot:repackage` is what actually produces the deployable executable JAR.

~~~java
// Development: ./gradlew bootRun          -- fast, runs from source
// Deployment:  ./gradlew bootJar && java -jar build/libs/app.jar
~~~
