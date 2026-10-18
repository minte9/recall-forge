## 1. Node.js and npm on Ubuntu

Angular applications are developed using Node.js, a JavaScript runtime that executes JavaScript outside the browser. Angular uses Node.js for its development tools, including compiling TypeScript, managing dependencies, running a development server, and producing optimized production builds. However, the final Angular application normally runs in the user's browser, not in Node.js.

npm (Node Package Manager) is included with Node.js and manages the libraries required by your Angular application. It plays a role similar to Maven or Gradle in Java projects. For example, npm downloads Angular dependencies, maintains their versions, and executes development scripts defined in `package.json`.

### Install Node.js using nvm

On Ubuntu, using nvm (Node Version Manager) is recommended because it allows you to install and switch between different Node.js versions without affecting the system installation.

Install nvm using the official instructions at [github.com/nvm-sh/nvm](https://github.com/nvm-sh/nvm?utm_source=chatgpt.com). Then install the latest LTS version of Node.js:

```sh
nvm install --lts
nvm use --lts
```

Verify the installation:

```sh
node --version
npm --version
```

Example output:

```
v24.x.x
11.x.x
```

These are illustrative versions. For a new Angular project, always verify the supported Node.js versions in the [Angular compatibility table](https://angular.dev/reference/versions?utm_source=chatgpt.com).

### Understand the main tools

|Tool|Purpose|Java equivalent|
|---|---|---|
|Node.js|JavaScript runtime|JDK runtime|
|npm|Dependency manager|Maven / Gradle|
|Angular CLI|Project and build tooling|Spring Boot CLI / build plugins|
|TypeScript|Typed programming language|Java (conceptually)|
|VS Code|Development IDE|IntelliJ IDEA|

## 2. Angular CLI

Angular CLI (Command Line Interface) is the official tool for creating, developing, testing, and building Angular applications. It generates a standardized project structure and configures the necessary build tools automatically.

Instead of manually configuring TypeScript, bundlers, routing, and development scripts, Angular CLI handles most of the initial configuration. This is similar to using Spring Initializr to generate a Spring Boot application with a predefined structure and dependencies.

### Install Angular CLI

```sh
npm install -g @angular/cli
```

Verify the installation:

```sh
ng version
```

The `ng` command is the entry point for Angular CLI.

### Common Angular CLI commands

|Command|Description|
|---|---|
|`ng new app-name`|Create a new Angular application|
|`ng serve`|Start development server|
|`ng generate component name`|Generate a component|
|`ng generate service name`|Generate a service|
|`ng build`|Build application|
|`ng test`|Run unit tests|
|`ng version`|Show Angular versions|

You can also execute Angular CLI without installing it globally:

```sh
npx @angular/cli new angular-demo
```

This is useful when you want to avoid maintaining a global CLI installation.

## 3. Creating an Angular Application

We will create a simple Angular application that can later communicate with a Java/Spring Boot backend.

### Step 1: Create the project

Open a terminal on Ubuntu:

```sh
mkdir -p ~/projects/angular
cd ~/projects/angular

nvm install 22
nvm use 22

node -v
	v22.23.3

npm -v
	10.9.9

ng new angular-demo
```

Angular CLI may ask several configuration questions. For this tutorial, use:

- Stylesheet format: CSS
- Server-side rendering (SSR/SSG): No
- Routing: Yes
- AI tooling configuration: None, if prompted
    
The exact prompts depend on your Angular CLI version. Use the default standalone component architecture.

### Step 2: Open the project

```sh
cd angular-demo
code .
```

If the `code` command is unavailable, open the project directory manually from Visual Studio Code.

### Step 3: Start the application

```
ng serve
```

Angular compiles the application and starts a local development server.

Open:

[http://localhost:4200](http://localhost:4200)

![[welcome.png]]

Illustrative Angular starter screen; its appearance varies by version.

The development server supports automatic rebuilding and browser updates when you modify source files.

To stop the server, press `Ctrl + C`.

### Step 4: Modify your first component

Open:

```
src/app/app.ts
```

Replace the generated component with:

```ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <h1>My First Angular Application</h1>

    <p>Welcome, {{ username }}!</p>

    <button (click)="changeName()">
      Change Name
    </button>
  `
})
export class App {
  username = 'Developer';

  changeName(): void {
    this.username = 'Angular Developer';
  }
}
```

The `App` class is the root component of the application. Its `username` property is displayed through interpolation, while `changeName()` is invoked when the user clicks the button.

After saving, the browser automatically updates.

This demonstrates Angular's automatic synchronization between component data and the user interface.

## 4. Angular Project Structure

Angular CLI generates several files and directories. Understanding their responsibilities is important before creating more complex applications.

A typical modern Angular project looks like this:

```
angular-demo/
│
├── node_modules/
│   └── ...
│
├── public/
│   └── favicon.ico
│
├── src/
│   ├── app/
│   │   ├── app.ts
│   │   ├── app.html
│   │   ├── app.css
│   │   ├── app.config.ts
│   │   ├── app.routes.ts
│   │   └── app.spec.ts
│   │
│   ├── index.html
│   ├── main.ts
│   └── styles.css
│
├── angular.json
├── package.json
├── package-lock.json
├── tsconfig.json
└── README.md
```

Some generated filenames and configuration details vary with the Angular CLI version.

### Important files

|File|Purpose|
|---|---|
|`package.json`|Dependencies and npm scripts|
|`angular.json`|Angular build configuration|
|`tsconfig.json`|TypeScript compiler configuration|
|`src/main.ts`|Application entry point|
|`src/index.html`|Main HTML document|
|`src/app/app.ts`|Root component|
|`src/app/app.html`|Root component template|
|`src/app/app.css`|Component-specific CSS|
|`src/app/app.routes.ts`|Application routes|
|`src/app/app.config.ts`|Application-wide providers|

### Application entry point

Open `src/main.ts`:

```ts
import { bootstrapApplication } from '@angular/platform-browser';

import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .catch(err => console.error(err));
```

`bootstrapApplication()` starts Angular and initializes the root component.

This is conceptually similar to starting a Spring Boot application:

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

The difference is that Spring Boot starts a backend application, while Angular initializes a frontend application inside the browser.

## 5. Development Server and Production Build

During development, Angular CLI runs a local development server. It watches source files, compiles TypeScript, processes templates and styles, and makes the application available through HTTP.

For production, Angular generates optimized static files that can be served by Nginx, Apache, or another web server. The production build does not normally require Node.js to execute in the browser.

### Development mode

```sh
ng serve
```

To expose the server on a different port:

```sh
ng serve --port 4300
```

### Production build

```sh
ng build --configuration production
```

The output is generated inside the `dist/` directory. For a client-rendered application, browser assets are typically located under:

```
dist/angular-demo/browser/
```

Example output:

```
dist/
└── angular-demo/
    └── browser/
        ├── index.html
        ├── main-XXXXXXXX.js
        ├── styles-XXXXXXXX.css
        └── ...
```

These files can be deployed to an Apache server, similar to how you currently deploy your other web applications. The web server must be configured to serve `index.html` for Angular client-side routes that are not physical files.

## 6. Preparing Angular for Spring Boot Integration

For development, Angular and Spring Boot will run independently:

To avoid CORS issues during development, Angular can forward API requests to Spring Boot using a development proxy.

Create `proxy.conf.json` in the Angular project root:

```json
{
  "/api/**": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

Start Angular with the proxy:

```sh
ng serve --proxy-config proxy.conf.json
```

Now, Angular can call:

```sh
this.http.get('/api/products');
```

Instead of:

```sh
this.http.get('http://localhost:8080/api/products');
```

Requests to `/api/...` are forwarded to Spring Boot, while Angular serves the frontend on port `4200`.

This proxy applies to development. In production, the reverse proxy or hosting server needs its own routing configuration.

## 7. Key Concepts to Remember

- Node.js is required for Angular development and build tooling, but not for running a typical compiled Angular SPA in the browser.
- npm manages dependencies, similar to Maven or Gradle.
- Angular CLI creates projects, components, services, and production builds.
- `ng serve` starts the development server, usually on port `4200`.
- `ng build` generates production-ready static assets.
- `src/main.ts` initializes the Angular application.
- `src/app/app.ts` defines the root component in the modern Angular project structure.
- Angular's development proxy allows convenient communication with a local Spring Boot backend.