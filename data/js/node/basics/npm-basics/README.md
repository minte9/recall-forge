## JS / Node / Basics / npm Basics

### 1. package.json

`package.json` declares a project's metadata, dependencies, and scripts;
`npm init` scaffolds one, and it's the file npm reads to know what to
install and how to run things.

~~~javascript
// package.json
{
  "name": "my-app",
  "dependencies": { "express": "^4.18.0" }
}
~~~


### 2. Installing Dependencies

`npm install` reads `package.json` and downloads packages into
`node_modules`, writing exact resolved versions to `package-lock.json`
so installs are reproducible across machines.

~~~javascript
// npm install express         -- adds to dependencies
// npm install --save-dev jest -- adds to devDependencies
~~~


### 3. npm Scripts

The `scripts` field defines shortcuts runnable via `npm run <name>`
(`start` and `test` don't need `run`), keeping common commands
documented and consistent across a team.

~~~javascript
// package.json
{
  "scripts": { "start": "node app.js", "test": "jest" }
}
~~~
