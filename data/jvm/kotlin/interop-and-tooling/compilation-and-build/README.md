## JVM / Kotlin / Interop & Tooling / Compilation & Build

### 1. Compilation Model & Build Speed

`kotlinc` compiles to the same `.class` files `javac` produces, so it runs on any JVM unmodified. Kotlin's newer K2 compiler brought build times much closer to `javac`'s -- older Kotlin compiler versions were a common complaint for slower incremental builds on large codebases, less of a gap today.

### 2. Same JVM Bytecode, Different Runtime Guarantees

Kotlin isn't "faster at runtime" than Java -- both compile to JVM bytecode and run under the same JIT. What Kotlin buys you is compile-time guarantees (null safety, exhaustive `when` on sealed types) that eliminate whole categories of runtime exceptions Java would only catch by testing or by convention.
