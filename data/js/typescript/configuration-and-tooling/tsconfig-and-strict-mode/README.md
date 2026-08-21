## JS / TypeScript / Configuration & Tooling / tsconfig & Strict Mode

### 1. tsconfig.json Basics

`tsconfig.json` configures the compiler: which files to include, the
output JS target/module format, and type-checking strictness — `tsc`
reads it automatically from the project root.

~~~javascript
// tsconfig.json
{
  "compilerOptions": { "target": "ES2020", "module": "ESNext" },
  "include": ["src"]
}
~~~


### 2. strict Mode

`"strict": true` enables a bundle of stricter checks at once
(`noImplicitAny`, `strictNullChecks`, etc.) — the recommended default
for new projects, catching far more bugs.

~~~javascript
// tsconfig.json
{ "compilerOptions": { "strict": true } }
~~~


### 3. Type Checking vs Compilation

`tsc` both type-checks and emits JS by default, but tools like
Babel/esbuild strip types without checking them at all — a separate
`tsc --noEmit` step is needed to still catch type errors in that setup.

~~~javascript
// esbuild/babel: fast, but silently ignores type errors
// tsc --noEmit: type-checks only, catches what esbuild misses
~~~
