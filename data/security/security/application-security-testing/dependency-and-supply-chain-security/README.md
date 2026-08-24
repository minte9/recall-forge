## Security / Security / Application Security Testing / Dependency and Supply Chain Security

### 1. Vulnerable Dependencies

Most application code today is dependencies, not code the team wrote -- a CVE in a transitive dependency (something your dependency depends on) is just as exploitable as one in code you wrote yourself, and far easier to miss since it's not something anyone on the team reviewed directly.

~~~xml
<!-- transitive: pulled in by another dependency, easy to miss -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
~~~

### 2. Software Bill of Materials (SBOM)

An SBOM is a complete inventory of every dependency, direct and transitive, in a build, generated automatically. When a new CVE is announced, an SBOM lets a team answer "are we affected?" in minutes by searching one file, instead of manually auditing every project's dependency tree.

~~~bash
# Generate an SBOM (CycloneDX format) for a Maven build
mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
~~~
